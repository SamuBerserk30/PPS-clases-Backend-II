package co.edu.cesde.pps.web.dto.error;

import java.time.LocalDateTime;
import java.util.List;

public record ApiErrorResponse(
        ApiErrorCode code,
        String message,
        List<ApiFieldErrorResponse> details,
        LocalDateTime timestamp,
        String path
) {
}

