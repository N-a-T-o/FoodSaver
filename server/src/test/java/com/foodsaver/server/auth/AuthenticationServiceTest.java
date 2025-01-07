package com.foodsaver.server.auth;

import com.foodsaver.server.authorization.JWTService;
import com.foodsaver.server.dtos.UserDTO;
import com.foodsaver.server.dtos.VerificationTokenDTO;
import com.foodsaver.server.dtos.request.AuthenticationRequest;
import com.foodsaver.server.dtos.request.RegisterRequest;
import com.foodsaver.server.dtos.response.AuthenticationResponse;
import com.foodsaver.server.exceptions.ApiRequestException;
import com.foodsaver.server.exceptions.User.UserCreateException;
import com.foodsaver.server.exceptions.User.UsernamePasswordException;
import com.foodsaver.server.model.User;
import com.foodsaver.server.model.VerificationToken;
import com.foodsaver.server.repositories.UserRepository;
import com.foodsaver.server.repositories.VerificationTokenRepository;
import com.foodsaver.server.services.EmailSenderService;
import com.foodsaver.server.services.VerificationTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AuthenticationServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JWTService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private VerificationTokenService verificationTokenService;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private EmailSenderService emailSenderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void authenticate_ShouldReturnAuthenticationResponseWhenValid() {
        AuthenticationRequest request = new AuthenticationRequest("johndoe", "password");
        User user = mock(User.class);
        String token = "jwtToken";

        when(userRepository.findByUsername(request.getUsername())).thenReturn(user);
        when(user.isEnabled()).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn(token);

        AuthenticationResponse response = authenticationService.authenticate(request);

        assertEquals(token, response.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(user);
    }

    @Test
    void authenticate_ShouldThrowUsernamePasswordExceptionWhenInvalidCredentials() {
        AuthenticationRequest request = new AuthenticationRequest("johndoe", "wrongpassword");

        doThrow(new BadCredentialsException("Invalid credentials"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(UsernamePasswordException.class, () -> authenticationService.authenticate(request));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(jwtService);
    }

    @Test
    void authenticate_ShouldThrowUsernamePasswordExceptionWhenUserNotEnabled() {
        AuthenticationRequest request = new AuthenticationRequest("johndoe", "password");
        User user = mock(User.class);

        when(userRepository.findByUsername(request.getUsername())).thenReturn(user);
        when(user.isEnabled()).thenReturn(false);

        assertThrows(UsernamePasswordException.class, () -> authenticationService.authenticate(request));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void register_ShouldRegisterUserSuccessfully() {
        RegisterRequest request = new RegisterRequest("john@example.com", "johndoe", "password123", "password123");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(request.getUsername())).thenReturn(null);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

        VerificationTokenDTO tokenDTO = new VerificationTokenDTO();
        tokenDTO.setId(1L);
        tokenDTO.setToken("verificationToken");
        tokenDTO.setExpiryDate(LocalDateTime.now().plusDays(1));
        tokenDTO.setUserDTO(mock(UserDTO.class)); // Mocked UserDTO object

        when(verificationTokenService.createVerificationToken(any(User.class))).thenReturn(tokenDTO);

        String result = authenticationService.register(request);

        assertEquals("User registered successfully!", result);
        verify(userRepository).save(any(User.class));
        verify(verificationTokenRepository).save(any(VerificationToken.class));
        verify(emailSenderService).sendVerificationToken(eq(tokenDTO), any(User.class)); // Fixed line
    }

    @Test
    void register_ShouldThrowExceptionWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("john@example.com", "johndoe", "password123", "password123");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(mock(User.class)));

        ApiRequestException exception = assertThrows(ApiRequestException.class, () -> authenticationService.register(request));
        assertEquals("User with such email already exists!", exception.getMessage());
    }

    @Test
    void register_ShouldThrowExceptionWhenUsernameAlreadyExists() {
        RegisterRequest request = new RegisterRequest("john@example.com", "johndoe", "password123", "password123");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(request.getUsername())).thenReturn(mock(User.class));

        ApiRequestException exception = assertThrows(ApiRequestException.class, () -> authenticationService.register(request));
        assertEquals("User with such username already exists!", exception.getMessage());
    }

    @Test
    void register_ShouldThrowExceptionWhenPasswordsDoNotMatch() {
        RegisterRequest request = new RegisterRequest("john@example.com", "johndoe", "password123", "differentPassword");

        ApiRequestException exception = assertThrows(ApiRequestException.class, () -> authenticationService.register(request));
        assertEquals("Passwords do not match!", exception.getMessage());
    }

    @Test
    void register_ShouldThrowExceptionWhenPasswordTooShort() {
        RegisterRequest request = new RegisterRequest("john@example.com", "johndoe", "short", "short");

        ApiRequestException exception = assertThrows(ApiRequestException.class, () -> authenticationService.register(request));
        assertEquals("Password must be at least 8 characters long!", exception.getMessage());
    }

    @Test
    void register_ShouldThrowUserCreateExceptionForDataIntegrityViolation() {
        RegisterRequest request = new RegisterRequest("john@example.com", "johndoe", "password123", "password123");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(request.getUsername())).thenReturn(null);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

        doThrow(DataIntegrityViolationException.class).when(userRepository).save(any(User.class));

        assertThrows(UserCreateException.class, () -> authenticationService.register(request));
    }

}

