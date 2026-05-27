package co.edu.cesde.pps.application;

import co.edu.cesde.pps.exception.AppAuthenticationException;
import co.edu.cesde.pps.model.User;
import co.edu.cesde.pps.security.PasswordHasher;
import co.edu.cesde.pps.service.UserService;
import co.edu.cesde.pps.service.UserSessionService;
import co.edu.cesde.pps.web.dto.request.ChangeMyPasswordRequest;
import co.edu.cesde.pps.web.dto.request.UpdateMyProfileRequest;
import co.edu.cesde.pps.web.dto.response.UserResponse;
import co.edu.cesde.pps.web.mapper.WebResponseMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserProfileApplicationService {

    private final UserSessionService userSessionService;
    private final UserService userService;
    private final PasswordHasher passwordHasher;
    private final WebResponseMapper webResponseMapper;

    public UserProfileApplicationService(UserSessionService userSessionService,
                                         UserService userService,
                                         PasswordHasher passwordHasher,
                                         WebResponseMapper webResponseMapper) {
        this.userSessionService = userSessionService;
        this.userService = userService;
        this.passwordHasher = passwordHasher;
        this.webResponseMapper = webResponseMapper;
    }

    @Transactional
    public UserResponse updateMyProfile(String sessionToken, UpdateMyProfileRequest request) {
        User user = userSessionService.requireAuthenticatedUser(sessionToken);
        return webResponseMapper.toUserResponse(
                userService.updateProfile(user.getUserId(), request.firstName(), request.lastName(), request.phone())
        );
    }

    @Transactional
    public void changeMyPassword(String sessionToken, ChangeMyPasswordRequest request) {
        User user = userSessionService.requireAuthenticatedUser(sessionToken);

        if (!passwordHasher.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new AppAuthenticationException("Current password is invalid");
        }

        userService.updatePasswordHash(user.getUserId(), passwordHasher.hash(request.newPassword()));
    }
}

