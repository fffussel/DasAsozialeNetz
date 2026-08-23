package org.example.logic.services;

import org.example.logic.AbstractIntegrationTest;
import org.example.logic.entity.PostEntity;
import org.example.logic.entity.UserEntity;
import org.example.logic.exception.BadRequestException;
import org.example.logic.exception.NotFoundException;
import org.example.logic.repo.PostRepository;
import org.example.logic.repo.UserRepository;
import org.example.logic.security.MyUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostServiceTest extends AbstractIntegrationTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    private UserEntity persist(String username, String email) {
        return userRepository.save(UserEntity.builder()
                .username(username)
                .email(email)
                .password("secret")
                .role("USER")
                .points(100)
                .build());
    }

    @Test
    void newPost_withoutMedia_createsPost() {
        UserEntity author = persist("alice", "alice@example.com");

        var response = postService.newPost("hello world", null, null, new MyUserDetails(author));

        assertThat(response.getBody().getMessage()).isEqualTo("hello world");
        assertThat(response.getBody().getAuthor()).isEqualTo(author.getId());
        assertThat(userRepository.findById(author.getId()).orElseThrow().getPosts())
                .containsExactly(response.getBody().getId());
    }

    @Test
    void newPost_withMedia_attachesMedia() throws Exception {
        UserEntity author = persist("alice", "alice@example.com");
        MockMultipartFile file = new MockMultipartFile("media", "pic.png", "image/png", "content".getBytes());

        var response = postService.newPost("with media", file, null, new MyUserDetails(author));

        assertThat(response.getBody().getMedia()).isNotNull();
    }

    @Test
    void newPost_asComment_linksToParentAndAwardsPoints() {
        UserEntity author = persist("alice", "alice@example.com");
        var parent = postService.newPost("parent post", null, null, new MyUserDetails(author)).getBody();

        var comment = postService.newPost("a comment", null, parent.getId(), new MyUserDetails(author)).getBody();

        PostEntity parentAfter = postRepository.findById(parent.getId()).orElseThrow();
        assertThat(parentAfter.getComments()).containsExactly(comment.getId());
        // a new post inherits the author's current points (100 from persist()), then +10 for the comment
        assertThat(parentAfter.getPoints()).isEqualTo(110);
        assertThat(comment.getParentPost()).isEqualTo(parent.getId());
    }

    @Test
    void newPost_unknownParent_throwsNotFound() {
        UserEntity author = persist("alice", "alice@example.com");

        assertThatThrownBy(() -> postService.newPost("orphan comment", null, UUID.randomUUID(), new MyUserDetails(author)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void editPost_byAuthor_updatesMessage() {
        UserEntity author = persist("alice", "alice@example.com");
        var post = postService.newPost("original", null, null, new MyUserDetails(author)).getBody();

        var response = postService.editPost(new MyUserDetails(author), post.getId(), "updated");

        assertThat(response.getBody().getMessage()).isEqualTo("updated");
        assertThat(response.getBody().getLastEditedAt()).isNotNull();
    }

    @Test
    void editPost_byOtherUser_returnsBadRequest() {
        UserEntity author = persist("alice", "alice@example.com");
        UserEntity other = persist("bob", "bob@example.com");
        var post = postService.newPost("original", null, null, new MyUserDetails(author)).getBody();

        var response = postService.editPost(new MyUserDetails(other), post.getId(), "hijacked");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void editPost_byAdmin_isAllowed() {
        UserEntity author = persist("alice", "alice@example.com");
        UserEntity admin = userRepository.save(UserEntity.builder()
                .username("root").email("root@example.com").password("secret").role("ADMIN").build());
        var post = postService.newPost("original", null, null, new MyUserDetails(author)).getBody();

        var response = postService.editPost(new MyUserDetails(admin), post.getId(), "moderated");

        assertThat(response.getBody().getMessage()).isEqualTo("moderated");
    }

    @Test
    void deletePost_byAuthor_removesPost() {
        UserEntity author = persist("alice", "alice@example.com");
        var post = postService.newPost("bye", null, null, new MyUserDetails(author)).getBody();

        postService.deletePost(new MyUserDetails(author), post.getId());

        assertThat(postRepository.findById(post.getId())).isEmpty();
    }

    @Test
    void deletePost_byOtherUser_returnsBadRequest() {
        UserEntity author = persist("alice", "alice@example.com");
        UserEntity other = persist("bob", "bob@example.com");
        var post = postService.newPost("keep me", null, null, new MyUserDetails(author)).getBody();

        var response = postService.deletePost(new MyUserDetails(other), post.getId());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(postRepository.findById(post.getId())).isPresent();
    }

    @Test
    void getPost_unknown_returnsNotFoundStatus() {
        var response = postService.getPost(UUID.randomUUID());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void toggleLike_addsThenRemovesLikeAndPoints() {
        UserEntity author = persist("alice", "alice@example.com");
        UserEntity liker = persist("bob", "bob@example.com");
        var post = postService.newPost("likeable", null, null, new MyUserDetails(author)).getBody();

        postService.toggleLike(post.getId(), new MyUserDetails(liker));

        PostEntity afterLike = postRepository.findById(post.getId()).orElseThrow();
        assertThat(afterLike.getLikes()).containsExactly(liker.getId());
        // a new post inherits the author's current points (100 from persist()), then +5 for the like
        assertThat(afterLike.getPoints()).isEqualTo(105);
        assertThat(userRepository.findById(author.getId()).orElseThrow().getPoints()).isEqualTo(101);

        postService.toggleLike(post.getId(), new MyUserDetails(liker));

        PostEntity afterUnlike = postRepository.findById(post.getId()).orElseThrow();
        assertThat(afterUnlike.getLikes()).isEmpty();
        assertThat(afterUnlike.getPoints()).isEqualTo(100);
        assertThat(userRepository.findById(author.getId()).orElseThrow().getPoints()).isEqualTo(100);
    }

    @Test
    void getPostsForTimeline_invalidSortType_throwsBadRequest() {
        assertThatThrownBy(() -> postService.getPostsForTimeline(0, 10, null, "bogus", true, ""))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void getPostsForTimeline_filtersByAuthorAndSearchText() {
        UserEntity author = persist("alice", "alice@example.com");
        UserEntity other = persist("bob", "bob@example.com");
        postService.newPost("hello from alice", null, null, new MyUserDetails(author));
        postService.newPost("hello from bob", null, null, new MyUserDetails(other));

        var results = postService.getPostsForTimeline(0, 10, "alice", "createdAt", true, "hello").getBody();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getAuthor()).isEqualTo(author.getId());
    }

    @Test
    void getCommentsForPost_returnsOnlyCommentsOfThatPost() {
        UserEntity author = persist("alice", "alice@example.com");
        var parent = postService.newPost("parent", null, null, new MyUserDetails(author)).getBody();
        postService.newPost("first comment", null, parent.getId(), new MyUserDetails(author));
        postService.newPost("second comment", null, parent.getId(), new MyUserDetails(author));

        var comments = postService.getCommentsForPost(parent.getId(), 10, null, "points", true, "").getBody();

        assertThat(comments).hasSize(2);
    }
}
