package org.example.logic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaDTO {
    private UUID id;

    private String contentType;
    private String originalFilename;
    private String filename;
    private String content;
    private UUID authorId;

    @Builder.Default
    private final LocalDateTime createdAt = LocalDateTime.now();
}
