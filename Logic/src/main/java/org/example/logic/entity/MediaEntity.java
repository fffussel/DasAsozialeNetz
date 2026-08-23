package org.example.logic.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaEntity {
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private UUID id;

    private String contentType;
    private String originalFilename;
    private String filename;

    @Column(columnDefinition = "TEXT")
    private String content;

    private UUID authorId;

    @Builder.Default
    private final LocalDateTime createdAt = LocalDateTime.now();
}
