package co.edu.cesde.pps.web.controller;

import co.edu.cesde.pps.application.AdminUserApplicationService;
import co.edu.cesde.pps.web.dto.request.CreateAdminUserRequest;
import co.edu.cesde.pps.web.dto.request.UpdateAdminUserRequest;
import co.edu.cesde.pps.web.dto.response.UserResponse;
import co.edu.cesde.pps.web.security.AdminAccessGuard;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiRoutes.ADMIN_USERS)
public class AdminUserController {

    private final AdminUserApplicationService adminUserApplicationService;
    private final AdminAccessGuard adminAccessGuard;

    public AdminUserController(AdminUserApplicationService adminUserApplicationService,
                               AdminAccessGuard adminAccessGuard) {
        this.adminUserApplicationService = adminUserApplicationService;
        this.adminAccessGuard = adminAccessGuard;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Valid @RequestBody CreateAdminUserRequest request) {
        adminAccessGuard.requireAdmin(authorizationHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(adminUserApplicationService.createUser(request));
    }

    @GetMapping
    public List<UserResponse> listUsers(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
        adminAccessGuard.requireAdmin(authorizationHeader);
        return adminUserApplicationService.listUsers();
    }

    @GetMapping("/{id}")
    public UserResponse getUser(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @PathVariable Long id) {
        adminAccessGuard.requireAdmin(authorizationHeader);
        return adminUserApplicationService.getUser(id);
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @PathVariable Long id,
            @Valid @RequestBody UpdateAdminUserRequest request) {
        adminAccessGuard.requireAdmin(authorizationHeader);
        return adminUserApplicationService.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @PathVariable Long id) {
        adminAccessGuard.requireAdmin(authorizationHeader);
        adminUserApplicationService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}

