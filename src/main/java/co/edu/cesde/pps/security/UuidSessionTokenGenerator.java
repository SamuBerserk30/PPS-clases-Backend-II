package co.edu.cesde.pps.security;

import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Implementación simple de token opaco basada en UUID.
 */
@Component
public class UuidSessionTokenGenerator implements SessionTokenGenerator {

    @Override
    public String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

