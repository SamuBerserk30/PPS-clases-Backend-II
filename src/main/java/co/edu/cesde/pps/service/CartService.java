package co.edu.cesde.pps.service;

import co.edu.cesde.pps.dto.CartDTO;
import co.edu.cesde.pps.enums.CartStatus;
import co.edu.cesde.pps.exception.CartMergeException;
import co.edu.cesde.pps.exception.EntityNotFoundException;
import co.edu.cesde.pps.exception.InsufficientStockException;
import co.edu.cesde.pps.exception.InvalidCartStateException;
import co.edu.cesde.pps.exception.ValidationException;
import co.edu.cesde.pps.mapper.CartMapper;
import co.edu.cesde.pps.model.Cart;
import co.edu.cesde.pps.model.CartItem;
import co.edu.cesde.pps.model.Product;
import co.edu.cesde.pps.model.User;
import co.edu.cesde.pps.model.UserSession;
import co.edu.cesde.pps.repository.CartRepository;
import co.edu.cesde.pps.repository.UserSessionRepository;
import co.edu.cesde.pps.util.CalculationUtils;
import co.edu.cesde.pps.util.ValidationUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Servicio para gestión de carritos de compra.
 *
 * Responsabilidades:
 * - CRUD de carritos
 * - Agregar/actualizar/remover items (gestión bidireccional)
 * - Calcular totales
 * - Validar disponibilidad de productos
 * - Actualizar timestamp (touch)
 * - **ALGORITMO DE CART MERGE** (fusión invitado → registrado)
 * - Limpiar carrito
 * - Conversión Entity <-> DTO
 *
 * NOTA: En Etapa 06 se agregará:
 * - @Service annotation
 * - @Transactional (crítico para Cart Merge)
 * - Inyección de CartRepository
 * - Persistencia real
 */
@Service
@Transactional(readOnly = true)
public class CartService {

    private final CartMapper cartMapper;
    private final UserService userService;
    private final ProductService productService;
    private final CartRepository cartRepository;
    private final UserSessionRepository userSessionRepository;

    public CartService(UserService userService, ProductService productService,
                       CartRepository cartRepository, UserSessionRepository userSessionRepository) {
        this.cartMapper = new CartMapper();
        this.userService = userService;
        this.productService = productService;
        this.cartRepository = cartRepository;
        this.userSessionRepository = userSessionRepository;
    }

