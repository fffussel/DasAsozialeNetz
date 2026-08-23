package org.example.logic.controller;

import org.example.logic.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PostControllerTest extends AbstractIntegrationTest {

    @Test
    void newPost_withoutMedia_returnsCreatedPost() throws Exception {
        String token = registerAndGetToken("alice", "alice@example.com", "password123");

        mockMvc.perform(post("/api/post/post")
                        .header("Authorization", token)
                        .param("message", "hello world"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("hello world"));
    }

    @Test
    void newPost_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/post/post").param("message", "hello"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getPost_returnsPersistedPost() throws Exception {
        String token = registerAndGetToken("alice", "alice@example.com", "password123");
        String body = mockMvc.perform(post("/api/post/post")
                        .header("Authorization", token)
                        .param("message", "hello world"))
                .andReturn().getResponse().getContentAsString();
        String id = json.readTree(body).get("id").asText();

        // "/api/post/*" is in the authenticated bucket in SecurityConfig, so even a plain GET needs a token
        mockMvc.perform(get("/api/post/" + id).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("hello world"));
    }

    @Test
    void toggleLike_incrementsLikeCount() throws Exception {
        String token = registerAndGetToken("alice", "alice@example.com", "password123");
        String body = mockMvc.perform(post("/api/post/post")
                        .header("Authorization", token)
                        .param("message", "likeable"))
                .andReturn().getResponse().getContentAsString();
        String id = json.readTree(body).get("id").asText();

        mockMvc.perform(put("/api/post/" + id + "/toggleLike").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value(org.hamcrest.Matchers.containsString("liked")));
    }

    @Test
    void timeline_returnsCreatedPosts() throws Exception {
        String token = registerAndGetToken("alice", "alice@example.com", "password123");
        mockMvc.perform(post("/api/post/post").header("Authorization", token).param("message", "first"));

        mockMvc.perform(get("/api/post/timeline").param("amount", "10").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value("first"));
    }

    @Test
    void editPost_byNonAuthor_returnsBadRequest() throws Exception {
        String authorToken = registerAndGetToken("alice", "alice@example.com", "password123");
        String otherToken = registerAndGetToken("bob", "bob@example.com", "password123");
        String body = mockMvc.perform(post("/api/post/post")
                        .header("Authorization", authorToken)
                        .param("message", "mine"))
                .andReturn().getResponse().getContentAsString();
        String id = json.readTree(body).get("id").asText();

        mockMvc.perform(put("/api/post/" + id + "/edit")
                        .header("Authorization", otherToken)
                        .content("hijacked"))
                .andExpect(status().isBadRequest());
    }
}
