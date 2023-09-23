package com.tim.redditclone.service;

import com.tim.redditclone.dto.AuthenticationRequest;
import com.tim.redditclone.dto.AuthenticationResponse;
import com.tim.redditclone.dto.RefreshTokenRequest;
import com.tim.redditclone.dto.RegisterRequest;
import com.tim.redditclone.exceptions.SpringRedditException;
import com.tim.redditclone.jwt.JwtService;
import com.tim.redditclone.model.*;
import com.tim.redditclone.repository.RefreshTokenRepository;
import com.tim.redditclone.repository.UserRepository;
import com.tim.redditclone.repository.VerificationTokenRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final MailService mailService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationTokenService authenticationTokenService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public void register(RegisterRequest request) {
        var user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.USER)
                .enabled(false)
                .created(Instant.now())
                .build();
        userRepository.save(user);
        String verificationToken = authenticationTokenService.generateVerificationToken(user);
        mailService.sendMail(new NotificationEmail(
                "Please Activate your Account",
                        user.getEmail(), "Thank you for signing up to Reddit Clone, " +
                        "http://localhost:8080/api/auth/accountVerification/" + verificationToken
        ));
    }

    @Transactional
    public void verification(String verificationToken) {
        Optional<VerificationToken> token = verificationTokenRepository.findByToken(verificationToken);
        // Check if Valid
        token.orElseThrow(() -> new SpringRedditException("Invalid Token"));
        // Check if Expired
        token.ifPresent(t -> {
            if (t.getExpiryDate().isBefore(Instant.now())) {
                throw new SpringRedditException("The token is expired");
            }
        });
        fetchUserAndEnable(token.get());
    }

    @Transactional
    private void fetchUserAndEnable(VerificationToken verificationToken) {
        String username = verificationToken.getUser().getUsername();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new SpringRedditException("User not found with name - " + username));
        user.setEnabled(true);
        // Save updated User
        userRepository.save(user);
    }

    @Transactional
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        return userRepository.findByUsername(currentPrincipalName)
                .orElseThrow(() -> new UsernameNotFoundException("User name not found - " + currentPrincipalName));
    }

    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        String username = request.getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new SpringRedditException("User not found for email: " + username));
        if (!user.isEnabled()) {
            throw new SpringRedditException("User is not enabled");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            username,
                            request.getPassword()
                    )
            );
            // Authentication was successful

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String authToken = jwtService.generateToken(user);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);


            return AuthenticationResponse.builder()
                    .username(username)
                    .token(authToken)
                    .refreshToken(refreshToken.getToken())
                    .build();
        } catch (org.springframework.security.core.AuthenticationException ex) {
            if (ex instanceof LockedException) {
                throw new SpringRedditException("Authentication failed: User account is locked. Please contact support.");
            } else {
                throw new SpringRedditException("Authentication failed: " + ex.getMessage());
            }
        }
    }

    public AuthenticationResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new SpringRedditException("Refresh Token not found"));

        refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();
        String username = user.getUsername();

        String authToken = jwtService.generateToken(user);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        refreshTokenRepository.delete(refreshToken);

        return AuthenticationResponse.builder()
                .username(username)
                .token(authToken)
                .refreshToken(newRefreshToken.getToken())
                .build();
    }

}
