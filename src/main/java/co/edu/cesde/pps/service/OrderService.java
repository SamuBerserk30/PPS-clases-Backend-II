package co.edu.cesde.pps.service;

import co.edu.cesde.pps.dto.OrderDTO;
import co.edu.cesde.pps.enums.CartStatus;
import co.edu.cesde.pps.exception.EntityNotFoundException;
import co.edu.cesde.pps.exception.InsufficientStockException;
import co.edu.cesde.pps.exception.InvalidCartStateException;
import co.edu.cesde.pps.exception.ValidationException;
import co.edu.cesde.pps.mapper.OrderMapper;
import co.edu.cesde.pps.model.*;
import co.edu.cesde.pps.repository.OrderRepository;
import co.edu.cesde.pps.repository.OrderStatusRepository;
import co.edu.cesde.pps.util.CalculationUtils;
import co.edu.cesde.pps.config.AppConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de órdenes.
 *
 * Responsabilidades:
 * - Proceso de checkout (Cart → Order)
 * - Generar número de orden único
 * - Calcular totales (subtotal, tax, shipping)
 * - Validar direcciones
 * - Actualizar stock de productos
 * - Marcar carrito como CONVERTED
 * - Búsqueda de órdenes
 * - Conversión Entity <-> DTO
 *
 * NOTA: En Etapa 06 se agregará:
 * - @Service annotation
 * - @Transactional (CRÍTICO para checkout)
 * - Inyección de OrderRepository
 * - Persistencia real
 */
