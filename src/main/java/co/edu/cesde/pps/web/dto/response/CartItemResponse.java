package co.edu.cesde.pps.web.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CartItemResponse(
        Long id,
        Long productId,
        String sku,
        String name,
        String image,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal,
        Boolean productAvailable,
        Integer productStock,
        LocalDateTime addedAt
) {
}

