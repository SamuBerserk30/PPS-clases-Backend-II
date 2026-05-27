package co.edu.cesde.pps.web.mapper;

import co.edu.cesde.pps.dto.AddressDTO;
import co.edu.cesde.pps.dto.CartDTO;
import co.edu.cesde.pps.dto.CartItemDTO;
import co.edu.cesde.pps.dto.CategoryDTO;
import co.edu.cesde.pps.dto.OrderDTO;
import co.edu.cesde.pps.dto.OrderItemDTO;
import co.edu.cesde.pps.dto.ProductDTO;
import co.edu.cesde.pps.dto.UserDTO;
import co.edu.cesde.pps.model.UserSession;
import co.edu.cesde.pps.web.dto.response.AddressResponse;
import co.edu.cesde.pps.web.dto.response.AuthSessionResponse;
import co.edu.cesde.pps.web.dto.response.CartItemResponse;
import co.edu.cesde.pps.web.dto.response.CartResponse;
import co.edu.cesde.pps.web.dto.response.CartSummaryResponse;
import co.edu.cesde.pps.web.dto.response.CategoryResponse;
import co.edu.cesde.pps.web.dto.response.OrderItemResponse;
import co.edu.cesde.pps.web.dto.response.OrderResponse;
import co.edu.cesde.pps.web.dto.response.OrderTotalsResponse;
import co.edu.cesde.pps.web.dto.response.ProductResponse;
import co.edu.cesde.pps.web.dto.response.UserResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * Convierte DTOs actuales de servicio a DTOs web estables para la futura capa HTTP.
 */
@Component
public class WebResponseMapper {

    public AuthSessionResponse toAuthSessionResponse(UserSession session, UserDTO user, CartDTO cart) {
        return new AuthSessionResponse(
                session.getSessionToken(),
                session.getSessionId(),
                session.getExpiresAt(),
                toUserResponse(user),
                toCartResponse(cart)
        );
    }

    public UserResponse toUserResponse(UserDTO dto) {
        if (dto == null) {
            return null;
        }

        return new UserResponse(
                dto.getUserId(),
                dto.getEmail(),
                dto.getFirstName(),
                dto.getLastName(),
                dto.getFullName(),
                dto.getRoleName(),
                dto.getPhone(),
                dto.getStatus(),
                dto.getCreatedAt()
        );
    }

    public List<UserResponse> toUserResponseList(List<UserDTO> dtos) {
        return dtos == null ? List.of() : dtos.stream().map(this::toUserResponse).toList();
    }

    public AddressResponse toAddressResponse(AddressDTO dto) {
        if (dto == null) {
            return null;
        }

        return new AddressResponse(
                dto.getAddressId(),
                dto.getUserId(),
                dto.getType(),
                dto.getLine1(),
                dto.getLine2(),
                dto.getCity(),
                dto.getState(),
                dto.getCountry(),
                dto.getPostalCode(),
                dto.getIsDefault()
        );
    }

    public List<AddressResponse> toAddressResponseList(List<AddressDTO> dtos) {
        return dtos == null ? List.of() : dtos.stream().map(this::toAddressResponse).toList();
    }

    public CategoryResponse toCategoryResponse(CategoryDTO dto) {
        if (dto == null) {
            return null;
        }

        List<CategoryResponse> subcategories = dto.getSubcategories() == null
                ? List.of()
                : dto.getSubcategories().stream()
                .map(this::toCategoryResponse)
                .toList();

        return new CategoryResponse(
                dto.getCategoryId(),
                dto.getParentId(),
                dto.getParentName(),
                dto.getName(),
                dto.getSlug(),
                dto.getIsRoot(),
                dto.getSubcategoriesCount(),
                dto.getProductsCount(),
                subcategories
        );
    }

    public List<CategoryResponse> toCategoryResponseList(List<CategoryDTO> dtos) {
        return dtos == null ? List.of() : dtos.stream().map(this::toCategoryResponse).toList();
    }

    public ProductResponse toProductResponse(ProductDTO dto) {
        if (dto == null) {
            return null;
        }

        return new ProductResponse(
                dto.getProductId(),
                dto.getCategoryId(),
                dto.getCategoryName(),
                dto.getSku(),
                dto.getName(),
                dto.getDescription(),
                dto.getImage(),
                dto.getPrice(),
                dto.getStockQty(),
                dto.getIsActive(),
                dto.getIsAvailable(),
                dto.getCreatedAt()
        );
    }

    public List<ProductResponse> toProductResponseList(List<ProductDTO> dtos) {
        return dtos == null ? List.of() : dtos.stream().map(this::toProductResponse).toList();
    }

    public CartResponse toCartResponse(CartDTO dto) {
        if (dto == null) {
            return null;
        }

        List<CartItemResponse> items = dto.getItems() == null
                ? List.of()
                : dto.getItems().stream().map(this::toCartItemResponse).toList();

        BigDecimal subtotal = items.stream()
                .map(CartItemResponse::lineTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Integer itemsCount = items.stream()
                .map(CartItemResponse::quantity)
                .filter(Objects::nonNull)
                .reduce(0, Integer::sum);
        BigDecimal total = dto.getTotal() != null ? dto.getTotal() : subtotal;

        return new CartResponse(
                dto.getCartId(),
                dto.getUserId(),
                dto.getUserEmail(),
                dto.getStatus() != null ? dto.getStatus().name() : null,
                dto.getIsGuest(),
                dto.getCreatedAt(),
                dto.getUpdatedAt(),
                items,
                new CartSummaryResponse(itemsCount, subtotal, BigDecimal.ZERO, BigDecimal.ZERO, total)
        );
    }

    public CartItemResponse toCartItemResponse(CartItemDTO dto) {
        if (dto == null) {
            return null;
        }

        return new CartItemResponse(
                dto.getCartItemId(),
                dto.getProductId(),
                dto.getProductSku(),
                dto.getProductName(),
                dto.getProductImageUrl(),
                dto.getQuantity(),
                dto.getUnitPrice(),
                dto.getSubtotal(),
                dto.getProductAvailable(),
                dto.getProductStock(),
                dto.getAddedAt()
        );
    }

    public OrderResponse toOrderResponse(OrderDTO dto) {
        if (dto == null) {
            return null;
        }

        List<OrderItemResponse> items = dto.getItems() == null
                ? List.of()
                : dto.getItems().stream().map(this::toOrderItemResponse).toList();

        return new OrderResponse(
                dto.getOrderId(),
                dto.getOrderNumber(),
                dto.getUserId(),
                dto.getUserEmail(),
                dto.getUserFullName(),
                dto.getOrderStatusName(),
                toAddressResponse(dto.getShippingAddress()),
                toAddressResponse(dto.getBillingAddress()),
                items,
                new OrderTotalsResponse(dto.getSubtotal(), dto.getTax(), dto.getShippingCost(), dto.getTotal()),
                dto.getCreatedAt()
        );
    }

    public List<OrderResponse> toOrderResponseList(List<OrderDTO> dtos) {
        return dtos == null ? List.of() : dtos.stream().map(this::toOrderResponse).toList();
    }

    public OrderItemResponse toOrderItemResponse(OrderItemDTO dto) {
        if (dto == null) {
            return null;
        }

        return new OrderItemResponse(
                dto.getOrderItemId(),
                dto.getProductId(),
                dto.getProductSku(),
                dto.getProductName(),
                dto.getProductImageUrl(),
                dto.getQuantity(),
                dto.getUnitPrice(),
                dto.getLineTotal()
        );
    }
}

