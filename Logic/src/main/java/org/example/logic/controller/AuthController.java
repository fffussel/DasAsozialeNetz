package org.example.logic.controller;

import org.example.logic.dto.LoginRequest;
import org.example.logic.dto.RegisterRequest;
import org.example.logic.dto.SingleStringDTO;
import org.example.logic.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<SingleStringDTO> register(@RequestBody RegisterRequest registerRequest) {
        return authService.register(registerRequest);
    }

    @PostMapping("/login-json")
    public ResponseEntity<SingleStringDTO> login(@RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }
}
