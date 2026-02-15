package co.edu.cesde.pps.model;

import co.edu.cesde.pps.util.CalculationUtils;
import co.edu.cesde.pps.util.ValidationUtils;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entidad Order - Representa una compra finalizada (pedido/orden).
 *
 * Una orden se crea cuando el usuario completa el checkout.
 * El checkout REQUIERE que el usuario esté registrado (userId NOT NULL).
 *
 * Campos:
 * - orderId: Identificador único de la orden (PK)
 * - orderNumber: Número de orden único (UNIQUE) - para tracking y referencia
 * - userId: Usuario que realizó la compra (FK a User) - NOT NULL
 * - orderStatusId: Estado actual de la orden (FK a OrderStatus)
 * - shippingAddressId: Dirección de envío (FK a Address)
 * - billingAddressId: Dirección de facturación (FK a Address)
 * - subtotal: Suma de precios de items antes de impuestos/envío (BigDecimal)
 * - tax: Impuestos aplicados (BigDecimal)
 * - shippingCost: Costo de envío (BigDecimal)
 * - total: Total final de la orden (subtotal + tax + shippingCost)
 * - createdAt: Fecha de creación de la orden
 *
 * Consideraciones de diseño:
 * - userId es obligatorio: los invitados deben registrarse antes del checkout
 * - Se guardan totales (subtotal, tax, shippingCost, total) para auditoría
 * - orderNumber único facilita búsqueda y tracking por parte del usuario
 * - Direcciones de envío y facturación pueden ser diferentes
 * - BigDecimal en todos los campos monetarios para precisión
 *
 * Relaciones (futuro - etapa02):
 * - N:1 con User (una orden pertenece a un usuario)
 * - N:1 con OrderStatus (estado actual)
 * - N:1 con Address (shipping_address_id)
 * - N:1 con Address (billing_address_id)
 * - 1:N con OrderItem (items de la orden)
 * - 1:N con Payment (pagos asociados, puede haber reintentos)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Order {

    private Long orderId;
    private String orderNumber;
    private Long userId; // NOT NULL - checkout requiere usuario registrado
    private Long orderStatusId;
    private Long shippingAddressId;
    private Long billingAddressId;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal shippingCost;
    private BigDecimal total;
    private LocalDateTime createdAt;

    // Colección para relación 1:N con OrderItem
    private List<OrderItem> items;

    // Constructor con campos obligatorios
    public Order(String orderNumber, Long userId, Long orderStatusId,
                 Long shippingAddressId, Long billingAddressId) {
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.orderStatusId = orderStatusId;
        this.shippingAddressId = shippingAddressId;
        this.billingAddressId = billingAddressId;
        this.subtotal = BigDecimal.ZERO;
        this.tax = BigDecimal.ZERO;
        this.shippingCost = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.items = new ArrayList<>();
    }

    // Constructor completo (excepto ID y timestamp autogenerado)
    public Order(String orderNumber, Long userId, Long orderStatusId,
                 Long shippingAddressId, Long billingAddressId,
                 BigDecimal subtotal, BigDecimal tax, BigDecimal shippingCost, BigDecimal total) {
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.orderStatusId = orderStatusId;
        this.shippingAddressId = shippingAddressId;
        this.billingAddressId = billingAddressId;
        this.subtotal = subtotal != null ? subtotal : BigDecimal.ZERO;
        this.tax = tax != null ? tax : BigDecimal.ZERO;
        this.shippingCost = shippingCost != null ? shippingCost : BigDecimal.ZERO;
        this.total = total != null ? total : BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.items = new ArrayList<>();
    }

    // Getters y Setters

    // Método helper para calcular total automáticamente
    public BigDecimal calculateTotal() {
        return CalculationUtils.calculateOrderTotal(subtotal, tax, shippingCost);
    }

    // equals y hashCode basados en ID

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(orderId, order.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }

    // toString sin navegación a objetos relacionados (solo IDs y tamaño de colección)
}
