package com.foodsaver.server.auth;

import com.foodsaver.server.dtos.UserDTO;
import com.foodsaver.server.dtos.request.AuthenticationRequest;
import com.foodsaver.server.dtos.request.RegisterRequest;
import com.foodsaver.server.dtos.request.VerificationRequest;
import com.foodsaver.server.dtos.response.AuthenticationResponse;
import com.foodsaver.server.exceptions.EmailNotVerifiedException;
import com.foodsaver.server.exceptions.UnauthorizedException;
import com.foodsaver.server.exceptions.User.UsernamePasswordException;
import com.foodsaver.server.model.User;
import com.foodsaver.server.repositories.UserRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthenticationControllerTest {

    @InjectMocks
    private AuthenticationController authenticationController;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAuthenticate_Success() {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setUsername("johndoe");
        request.setPassword("password");

        User user = new User();
        user.setEnabled(true);

        AuthenticationResponse authResponse = new AuthenticationResponse();
        authResponse.setToken("jwt-token");

        when(userRepository.findByUsername(request.getUsername())).thenReturn(user);
        when(authenticationService.authenticate(any(AuthenticationRequest.class))).thenReturn(authResponse);

        ResponseEntity<AuthenticationResponse> response = authenticationController.authenticate(request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("jwt-token", response.getBody().getToken());
        verify(userRepository, times(1)).findByUsername(request.getUsername());
        verify(authenticationService, times(1)).authenticate(request);
    }

    @Test
    void testAuthenticate_UserNotFound() {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setUsername("nonexistent");
        request.setPassword("password");

        when(userRepository.findByUsername(request.getUsername())).thenReturn(null);

        assertThrows(UsernamePasswordException.class, () -> authenticationController.authenticate(request));
        verify(userRepository, times(1)).findByUsername(request.getUsername());
        verifyNoInteractions(authenticationService);
    }

    @Test
    void testAuthenticate_EmailNotVerified() {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setUsername("johndoe");
        request.setPassword("password");

        User user = new User();
        user.setEnabled(false);

        when(userRepository.findByUsername(request.getUsername())).thenReturn(user);

        assertThrows(EmailNotVerifiedException.class, () -> authenticationController.authenticate(request));
        verify(userRepository, times(1)).findByUsername(request.getUsername());
        verifyNoInteractions(authenticationService);
    }

    @Test
    void testRegister_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("john.doe@example.com");
        request.setUsername("johndoe");
        request.setPassword("password");

        when(authenticationService.register(any(RegisterRequest.class))).thenReturn("Registration successful");

        ResponseEntity<String> response = authenticationController.register(request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Registration successful", response.getBody());
        verify(authenticationService, times(1)).register(request);
    }

    @Test
    void testRegister_ValidationFailure() {
        RegisterRequest request = new RegisterRequest();

        when(authenticationService.register(any(RegisterRequest.class))).thenThrow(ConstraintViolationException.class);

        assertThrows(ConstraintViolationException.class, () -> authenticationController.register(request));
        verify(authenticationService, times(1)).register(request);
    }

    @Test
    void testVerifyVerificationToken() {
        VerificationRequest verificationRequest = new VerificationRequest();
        verificationRequest.setToken("verification-token");
        verificationRequest.setEmail("john.doe@example.com");

        when(authenticationService.verifyVerificationToken(any(VerificationRequest.class))).thenReturn("Your account is verified!");

        ResponseEntity<String> response = authenticationController.verifyVerificationToken(verificationRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Your account is verified!", response.getBody());
        verify(authenticationService, times(1)).verifyVerificationToken(verificationRequest);
    }

    @Test
    void testGetUserInfo_Success() {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testuser");
        userDTO.setEmail("test@example.com");

        when(authenticationService.getUserInfo()).thenReturn(userDTO);

        // Act
        ResponseEntity<UserDTO> response = authenticationController.getUserInfo();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("testuser", response.getBody().getUsername());
        assertEquals("test@example.com", response.getBody().getEmail());
        verify(authenticationService, times(1)).getUserInfo();
    }

    @Test
    void testGetUserInfo_ShouldThrowException_WhenNoUserLogged() {
        // Arrange
        doThrow(new UnauthorizedException("No logged user!"))
                .when(authenticationService).getUserInfo();

        // Act & Assert
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> authenticationController.getUserInfo()
        );
        assertEquals("No logged user!", exception.getMessage());
        verify(authenticationService, times(1)).getUserInfo();
    }
}