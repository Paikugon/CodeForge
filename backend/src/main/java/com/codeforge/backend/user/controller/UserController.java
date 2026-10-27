package com.codeforge.backend.user.controller;

import com.codeforge.backend.user.dto.UserProfileResponse;
import com.codeforge.backend.user.service.UserService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
    
}
