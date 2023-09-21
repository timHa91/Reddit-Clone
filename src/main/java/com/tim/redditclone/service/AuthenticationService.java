package com.tim.redditclone.service;

import com.tim.redditclone.dto.AuthenticationRequest;
import com.tim.redditclone.dto.AuthenticationResponse;
import com.tim.redditclone.dto.RegisterRequest;
import com.tim.redditclone.exceptions.SpringRedditException;
import com.tim.redditclone.jwt.JwtService;
import com.tim.redditclone.model.NotificationEmail;
import com.tim.redditclone.model.User;
import com.tim.redditclone.model.UserRole;
import com.tim.redditclone.model.VerificationToken;
import com.tim.redditclone.repository.UserRepository;
import com.tim.redditclone.repository.VerificationTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final MailService mailService;
    private final VerificationTokenRepository verificationTokenRepository;


    @Transactional
    public void register(RegisterRequest request) {
        var user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.USER)
                .enabled(false)
                .created(Instant.now())
                .build();
        userRepository.save(user);
        String verificationToken = generateVerificationToken(user);
        mailService.sendMail(new NotificationEmail(
                "Please Activate your Account",
                        user.getEmail(), "Thank you for signing up to Reddit Clone, " +
                        "http://localhost:8080/api/v1/auth/accountVerification/" + verificationToken
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
        String email = verificationToken.getUser().getUsername();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new SpringRedditException("User not found with name - " + email));
        user.setEnabled(true);
        // Save updated User
        userRepository.save(user);
    }

    private String generateVerificationToken(User user) {
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

    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        String email = request.getEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new SpringRedditException("User not found for email: " + email));
        if (!user.isEnabled()) {
            throw new SpringRedditException("User is not enabled");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email,
                            request.getPassword()
                    )
            );
            // Authentication was successful
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String authToken = jwtService.generateToken(user);
            return AuthenticationResponse.builder()
                    .token(authToken)
                    .build();
        } catch (org.springframework.security.core.AuthenticationException ex) {
            if (ex instanceof LockedException) {
                throw new SpringRedditException("Authentication failed: User account is locked. Please contact support.");
            } else {
                throw new SpringRedditException("Authentication failed: " + ex.getMessage());
            }
        }
    }
}
