package co.edu.cesde.pps.web.error;

import co.edu.cesde.pps.exception.AppAuthenticationException;
import co.edu.cesde.pps.exception.AuthorizationException;
import co.edu.cesde.pps.exception.CartMergeException;
import co.edu.cesde.pps.exception.DuplicateEntityException;
import co.edu.cesde.pps.exception.EntityNotFoundException;
import co.edu.cesde.pps.exception.InsufficientStockException;
import co.edu.cesde.pps.exception.InvalidCartStateException;
import co.edu.cesde.pps.exception.ValidationException;
import co.edu.cesde.pps.web.dto.error.ApiErrorCode;
import org.springframework.stereotype.Component;

/**
 * Mapea excepciones de dominio a códigos de error públicos de la API.
 * Será reutilizado por el advice HTTP en etapa12.
 */
@Component
public class DomainExceptionMapper {

    public ApiErrorCode map(Throwable throwable) {
        if (throwable instanceof ValidationException) {
            return ApiErrorCode.VALIDATION_ERROR;
        }
        if (throwable instanceof EntityNotFoundException) {
            return ApiErrorCode.RESOURCE_NOT_FOUND;
        }
        if (throwable instanceof DuplicateEntityException) {
            return ApiErrorCode.DUPLICATE_RESOURCE;
        }
        if (throwable instanceof InsufficientStockException) {
            return ApiErrorCode.INSUFFICIENT_STOCK;
        }
        if (throwable instanceof InvalidCartStateException) {
            return ApiErrorCode.INVALID_CART_STATE;
        }
        if (throwable instanceof CartMergeException) {
            return ApiErrorCode.CART_MERGE_ERROR;
        }
        if (throwable instanceof AppAuthenticationException) {
            return ApiErrorCode.UNAUTHORIZED;
        }
        if (throwable instanceof AuthorizationException) {
            return ApiErrorCode.FORBIDDEN;
        }
        return ApiErrorCode.INTERNAL_SERVER_ERROR;
    }
}

