package org.example.logic.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostEntity {
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private UUID id;

    private String message;
    private UUID author;

    @Builder.Default
    private final List<UUID> comments = new ArrayList<>();

    @Builder.Default
    private final List<UUID> likes = new ArrayList<>();

    private UUID media;

    private UUID parentPost;

    @Builder.Default
    private int points = 0;

    @Builder.Default
    private final LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime lastEditedAt;

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
