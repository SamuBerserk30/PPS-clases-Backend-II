package co.edu.cesde.pps.web.error;

import co.edu.cesde.pps.web.dto.error.ApiErrorResponse;
import co.edu.cesde.pps.web.dto.error.ApiFieldErrorResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Construye respuestas de error normalizadas para la futura capa HTTP.
 */
@Component
public class ErrorResponseFactory {

    private final DomainExceptionMapper domainExceptionMapper;

    public ErrorResponseFactory(DomainExceptionMapper domainExceptionMapper) {
        this.domainExceptionMapper = domainExceptionMapper;
    }

    public ApiErrorResponse fromException(Throwable throwable, String path) {
        return new ApiErrorResponse(
                domainExceptionMapper.map(throwable),
                throwable.getMessage(),
                List.of(),
                LocalDateTime.now(),
                path
        );
    }

    public ApiErrorResponse fromFieldErrors(Throwable throwable, String path,
                                            List<ApiFieldErrorResponse> fieldErrors) {
        return new ApiErrorResponse(
                domainExceptionMapper.map(throwable),
                throwable.getMessage(),
                fieldErrors == null ? List.of() : fieldErrors,
                LocalDateTime.now(),
                path
        );
    }
}

