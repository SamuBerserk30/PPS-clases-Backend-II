package co.edu.cesde.pps.security;

/**
 * Abstracción para hashing y verificación de contraseñas.
 *
 * Permite preparar la lógica de autenticación en etapa11 sin acoplarla
 * a una librería específica desde la capa de aplicación.
 */
public interface PasswordHasher {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String hashedPassword);
}

