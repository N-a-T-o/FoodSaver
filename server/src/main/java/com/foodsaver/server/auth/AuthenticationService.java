package com.foodsaver.server.auth;

import com.foodsaver.server.authorization.JWTService;
import com.foodsaver.server.dtos.request.AuthenticationRequest;
import com.foodsaver.server.dtos.response.AuthenticationResponse;
import com.foodsaver.server.exceptions.UsernamePasswordException;
import com.foodsaver.server.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final AuthenticationManager authenticationManager;
    private static final String INVALID_TOKEN = "Invalid token!";

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
            var user = userRepository.findByUsername(request.getUsername());
            if (!user.isEnabled()) throw new UsernamePasswordException("User not verified!!!");
            var jwtToken = jwtService.generateToken(user);

            return AuthenticationResponse.builder()
                    .token(jwtToken)
                    .build();
        } catch (DisabledException e) {
            throw new UsernamePasswordException("Account not verified!");
        } catch (Exception e) {
            throw new UsernamePasswordException("Wrong username or password!");
        }
    }
}
