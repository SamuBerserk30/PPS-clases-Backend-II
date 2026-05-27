package co.edu.cesde.pps.web.dto.request;

import co.edu.cesde.pps.enums.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddressUpsertRequest(
        @NotNull
        AddressType type,
        @NotBlank
        String line1,
        String line2,
        @NotBlank
        String city,
        @NotBlank
        String state,
        @NotBlank
        String country,
        @NotBlank
        String postalCode,
        Boolean isDefault
) {
}

