package co.edu.cesde.pps.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DTO para transferencia de datos de Item del Carrito.
 *
 * Se utiliza para:
 * - Mostrar items en el carrito
 * - Agregar/actualizar items
 * - Respuestas de API
 */
public class CartItemDTO {

    private Long cartItemId;
    private Long cartId;
    private Long productId;
    private String productName;
    private String productSku;
    private String productImageUrl;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private String unitPriceFormatted;
    private String subtotalFormatted;
    private LocalDateTime addedAt;
    private Boolean productAvailable;
    private Integer productStock;
    private String productImage;
    private String productCategoryName;

    // Constructor vacío
    public CartItemDTO() {
    }

    // Constructor completo
    public CartItemDTO(Long cartItemId, Long cartId, Long productId, String productName,
                       String productSku, Integer quantity, BigDecimal unitPrice,
                       BigDecimal subtotal, LocalDateTime addedAt, String productImage, String productCategoryName) {
        this.cartItemId = cartItemId;
        this.cartId = cartId;
        this.productId = productId;
        this.productName = productName;
        this.productSku = productSku;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
        this.addedAt = addedAt;
        this.productImage = productImage;
        this.productCategoryName = productCategoryName;
    }

    // Getters y Setters

    public Long getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(Long cartItemId) {
        this.cartItemId = cartItemId;
    }

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public String getProductImageUrl() {
        return productImageUrl;
    }

    public void setProductImageUrl(String productImageUrl) {
        this.productImageUrl = productImageUrl;
    }

    public String getProductCategoryName() {return productCategoryName;}

    public void setProductCategoryName(String productCategoryName) {this.productCategoryName = productCategoryName;}

    public String getProductImage() {return productImage;}

    public void setProductImage(String productImage) {this.productImage = productImage;}

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public String getUnitPriceFormatted() {
        return unitPriceFormatted;
    }

    public void setUnitPriceFormatted(String unitPriceFormatted) {
        this.unitPriceFormatted = unitPriceFormatted;
    }

    public String getSubtotalFormatted() {
        return subtotalFormatted;
    }

    public void setSubtotalFormatted(String subtotalFormatted) {
        this.subtotalFormatted = subtotalFormatted;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }

    public Boolean getProductAvailable() {
        return productAvailable;
    }

    public void setProductAvailable(Boolean productAvailable) {
        this.productAvailable = productAvailable;
    }

    public Integer getProductStock() {
        return productStock;
    }

    public void setProductStock(Integer productStock) {
        this.productStock = productStock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItemDTO that = (CartItemDTO) o;
        return Objects.equals(cartItemId, that.cartItemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cartItemId);
    }

    @Override
    public String toString() {
        return "CartItemDTO{" +
                "cartItemId=" + cartItemId +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", subtotal=" + subtotal +
                '}';
    }
}
