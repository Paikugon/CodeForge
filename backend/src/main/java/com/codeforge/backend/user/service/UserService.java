package com.codeforge.backend.user.service;

import com.codeforge.backend.submission.repository.SubmissionRepository;
import com.codeforge.backend.user.dto.AdminUpdateUserRequest;
import com.codeforge.backend.user.dto.UserProfileResponse;
import com.codeforge.backend.user.enums.UserRole;
import com.codeforge.backend.user.model.User;
import com.codeforge.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.springframework.http.HttpStatus;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;

    public UserService(
            UserRepository userRepository,
            SubmissionRepository submissionRepository) {
        this.userRepository = userRepository;
        this.submissionRepository = submissionRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(long userId, boolean includePrivateInfo) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND, "User not found"));

        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                includePrivateInfo ? user.getEmail() : null,
                includePrivateInfo ? user.getRole().name() : null,
                submissionRepository.countSolvedProblemsByUserId(userId),
                submissionRepository.countAcceptedByUserId(userId),
                submissionRepository.countByUserId(userId),
                submissionRepository.countUnsolvedProblemsByUserId(userId),
                user.getCreatedAt(),
                includePrivateInfo ? user.isMustChangePassword() : null);
    }

    @Transactional
    public void updateUser(long userId, AdminUpdateUserRequest request) {
        if (request.role() == null && request.enabled() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "At least one field is required");
        }

        // Lấy khóa trước khi đọc user và kiểm tra số admin.
        var admins = userRepository.findAllByRoleForUpdate(UserRole.ADMIN);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND, "User not found"));

        UserRole nextRole = request.role() != null
                ? request.role()
                : user.getRole();

        boolean nextEnabled = request.enabled() != null
                ? request.enabled()
                : user.isEnabled();

        boolean removesActiveAdmin = user.getRole() == UserRole.ADMIN
                && user.isEnabled()
                && (nextRole != UserRole.ADMIN || !nextEnabled);

        long activeAdminCount = admins.stream()
                .filter(User::isEnabled)
                .count();

        if (removesActiveAdmin && activeAdminCount <= 1) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Cannot disable or demote the last active admin");
        }

        user.setRole(nextRole);
        user.setEnabled(nextEnabled);
    }
}
