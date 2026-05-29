package co.edu.cesde.pps.mapper;

import co.edu.cesde.pps.dto.ProductDTO;
import co.edu.cesde.pps.model.Product;
import co.edu.cesde.pps.util.MoneyUtils;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para conversión entre Product (Entity) y ProductDTO.
 *
 * Responsabilidades:
 * - Convertir Entity a DTO (toDTO)
 * - Convertir DTO a Entity (toEntity)
 * - Manejar null safety
 * - Extraer categoryId y categoryName de la relación
 * - Calcular isAvailable
 * - Formatear precio
 */
public class ProductMapper {

    /**
     * Convierte Product Entity a ProductDTO.
     *
     * @param product Entity a convertir
     * @return ProductDTO o null si product es null
     */
    public ProductDTO toDTO(Product product) {
        if (product == null) {
            return null;
        }

        ProductDTO dto = new ProductDTO();
        dto.setProductId(product.getProductId());

        // Extraer datos de Category (relación)
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getCategoryId());
            dto.setCategoryName(product.getCategory().getName());
        }

        dto.setSku(product.getSku());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setImage(product.getImage());
        dto.setPrice(product.getPrice());
        dto.setStockQty(product.getStockQty());
        dto.setIsActive(product.getIsActive());
        dto.setCreatedAt(product.getCreatedAt());

        // Campos calculados
        dto.setIsAvailable(product.isAvailable()); // Método helper de Product

        // Campos formateados
        if (product.getPrice() != null) {
            dto.setPriceFormatted(MoneyUtils.formatUSD(product.getPrice()));
        }

        return dto;
    }

    /**
     * Convierte ProductDTO a Product Entity.
     *
     * NOTA: No convierte Category, eso se maneja en el servicio.
     *
     * @param dto DTO a convertir
     * @return Product Entity o null si dto es null
     */
    public Product toEntity(ProductDTO dto) {
        if (dto == null) {
            return null;
        }

        Product product = new Product();
        product.setProductId(dto.getProductId());
        // Category se debe asignar en el servicio
        product.setSku(dto.getSku());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setImage(dto.getImage());
        product.setPrice(dto.getPrice());
        product.setStockQty(dto.getStockQty());
        product.setIsActive(dto.getIsActive());
        product.setCreatedAt(dto.getCreatedAt());

        return product;
    }

    /**
     * Convierte lista de Product Entities a lista de ProductDTOs.
     *
     * @param products Lista de entities
     * @return Lista de DTOs o lista vacía si products es null
     */
    public List<ProductDTO> toDTOList(List<Product> products) {
        if (products == null) {
            return List.of();
        }

        return products.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convierte lista de ProductDTOs a lista de Product Entities.
     *
     * @param dtos Lista de DTOs
     * @return Lista de entities o lista vacía si dtos es null
     */
    public List<Product> toEntityList(List<ProductDTO> dtos) {
        if (dtos == null) {
            return List.of();
        }

        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
