package co.edu.cesde.pps.web.dto.response;

import co.edu.cesde.pps.enums.UserStatus;
import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String fullName,
        String role,
        String phone,
        UserStatus status,
        LocalDateTime createdAt
) {
}

