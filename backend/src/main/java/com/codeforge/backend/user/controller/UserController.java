package com.codeforge.backend.user.controller;

import com.codeforge.backend.user.dto.AdminUpdateUserRequest;
import com.codeforge.backend.user.dto.UserProfileResponse;
import com.codeforge.backend.user.service.UserService;

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

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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
}
