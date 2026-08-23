package org.example.logic.services;

import org.example.logic.AbstractIntegrationTest;
import org.example.logic.dto.LoginRequest;
import org.example.logic.dto.RegisterRequest;
import org.example.logic.entity.UserEntity;
import org.example.logic.exception.AccessDeniedException;
import org.example.logic.exception.AlreadyExistsException;
import org.example.logic.exception.BadCredentialsException;
import org.example.logic.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthServiceTest extends AbstractIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void register_firstUser_becomesAdmin() {
        var response = authService.register(new RegisterRequest("alice", "alice@example.com", "password123"));

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody().getValue()).isNotBlank();

        UserEntity saved = userRepository.findByUsername("alice");
        assertThat(saved.getRole()).isEqualTo("ADMIN");
        assertThat(saved.getPassword()).isNotEqualTo("password123");
    }

    @Test
    void register_secondUser_becomesRegularUser() {
        authService.register(new RegisterRequest("alice", "alice@example.com", "password123"));
        authService.register(new RegisterRequest("bob", "bob@example.com", "password123"));

        assertThat(userRepository.findByUsername("bob").getRole()).isEqualTo("USER");
    }

    @Test
    void register_duplicateUsername_throwsAlreadyExists() {
        authService.register(new RegisterRequest("alice", "alice@example.com", "password123"));

        assertThatThrownBy(() -> authService.register(new RegisterRequest("alice", "other@example.com", "password123")))
                .isInstanceOf(AlreadyExistsException.class);
    }

    @Test
    void register_duplicateEmail_throwsAlreadyExists() {
        authService.register(new RegisterRequest("alice", "alice@example.com", "password123"));

        assertThatThrownBy(() -> authService.register(new RegisterRequest("other", "alice@example.com", "password123")))
                .isInstanceOf(AlreadyExistsException.class);
    }

    @Test
    void login_correctCredentials_returnsToken() {
        authService.register(new RegisterRequest("alice", "alice@example.com", "password123"));

        var response = authService.login(new LoginRequest("alice", "password123"));

        assertThat(response.getBody().getValue()).isNotBlank();
    }

    @Test
    void login_wrongPassword_throwsAccessDenied() {
        authService.register(new RegisterRequest("alice", "alice@example.com", "password123"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("alice", "wrong-password")))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void login_bannedUser_throwsBadCredentials() {
        authService.register(new RegisterRequest("alice", "alice@example.com", "password123"));
        UserEntity user = userRepository.findByUsername("alice");
        user.setBannedUntil(LocalDateTime.now().plusDays(1));
        userRepository.save(user);

        assertThatThrownBy(() -> authService.login(new LoginRequest("alice", "password123")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_banExpired_clearsBanAndSucceeds() {
        authService.register(new RegisterRequest("alice", "alice@example.com", "password123"));
        UserEntity user = userRepository.findByUsername("alice");
        user.setBannedUntil(LocalDateTime.now().minusMinutes(1));
        userRepository.save(user);

        var response = authService.login(new LoginRequest("alice", "password123"));

        assertThat(response.getBody().getValue()).isNotBlank();
        assertThat(userRepository.findByUsername("alice").getBannedUntil()).isNull();
    }
}
