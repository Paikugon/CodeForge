package com.codeforge.backend.user.service;

import com.codeforge.backend.submission.repository.SubmissionRepository;
import com.codeforge.backend.user.dto.UserProfileResponse;
import com.codeforge.backend.user.model.User;
import com.codeforge.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

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
                user.getCreatedAt());
    }
}
