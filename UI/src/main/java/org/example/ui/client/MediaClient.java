package org.example.ui.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "logic-media", url = "${logic.service.url}")
public interface MediaClient {

    @PostMapping(value = "/api/media/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String uploadMedia(@RequestHeader("Authorization") String bearerToken, @RequestPart("file") MultipartFile file);

    @GetMapping("/api/media/{fileName}")
    byte[] getMedia(@PathVariable("fileName") String fileName);

    @GetMapping("/api/media/{filename}/datatype")
    String getMediaType(@PathVariable("filename") String filename);

    @DeleteMapping("/api/media/{fileName}")
    String deleteMedia(@RequestHeader("Authorization") String bearerToken, @PathVariable("fileName") String fileName);
}
