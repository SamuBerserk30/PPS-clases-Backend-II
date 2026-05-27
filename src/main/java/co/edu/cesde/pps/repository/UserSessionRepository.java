package co.edu.cesde.pps.repository;

import co.edu.cesde.pps.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findBySessionToken(String sessionToken);

    boolean existsBySessionToken(String sessionToken);

    List<UserSession> findByUser_UserIdAndExpiresAtAfterOrderByCreatedAtDesc(
            Long userId, LocalDateTime expiresAt);
}