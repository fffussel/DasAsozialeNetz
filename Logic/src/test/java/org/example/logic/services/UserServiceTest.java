package org.example.logic.services;

import org.example.logic.AbstractIntegrationTest;
import org.example.logic.entity.UserEntity;
import org.example.logic.exception.NotFoundException;
import org.example.logic.repo.UserRepository;
import org.example.logic.security.MyUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserServiceTest extends AbstractIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private UserEntity persist(String username, String email) {
        return userRepository.save(UserEntity.builder()
                .username(username)
                .email(email)
                .password("secret")
                .role("USER")
                .build());
    }

    @Test
    void getUser_byUsernameOrId_returnsResponseUser() {
        UserEntity user = persist("alice", "alice@example.com");

        assertThat(userService.getUser("alice").getBody().getUsername()).isEqualTo("alice");
        assertThat(userService.getUser(user.getId().toString()).getBody().getUsername()).isEqualTo("alice");
    }

    @Test
    void getUser_unknown_throwsNotFound() {
        assertThatThrownBy(() -> userService.getUser("ghost")).isInstanceOf(NotFoundException.class);
    }

    @Test
    void getSelf_returnsOwnData() {
        UserEntity user = persist("alice", "alice@example.com");

        var response = userService.getSelf(new MyUserDetails(user));

        assertThat(response.getBody().getId()).isEqualTo(user.getId());
    }

    @Test
    void deleteSelf_removesUser() {
        UserEntity user = persist("alice", "alice@example.com");

        userService.deleteSelf(new MyUserDetails(user));

        assertThat(userRepository.findById(user.getId())).isEmpty();
    }

    @Test
    void deleteUser_byName_removesUser() {
        persist("alice", "alice@example.com");

        userService.deleteUser("alice");

        assertThat(userRepository.findByUsername("alice")).isNull();
    }

    @Test
    void changeOwnPassword_updatesStoredPassword() {
        UserEntity user = persist("alice", "alice@example.com");

        userService.changeOwnPassword(new MyUserDetails(user), "new-password");

        assertThat(userRepository.findById(user.getId()).orElseThrow().getPassword()).isEqualTo("new-password");
    }

    @Test
    void followerLists_startEmpty() {
        persist("alice", "alice@example.com");

        assertThat(userService.getUserFollowers("alice").getBody()).isEmpty();
        assertThat(userService.getUserFollowing("alice").getBody()).isEmpty();
    }

    @Test
    void toggleFollowUser_followsThenUnfollows() {
        UserEntity self = persist("alice", "alice@example.com");
        persist("bob", "bob@example.com");

        userService.toggleFollowUser(new MyUserDetails(self), "bob");

        UserEntity bobAfterFollow = userRepository.findByUsername("bob");
        UserEntity aliceAfterFollow = userRepository.findByUsername("alice");
        assertThat(bobAfterFollow.getFollowers()).containsExactly(self.getId());
        assertThat(aliceAfterFollow.getFollowing()).containsExactly(bobAfterFollow.getId());
        assertThat(bobAfterFollow.getPoints()).isEqualTo(110);

        userService.toggleFollowUser(new MyUserDetails(aliceAfterFollow), "bob");

        UserEntity bobAfterUnfollow = userRepository.findByUsername("bob");
        assertThat(bobAfterUnfollow.getFollowers()).isEmpty();
        assertThat(bobAfterUnfollow.getPoints()).isEqualTo(100);
    }

    @Test
    void toggleFollowUser_self_throwsNotFound() {
        UserEntity self = persist("alice", "alice@example.com");

        assertThatThrownBy(() -> userService.toggleFollowUser(new MyUserDetails(self), "alice"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void banUser_withTime_setsBannedUntilInFuture() {
        persist("alice", "alice@example.com");

        userService.banUser("alice", 5);

        LocalDateTime bannedUntil = userRepository.findByUsername("alice").getBannedUntil();
        assertThat(bannedUntil).isAfter(LocalDateTime.now());
        assertThat(bannedUntil).isBefore(LocalDateTime.now().plusDays(6));
    }

    @Test
    void banUser_permanent_setsFarFutureBan() {
        persist("alice", "alice@example.com");

        userService.banUser("alice", 0);

        assertThat(userRepository.findByUsername("alice").getBannedUntil()).isAfter(LocalDateTime.now().plusYears(999));
    }

    @Test
    void unbanUser_clearsBan() {
        UserEntity user = persist("alice", "alice@example.com");
        user.setBannedUntil(LocalDateTime.now().plusDays(1));
        userRepository.save(user);

        userService.unbanUser("alice");

        assertThat(userRepository.findByUsername("alice").getBannedUntil()).isNull();
    }

    @Test
    void searchUser_matchesPartialUsername() {
        persist("alice", "alice@example.com");
        persist("bob", "bob@example.com");

        var results = userService.searchUser("ali").getBody();

        assertThat(results).extracting(r -> r.getUsername()).containsExactly("alice");
    }
}
