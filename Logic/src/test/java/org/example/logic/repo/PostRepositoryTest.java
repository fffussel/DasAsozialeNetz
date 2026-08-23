package org.example.logic.repo;

import org.example.logic.entity.PostEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    private PostEntity save(UUID author, String message) {
        return postRepository.save(PostEntity.builder()
                .author(author)
                .message(message)
                .build());
    }

    @Test
    void findByParentPostIsNullAndMessageContainsIgnoreCase_matchesTopLevelPostsOnly() {
        UUID author = UUID.randomUUID();
        save(author, "Hello World");
        PostEntity comment = save(author, "Hello Comment");
        comment.setParentPost(UUID.randomUUID());
        postRepository.save(comment);

        List<PostEntity> results = postRepository.findByParentPostIsNullAndMessageContainsIgnoreCase(
                "hello", PageRequest.of(0, 10));

        assertThat(results).extracting(PostEntity::getMessage).containsExactly("Hello World");
    }

    @Test
    void findByParentPostIsNullAndMessageContainsIgnoreCaseAndAuthor_filtersByAuthor() {
        UUID author1 = UUID.randomUUID();
        UUID author2 = UUID.randomUUID();
        save(author1, "shared text");
        save(author2, "shared text");

        List<PostEntity> results = postRepository.findByParentPostIsNullAndMessageContainsIgnoreCaseAndAuthor(
                "shared", author1, PageRequest.of(0, 10));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getAuthor()).isEqualTo(author1);
    }

    @Test
    void findAllByRandom_returnsAllPersistedPosts() {
        UUID author = UUID.randomUUID();
        save(author, "one");
        save(author, "two");
        save(author, "three");

        List<PostEntity> results = postRepository.findAllByRandom();

        assertThat(results).hasSize(3);
    }

    @Test
    void save_defaultsPointsAndTimestamps() {
        PostEntity saved = save(UUID.randomUUID(), "defaults");

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getPoints()).isZero();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getComments()).isEmpty();
        assertThat(saved.getLikes()).isEmpty();
    }
}
