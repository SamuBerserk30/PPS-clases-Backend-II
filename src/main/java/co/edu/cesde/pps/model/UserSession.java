package co.edu.cesde.pps.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad UserSession - Representa sesiones activas para navegación.
 *
 * Crucial para manejar carritos de invitado y sesiones de usuarios registrados.
 *
 * Campos:
 * - sessionId: Identificador único de la sesión (PK)
 * - user: Usuario asociado (N:1 con User) - NULLABLE para invitados
 * - sessionToken: Token único de sesión (UNIQUE) - mapea con cookie/JWT
 * - createdAt: Fecha de creación de la sesión
 * - expiresAt: Fecha de expiración de la sesión
 *
 * Comportamiento:
 * - user = NULL → sesión de invitado (guest)
 * - user = <User> → sesión de usuario registrado
 *
 * Relaciones:
 * - N:1 con User (opcional, nullable - muchas sesiones pueden pertenecer a un usuario)
 * - 1:N con Cart (una sesión puede tener múltiples carritos en el tiempo)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserSession {

    private Long sessionId;
    private User user; // Nullable - NULL para invitados
    private String sessionToken;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    // Constructor vacío (requerido para JPA futuro)

    // Constructor para sesión de invitado (sin user)
    public UserSession(String sessionToken, LocalDateTime expiresAt) {
        this.user = null; // Invitado
        this.sessionToken = sessionToken;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
    }

    // Constructor para sesión de usuario registrado
    public UserSession(User user, String sessionToken, LocalDateTime expiresAt) {
        this.user = user;
        this.sessionToken = sessionToken;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
    }

    // Constructor completo (excepto ID y createdAt autogenerados)
    public UserSession(User user, String sessionToken, LocalDateTime expiresAt, LocalDateTime createdAt) {
        this.user = user;
        this.sessionToken = sessionToken;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.expiresAt = expiresAt;
    }

    // Getters y Setters

    // Método helper para verificar si es sesión de invitado
    public boolean isGuestSession() {
        return user == null;
    }

    // Método helper para verificar si la sesión ha expirado
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    // equals y hashCode basados en ID

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserSession that = (UserSession) o;
        return Objects.equals(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId);
    }

    // toString sin navegación a objetos relacionados (solo IDs)

}
