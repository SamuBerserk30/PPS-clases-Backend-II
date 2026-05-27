package co.edu.cesde.pps.web.security;

import co.edu.cesde.pps.model.User;
import co.edu.cesde.pps.model.UserSession;
import co.edu.cesde.pps.service.UserSessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class CurrentSessionResolver {

    private final BearerTokenExtractor bearerTokenExtractor;
    private final UserSessionService userSessionService;

    public CurrentSessionResolver(BearerTokenExtractor bearerTokenExtractor,
                                  UserSessionService userSessionService) {
        this.bearerTokenExtractor = bearerTokenExtractor;
        this.userSessionService = userSessionService;
    }

    public String resolveCurrentToken(String authorizationHeader) {
        return bearerTokenExtractor.extractRequiredToken(authorizationHeader);
    }

    public String resolveCurrentToken(HttpServletRequest request) {
        return bearerTokenExtractor.extractRequiredToken(request);
    }

    public UserSession resolveCurrentSession(String authorizationHeader) {
        return userSessionService.requireActiveSession(resolveCurrentToken(authorizationHeader));
    }

    public UserSession resolveCurrentSession(HttpServletRequest request) {
        return userSessionService.requireActiveSession(resolveCurrentToken(request));
    }

    public User resolveAuthenticatedUser(String authorizationHeader) {
        return userSessionService.requireAuthenticatedUser(resolveCurrentToken(authorizationHeader));
    }

    public User resolveAuthenticatedUser(HttpServletRequest request) {
        return userSessionService.requireAuthenticatedUser(resolveCurrentToken(request));
    }
}

