package org.example.logic.controller;

import org.example.logic.dto.MediaDTO;
import org.example.logic.security.MyUserDetails;
import org.example.logic.services.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    @Autowired
    private MediaService mediaService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadMedia(@AuthenticationPrincipal MyUserDetails userDetails, @RequestParam("file") MultipartFile file) {
        MediaDTO mediaDto = mediaService.uploadMedia(userDetails, file);
        if (mediaDto != null) {
            return ResponseEntity.ok("Media uploaded successfully: " + mediaDto.getOriginalFilename() + " ID: " + mediaDto.getId());
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<Resource> getMedia(@PathVariable("fileName") String fileName) {
        Resource resource = mediaService.getMedia(fileName);
        if (resource != null) {
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{filename}/datatype")
    public ResponseEntity<String> getMediaType(@PathVariable("filename") String filename) {
        return ResponseEntity.ok(mediaService.getMediaFilename(filename));
    }

    @DeleteMapping("/{fileName}")
    public ResponseEntity<String> deleteMedia(@AuthenticationPrincipal MyUserDetails userDetails, @PathVariable("fileName") String fileName) {
        String message = mediaService.deleteMedia(userDetails, fileName);
        if (message != null) {
            return ResponseEntity.ok(message);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
