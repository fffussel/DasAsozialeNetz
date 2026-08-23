package org.example.ui.client;

import org.example.logic.dto.ResponseUser;
import org.example.logic.dto.SingleStringDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "logic-user", url = "${logic.service.url}")
public interface UserClient {

    @GetMapping("/api/user/{name}")
    ResponseUser getUser(@RequestHeader("Authorization") String bearerToken, @PathVariable("name") String name);

    @GetMapping("/api/user/search")
    List<ResponseUser> searchUser(@RequestHeader("Authorization") String bearerToken, @RequestParam("param") String param);

    @GetMapping("/api/user/me")
    ResponseUser getSelf(@RequestHeader("Authorization") String bearerToken);

    @DeleteMapping("/api/user/{name}")
    SingleStringDTO deleteUser(@RequestHeader("Authorization") String bearerToken, @PathVariable("name") String name);

    @DeleteMapping("/api/user/me")
    SingleStringDTO deleteSelf(@RequestHeader("Authorization") String bearerToken);

    @PutMapping("/api/user/me/changePassword")
    SingleStringDTO changeOwnPassword(@RequestHeader("Authorization") String bearerToken, @RequestBody String newPassword);

    @PostMapping(value = "/api/user/changeProfilePicture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    SingleStringDTO changeProfilePicture(@RequestHeader("Authorization") String bearerToken, @RequestPart("file") MultipartFile file);

    @GetMapping("/api/user/{name}/follower")
    List<UUID> getUserFollowers(@PathVariable("name") String name);

    @GetMapping("/api/user/{name}/following")
    List<UUID> getUserFollowing(@PathVariable("name") String name);

    @PutMapping("/api/user/{name}/toggleFollow")
    String toggleFollowUser(@RequestHeader("Authorization") String bearerToken, @PathVariable("name") String name);

    @PutMapping("/api/user/{name}/unban")
    SingleStringDTO unbanUser(@PathVariable("name") String name);

    @PutMapping("/api/user/{name}/ban")
    SingleStringDTO banUser(@PathVariable("name") String name, @RequestParam(value = "time", defaultValue = "0") long time);
}