    /**
     * Crea un carrito para invitado (sin usuario).
     *
     * @param sessionId ID de la sesión
     * @return CartDTO del carrito creado
     */
    @Transactional
    public CartDTO createCartForGuest(Long sessionId) {
        Cart cart = Cart.builder()
                .user(null)
                .session(resolveSession(sessionId))
                .status(CartStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        cart = cartRepository.save(cart);

        return cartMapper.toDTO(cart);
    }

    /**
     * Crea un carrito para usuario registrado.
     *
     * @param userId ID del usuario
     * @return CartDTO del carrito creado
     * @throws EntityNotFoundException si el usuario no existe
     */
    @Transactional
    public CartDTO createCartForUser(Long userId) {
        User user = userService.findUserEntityOrThrow(userId);

        Cart cart = Cart.builder()
                .user(user)
                .status(CartStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        cart = cartRepository.save(cart);

        return cartMapper.toDTO(cart);
    }

    /**
     * Busca carrito por ID.
     *
     * @param cartId ID del carrito
     * @return CartDTO
     * @throws EntityNotFoundException si no existe
     */
    public CartDTO findById(Long cartId) {
        Cart cart = findCartEntityOrThrow(cartId);
        return cartMapper.toDTO(cart);
    }

    /**
     * Busca carrito OPEN del usuario (puede no existir).
     *
     * @param userId ID del usuario
     * @return CartDTO o null si no existe
     */
    public CartDTO findOpenCartByUser(Long userId) {
        Cart cart = cartRepository.findByUser_UserIdAndStatus(userId, CartStatus.OPEN)
                .orElse(null);

        return cart != null ? cartMapper.toDTO(cart) : null;
    }

    /**
     * Busca carrito OPEN asociado a una sesión (puede no existir).
     *
     * @param sessionId ID de la sesión
     * @return CartDTO o null si no existe
     */
    public CartDTO findOpenCartBySession(Long sessionId) {
        if (sessionId == null) {
            return null;
        }

        Cart cart = cartRepository.findBySession_SessionIdAndStatus(sessionId, CartStatus.OPEN)
                .orElse(null);

        return cart != null ? cartMapper.toDTO(cart) : null;
    }

    /**
     * Busca o crea carrito OPEN para usuario.
     *
     * @param userId ID del usuario
     * @return CartDTO activo del usuario
     */
    @Transactional
    public CartDTO findOrCreateOpenCartForUser(Long userId) {
        return cartMapper.toDTO(findOrCreateOpenCartEntityForUser(userId));
    }

    /**
     * Busca o crea carrito OPEN para una sesión guest.
     *
     * @param sessionId ID de la sesión guest
     * @return CartDTO activo de la sesión
     */
    @Transactional
    public CartDTO findOrCreateOpenCartForGuestSession(Long sessionId) {
        ValidationUtils.validateNotNull(sessionId, "sessionId");

        Cart cart = cartRepository.findBySession_SessionIdAndStatus(sessionId, CartStatus.OPEN)
                .orElseGet(() -> {
                    UserSession session = resolveSession(sessionId);
                    return cartRepository.save(Cart.builder()
                            .user(null)
                            .session(session)
                            .status(CartStatus.OPEN)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build());
                });

        return cartMapper.toDTO(cart);
    }

    /**
     * Agrega un item al carrito (gestión bidireccional).
     *
     * @param cartId ID del carrito
     * @param productId ID del producto
     * @param quantity Cantidad a agregar
     * @return CartDTO actualizado
     * @throws EntityNotFoundException si carrito o producto no existen
     * @throws InvalidCartStateException si el carrito no está OPEN
     * @throws InsufficientStockException si no hay stock suficiente
     * @throws ValidationException si el producto no está activo
     */
    @Transactional
    public CartDTO addItem(Long cartId, Long productId, Integer quantity) {
        // Validar cantidad
        ValidationUtils.validatePositive(quantity, "quantity");

        // Obtener carrito y validar estado
        Cart cart = findCartEntityOrThrow(cartId);
        if (cart.getStatus() != CartStatus.OPEN) {
            throw new InvalidCartStateException(cartId, cart.getStatus(),
                    CartStatus.OPEN, "add item");
        }

        // Obtener producto y validar disponibilidad
        Product product = productService.findProductEntityOrThrow(productId);
        if (!Boolean.TRUE.equals(product.getIsActive())) {
            throw new ValidationException("Product '" + product.getName() + "' is not active");
        }

        // Validar stock disponible
        if (!CalculationUtils.hasEnoughStock(product.getStockQty(), quantity)) {
            throw new InsufficientStockException(productId, product.getSku(),
                    quantity, product.getStockQty());
        }

        // Buscar si el producto ya existe en el carrito
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getProductId().equals(productId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // Producto ya existe: actualizar cantidad
            int newQuantity = existingItem.getQuantity() + quantity;

            // Validar stock para nueva cantidad
            if (!CalculationUtils.hasEnoughStock(product.getStockQty(), newQuantity)) {
                throw new InsufficientStockException(productId, product.getSku(),
                        newQuantity, product.getStockQty());
            }

            existingItem.setQuantity(newQuantity);
        } else {
            // Producto nuevo: crear CartItem y gestión bidireccional
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .unitPrice(product.getPrice())
                    .addedAt(LocalDateTime.now())
                    .build();

            cart.getItems().add(newItem);      // Agregar a colección del carrito
            newItem.setCart(cart);             // Establecer referencia al carrito
        }

        // Actualizar timestamp del carrito
        touchCart(cart);

        cart = cartRepository.save(cart);

        return cartMapper.toDTO(cart);
    }

    /**
     * Actualiza la cantidad de un item en el carrito.
     *
     * @param cartId ID del carrito
     * @param productId ID del producto
     * @param newQuantity Nueva cantidad
     * @return CartDTO actualizado
     * @throws EntityNotFoundException si no existe carrito o producto
     * @throws InvalidCartStateException si el carrito no está OPEN
     * @throws InsufficientStockException si no hay stock suficiente
     * @throws ValidationException si el producto no está en el carrito
     */
    @Transactional
    public CartDTO updateItemQuantity(Long cartId, Long productId, Integer newQuantity) {
        ValidationUtils.validatePositive(newQuantity, "quantity");

        Cart cart = findCartEntityOrThrow(cartId);
        if (cart.getStatus() != CartStatus.OPEN) {
            throw new InvalidCartStateException(cartId, cart.getStatus(),
                    CartStatus.OPEN, "update item");
        }

        // Buscar item en el carrito
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProduct().getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Product not found in cart"));

        // Validar stock
        Product product = item.getProduct();
        if (!CalculationUtils.hasEnoughStock(product.getStockQty(), newQuantity)) {
            throw new InsufficientStockException(productId, product.getSku(),
                    newQuantity, product.getStockQty());
        }

        item.setQuantity(newQuantity);
        touchCart(cart);

        cart = cartRepository.save(cart);

        return cartMapper.toDTO(cart);
    }

    /**
     * Remueve un item del carrito (gestión bidireccional).
     *
     * @param cartId ID del carrito
     * @param productId ID del producto
     * @return CartDTO actualizado
     * @throws EntityNotFoundException si no existe
     * @throws InvalidCartStateException si el carrito no está OPEN
     * @throws ValidationException si el producto no está en el carrito
     */
    @Transactional
    public CartDTO removeItem(Long cartId, Long productId) {
        Cart cart = findCartEntityOrThrow(cartId);
        if (cart.getStatus() != CartStatus.OPEN) {
            throw new InvalidCartStateException(cartId, cart.getStatus(),
                    CartStatus.OPEN, "remove item");
        }

        // Buscar item
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProduct().getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Product not found in cart"));

        // Gestión bidireccional
        cart.getItems().remove(item);     // Remover de colección
        item.setCart(null);                // Remover referencia

        touchCart(cart);

        cart = cartRepository.save(cart);

        return cartMapper.toDTO(cart);
    }

    /**
     * Limpia todos los items del carrito.
     *
     * @param cartId ID del carrito
     * @throws EntityNotFoundException si no existe
     * @throws InvalidCartStateException si el carrito no está OPEN
     */
    @Transactional
    public void clearCart(Long cartId) {
        Cart cart = findCartEntityOrThrow(cartId);
        if (cart.getStatus() != CartStatus.OPEN) {
            throw new InvalidCartStateException(cartId, cart.getStatus(),
                    CartStatus.OPEN, "clear");
        }

        cart.getItems().clear();
        touchCart(cart);

        cartRepository.save(cart);
    }

    /**
     * Calcula el total del carrito.
     *
     * @param cartId ID del carrito
     * @return Total del carrito
     * @throws EntityNotFoundException si no existe
     */
    public BigDecimal calculateCartTotal(Long cartId) {
        Cart cart = findCartEntityOrThrow(cartId);
        return cart.calculateTotal(); // Usa método del modelo
    }

    /**
     * ALGORITMO DE CART MERGE (CRÍTICO)
     * ===================================
     * Fusiona carrito de invitado con carrito de usuario registrado.
     *
     * Escenario:
     * - Usuario invitado agrega productos a carrito A
     * - Usuario se registra o inicia sesión
     * - Usuario ya tiene carrito B en su cuenta
     * - Se debe fusionar A → B sin perder productos
     *
     * Proceso:
     * 1. Obtener ambos carritos y validar
     * 2. Para cada item del carrito invitado:
     *    - Si producto existe en carrito usuario: sumar cantidades
     *    - Si producto NO existe: mover item al carrito usuario
     * 3. Marcar carrito invitado como ABANDONED
     * 4. Actualizar timestamps
     *
     * @param guestCartId ID del carrito de invitado
     * @param userId ID del usuario registrado
     * @return CartDTO del carrito fusionado
     * @throws EntityNotFoundException si algún carrito no existe
     * @throws InvalidCartStateException si los carritos no están OPEN
     * @throws CartMergeException si el carrito guest ya tiene usuario asignado
     * @throws InsufficientStockException si no hay stock suficiente para cantidad fusionada
     */
    @Transactional
    public CartDTO mergeGuestCartToUserCart(Long guestCartId, Long userId) {
        // 1. Obtener ambos carritos
        Cart guestCart = findCartEntityOrThrow(guestCartId);
        Cart userCart = findOrCreateOpenCartEntityForUser(userId);

        // 2. Validar estados
        if (guestCart.getStatus() != CartStatus.OPEN) {
            throw new InvalidCartStateException(guestCartId, guestCart.getStatus(),
                    CartStatus.OPEN, "merge");
        }
        if (userCart.getStatus() != CartStatus.OPEN) {
            throw new InvalidCartStateException(userCart.getCartId(),
                    userCart.getStatus(), CartStatus.OPEN, "merge");
        }

        // 3. Validar que guestCart sea realmente de invitado
        if (guestCart.getUser() != null) {
            throw new CartMergeException(guestCartId, userCart.getCartId(),
                    "Guest cart already has a user assigned");
        }

        // 4. Fusionar items del carrito invitado al carrito usuario
        for (CartItem guestItem : new ArrayList<>(guestCart.getItems())) {
            Product product = guestItem.getProduct();
            Integer guestQuantity = guestItem.getQuantity();

            // Buscar si el producto ya existe en carrito de usuario
            CartItem userItem = userCart.getItems().stream()
                    .filter(item -> item.getProduct().getProductId()
                            .equals(product.getProductId()))
                    .findFirst()
                    .orElse(null);

            if (userItem != null) {
                // Producto YA existe en carrito usuario: sumar cantidades
                int totalQuantity = userItem.getQuantity() + guestQuantity;

                // Validar stock para cantidad fusionada
                if (!CalculationUtils.hasEnoughStock(product.getStockQty(), totalQuantity)) {
                    throw new InsufficientStockException(product.getProductId(),
                            product.getSku(), totalQuantity, product.getStockQty());
                }

                userItem.setQuantity(totalQuantity);

                // Resolver conflicto de precio: mantener más reciente
                if (guestItem.getAddedAt().isAfter(userItem.getAddedAt())) {
                    userItem.setUnitPrice(guestItem.getUnitPrice());
                }
            } else {
                // Producto NO existe en carrito usuario: mover item
                // Validar stock disponible
                if (!CalculationUtils.hasEnoughStock(product.getStockQty(), guestQuantity)) {
                    throw new InsufficientStockException(product.getProductId(),
                            product.getSku(), guestQuantity, product.getStockQty());
                }

                // Crear nuevo item en carrito de usuario
                CartItem newItem = CartItem.builder()
                        .cart(userCart)
                        .product(product)
                        .quantity(guestQuantity)
                        .unitPrice(guestItem.getUnitPrice())
                        .addedAt(guestItem.getAddedAt())
                        .build();

                // Gestión bidireccional
                userCart.getItems().add(newItem);
            }
        }

        // 5. Marcar carrito invitado como ABANDONED
        guestCart.setStatus(CartStatus.ABANDONED);
        touchCart(guestCart);

        // 6. Actualizar carrito de usuario
        touchCart(userCart);

        cartRepository.save(guestCart);
        userCart = cartRepository.save(userCart);

        return cartMapper.toDTO(userCart);
    }

    /**
     * Verifica si el carrito está abierto.
     *
     * @param cartId ID del carrito
     * @return true si está OPEN
     */
    public boolean isCartOpen(Long cartId) {
        Cart cart = findCartEntityOrThrow(cartId);
        return cart.isOpen();
    }

    /**
     * Actualiza el timestamp del carrito (touch).
     *
     * @param cartId ID del carrito
     */
    @Transactional
    public void touchCartById(Long cartId) {
        Cart cart = findCartEntityOrThrow(cartId);
        touchCart(cart);
        cartRepository.save(cart);
    }

    /**
     * Busca entity Cart por ID o lanza excepción.
     *
     * @param cartId ID del carrito
     * @return Cart entity
     * @throws EntityNotFoundException si no existe
     */
    public Cart findCartEntityOrThrow(Long cartId) {
        return cartRepository.findById(cartId)
                .orElseThrow(() -> new EntityNotFoundException("Cart", cartId));
    }

    // Métodos privados auxiliares

    /**
     * Busca carrito OPEN del usuario o crea uno nuevo si no existe.
     */
    private Cart findOrCreateOpenCartEntityForUser(Long userId) {
        User user = userService.findUserEntityOrThrow(userId);

        return cartRepository.findByUser_UserIdAndStatus(userId, CartStatus.OPEN)
                .orElseGet(() -> cartRepository.save(Cart.builder()
                        .user(user)
                        .status(CartStatus.OPEN)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()));
    }

    /**
     * Actualiza el timestamp updatedAt del carrito.
     */
    private void touchCart(Cart cart) {
        cart.setUpdatedAt(LocalDateTime.now());
    }

    private UserSession resolveSession(Long sessionId) {
        if (sessionId == null) {
            return null;
        }

        return userSessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("UserSession", sessionId));
    }
}