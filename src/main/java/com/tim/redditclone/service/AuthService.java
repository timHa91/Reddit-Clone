package com.tim.redditclone.service;

import com.tim.redditclone.dto.AuthenticationResponse;
import com.tim.redditclone.dto.LoginRequest;
import com.tim.redditclone.dto.RegisterRequest;
import com.tim.redditclone.exceptions.SpringRedditException;
import com.tim.redditclone.model.NotificationEmail;
import com.tim.redditclone.model.User;
import com.tim.redditclone.model.VerificationToken;
import com.tim.redditclone.repository.UserRepository;
import com.tim.redditclone.repository.VerificationTokenRepository;
import com.tim.redditclone.security.JwtProvider;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final MailService mailService;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    // Transactional, wenn mehrere Datenbankoperationen ausführt werden müssen, um sicherstellen, dass alle erfolgreich oder gar nicht abgeschlossen werden.
    @Transactional
    public void signUp(RegisterRequest registerRequest) {
        User newUser = new User();
        newUser.setUsername(registerRequest.getUsername());
        newUser.setEmail(registerRequest.getEmail());
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        newUser.setCreated(Instant.now());
        newUser.setEnabled(false);

        userRepository.save(newUser);

        String token = generateVerificationToken(newUser);
        mailService.sendMail(new NotificationEmail("Please Acitiate your Account",
                newUser.getEmail(), "Thank you for signing up to Reddit Clone, " +
                "http://localhost:8080/api/auth/accountVerification/" + token));
        // Wenn der user die URL benutzt wird der Token als Parameter benutzt um den User in der DB zu fetchen und den User enablen
    }

    private String generateVerificationToken(User user) {
        String tokenValue = UUID.randomUUID().toString();
        VerificationToken token = new VerificationToken();
        token.setToken(tokenValue);
        token.setUser(user);

        verificationTokenRepository.save(token);
        return tokenValue;
    }

    public void verifyAccount(String token) {
        Optional<VerificationToken> verificationToken = verificationTokenRepository.findByToken(token);
        verificationToken.orElseThrow(() -> new SpringRedditException("Invalid Token"));
        fetchUserAndEnable(verificationToken.get());
    }

    @Transactional
    private void fetchUserAndEnable(VerificationToken verificationToken) {
        String username = verificationToken.getUser().getUsername();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new SpringRedditException("User not found with name - " + username));
        user.setEnabled(true);
        // Save updated User
        userRepository.save(user);
    }

    public AuthenticationResponse login(LoginRequest loginRequest) {
        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
                loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authenticate);
        String token = jwtProvider.generateToken(authenticate);
        return new AuthenticationResponse(token, loginRequest.getUsername());
    }
}
