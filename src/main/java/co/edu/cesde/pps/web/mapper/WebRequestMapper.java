package co.edu.cesde.pps.web.mapper;

import co.edu.cesde.pps.dto.AddressDTO;
import co.edu.cesde.pps.dto.CategoryDTO;
import co.edu.cesde.pps.dto.ProductDTO;
import co.edu.cesde.pps.web.dto.request.AddressUpsertRequest;
import co.edu.cesde.pps.web.dto.request.CategoryUpsertRequest;
import co.edu.cesde.pps.web.dto.request.ProductUpsertRequest;
import org.springframework.stereotype.Component;

/**
 * Convierte request DTOs de la futura capa web hacia los DTOs actuales de servicio.
 */
@Component
public class WebRequestMapper {

    public AddressDTO toAddressDTO(AddressUpsertRequest request) {
        if (request == null) {
            return null;
        }

        AddressDTO dto = new AddressDTO();
        dto.setType(request.type());
        dto.setLine1(request.line1());
        dto.setLine2(request.line2());
        dto.setCity(request.city());
        dto.setState(request.state());
        dto.setCountry(request.country());
        dto.setPostalCode(request.postalCode());
        dto.setIsDefault(request.isDefault());
        return dto;
    }

    public CategoryDTO toCategoryDTO(CategoryUpsertRequest request) {
        if (request == null) {
            return null;
        }

        CategoryDTO dto = new CategoryDTO();
        dto.setParentId(request.parentId());
        dto.setName(request.name());
        dto.setSlug(request.slug());
        return dto;
    }

    public ProductDTO toProductDTO(ProductUpsertRequest request) {
        if (request == null) {
            return null;
        }

        ProductDTO dto = new ProductDTO();
        dto.setCategoryId(request.categoryId());
        dto.setSku(request.sku());
        dto.setName(request.name());
        dto.setDescription(request.description());
        dto.setImage(request.image());
        dto.setPrice(request.price());
        dto.setStockQty(request.stockQty());
        dto.setIsActive(request.isActive());
        return dto;
    }
}

