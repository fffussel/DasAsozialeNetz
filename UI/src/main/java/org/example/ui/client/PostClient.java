package org.example.ui.client;

import org.example.logic.dto.PostDTO;
import org.example.logic.dto.SingleStringDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "logic-post", url = "${logic.service.url}")
public interface PostClient {

    @PostMapping(value = "/api/post/post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    PostDTO newPost(@RequestHeader("Authorization") String bearerToken,
                     @RequestPart("message") String message,
                     @RequestPart(value = "media", required = false) MultipartFile media);

    @PutMapping("/api/post/{id}/edit")
    PostDTO editPost(@RequestHeader("Authorization") String bearerToken, @PathVariable("id") UUID id, @RequestBody String message);

    @DeleteMapping("/api/post/{id}/delete")
    PostDTO deletePost(@RequestHeader("Authorization") String bearerToken, @PathVariable("id") UUID id);

    @GetMapping("/api/post/{id}")
    PostDTO getPost(@RequestHeader("Authorization") String bearerToken, @PathVariable("id") UUID id);

    @PostMapping(value = "/api/post/{id}/comments/new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    PostDTO newComment(@RequestHeader("Authorization") String bearerToken,
                        @PathVariable("id") UUID parentPostId,
                        @RequestPart("message") String message,
                        @RequestPart(value = "media", required = false) MultipartFile media);

    @PutMapping("/api/post/{id}/toggleLike")
    SingleStringDTO toggleLike(@RequestHeader("Authorization") String bearerToken, @PathVariable("id") UUID id);

    @GetMapping("/api/post/timeline")
    List<PostDTO> getPostsForTimeline(@RequestHeader("Authorization") String bearerToken,
                                       @RequestParam("amount") int amount,
                                       @RequestParam(value = "page", defaultValue = "0") int page,
                                       @RequestParam(value = "user", required = false) String user,
                                       @RequestParam(value = "sortTyp", defaultValue = "points") String sortTyp,
                                       @RequestParam(value = "descending", defaultValue = "true") boolean descending,
                                       @RequestParam(value = "search", defaultValue = "") String search);

    @GetMapping("/api/post/{id}/comments")
    List<PostDTO> getCommentsForPost(@RequestHeader("Authorization") String bearerToken,
                                      @PathVariable("id") UUID postId,
                                      @RequestParam("amount") int amount,
                                      @RequestParam(value = "user", required = false) String user,
                                      @RequestParam(value = "sortTyp", defaultValue = "points") String sortTyp,
                                      @RequestParam(value = "descending", defaultValue = "true") boolean descending,
                                      @RequestParam(value = "search", defaultValue = "") String search);
}
