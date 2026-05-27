package co.edu.cesde.pps.exception;

/**
 * Excepción lanzada cuando una operación requiere autenticación válida
 * y la sesión o credenciales no cumplen las reglas esperadas.
 */
public class AppAuthenticationException extends BusinessException {

    public AppAuthenticationException(String message) {
        super(message);
    }

    public AppAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}

