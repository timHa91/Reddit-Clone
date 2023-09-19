package com.tim.redditclone.service;

import com.tim.redditclone.dto.RegisterRequest;
import com.tim.redditclone.model.NotificationEmail;
import com.tim.redditclone.model.User;
import com.tim.redditclone.model.VerificationToken;
import com.tim.redditclone.repository.UserRepository;
import com.tim.redditclone.repository.VerificationTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final MailService mailService;

    @Autowired
    public AuthService(PasswordEncoder passwordEncoder, UserRepository userRepository,
                       VerificationTokenRepository verificationTokenRepository, MailService mailService) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.mailService = mailService;
    }

    // Transactional ist besonders nützlich, wenn mehrere Datenbankoperationen ausführen müssen und um sicherstellen, dass alle erfolgreich oder gar nicht abgeschlossen werden.
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
}
