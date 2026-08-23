package org.example.logic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDTO {
    private UUID id;

    private String message;
    private UUID author;

    @Builder.Default
    private final List<UUID> comments = new ArrayList<>();

    @Builder.Default
    private final List<UUID> likes = new ArrayList<>();

    private LocalDateTime lastEditedAt;

    private UUID media;

    private UUID parentPost;

    @Builder.Default
    private final LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private final int points = 0;
}