@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderMapper orderMapper;
    private final UserService userService;
    private final CartService cartService;
    private final AddressService addressService;
    private final ProductService productService;
    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final Random random;

    public OrderService(UserService userService, CartService cartService,
                        AddressService addressService, ProductService productService,
                        OrderRepository orderRepository, OrderStatusRepository orderStatusRepository) {
        this.orderMapper = new OrderMapper();
        this.userService = userService;
        this.cartService = cartService;
        this.addressService = addressService;
        this.productService = productService;
        this.orderRepository = orderRepository;
        this.orderStatusRepository = orderStatusRepository;
        this.random = new Random();
    }

    /**
     * PROCESO DE CHECKOUT (CRÍTICO)
     * ==============================
     * Convierte un carrito en una orden completada.
     *
     * Proceso:
     * 1. Validar usuario registrado
     * 2. Validar carrito (OPEN, no vacío, pertenece al usuario)
     * 3. Validar direcciones existen
     * 4. Verificar disponibilidad y stock de todos los productos
     * 5. Crear orden con número único
     * 6. Copiar items del carrito a la orden (congelar precios)
     * 7. Calcular totales (subtotal, tax, shipping, total)
     * 8. Actualizar stock de productos
     * 9. Marcar carrito como CONVERTED
     *
     * @param userId ID del usuario
     * @param cartId ID del carrito
     * @param shippingAddressId ID de la dirección de envío
     * @param billingAddressId ID de la dirección de facturación
     * @return OrderDTO de la orden creada
     * @throws EntityNotFoundException si no existe usuario, carrito o dirección
     * @throws InvalidCartStateException si el carrito no está OPEN
     * @throws ValidationException si el carrito está vacío o no pertenece al usuario
     * @throws InsufficientStockException si no hay stock suficiente
     */
    @Transactional
    public OrderDTO checkout(Long userId, Long cartId, Long shippingAddressId,
                             Long billingAddressId) {
        User user = userService.findUserEntityOrThrow(userId);
        Cart cart = cartService.findCartEntityOrThrow(cartId);

        // Validar estado OPEN
        if (cart.getStatus() != CartStatus.OPEN) {
            throw new InvalidCartStateException(cartId, cart.getStatus(),
                    CartStatus.OPEN, "checkout");
        }

        // Validar no vacío
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new ValidationException("Cannot checkout empty cart");
        }

        // Validar que pertenece al usuario
        if (cart.getUser() == null || !cart.getUser().getUserId().equals(userId)) {
            throw new ValidationException("Cart does not belong to user");
        }

        // 3. Validar que las direcciones existen
        Address shippingAddress = addressService.findAddressEntityOrThrow(shippingAddressId);
        Address billingAddress = addressService.findAddressEntityOrThrow(billingAddressId);

        // Validar que las direcciones pertenecen al usuario
        if (!shippingAddress.getUser().getUserId().equals(userId)) {
            throw new ValidationException("Shipping address does not belong to user");
        }
        if (!billingAddress.getUser().getUserId().equals(userId)) {
            throw new ValidationException("Billing address does not belong to user");
        }

        // 4. Verificar disponibilidad y stock de todos los productos
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();

            if (!Boolean.TRUE.equals(product.getIsActive())) {
                throw new ValidationException("Product '" + product.getName() +
                        "' is no longer available");
            }

            // Verificar stock suficiente
            if (!CalculationUtils.hasEnoughStock(product.getStockQty(), item.getQuantity())) {
                throw new InsufficientStockException(product.getProductId(),
                        product.getSku(), item.getQuantity(), product.getStockQty());
            }
        }

        // 5. Crear orden con número único
        String orderNumber = generateOrderNumber();

        OrderStatus pendingStatus = orderStatusRepository.findByNameIgnoreCase("PENDING")
                .orElseThrow(() -> new EntityNotFoundException("OrderStatus", "PENDING"));

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(user)
                .orderStatus(pendingStatus)
                .shippingAddress(shippingAddress)
                .billingAddress(billingAddress)
                .subtotal(BigDecimal.ZERO)
                .tax(BigDecimal.ZERO)
                .shippingCost(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();

        // 6. Copiar items del carrito a la orden (congelar precios históricos)
        for (CartItem cartItem : cart.getItems()) {
            BigDecimal lineTotal = CalculationUtils.calculateOrderItemLineTotal(
                    cartItem.getUnitPrice(), cartItem.getQuantity());

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(cartItem.getProduct())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getUnitPrice())
                    .lineTotal(lineTotal)
                    .build();

            order.getItems().add(orderItem);
            orderItem.setOrder(order);
        }

        // 7. Calcular totales
        List<BigDecimal> lineTotals = order.getItems().stream()
                .map(OrderItem::getLineTotal)
                .toList();

        BigDecimal subtotal = CalculationUtils.calculateOrderSubtotal(lineTotals);
        BigDecimal taxRate = BigDecimal.valueOf(AppConfig.getDefaultTaxRate());
        BigDecimal tax = CalculationUtils.calculateTax(subtotal, taxRate);
        BigDecimal shippingCost = calculateShippingCost(subtotal);
        BigDecimal total = CalculationUtils.calculateOrderTotal(subtotal, tax, shippingCost);

        order.setSubtotal(subtotal);
        order.setTax(tax);
        order.setShippingCost(shippingCost);
        order.setTotal(total);

        // 8. Actualizar stock de productos
        for (CartItem item : cart.getItems()) {
            productService.decreaseStock(item.getProduct().getProductId(),
                    item.getQuantity());
        }

        // 9. Marcar carrito como CONVERTED
        cart.setStatus(CartStatus.CONVERTED);
        cart.setUpdatedAt(LocalDateTime.now());

        order = orderRepository.save(order);

        return orderMapper.toDTO(order);
    }

    /**
     * Busca orden por ID.
     *
     * @param orderId ID de la orden
     * @return OrderDTO
     * @throws EntityNotFoundException si no existe
     */
    public OrderDTO findById(Long orderId) {
        Order order = findOrderEntityOrThrow(orderId);
        return orderMapper.toDTO(order);
    }

    /**
     * Busca orden por número de orden.
     *
     * @param orderNumber Número de orden
     * @return OrderDTO
     * @throws EntityNotFoundException si no existe
     */
    public OrderDTO findByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumberIgnoreCase(orderNumber)
                .orElseThrow(() -> new EntityNotFoundException("Order with number: " + orderNumber));

        return orderMapper.toDTO(order);
    }

    /**
     * Lista todas las órdenes de un usuario.
     *
     * @param userId ID del usuario
     * @return Lista de OrderDTO
     */
    public List<OrderDTO> findOrdersByUser(Long userId) {
        userService.findUserEntityOrThrow(userId);
        return orderMapper.toDTOList(orderRepository.findByUser_UserId(userId));
    }

    /**
     * Lista órdenes por estado.
     *
     * @param statusId ID del estado
     * @return Lista de OrderDTO
     */
    public List<OrderDTO> findOrdersByStatus(Long statusId) {
        return orderMapper.toDTOList(orderRepository.findByOrderStatus_OrderStatusId(statusId));
    }

    /**
     * Lista órdenes por rango de fechas.
     *
     * @param startDate Fecha inicio
     * @param endDate Fecha fin
     * @return Lista de OrderDTO
     */
    public List<OrderDTO> findOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderMapper.toDTOList(orderRepository.findByCreatedAtBetween(startDate, endDate));
    }

    /**
     * Genera un número de orden único.
     *
     * Formato: {PREFIX}-YYYYMMDD-XXXXXX
     * Ejemplo: ORD-20260203-123456
     *
     * @return Número de orden único
     */
    public String generateOrderNumber() {
        String prefix = AppConfig.getOrderNumberPrefix();
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String orderNumber;
        do {
            String randomPart = String.format("%06d", random.nextInt(1000000));
            orderNumber = prefix + date + "-" + randomPart;
        } while (orderRepository.existsByOrderNumberIgnoreCase(orderNumber));

        return orderNumber;
    }

    /**
     * Calcula el costo de envío basado en el subtotal.
     *
     * Reglas:
     * - Si subtotal >= threshold: envío gratis
     * - Si subtotal < threshold: costo base
     *
     * @param subtotal Subtotal de la orden
     * @return Costo de envío
     */
    private BigDecimal calculateShippingCost(BigDecimal subtotal) {

        return CalculationUtils.calculateShippingCost(subtotal, 1); // shippingZone = 1 por defecto
    }

    /**
     * Busca entity Order por ID o lanza excepción.
     *
     * @param orderId ID de la orden
     * @return Order entity
     * @throws EntityNotFoundException si no existe
     */
    public Order findOrderEntityOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order", orderId));
    }

    public OrderDTO findByIdForUser(Long userId, Long orderId) {
        OrderDTO order = findById(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new EntityNotFoundException("Order", orderId);
        }
        return order;
    }

    // Métodos auxiliares para simular auto-increment
    private Long generateNextId() {
        return orderRepository.findAll().stream()
                .mapToLong(Order::getOrderId)
                .max()
                .orElse(0L) + 1;
    }

    private Long generateNextOrderItemId() {
        return orderRepository.findAll().stream()
                .flatMap(order -> order.getItems().stream())
                .mapToLong(OrderItem::getOrderItemId)
                .max()
                .orElse(0L) + 1;
    }
}
