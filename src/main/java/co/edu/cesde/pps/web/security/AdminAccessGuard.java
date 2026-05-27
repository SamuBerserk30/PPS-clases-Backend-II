package co.edu.cesde.pps.web.security;

import co.edu.cesde.pps.dto.UserDTO;
import co.edu.cesde.pps.exception.AuthorizationException;
import co.edu.cesde.pps.model.User;
import co.edu.cesde.pps.service.UserService;
import co.edu.cesde.pps.util.Constants;
import org.springframework.stereotype.Component;

@Component
public class AdminAccessGuard {

    private final CurrentSessionResolver currentSessionResolver;
    private final UserService userService;

    public AdminAccessGuard(CurrentSessionResolver currentSessionResolver,
                            UserService userService) {
        this.currentSessionResolver = currentSessionResolver;
        this.userService = userService;
    }

    public void requireAdmin(String authorizationHeader) {
        User user = currentSessionResolver.resolveAuthenticatedUser(authorizationHeader);
        UserDTO userDTO = userService.findById(user.getUserId());
        String roleName = userDTO.getRoleName();

        if (!Constants.ROLE_ADMIN.equalsIgnoreCase(roleName)) {
            throw new AuthorizationException("Admin role is required for this operation");
        }
    }
}



