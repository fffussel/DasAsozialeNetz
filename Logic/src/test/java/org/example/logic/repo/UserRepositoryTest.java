package org.example.logic.repo;

import org.example.logic.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private UserEntity save(String username, String email) {
        return userRepository.save(UserEntity.builder()
                .username(username)
                .email(email)
                .password("secret")
                .role("USER")
                .build());
    }

    @Test
    void findByUsername_returnsMatchingUser() {
        save("alice", "alice@example.com");

        UserEntity found = userRepository.findByUsername("alice");

        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void findByUsername_returnsNullWhenMissing() {
        assertThat(userRepository.findByUsername("ghost")).isNull();
    }

    @Test
    void existsByUsernameAndEmail_reflectPersistedState() {
        save("bob", "bob@example.com");

        assertThat(userRepository.existsByUsername("bob")).isTrue();
        assertThat(userRepository.existsByEmail("bob@example.com")).isTrue();
        assertThat(userRepository.existsByUsername("nobody")).isFalse();
        assertThat(userRepository.existsByEmail("nobody@example.com")).isFalse();
    }

    @Test
    void findByEmail_returnsMatchingUser() {
        save("carol", "carol@example.com");

        assertThat(userRepository.findByEmail("carol@example.com").getUsername()).isEqualTo("carol");
    }

    @Test
    void findByUsernameOrEmail_matchesEitherField() {
        save("dave", "dave@example.com");

        assertThat(userRepository.findByUsernameOrEmail("dave")).isNotNull();
        assertThat(userRepository.findByUsernameOrEmail("dave@example.com")).isNotNull();
        assertThat(userRepository.findByUsernameOrEmail("unknown")).isNull();
    }

    @Test
    void findByUsernameContainingIgnoreCase_isCaseInsensitiveAndPartial() {
        save("EveOnline", "eve@example.com");

        Iterable<UserEntity> results = userRepository.findByUsernameContainingIgnoreCase("eveonl");

        assertThat(results).extracting(UserEntity::getUsername).containsExactly("EveOnline");
    }

    @Test
    void save_generatesId() {
        UserEntity saved = save("frank", "frank@example.com");

        assertThat(saved.getId()).isNotNull();
        assertThat(userRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void deleteById_removesUser() {
        UserEntity saved = save("gina", "gina@example.com");
        UUID id = saved.getId();

        userRepository.deleteById(id);

        assertThat(userRepository.findById(id)).isEmpty();
    }
}
