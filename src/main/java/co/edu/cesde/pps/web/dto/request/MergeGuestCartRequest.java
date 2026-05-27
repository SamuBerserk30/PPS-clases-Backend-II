package co.edu.cesde.pps.web.dto.request;

import jakarta.validation.constraints.NotNull;

public record MergeGuestCartRequest(
        @NotNull
        Long guestCartId
) {
}

