package com.foodsaver.server.auth;

import com.foodsaver.server.dtos.request.AuthenticationRequest;
import com.foodsaver.server.dtos.request.RegisterRequest;
import com.foodsaver.server.dtos.response.AuthenticationResponse;
import com.foodsaver.server.exceptions.EmailNotVerifiedException;
import com.foodsaver.server.exceptions.User.UsernamePasswordException;
import com.foodsaver.server.model.User;
import com.foodsaver.server.repositories.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService service;

    private final UserRepository userRepository;

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request){
        User user = userRepository.findByUsername(request.getUsername());
        if (user == null) throw new UsernamePasswordException();
        if(!user.isEnabled()) {
            throw new EmailNotVerifiedException();
        }

        return ResponseEntity.ok(service.authenticate(request));
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(service.register(request));
    }
}