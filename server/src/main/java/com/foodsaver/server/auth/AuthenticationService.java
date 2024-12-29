package com.foodsaver.server.auth;

import com.foodsaver.server.authorization.JWTService;
import com.foodsaver.server.dtos.VerificationTokenDTO;
import com.foodsaver.server.dtos.request.AuthenticationRequest;
import com.foodsaver.server.dtos.request.RegisterRequest;
import com.foodsaver.server.dtos.response.AuthenticationResponse;
import com.foodsaver.server.enums.Role;
import com.foodsaver.server.exceptions.ApiRequestException;
import com.foodsaver.server.exceptions.User.UserCreateException;
import com.foodsaver.server.exceptions.User.UsernamePasswordException;
import com.foodsaver.server.model.User;
import com.foodsaver.server.model.VerificationToken;
import com.foodsaver.server.repositories.UserRepository;
import com.foodsaver.server.repositories.VerificationTokenRepository;
import com.foodsaver.server.services.EmailSenderService;
import com.foodsaver.server.services.VerificationTokenService;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final AuthenticationManager authenticationManager;
    private final VerificationTokenService verificationTokenService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailSenderService emailSenderService;
    private static final String INVALID_TOKEN = "Invalid token!";

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (AuthenticationException ex) {
            throw new UsernamePasswordException();
        }

        User user = userRepository.findByUsername(request.getUsername());
        if (!user.isEnabled()) throw new UsernamePasswordException();
        var jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public String register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent())
            throw new ApiRequestException("User with such email already exists!");
        if (userRepository.findByUsername(request.getUsername()) != null)
            throw new ApiRequestException("User with such username already exists!");
        if (!request.getPassword().equals(request.getRepeatedPassword())) throw new ApiRequestException("Passwords do not match!");
        if (request.getPassword().length() < 8) throw new ApiRequestException("Password must be at least 8 characters long!");
        try {
            User user = User.builder()
                    .email(request.getEmail())
                    .username(request.getUsername())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .createdAt(LocalDateTime.now())
                    .enabled(false)
                    .role(Role.USER)
                    .build();
            userRepository.save(user);
            VerificationTokenDTO verificationTokenDTO = verificationTokenService.createVerificationToken(user);
            verificationTokenRepository.save(new VerificationToken(verificationTokenDTO.getId(), verificationTokenDTO.getToken(),
                    verificationTokenDTO.getExpiryDate(), user));
            emailSenderService.sendVerificationToken(verificationTokenDTO, user);
        } catch (DataIntegrityViolationException exception) {
            throw new UserCreateException(true);
        } catch (ConstraintViolationException exception) {
            throw new UserCreateException(exception.getConstraintViolations());
        }
        return "User registered successfully!";
    }
}
