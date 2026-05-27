package co.edu.cesde.pps.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DTO para transferencia de datos de Producto.
 *
 * Se utiliza para:
 * - Listados de productos en catálogo
 * - Detalles de producto
 * - Búsqueda de productos
 * - Respuestas de API
 */
public class ProductDTO {

    private Long productId;
    private Long categoryId;
    private String categoryName;
    private String sku;
    private String name;
    private String description;
    private String image;
    private BigDecimal price;
    private Integer stockQty;
    private Boolean isActive;
    private Boolean isAvailable;
    private LocalDateTime createdAt;
    private String priceFormatted;

    // Constructor vacío
    public ProductDTO() {
    }

    // Constructor completo
    public ProductDTO(Long productId, Long categoryId, String categoryName, String sku,
                      String name, String description, String image, BigDecimal price, Integer stockQty,
                      Boolean isActive, LocalDateTime createdAt) {
        this.productId = productId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.image = image;
        this.price = price;
        this.stockQty = stockQty;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.isAvailable = isActive && stockQty > 0;
    }

    // Getters y Setters

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStockQty() {
        return stockQty;
    }

    public void setStockQty(Integer stockQty) {
        this.stockQty = stockQty;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getPriceFormatted() {
        return priceFormatted;
    }

    public void setPriceFormatted(String priceFormatted) {
        this.priceFormatted = priceFormatted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductDTO that = (ProductDTO) o;
        return Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }

    @Override
    public String toString() {
        return "ProductDTO{" +
                "productId=" + productId +
                ", sku='" + sku + '\'' +
                ", name='" + name + '\'' +
                ", image='" + image + '\'' +
                ", price=" + price +
                ", stockQty=" + stockQty +
                ", isAvailable=" + isAvailable +
                '}';
    }
}
