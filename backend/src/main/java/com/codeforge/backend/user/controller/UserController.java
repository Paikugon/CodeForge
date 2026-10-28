package com.codeforge.backend.user.controller;

import com.codeforge.backend.auth.service.AuthService;
import com.codeforge.backend.user.dto.AdminResetPasswordRequest;
import com.codeforge.backend.user.dto.AdminUpdateUserRequest;
import com.codeforge.backend.user.dto.ChangePasswordRequest;
import com.codeforge.backend.user.dto.UserProfileResponse;
import com.codeforge.backend.user.service.UserService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    public UserController(
            UserService userService,
            AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @GetMapping("/profile/me")
    public UserProfileResponse getCurrentUser(
            @AuthenticationPrincipal Jwt jwt) {
        return userService.getProfile(Long.parseLong(jwt.getSubject()), true);
    }

    @GetMapping("/profile/{userId}")
    public UserProfileResponse getProfile(
            @PathVariable Long userId) {
        return userService.getProfile(userId, false);
    }

    @PatchMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateUser(
            @PathVariable Long userId,
            @RequestBody AdminUpdateUserRequest request) {

        userService.updateUser(userId, request);
    }

    @PutMapping("/{userId}/password")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(
            @PathVariable Long userId,
            @Valid @RequestBody AdminResetPasswordRequest request) {

        authService.resetPassword(userId, request);
    }

    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ChangePasswordRequest request) {

        authService.changePassword(
                Long.parseLong(jwt.getSubject()), request);
    }
}
