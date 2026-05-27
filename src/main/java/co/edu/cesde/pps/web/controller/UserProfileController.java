package co.edu.cesde.pps.web.controller;

import co.edu.cesde.pps.application.UserProfileApplicationService;
import co.edu.cesde.pps.web.dto.request.ChangeMyPasswordRequest;
import co.edu.cesde.pps.web.dto.request.UpdateMyProfileRequest;
import co.edu.cesde.pps.web.dto.response.UserResponse;
import co.edu.cesde.pps.web.security.CurrentSessionResolver;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiRoutes.USER_PROFILE)
public class UserProfileController {

    private final UserProfileApplicationService userProfileApplicationService;
    private final CurrentSessionResolver currentSessionResolver;

    public UserProfileController(UserProfileApplicationService userProfileApplicationService,
                                 CurrentSessionResolver currentSessionResolver) {
        this.userProfileApplicationService = userProfileApplicationService;
        this.currentSessionResolver = currentSessionResolver;
    }

    @PutMapping
    public UserResponse updateMyProfile(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false)
                                        String authorizationHeader,
                                        @Valid @RequestBody UpdateMyProfileRequest request) {
        return userProfileApplicationService.updateMyProfile(
                currentSessionResolver.resolveCurrentToken(authorizationHeader),
                request
        );
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changeMyPassword(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false)
                                                 String authorizationHeader,
                                                 @Valid @RequestBody ChangeMyPasswordRequest request) {
        userProfileApplicationService.changeMyPassword(
                currentSessionResolver.resolveCurrentToken(authorizationHeader),
                request
        );
        return ResponseEntity.noContent().build();
    }
}

