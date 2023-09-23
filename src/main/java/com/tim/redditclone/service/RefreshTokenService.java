package com.tim.redditclone.service;

import com.tim.redditclone.exceptions.SpringRedditException;
import com.tim.redditclone.model.RefreshToken;
import com.tim.redditclone.model.User;
import com.tim.redditclone.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value(("${jwtRefreshExpirationMs}"))
    private long jwtRefreshExpirationMs;

    private final RefreshTokenRepository refreshTokenRepository;

    public void verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new SpringRedditException("Refresh token was expired. Please make a new signin request");
        }
    }


    public RefreshToken createRefreshToken(User user) {
        // Check if User already has a refresh Token
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByUser(user);

        if (refreshToken.isPresent()) {
            refreshTokenRepository.deleteByUser(user);
        }
        refreshTokenRepository.flush();

            RefreshToken newToken = RefreshToken.builder()
                    .user(user)
                    .expiryDate(Instant.now().plusMillis(jwtRefreshExpirationMs))
                    .token(UUID.randomUUID().toString())
                    .build();
            return refreshTokenRepository.save(newToken);
    }
}


