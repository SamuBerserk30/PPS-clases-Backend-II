package co.edu.cesde.pps.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email
        String email,
        @NotBlank @Size(min = 6, max = 15)
        String password,
        @NotBlank
        String firstName,
        @NotBlank
        String lastName,
        String phone,
        Long guestCartId
) {
}

