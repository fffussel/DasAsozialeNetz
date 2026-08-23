package org.example.logic.repo;

import org.example.logic.entity.MediaEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class MediaRepositoryTest {

    @Autowired
    private MediaRepository mediaRepository;

    private MediaEntity save(String filename) {
        return mediaRepository.save(MediaEntity.builder()
                .contentType("image/png")
                .originalFilename("original.png")
                .filename(filename)
                .content("YmFzZTY0")
                .authorId(UUID.randomUUID())
                .build());
    }

    @Test
    void findByFilename_returnsMatchingMedia() {
        save("abc.png");

        MediaEntity found = mediaRepository.findByFilename("abc.png");

        assertThat(found).isNotNull();
        assertThat(found.getOriginalFilename()).isEqualTo("original.png");
    }

    @Test
    void findByFilename_returnsNullWhenMissing() {
        assertThat(mediaRepository.findByFilename("missing.png")).isNull();
    }

    @Test
    void deleteByFilename_removesMedia() {
        save("todelete.png");

        mediaRepository.deleteByFilename("todelete.png");

        assertThat(mediaRepository.findByFilename("todelete.png")).isNull();
    }

    @Test
    void save_generatesIdAndCreatedAt() {
        MediaEntity saved = save("new.png");

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }
}
