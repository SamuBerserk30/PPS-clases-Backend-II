package co.edu.cesde.pps.web.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record CartResponse(
        Long id,
        Long userId,
        String userEmail,
        String status,
        Boolean isGuest,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<CartItemResponse> items,
        CartSummaryResponse summary
) {
}

