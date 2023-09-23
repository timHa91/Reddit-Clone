package com.tim.redditclone.service;

import com.tim.redditclone.model.User;
import com.tim.redditclone.model.VerificationToken;
import com.tim.redditclone.repository.VerificationTokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthenticationTokenService {

    private final VerificationTokenRepository verificationTokenRepository;

    public String generateVerificationToken(User user) {
        String tokenValue = UUID.randomUUID().toString();
        VerificationToken token = new VerificationToken();
        // Erstellen Sie den Ablaufzeitpunkt (1 Stunde ab jetzt)
        Instant now = Instant.now();
        Instant expiration = now.plus(1, ChronoUnit.HOURS);
        token.setExpiryDate(expiration);
        token.setToken(tokenValue);
        token.setUser(user);

        verificationTokenRepository.save(token);
        return tokenValue;
    }
}
