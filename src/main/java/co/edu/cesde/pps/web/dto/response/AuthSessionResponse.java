package co.edu.cesde.pps.web.dto.response;

import java.time.LocalDateTime;

public record AuthSessionResponse(
        String sessionToken,
        Long sessionId,
        LocalDateTime expiresAt,
        UserResponse user,
        CartResponse cart
) {
}

