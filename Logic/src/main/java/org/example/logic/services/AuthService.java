package org.example.logic.services;

import org.example.logic.dto.LoginRequest;
import org.example.logic.dto.RegisterRequest;
import org.example.logic.dto.SingleStringDTO;
import org.example.logic.entity.UserEntity;
import org.example.logic.exception.AccessDeniedException;
import org.example.logic.exception.AlreadyExistsException;
import org.example.logic.exception.BadCredentialsException;
import org.example.logic.repo.UserRepository;
import org.example.logic.security.MyUserDetailsService;
import org.springframework.security.authentication.AuthenticationManager;
import org.example.logic.utility.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private MyUserDetailsService userDetailsService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public String createJwtToken(String name, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(name, password));
        } catch (Exception e) {
            throw new AccessDeniedException("Invalid username or password supplied");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(name);

        UserEntity user = userRepository.findByUsernameOrEmail(name);
        if (user.getBannedUntil() != null) {
            if (!LocalDateTime.now().isAfter(user.getBannedUntil())) {
                Duration remaining = Duration.between(LocalDateTime.now(), user.getBannedUntil());
                throw new BadCredentialsException("User is banned for another " + remaining.toHours() + " minutes.");
            } else {
                user.setBannedUntil(null);
            }
        }

        final String jwt = jwtUtil.generateToken(userDetails);
        return jwt;
    }

    public ResponseEntity<SingleStringDTO> register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername()) || userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new AlreadyExistsException("User already exists");
        }
        String role = "USER";
        if (userRepository.findAll().isEmpty()) {
            role = "ADMIN";
        }

        UserEntity userEntity = UserEntity.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .points(1)
                .role(role).build();
        userRepository.save(userEntity);

        String jwtToken = createJwtToken(registerRequest.getEmail(), registerRequest.getPassword());

        return ResponseEntity.ok(new SingleStringDTO(jwtToken));
    }

    public ResponseEntity<SingleStringDTO> login(LoginRequest loginRequest) {
        String jwtToken = createJwtToken(loginRequest.getName(), loginRequest.getPassword());
        return ResponseEntity.ok(new SingleStringDTO(jwtToken));
    }
}
