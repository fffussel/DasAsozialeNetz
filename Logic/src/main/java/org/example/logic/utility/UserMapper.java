package org.example.logic.utility;

import org.example.logic.dto.ResponseUser;
import org.example.logic.dto.UserDTO;
import org.example.logic.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserEntity toUserEntity(UserDTO userDTO) {
        return UserEntity.builder()
                .id(userDTO.getId())
                .username(userDTO.getUsername())
                .password(userDTO.getPassword())
                .email(userDTO.getEmail())
                .followers(userDTO.getFollowers())
                .following(userDTO.getFollowing())
                .posts(userDTO.getPosts())
                .bannedUntil(userDTO.getBannedUntil())
                .createdAt(userDTO.getCreatedAt())
                .profilePicture(userDTO.getProfilePicture())
                .points(userDTO.getPoints())
                .role(userDTO.getRole())
                .build();
    }

    public UserDTO toUserDTO(UserEntity userEntity) {
        return UserDTO.builder()
                .id(userEntity.getId())
                .username(userEntity.getUsername())
                .password(userEntity.getPassword())
                .email(userEntity.getEmail())
                .followers(userEntity.getFollowers())
                .following(userEntity.getFollowing())
                .posts(userEntity.getPosts())
                .bannedUntil(userEntity.getBannedUntil())
                .createdAt(userEntity.getCreatedAt())
                .profilePicture(userEntity.getProfilePicture())
                .points(userEntity.getPoints())
                .role(userEntity.getRole())
                .build();
    }

    public ResponseUser toResponseUser(UserEntity userEntity) {
        return ResponseUser.builder()
                .id(userEntity.getId())
                .username(userEntity.getUsername())
                .email(userEntity.getEmail())
                .role(userEntity.getRole())
                .profilePicture(userEntity.getProfilePicture())
                .points(userEntity.getPoints())
                .followers(userEntity.getFollowers())
                .following(userEntity.getFollowing())
                .posts(userEntity.getPosts())
                .build();
    }
}
