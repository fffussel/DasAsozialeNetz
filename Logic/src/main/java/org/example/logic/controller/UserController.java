package org.example.logic.controller;

import org.example.logic.dto.ResponseUser;
import org.example.logic.dto.SingleStringDTO;
import org.example.logic.security.MyUserDetails;
import org.example.logic.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{name}")
    public ResponseEntity<ResponseUser> getUser(@PathVariable(value = "name") String name) {
        return userService.getUser(name);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ResponseUser>> searchUser(@RequestParam(value = "param") String param) {
        return userService.searchUser(param);
    }

    @GetMapping("/me")
    public ResponseEntity<ResponseUser> getSelf(@AuthenticationPrincipal MyUserDetails userDetails) {
        return userService.getSelf(userDetails);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<SingleStringDTO> deleteUser(@PathVariable String name) {
        return userService.deleteUser(name);
    }

    @DeleteMapping("/me")
    public ResponseEntity<SingleStringDTO> deleteSelf(@AuthenticationPrincipal MyUserDetails userDetails) {
        return userService.deleteSelf(userDetails);
    }

    @PutMapping("/me/changePassword")
    public ResponseEntity<SingleStringDTO> changeOwnPassword(@AuthenticationPrincipal MyUserDetails userDetails, @RequestBody String newPassword) {
        return userService.changeOwnPassword(userDetails, newPassword);
    }

    @PostMapping("/changeProfilePicture")
    public ResponseEntity<SingleStringDTO> changeProfilePicture(@AuthenticationPrincipal MyUserDetails userDetails, @RequestParam(value = "file") MultipartFile media) {
        return userService.changeProfilePicture(userDetails, media);
    }

    @GetMapping("/{name}/follower")
    public ResponseEntity<List<UUID>> userFollowers(@PathVariable(value = "name") String name) {
        return userService.getUserFollowers(name);
    }

    @GetMapping("/{name}/following")
    public ResponseEntity<List<UUID>> userFollowing(@PathVariable(value = "name") String name) {
        return userService.getUserFollowing(name);
    }

    @PutMapping("/{name}/toggleFollow")
    public ResponseEntity<String> toggleFollowUser(@AuthenticationPrincipal MyUserDetails userDetails, @PathVariable(value = "name") String name) {
        return userService.toggleFollowUser(userDetails, name);
    }

    @PutMapping("/{name}/unban")
    public ResponseEntity<SingleStringDTO> unbanUser(@PathVariable(value = "name") String name) {
        return userService.unbanUser(name);
    }

    @PutMapping("/{name}/ban")
    public ResponseEntity<SingleStringDTO> banUserTemp(@PathVariable(value = "name") String name, @RequestParam(required = false, value = "time", defaultValue = "0") long time) {
        return userService.banUser(name, time);
    }
}
