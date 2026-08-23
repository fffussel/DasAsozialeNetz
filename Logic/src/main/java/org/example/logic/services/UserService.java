package org.example.logic.services;

import org.example.logic.dto.MediaDTO;
import org.example.logic.dto.ResponseUser;
import org.example.logic.dto.SingleStringDTO;
import org.example.logic.entity.UserEntity;
import org.example.logic.exception.NotFoundException;
import org.example.logic.repo.UserRepository;
import org.example.logic.security.MyUserDetails;
import org.example.logic.utility.RepositoryUtil;
import org.example.logic.utility.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RepositoryUtil repositoryUtil;
    @Autowired
    private MediaService mediaService;
    @Autowired
    private UserMapper userMapper;

    public ResponseEntity<ResponseUser> getSelf(MyUserDetails userDetails) {
        UserEntity userEntity = userDetails.getUser();
        ResponseUser responseUser = userMapper.toResponseUser(userEntity);
        return ResponseEntity.ok(responseUser);
    }

    public ResponseEntity<ResponseUser> getUser(String name) {
        UserEntity userEntity = repositoryUtil.findUserByAll(name);
        ResponseUser responseUser = userMapper.toResponseUser(userEntity);
        return ResponseEntity.ok(responseUser);
    }

    public ResponseEntity<SingleStringDTO> deleteSelf(MyUserDetails userDetails) {
        UserEntity userEntity = userDetails.getUser();
        userRepository.delete(userEntity);
        return ResponseEntity.ok(new SingleStringDTO(String.format("User %s (%s) deleted successfully.", userEntity.getUsername(), userEntity.getId())));
    }

    public ResponseEntity<SingleStringDTO> deleteUser(String name) {
        UserEntity userEntity = repositoryUtil.findUserByAll(name);
        userRepository.delete(userEntity);
        return ResponseEntity.ok(new SingleStringDTO(String.format("User %s (%s) deleted successfully.", userEntity.getUsername(), userEntity.getId())));
    }

    public ResponseEntity<SingleStringDTO> changeProfilePicture(MyUserDetails userDetails, MultipartFile file) {
        UserEntity userEntity = userDetails.getUser();
        MediaDTO mediaDTO = mediaService.uploadMedia(userDetails, file);
        userEntity.setProfilePicture(mediaDTO.getId());
        userRepository.save(userEntity);
        return ResponseEntity.ok(new SingleStringDTO("profile picture changed successfully"));
    }

    public ResponseEntity<SingleStringDTO> changeOwnPassword(MyUserDetails userDetails, String newPassword) {
        UserEntity userEntity = userDetails.getUser();
        userEntity.setPassword(newPassword);
        userRepository.save(userEntity);
        return ResponseEntity.ok(new SingleStringDTO("password changed successfully"));
    }

    public ResponseEntity<List<UUID>> getUserFollowers(String name) {
        UserEntity userEntity = repositoryUtil.findUserByAll(name);
        return ResponseEntity.ok(userEntity.getFollowers());
    }

    public ResponseEntity<List<UUID>> getUserFollowing(String name) {
        UserEntity userEntity = repositoryUtil.findUserByAll(name);
        return ResponseEntity.ok(userEntity.getFollowing());
    }

    public ResponseEntity<String> toggleFollowUser(MyUserDetails userDetails, String name) {
        UserEntity userSelf = userDetails.getUser();
        UserEntity user = repositoryUtil.findUserByAll(name);
        if (user.getId().equals(userSelf.getId())) {
            throw new NotFoundException("You cannot follow yourself");
        }
        if (userSelf.getFollowing().contains(user.getId())) {
            user.addPoints(-10);
            userSelf.getFollowing().remove(user.getId());
            user.getFollowers().remove(userSelf.getId());
        } else {
            user.addPoints(10);
            userSelf.getFollowing().add(user.getId());
            user.getFollowers().add(userSelf.getId());
        }
        userRepository.save(userSelf);
        userRepository.save(user);

        boolean isFollowing = userSelf.getFollowing().contains(user.getId());
        int followerCount = user.getFollowers().size();

        String json = String.format("{\n  \"isFollowing\": %b,\n  \"followerCount\": %d\n}", isFollowing, followerCount);
        return ResponseEntity.ok(json);
    }

    public ResponseEntity<SingleStringDTO> banUser(String name, long time) {
        UserEntity user = repositoryUtil.findUserByAll(name);
        if (time != 0L) {
            user.setBannedUntil(LocalDateTime.now().plusDays(time));
        } else {
            user.setBannedUntil(LocalDateTime.now().plusYears(1000));
        }
        userRepository.save(user);
        return ResponseEntity.ok(new SingleStringDTO(String.format("User %s (%s) banned %s", user.getUsername(), user.getId(), time != 0L ? "for " + time + " minutes" : "permanently")));
    }

    public ResponseEntity<SingleStringDTO> unbanUser(String name) {
        UserEntity user = repositoryUtil.findUserByAll(name);
        user.setBannedUntil(null);
        userRepository.save(user);
        return ResponseEntity.ok(new SingleStringDTO(String.format("User %s (%s) unbanned.", user.getUsername(), user.getId())));
    }

    public ResponseEntity<List<ResponseUser>> searchUser(String param) {
        List<ResponseUser> users = new ArrayList<>();
        userRepository.findByUsernameContainingIgnoreCase(param).forEach((user) -> {
            users.add(userMapper.toResponseUser(user));
        });
        return ResponseEntity.ok(users);
    }
}
