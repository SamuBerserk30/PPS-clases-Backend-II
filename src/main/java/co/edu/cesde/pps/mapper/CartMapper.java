package co.edu.cesde.pps.mapper;

import co.edu.cesde.pps.dto.CartDTO;
import co.edu.cesde.pps.dto.CartItemDTO;
import co.edu.cesde.pps.model.Cart;
import co.edu.cesde.pps.model.CartItem;
import co.edu.cesde.pps.util.MoneyUtils;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para conversión entre Cart/CartItem (Entities) y CartDTO/CartItemDTO.
 *
 * Responsabilidades:
 * - Convertir Entity a DTO (toDTO)
 * - Convertir DTO a Entity (toEntity)
 * - Manejar null safety
 * - Convertir items anidados
 * - Calcular totales
 * - Formatear valores monetarios
 */
public class CartMapper {

    /**
     * Convierte Cart Entity a CartDTO.
     *
     * @param cart Entity a convertir
     * @return CartDTO o null si cart es null
     */
    public CartDTO toDTO(Cart cart) {
        if (cart == null) {
            return null;
        }

        CartDTO dto = new CartDTO();
        dto.setCartId(cart.getCartId());

        // Extraer datos de User (relación)
        if (cart.getUser() != null) {
            dto.setUserId(cart.getUser().getUserId());
            dto.setUserEmail(cart.getUser().getEmail());
        }

        dto.setStatus(cart.getStatus());
        dto.setIsGuest(cart.isGuestCart()); // Método helper de Cart
        dto.setCreatedAt(cart.getCreatedAt());
        dto.setUpdatedAt(cart.getUpdatedAt());

        // Convertir items
        if (cart.getItems() != null) {
            List<CartItemDTO> itemDTOs = cart.getItems().stream()
                    .map(this::toCartItemDTO)
                    .collect(Collectors.toList());
            dto.setItems(itemDTOs);
            dto.setItemsCount(itemDTOs.size());
        } else {
            dto.setItemsCount(0);
        }

        // Calcular y formatear total
        dto.setTotal(cart.calculateTotal()); // Método helper de Cart
        if (dto.getTotal() != null) {
            dto.setTotalFormatted(MoneyUtils.formatUSD(dto.getTotal()));
        }

        return dto;
    }

    /**
     * Convierte CartItem Entity a CartItemDTO.
     *
     * @param item Entity a convertir
     * @return CartItemDTO o null si item es null
     */
    public CartItemDTO toCartItemDTO(CartItem item) {
        if (item == null) {
            return null;
        }

        CartItemDTO dto = new CartItemDTO();
        dto.setCartItemId(item.getCartItemId());

        if (item.getCart() != null) {
            dto.setCartId(item.getCart().getCartId());
        }

        // Extraer datos de Product (relación)
        if (item.getProduct() != null) {
            dto.setProductId(item.getProduct().getProductId());
            dto.setProductName(item.getProduct().getName());
            dto.setProductSku(item.getProduct().getSku());
            dto.setProductAvailable(item.getProduct().isAvailable());
            dto.setProductStock(item.getProduct().getStockQty());
            dto.setProductImage(item.getProduct().getImage());
            dto.setProductCategoryName(item.getProduct().getCategory() != null
                    ? item.getProduct().getCategory().getName()
                    : "Sin categoría");
        }

        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setSubtotal(item.calculateSubtotal()); // Método helper de CartItem
        dto.setAddedAt(item.getAddedAt());

        // Formatear valores
        if (dto.getUnitPrice() != null) {
            dto.setUnitPriceFormatted(MoneyUtils.formatUSD(dto.getUnitPrice()));
        }
        if (dto.getSubtotal() != null) {
            dto.setSubtotalFormatted(MoneyUtils.formatUSD(dto.getSubtotal()));
        }

        return dto;
    }

    /**
     * Convierte CartDTO a Cart Entity.
     *
     * NOTA: No convierte User ni items completamente, eso se maneja en el servicio.
     *
     * @param dto DTO a convertir
     * @return Cart Entity o null si dto es null
     */
    public Cart toEntity(CartDTO dto) {
        if (dto == null) {
            return null;
        }

        Cart cart = new Cart();
        cart.setCartId(dto.getCartId());
        // User se debe asignar en el servicio
        cart.setStatus(dto.getStatus());
        cart.setCreatedAt(dto.getCreatedAt());
        cart.setUpdatedAt(dto.getUpdatedAt());

        return cart;
    }

    /**
     * Convierte CartItemDTO a CartItem Entity.
     *
     * NOTA: No convierte Cart ni Product, eso se maneja en el servicio.
     *
     * @param dto DTO a convertir
     * @return CartItem Entity o null si dto es null
     */
    public CartItem toCartItemEntity(CartItemDTO dto) {
        if (dto == null) {
            return null;
        }

        CartItem item = new CartItem();
        item.setCartItemId(dto.getCartItemId());
        // Cart y Product se deben asignar en el servicio
        item.setQuantity(dto.getQuantity());
        item.setUnitPrice(dto.getUnitPrice());
        item.setAddedAt(dto.getAddedAt());

        return item;
    }

    /**
     * Convierte lista de Cart Entities a lista de CartDTOs.
     *
     * @param carts Lista de entities
     * @return Lista de DTOs o lista vacía si carts es null
     */
    public List<CartDTO> toDTOList(List<Cart> carts) {
        if (carts == null) {
            return List.of();
        }

        return carts.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
