package co.edu.cesde.pps.service;

import co.edu.cesde.pps.config.AppConfig;
import co.edu.cesde.pps.exception.AppAuthenticationException;
import co.edu.cesde.pps.exception.DuplicateEntityException;
import co.edu.cesde.pps.exception.EntityNotFoundException;
import co.edu.cesde.pps.model.User;
import co.edu.cesde.pps.model.UserSession;
import co.edu.cesde.pps.repository.UserSessionRepository;
import co.edu.cesde.pps.security.SessionTokenGenerator;
import co.edu.cesde.pps.util.ValidationUtils;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para gestión de sesiones opacas basadas en {@link UserSession}.
 *
 * Prepara en etapa11 la base reusable para auth y resolución de contexto
 * antes de exponer controllers en etapa12.
 */
@Service
@Transactional(readOnly = true)
public class UserSessionService {

    private final UserSessionRepository userSessionRepository;
    private final SessionTokenGenerator sessionTokenGenerator;

    public UserSessionService(UserSessionRepository userSessionRepository,
                              SessionTokenGenerator sessionTokenGenerator) {
        this.userSessionRepository = userSessionRepository;
        this.sessionTokenGenerator = sessionTokenGenerator;
    }

    @Transactional
    public UserSession createGuestSession() {
        return userSessionRepository.save(UserSession.builder()
                .user(null)
                .sessionToken(generateUniqueToken())
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(AppConfig.getGuestSessionTimeoutHours()))
                .build());
    }

    @Transactional
    public UserSession createAuthenticatedSession(User user) {
        return userSessionRepository.save(UserSession.builder()
                .user(user)
                .sessionToken(generateUniqueToken())
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(AppConfig.getUserSessionTimeoutHours()))
                .build());
    }

    public UserSession findByIdOrThrow(Long sessionId) {
        return userSessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("UserSession", sessionId));
    }

    public UserSession findByTokenOrThrow(String sessionToken) {
        ValidationUtils.validateNotBlank(sessionToken, "sessionToken");
        return userSessionRepository.findBySessionToken(sessionToken)
                .orElseThrow(() -> new EntityNotFoundException("UserSession", sessionToken));
    }

    public UserSession requireActiveSession(String sessionToken) {
        UserSession session = findByTokenOrThrow(sessionToken);
        if (session.isExpired()) {
            throw new AppAuthenticationException("Session token has expired");
        }
        return session;
    }

    public User requireAuthenticatedUser(String sessionToken) {
        UserSession session = requireActiveSession(sessionToken);
        if (session.getUser() == null) {
            throw new AppAuthenticationException("Authenticated user is required for this operation");
        }
        return session.getUser();
    }

    public List<UserSession> findActiveSessionsByUser(Long userId) {
        return userSessionRepository.findByUser_UserIdAndExpiresAtAfterOrderByCreatedAtDesc(
                userId, LocalDateTime.now());
    }

    @Transactional
    public void expireSession(String sessionToken) {
        UserSession session = findByTokenOrThrow(sessionToken);
        session.setExpiresAt(LocalDateTime.now().minusSeconds(1));
        userSessionRepository.save(session);
    }

    private String generateUniqueToken() {
        for (int attempts = 0; attempts < 5; attempts++) {
            String candidate = sessionTokenGenerator.generateToken();
            if (!userSessionRepository.existsBySessionToken(candidate)) {
                return candidate;
            }
        }
        throw new DuplicateEntityException("UserSession", "sessionToken",
                "Could not generate a unique session token after multiple attempts");
    }
}

