package co.edu.cesde.pps.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateAdminUserRequest(
        @NotBlank @Email
        String email,
        @NotBlank @Size(min = 8, max = 100)
        String password,
        @NotBlank
        String firstName,
        @NotBlank
        String lastName,
        String phone,
        @NotBlank
        @Pattern(regexp = "(?i)ADMIN|CUSTOMER", message = "role must be ADMIN or CUSTOMER")
        String role,
        @NotBlank
        @Pattern(regexp = "(?i)ACTIVE|INACTIVE", message = "status must be ACTIVE or INACTIVE")
        String status
) {
}

