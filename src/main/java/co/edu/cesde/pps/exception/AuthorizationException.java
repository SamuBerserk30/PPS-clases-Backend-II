package co.edu.cesde.pps.exception;

public class AuthorizationException extends BusinessException {

    public AuthorizationException(String message) {
        super(message);
    }

    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}

