package co.edu.cesde.pps.web.dto.request;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductUpsertRequest(
        @NotNull
        Long categoryId,
        @NotBlank
        String sku,
        @NotBlank
        String name,
        String description,
        @Size(max = 1000)
        String image,
        @NotNull @PositiveOrZero
        BigDecimal price,
        @NotNull @PositiveOrZero
        Integer stockQty,
        @NotNull
        Boolean isActive
) {
}

