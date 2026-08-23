package org.example.logic.controller;

import org.example.logic.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MediaControllerTest extends AbstractIntegrationTest {

    private final MockMultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", "content".getBytes());

    private String uploadAndExtractId(String token) throws Exception {
        String body = mockMvc.perform(multipart("/api/media/upload").file(file).header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        // response is "Media uploaded successfully: photo.png ID: <uuid>"
        return body.substring(body.lastIndexOf("ID: ") + 4);
    }

    @Test
    void uploadMedia_withToken_succeeds() throws Exception {
        String token = registerAndGetToken("alice", "alice@example.com", "password123");

        mockMvc.perform(multipart("/api/media/upload").file(file).header("Authorization", token))
                .andExpect(status().isOk());
    }

    @Test
    void uploadMedia_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(multipart("/api/media/upload").file(file))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMedia_isPubliclyAccessibleWithoutToken() throws Exception {
        String token = registerAndGetToken("alice", "alice@example.com", "password123");
        String id = uploadAndExtractId(token);

        mockMvc.perform(get("/api/media/" + id))
                .andExpect(status().isOk());
    }

    @Test
    void deleteMedia_withoutToken_returnsUnauthorized() throws Exception {
        String token = registerAndGetToken("alice", "alice@example.com", "password123");
        String id = uploadAndExtractId(token);

        mockMvc.perform(delete("/api/media/" + id))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteMedia_byOwner_succeeds() throws Exception {
        String token = registerAndGetToken("alice", "alice@example.com", "password123");
        String id = uploadAndExtractId(token);

        mockMvc.perform(delete("/api/media/" + id).header("Authorization", token))
                .andExpect(status().isOk());
    }

    @Test
    void deleteMedia_byNonOwner_currentlyAlsoSucceeds() throws Exception {
        // Documents an existing bug in MediaService.deleteMedia: the ownership check compares
        // userDetails.getId() to itself instead of the media's authorId, so it never rejects.
        String ownerToken = registerAndGetToken("alice", "alice@example.com", "password123");
        String otherToken = registerAndGetToken("bob", "bob@example.com", "password123");
        String id = uploadAndExtractId(ownerToken);

        mockMvc.perform(delete("/api/media/" + id).header("Authorization", otherToken))
                .andExpect(status().isOk());
    }
}
