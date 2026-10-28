package com.codeforge.backend.auth.service;

import com.codeforge.backend.auth.dto.RegisterRequest;
import com.codeforge.backend.auth.dto.UserResponse;
import com.codeforge.backend.user.dto.AdminResetPasswordRequest;
import com.codeforge.backend.user.dto.ChangePasswordRequest;
import com.codeforge.backend.user.model.User;
import com.codeforge.backend.user.repository.UserRepository;
import com.codeforge.backend.auth.dto.LoginRequest;
import com.codeforge.backend.auth.dto.LoginResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;

	public AuthService(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			AuthenticationManager authenticationManager,
			JwtService jwtService) {

		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
	}

	@Transactional
	public UserResponse register(RegisterRequest request) {

		String username = request.username().trim();
		String email = request.email()
				.trim()
				.toLowerCase(Locale.ROOT);

		if (userRepository.existsByUsername(username)) {
			throw new ResponseStatusException(
					HttpStatus.CONFLICT,
					"Username already exists");
		}

		if (userRepository.existsByEmail(email)) {
			throw new ResponseStatusException(
					HttpStatus.CONFLICT,
					"Email already exists");
		}

		if (request.password()
				.getBytes(StandardCharsets.UTF_8).length > 72) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST,
					"Password exceeds BCrypt byte limit");
		}

		String passwordHash = passwordEncoder.encode(
				request.password());

		User user = new User(
				username,
				email,
				passwordHash);

		User saved;
		try {
			saved = userRepository.saveAndFlush(user);
		} catch (DataIntegrityViolationException exception) {
			throw new ResponseStatusException(
					HttpStatus.CONFLICT,
					"Username or email already exists",
					exception);
		}

		return new UserResponse(
				saved.getId(),
				saved.getUsername(),
				saved.getEmail(),
				saved.getRole().name());
	}

	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {

		String email = request.email()
				.trim()
				.toLowerCase(Locale.ROOT);

		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(
							email,
							request.password()));
		} catch (DisabledException ex) {
			throw new ResponseStatusException(
					HttpStatus.FORBIDDEN,
					"Account is banned");
		} catch (AuthenticationException ex) {
			throw new ResponseStatusException(
					HttpStatus.UNAUTHORIZED,
					"Invalid email or password");
		}

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new BadCredentialsException(
						"Invalid email or password"));

		String accessToken = jwtService.generateToken(user);

		return new LoginResponse(
				accessToken,
				"Bearer",
				jwtService.getExpiresInSeconds(),
				user.isMustChangePassword());
	}

	private void validateNewPassword(String newPassword) {
		if (newPassword.getBytes(StandardCharsets.UTF_8).length > 72) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST,
					"Password exceeds BCrypt byte limit");
		}
		if (newPassword.length() < 8 || newPassword.length() > 72) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST,
					"Password must be between 8 and 72 characters");
		}
	}

	@Transactional
	public void resetPassword(
			long userId,
			AdminResetPasswordRequest request) {
		User user = userRepository.findByIdForUpdate(userId)
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND, "User not found"));

		validateNewPassword(request.newPassword());

		user.setPasswordHash(
				passwordEncoder.encode(request.newPassword()));
		user.setMustChangePassword(true);
		user.incrementTokenVersion();
	}

	@Transactional
	public void changePassword(
			long userId,
			ChangePasswordRequest request) {

		validateNewPassword(request.newPassword());

		User user = userRepository.findByIdForUpdate(userId)
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND, "User not found"));

		if (!passwordEncoder.matches(
				request.currentPassword(), user.getPasswordHash())) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST,
					"Current password is incorrect");
		}

		if (passwordEncoder.matches(
				request.newPassword(), user.getPasswordHash())) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST,
					"New password must differ from current password");
		}

		user.setPasswordHash(
				passwordEncoder.encode(request.newPassword()));
		user.setMustChangePassword(false);
		user.incrementTokenVersion();
	}
}