package co.edu.cesde.pps.web.security;

import co.edu.cesde.pps.exception.AppAuthenticationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class BearerTokenExtractor {

    public String extractRequiredToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new AppAuthenticationException("Authorization header is required");
        }

        String[] parts = authorizationHeader.trim().split("\\s+");
        if (parts.length != 2 || !"Bearer".equals(parts[0]) || parts[1].isBlank()) {
            throw new AppAuthenticationException("Authorization header must use the format 'Bearer <token>'");
        }

        return parts[1];
    }

    public String extractRequiredToken(HttpServletRequest request) {
        return extractRequiredToken(request.getHeader(HttpHeaders.AUTHORIZATION));
    }
}

