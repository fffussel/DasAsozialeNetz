package org.example.logic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
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
    private int points = 100;

    public void addPoints(int value) {
        points += value;
    }

    public void addPoints() {
        points++;
    }

    public void removePoints(int value) {
        points -= value;
    }

    public void removePoints() {
        points--;
    }
}
