package com.foodsaver.server.authorization;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JWTAuthenticationFilterTest {
    @InjectMocks
    private JWTAuthenticationFilter filter;

    @Mock
    private JWTService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        filter = new JWTAuthenticationFilter(jwtService, userDetailsService);
    }

    @Test
    void testDoFilterInternalWithoutAuthorizationHeaderShouldContinueFilterChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternalWithInvalidAuthorizationHeaderShouldContinueFilterChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("InvalidToken");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternalWithExpiredToken() throws ServletException, IOException {
        String expiredToken = "ExpiredToken";
        String username = "testuser";
        UserDetails userDetails = mock(UserDetails.class);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + expiredToken);
        when(jwtService.extractUsername(expiredToken)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtService.isTokenValid(expiredToken, userDetails)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternalWithValidAuthorizationHeaderShouldAuthenticateUser() throws ServletException, IOException {
        String validToken = "ValidToken";
        String username = "testuser";
        UserDetails userDetails = mock(UserDetails.class);
        Authentication authentication = mock(Authentication.class);
        UsernamePasswordAuthenticationToken authToken = mock(UsernamePasswordAuthenticationToken.class);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtService.extractUsername(validToken)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtService.isTokenValid(validToken, userDetails)).thenReturn(true);
        when(authentication.getDetails()).thenReturn(null);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(authToken.getPrincipal()).thenReturn(userDetails);
        when(authToken.getCredentials()).thenReturn(null);

        try (MockedStatic<SecurityContextHolder> mockedContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedContextHolder.when(SecurityContextHolder::getContext).thenReturn(mock(SecurityContext.class));

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verify(SecurityContextHolder.getContext()).setAuthentication(any(UsernamePasswordAuthenticationToken.class));
        }
    }

    @Test
    void testDoFilterInternal_SignatureException() throws ServletException, IOException {
        String invalidJwtToken = "InvalidToken";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidJwtToken);
        when(jwtService.extractUsername(invalidJwtToken)).thenThrow(new SignatureException("Invalid signature"));

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "No valid signature");
    }

    @Test
    void testDoFilterInternal_MalformedJwtException() throws ServletException, IOException {
        String invalidJwtToken = "InvalidToken";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidJwtToken);
        when(jwtService.extractUsername(invalidJwtToken)).thenThrow(new MalformedJwtException("Malformed token"));

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid authorization token");
    }

    @Test
    void testDoFilterInternal_ExpiredJwtException() throws ServletException, IOException {
        String expiredJwtToken = "ExpiredToken";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + expiredJwtToken);
        when(jwtService.extractUsername(expiredJwtToken)).thenThrow(new ExpiredJwtException(null, null, "Expired token"));

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Expired authorization token");
    }

    @Test
    void testDoFilterInternal_UnsupportedJwtException() throws ServletException, IOException {
        String unsupportedJwtToken = "UnsupportedToken";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + unsupportedJwtToken);
        when(jwtService.extractUsername(unsupportedJwtToken)).thenThrow(new UnsupportedJwtException("Unsupported token"));

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Unsupported authorization token ");
    }

    @Test
    void testDoFilterInternal_IllegalArgumentException() throws ServletException, IOException {
        String emptyJwtToken = "";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + emptyJwtToken);
        when(jwtService.extractUsername(emptyJwtToken)).thenThrow(new IllegalArgumentException("Empty token"));

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "JWT claims string is empty");
    }
}
