package co.edu.cesde.pps.web.dto.error;

public record ApiFieldErrorResponse(
        String field,
        String message
) {
}

