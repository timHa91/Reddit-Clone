package com.tim.redditclone.controller;

import com.tim.redditclone.dto.AuthenticationRequest;
import com.tim.redditclone.dto.AuthenticationResponse;
import com.tim.redditclone.model.User;
import com.tim.redditclone.service.AuthenticationService;
import com.tim.redditclone.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        service.register(request);
       return ResponseEntity.ok("User Registration Successfull");
    }

    @GetMapping("/accountVerification/{token}")
    public ResponseEntity<String> accountVerification(@PathVariable String token) {
        service.verification(token);
        return new ResponseEntity<>("Account Activated Successfully", HttpStatus.OK);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(service.authenticate(request));
    }
}
