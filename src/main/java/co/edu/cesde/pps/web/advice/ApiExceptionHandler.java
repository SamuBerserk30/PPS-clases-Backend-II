package co.edu.cesde.pps.web.advice;

import co.edu.cesde.pps.exception.AppAuthenticationException;
import co.edu.cesde.pps.exception.AuthorizationException;
import co.edu.cesde.pps.exception.ValidationException;
import co.edu.cesde.pps.web.dto.error.ApiErrorCode;
import co.edu.cesde.pps.web.dto.error.ApiErrorResponse;
import co.edu.cesde.pps.web.dto.error.ApiFieldErrorResponse;
import co.edu.cesde.pps.web.error.DomainExceptionMapper;
import co.edu.cesde.pps.web.error.ErrorResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    private final DomainExceptionMapper domainExceptionMapper;
    private final ErrorResponseFactory errorResponseFactory;

    public ApiExceptionHandler(DomainExceptionMapper domainExceptionMapper,
                               ErrorResponseFactory errorResponseFactory) {
        this.domainExceptionMapper = domainExceptionMapper;
        this.errorResponseFactory = errorResponseFactory;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
                                                                         HttpServletRequest request) {
        List<ApiFieldErrorResponse> fieldErrors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldError)
                .toList();

        ValidationException validationException = new ValidationException("Request validation failed");
        return ResponseEntity.badRequest().body(
                errorResponseFactory.fromFieldErrors(validationException, request.getRequestURI(), fieldErrors)
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException exception,
                                                                      HttpServletRequest request) {
        List<ApiFieldErrorResponse> fieldErrors = exception.getConstraintViolations()
                .stream()
                .map(violation -> new ApiFieldErrorResponse(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()
                ))
                .toList();

        ValidationException validationException = new ValidationException("Request validation failed");
        return ResponseEntity.badRequest().body(
                errorResponseFactory.fromFieldErrors(validationException, request.getRequestURI(), fieldErrors)
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException exception,
                                                                     HttpServletRequest request) {
        ValidationException validationException = new ValidationException("Request body is invalid or malformed");
        return ResponseEntity.badRequest().body(
                errorResponseFactory.fromException(validationException, request.getRequestURI())
        );
    }

    @ExceptionHandler(AppAuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthentication(AppAuthenticationException exception,
                                                                 HttpServletRequest request) {
        return buildErrorResponse(exception, request);
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthorization(AuthorizationException exception,
                                                                HttpServletRequest request) {
        return buildErrorResponse(exception, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAnyException(Exception exception,
                                                               HttpServletRequest request) {
        return buildErrorResponse(exception, request);
    }

    private ResponseEntity<ApiErrorResponse> buildErrorResponse(Exception exception, HttpServletRequest request) {
        ApiErrorCode errorCode = domainExceptionMapper.map(exception);
        HttpStatus status = resolveHttpStatus(errorCode);
        return ResponseEntity.status(status)
                .body(errorResponseFactory.fromException(exception, request.getRequestURI()));
    }

    private HttpStatus resolveHttpStatus(ApiErrorCode errorCode) {
        return switch (errorCode) {
            case VALIDATION_ERROR -> HttpStatus.BAD_REQUEST;
            case RESOURCE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case DUPLICATE_RESOURCE, INSUFFICIENT_STOCK, INVALID_CART_STATE, CART_MERGE_ERROR -> HttpStatus.CONFLICT;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case INTERNAL_SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private ApiFieldErrorResponse toFieldError(FieldError fieldError) {
        return new ApiFieldErrorResponse(
                fieldError.getField(),
                fieldError.getDefaultMessage() == null ? "Invalid value" : fieldError.getDefaultMessage()
        );
    }
}

