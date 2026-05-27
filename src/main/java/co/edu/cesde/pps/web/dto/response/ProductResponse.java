package co.edu.cesde.pps.web.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String sku,
        String name,
        String description,
        String image,
        BigDecimal price,
        Integer stockQty,
        Boolean isActive,
        Boolean isAvailable,
        LocalDateTime createdAt
) {
}

