package co.edu.cesde.pps.web.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddCartItemRequest(
        @NotNull
        Long productId,
        @NotNull @Positive
        Integer quantity
) {
}

