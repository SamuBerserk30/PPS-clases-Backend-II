package co.edu.cesde.pps.web.dto.request;

import jakarta.validation.constraints.NotNull;

public record CheckoutRequest(
        @NotNull
        Long cartId,
        @NotNull
        Long shippingAddressId,
        @NotNull
        Long billingAddressId
) {
}

