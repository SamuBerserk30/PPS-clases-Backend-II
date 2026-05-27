package co.edu.cesde.pps.application;

import co.edu.cesde.pps.config.AppConfig;
import co.edu.cesde.pps.dto.CartDTO;
import co.edu.cesde.pps.dto.UserDTO;
import co.edu.cesde.pps.enums.UserStatus;
import co.edu.cesde.pps.exception.AppAuthenticationException;
import co.edu.cesde.pps.model.User;
import co.edu.cesde.pps.model.UserSession;
import co.edu.cesde.pps.security.PasswordHasher;
import co.edu.cesde.pps.service.CartService;
import co.edu.cesde.pps.service.UserService;
import co.edu.cesde.pps.service.UserSessionService;
import co.edu.cesde.pps.util.ValidationUtils;
import co.edu.cesde.pps.web.dto.request.LoginRequest;
import co.edu.cesde.pps.web.dto.request.RegisterRequest;
import co.edu.cesde.pps.web.dto.response.AuthSessionResponse;
import co.edu.cesde.pps.web.dto.response.UserResponse;
import co.edu.cesde.pps.web.mapper.WebResponseMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Capa de aplicación para auth/sesión previa a la exposición HTTP real.
 */
@Service
@Transactional(readOnly = true)
public class AuthApplicationService {

    private static final String PASSWORD_FIELD = "password";

    private final UserService userService;
    private final CartService cartService;
    private final UserSessionService userSessionService;
    private final PasswordHasher passwordHasher;
    private final WebResponseMapper webResponseMapper;

    public AuthApplicationService(UserService userService,
                                  CartService cartService,
                                  UserSessionService userSessionService,
                                  PasswordHasher passwordHasher,
                                  WebResponseMapper webResponseMapper) {
        this.userService = userService;
        this.cartService = cartService;
        this.userSessionService = userSessionService;
        this.passwordHasher = passwordHasher;
        this.webResponseMapper = webResponseMapper;
    }

    @Transactional
    public AuthSessionResponse createGuestSession() {
        UserSession session = userSessionService.createGuestSession();
        CartDTO cart = cartService.findOrCreateOpenCartForGuestSession(session.getSessionId());
        return webResponseMapper.toAuthSessionResponse(session, null, cart);
    }

    @Transactional
    public AuthSessionResponse register(RegisterRequest request) {
        validateRegisterRequest(request);

        String hashedPassword = passwordHasher.hash(request.password());
        UserDTO user = userService.registerUser(
                request.email(),
                hashedPassword,
                request.firstName(),
                request.lastName(),
                request.phone()
        );

        User persistedUser = userService.findUserEntityOrThrow(user.getUserId());
        UserSession session = userSessionService.createAuthenticatedSession(persistedUser);
        CartDTO cart = resolveAuthenticatedCart(user.getUserId(), request.guestCartId());

        return webResponseMapper.toAuthSessionResponse(session, user, cart);
    }

    @Transactional
    public AuthSessionResponse login(LoginRequest request) {
        validateLoginRequest(request);

        User user = userService.findUserEntityByEmailOrThrow(request.email());
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AppAuthenticationException("User account is inactive");
        }
        if (!passwordHasher.matches(request.password(), user.getPasswordHash())) {
            throw new AppAuthenticationException("Invalid credentials");
        }

        UserSession session = userSessionService.createAuthenticatedSession(user);
        UserDTO userDTO = userService.findById(user.getUserId());
        CartDTO cart = resolveAuthenticatedCart(user.getUserId(), request.guestCartId());

        return webResponseMapper.toAuthSessionResponse(session, userDTO, cart);
    }

    public UserResponse getCurrentUser(String sessionToken) {
        User user = userSessionService.requireAuthenticatedUser(sessionToken);
        return webResponseMapper.toUserResponse(userService.findById(user.getUserId()));
    }

    @Transactional
    public void logout(String sessionToken) {
        userSessionService.expireSession(sessionToken);
    }

    private CartDTO resolveAuthenticatedCart(Long userId, Long guestCartId) {
        if (guestCartId != null) {
            return cartService.mergeGuestCartToUserCart(guestCartId, userId);
        }
        return cartService.findOrCreateOpenCartForUser(userId);
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (request == null) {
            throw new AppAuthenticationException("Register request is required");
        }
        ValidationUtils.validateEmail(request.email(), "email");
        ValidationUtils.validateNotBlank(request.password(), PASSWORD_FIELD);
        ValidationUtils.validateMinLength(request.password(), AppConfig.getMinPasswordLength(), PASSWORD_FIELD);
        ValidationUtils.validateNotBlank(request.firstName(), "firstName");
        ValidationUtils.validateNotBlank(request.lastName(), "lastName");
        if (request.phone() != null && !request.phone().isBlank()) {
            ValidationUtils.validatePhone(request.phone(), "phone");
        }
    }

    private void validateLoginRequest(LoginRequest request) {
        if (request == null) {
            throw new AppAuthenticationException("Login request is required");
        }
        ValidationUtils.validateEmail(request.email(), "email");
        ValidationUtils.validateNotBlank(request.password(), PASSWORD_FIELD);
    }
}

