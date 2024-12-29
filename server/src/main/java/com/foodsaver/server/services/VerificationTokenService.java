package com.foodsaver.server.services;

import com.foodsaver.server.dtos.VerificationTokenDTO;
import com.foodsaver.server.model.User;
import com.foodsaver.server.model.VerificationToken;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@AllArgsConstructor
public class VerificationTokenService {
    private final ModelMapper modelMapper;

    public VerificationTokenDTO verificationTokenToVerificationTokenDTO(VerificationToken token) {
        return modelMapper.map(token, VerificationTokenDTO.class);
    }

    public VerificationToken verificationTokenDTOToVerificationToken(VerificationTokenDTO tokenDTO) {
        return modelMapper.map(tokenDTO, VerificationToken.class);
    }

    public VerificationTokenDTO createVerificationToken(User alternativeUser) {
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setExpiryDate(LocalDateTime.now().plus(15, ChronoUnit.MINUTES));
        verificationToken.setUser(alternativeUser);
        verificationToken.setToken(generateToken());
        verificationToken.setUser(alternativeUser);
        return verificationTokenToVerificationTokenDTO(verificationToken);
    }

    private String generateToken() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        int length = 8;
        StringBuilder code = new StringBuilder();
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            code.append(characters.charAt(index));
        }

        return code.toString();
    }

    public boolean isTokenExpired(VerificationToken token) {
        return token.getExpiryDate().isBefore(LocalDateTime.now());
    }
}
