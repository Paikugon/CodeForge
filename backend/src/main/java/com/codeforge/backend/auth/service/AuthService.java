package com.codeforge.backend.auth.service;

import com.codeforge.backend.auth.dto.RegisterRequest;
import com.codeforge.backend.auth.dto.UserResponse;
import com.codeforge.backend.user.model.User;
import com.codeforge.backend.user.repository.UserRepository;
import com.codeforge.backend.auth.dto.LoginRequest;
import com.codeforge.backend.auth.dto.LoginResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
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

		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						email,
						request.password()));

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new BadCredentialsException(
						"Invalid email or password"));

		String accessToken = jwtService.generateToken(user);

		return new LoginResponse(
				accessToken,
				"Bearer",
				jwtService.getExpiresInSeconds());
	}
}