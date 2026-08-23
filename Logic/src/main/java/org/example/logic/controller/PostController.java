package org.example.logic.controller;

import org.example.logic.dto.PostDTO;
import org.example.logic.dto.SingleStringDTO;
import org.example.logic.security.MyUserDetails;
import org.example.logic.services.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/post")
public class PostController {
    @Autowired
    PostService postService;

    @PostMapping("/post")
    public ResponseEntity<PostDTO> newPost(@RequestParam(value = "message") String message, @RequestParam(required = false, value = "media") MultipartFile media, @AuthenticationPrincipal MyUserDetails userDetails) {
        return postService.newPost(message, media, null, userDetails);
    }

    @PutMapping("/{id}/edit")
    public ResponseEntity<PostDTO> editPost(@AuthenticationPrincipal MyUserDetails userDetails, @PathVariable(value = "id") UUID id, @RequestBody String message) {
        return postService.editPost(userDetails, id, message);
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<PostDTO> deletePost(@AuthenticationPrincipal MyUserDetails userDetails, @PathVariable(value = "id") UUID id) {
        return postService.deletePost(userDetails, id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPost(@PathVariable(value = "id") UUID id) {
        return postService.getPost(id);
    }

    @PostMapping("/{id}/comments/new")
    public ResponseEntity<PostDTO> newComment(@RequestParam(value = "message") String message, @RequestParam(required = false, value = "media") MultipartFile media, @PathVariable(value = "id") UUID parentPostId, @AuthenticationPrincipal MyUserDetails userDetails) {
        return postService.newPost(message, media, parentPostId, userDetails);
    }

    @PutMapping("/{id}/toggleLike")
    public ResponseEntity<SingleStringDTO> toggleLike(@PathVariable(value = "id") UUID id, @AuthenticationPrincipal MyUserDetails userDetails) {
        return postService.toggleLike(id, userDetails);
    }

    @GetMapping("/timeline")
    public ResponseEntity<List<PostDTO>> getPostsForTimeline(
            @RequestParam(value = "amount") int amount,
            @RequestParam(required = false, value = "page", defaultValue = "0") int page,
            @RequestParam(required = false, value = "user") String user,
            @RequestParam(required = false, value = "sortTyp", defaultValue = "points") String sortTyp,
            @RequestParam(value = "descending", required = false, defaultValue = "true") Boolean descending,
            @RequestParam(value = "search", required = false, defaultValue = "") String searchString) {
        return postService.getPostsForTimeline(page, amount, user, sortTyp, descending, searchString);
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<PostDTO>> getCommentsForPost(
            @PathVariable(value = "id") UUID postId,
            @RequestParam(value = "amount") int amount,
            @RequestParam(required = false, value = "user") String user,
            @RequestParam(required = false, value = "sortTyp", defaultValue = "points") String sortTyp,
            @RequestParam(value = "descending", required = false, defaultValue = "true") Boolean descending,
            @RequestParam(value = "search", required = false, defaultValue = "") String searchString) {
        return postService.getCommentsForPost(postId, amount, user, sortTyp, descending, searchString);
    }
}
