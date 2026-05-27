package co.edu.cesde.pps.application;

import co.edu.cesde.pps.enums.UserStatus;
import co.edu.cesde.pps.exception.ValidationException;
import co.edu.cesde.pps.security.PasswordHasher;
import co.edu.cesde.pps.service.UserService;
import co.edu.cesde.pps.web.dto.request.CreateAdminUserRequest;
import co.edu.cesde.pps.web.dto.request.UpdateAdminUserRequest;
import co.edu.cesde.pps.web.dto.response.UserResponse;
import co.edu.cesde.pps.web.mapper.WebResponseMapper;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminUserApplicationService {

    private final UserService userService;
    private final PasswordHasher passwordHasher;
    private final WebResponseMapper webResponseMapper;

    public AdminUserApplicationService(UserService userService,
                                       PasswordHasher passwordHasher,
                                       WebResponseMapper webResponseMapper) {
        this.userService = userService;
        this.passwordHasher = passwordHasher;
        this.webResponseMapper = webResponseMapper;
    }

    @Transactional
    public UserResponse createUser(CreateAdminUserRequest request) {
        return webResponseMapper.toUserResponse(
                userService.createAdminUser(
                        request.email(),
                        passwordHasher.hash(request.password()),
                        request.firstName(),
                        request.lastName(),
                        request.phone(),
                        normalizeRole(request.role()),
                        resolveAllowedStatus(request.status())
                )
        );
    }

    public List<UserResponse> listUsers() {
        return webResponseMapper.toUserResponseList(userService.findAllUsers());
    }

    public UserResponse getUser(Long userId) {
        return webResponseMapper.toUserResponse(userService.findById(userId));
    }

    @Transactional
    public UserResponse updateUser(Long userId, UpdateAdminUserRequest request) {
        return webResponseMapper.toUserResponse(
                userService.updateAdminUser(
                        userId,
                        request.email(),
                        request.firstName(),
                        request.lastName(),
                        request.phone(),
                        normalizeRole(request.role()),
                        resolveAllowedStatus(request.status())
                )
        );
    }

    @Transactional
    public void deleteUser(Long userId) {
        userService.deleteUser(userId);
    }

    private String normalizeRole(String role) {
        return role.trim().toUpperCase(Locale.ROOT);
    }

    private UserStatus resolveAllowedStatus(String status) {
        UserStatus resolvedStatus = UserStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        if (resolvedStatus != UserStatus.ACTIVE && resolvedStatus != UserStatus.INACTIVE) {
            throw new ValidationException("status", status, "Status must be ACTIVE or INACTIVE");
        }
        return resolvedStatus;
    }
}

