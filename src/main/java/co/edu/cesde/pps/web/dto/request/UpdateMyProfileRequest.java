package co.edu.cesde.pps.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateMyProfileRequest(
        @NotBlank
        String firstName,
        @NotBlank
        String lastName,
        String phone
) {
}

