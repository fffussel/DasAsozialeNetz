package org.example.logic.controller;

import org.example.logic.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest extends AbstractIntegrationTest {

    @Test
    void getSelf_withToken_returnsOwnProfile() throws Exception {
        String token = registerAndGetToken("alice", "alice@example.com", "password123");

        mockMvc.perform(get("/api/user/me").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    void getSelf_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/user/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void toggleFollow_updatesFollowerCount() throws Exception {
        String aliceToken = registerAndGetToken("alice", "alice@example.com", "password123");
        registerAndGetToken("bob", "bob@example.com", "password123");

        mockMvc.perform(put("/api/user/bob/toggleFollow").header("Authorization", aliceToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/user/bob/follower").header("Authorization", aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void banEndpoint_withoutAdminRole_returnsForbidden() throws Exception {
        // the first-ever registered user becomes ADMIN, so a throwaway user is registered first
        // to make sure "alice" here is a regular USER
        registerAndGetToken("root", "root@example.com", "password123");
        String aliceToken = registerAndGetToken("alice", "alice@example.com", "password123");
        registerAndGetToken("bob", "bob@example.com", "password123");

        mockMvc.perform(put("/api/user/bob/ban").header("Authorization", aliceToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void banEndpoint_asFirstRegisteredAdmin_succeeds() throws Exception {
        String adminToken = registerAndGetToken("alice", "alice@example.com", "password123");
        registerAndGetToken("bob", "bob@example.com", "password123");

        mockMvc.perform(put("/api/user/bob/ban").header("Authorization", adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void searchUser_findsMatchingUsername() throws Exception {
        String token = registerAndGetToken("alice", "alice@example.com", "password123");

        mockMvc.perform(get("/api/user/search").param("param", "ali").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("alice"));
    }
}
