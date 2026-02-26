package co.edu.cesde.pps.model;

import jakarta.persistence.*;
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
@Entity
@Table(name = "user_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long sessionId;

    @Column(name = "user_id", nullable = false, length = 50)
    private User user;

    @Column(name = "session_token", nullable = false, unique = true, length = 100)
    private String sessionToken;

    @Builder.Default
    @Column(name = "created_at", nullable = false, length = 50)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "expires_at", nullable = false, length = 50)
    private LocalDateTime expiresAt;

    // Constructor vacío (requerido para JPA futuro)

    // Constructor para sesión de invitado (sin user)

    // Constructor para sesión de usuario registrado

    // Constructor completo (excepto ID y createdAt autogenerados)

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

    @Override
    public String toString() {
        return "UserSession{" +
                "sessionId=" + sessionId +
                ", userId=" + (user != null ? user.getUserId() : null) +
                ", sessionToken='" + sessionToken + '\'' +
                ", createdAt=" + createdAt +
                ", expiresAt=" + expiresAt +
                ", isGuest=" + isGuestSession() +
                ", isExpired=" + isExpired() +
                '}';
    }
}
