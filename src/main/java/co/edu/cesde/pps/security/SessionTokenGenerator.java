package co.edu.cesde.pps.security;

/**
 * Generador de tokens opacos para sesiones de usuario o invitado.
 */
public interface SessionTokenGenerator {

    String generateToken();
}

