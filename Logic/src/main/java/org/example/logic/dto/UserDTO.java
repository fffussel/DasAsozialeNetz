package org.example.logic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO implements Serializable {
    private UUID id;

    private String username;
    private String password;
    private String email;
    private String role;
    private UUID profilePicture;
    private LocalDateTime bannedUntil;

    @Builder.Default
    private final LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private final List<UUID> followers = new ArrayList<>();
    @Builder.Default
    private final List<UUID> following = new ArrayList<>();
    @Builder.Default
    private final List<UUID> posts = new ArrayList<>();
    @Builder.Default
    private final int points = 100;
}
