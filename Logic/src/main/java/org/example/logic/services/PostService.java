package org.example.logic.services;

import org.example.logic.dto.MediaDTO;
import org.example.logic.dto.PostDTO;
import org.example.logic.dto.SingleStringDTO;
import org.example.logic.entity.PostEntity;
import org.example.logic.entity.UserEntity;
import org.example.logic.exception.BadRequestException;
import org.example.logic.exception.NotFoundException;
import org.example.logic.repo.PostRepository;
import org.example.logic.repo.UserRepository;
import org.example.logic.security.MyUserDetails;
import org.example.logic.utility.PostMapper;
import org.example.logic.utility.RepositoryUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private MediaService mediaService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RepositoryUtil repositoryUtil;

    public ResponseEntity<PostDTO> newPost(String message, MultipartFile media, UUID parentPost, MyUserDetails userDetails) {
        UserEntity userEntity = userRepository.findByEmail(userDetails.getEmail());
        if (userEntity == null) {
            throw new NotFoundException("User does not exist");
        }
        PostEntity postEntity = PostEntity.builder()
                .author(userEntity.getId())
                .message(message).build();
        if (media != null) {
            MediaDTO mediaDTO = mediaService.uploadMedia(userDetails, media);
            if (mediaDTO != null) {
                postEntity.setMedia(mediaDTO.getId());
            }
        }

        postEntity.setPoints(userEntity.getPoints());

        postRepository.save(postEntity);

        if (parentPost != null) {
            Optional<PostEntity> optionalPost = postRepository.findById(parentPost);
            if (optionalPost.isPresent()) {
                PostEntity parentPostEntity = optionalPost.get();
                UserEntity author = userRepository.findById(parentPostEntity.getAuthor()).get();
                postEntity.setParentPost(parentPost);
                parentPostEntity.getComments().add(postEntity.getId());
                parentPostEntity.addPoints(10);
                postRepository.save(parentPostEntity);
                postRepository.save(postEntity);
            } else {
                throw new NotFoundException("Parent post does not exist");
            }
        }

        userEntity.getPosts().add(postEntity.getId());
        userRepository.save(userEntity);

        PostDTO postDTO = postMapper.toPostDTO(postEntity);
        return ResponseEntity.ok(postDTO);
    }

    public ResponseEntity<PostDTO> editPost(MyUserDetails userDetails, UUID id, String message) {
        Optional<PostEntity> optionalPost = postRepository.findById(id);
        if (optionalPost.isPresent()) {
            UserEntity userEntity = userRepository.findByEmail(userDetails.getEmail());
            if (!userEntity.getRole().equals("ADMIN") && !optionalPost.get().getAuthor().equals(userEntity.getId())) {
                return ResponseEntity.badRequest().build();
            }
            PostEntity postEntity = optionalPost.get();
            postEntity.setMessage(message);
            postEntity.setLastEditedAt(LocalDateTime.now());
            postRepository.save(postEntity);
            return ResponseEntity.ok(postMapper.toPostDTO(postEntity));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    public ResponseEntity<PostDTO> deletePost(MyUserDetails userDetails, UUID id) {
        Optional<PostEntity> optionalPost = postRepository.findById(id);
        if (optionalPost.isPresent()) {
            PostEntity postEntity = optionalPost.get();
            UserEntity userEntity = userRepository.findByEmail(userDetails.getEmail());
            if (!userEntity.getRole().equals("ADMIN") && !postEntity.getAuthor().equals(userEntity.getId())) {
                return ResponseEntity.badRequest().build();
            }
            if (postEntity.getMedia() != null) {
                mediaService.deleteMedia(userDetails, String.valueOf(postEntity.getMedia()));
            }
            postRepository.delete(postEntity);
            return ResponseEntity.ok(postMapper.toPostDTO(postEntity));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    public ResponseEntity<PostDTO> getPost(UUID id) {
        Optional<PostEntity> optionalPost = postRepository.findById(id);
        if (optionalPost.isPresent()) {
            return ResponseEntity.ok(postMapper.toPostDTO(optionalPost.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    public ResponseEntity<SingleStringDTO> toggleLike(UUID id, MyUserDetails userDetails) {
        Optional<PostEntity> optionalPost = postRepository.findById(id);
        if (optionalPost.isPresent()) {
            PostEntity postEntity = optionalPost.get();
            UserEntity userEntity = userDetails.getUser();
            UserEntity author = userRepository.findById(postEntity.getAuthor()).get();

            if (postEntity.getLikes().contains(userEntity.getId())) {
                postEntity.getLikes().remove(userEntity.getId());
                postEntity.removePoints(5);
                if (!author.getId().equals(userEntity.getId())) {
                    author.removePoints();
                }
            } else {
                postEntity.getLikes().add(userEntity.getId());
                postEntity.addPoints(5);
                if (!author.getId().equals(userEntity.getId())) {
                    author.addPoints();
                }
            }
            postRepository.save(postEntity);
            userRepository.save(author);
            return ResponseEntity.ok(new SingleStringDTO(String.format("Post %s %s successfully.", postEntity.getId(), postEntity.getLikes().contains(userEntity.getId()) ? "liked" : "unliked")));
        } else {
            throw new NotFoundException("Post does not exist");
        }
    }

    public ResponseEntity<List<PostDTO>> getPostsForTimeline(int page, int amount, String name, String sortTyp, boolean descending, String searchString) {

        List<String> types = Arrays.asList("points", "createdAt", "lastEditedAt");

        if (!types.contains(sortTyp)) {
            throw new BadRequestException(String.format("sort type %s not supported", sortTyp));
        }

        Pageable pageable = descending
                ? PageRequest.of(page, amount, Sort.by(sortTyp).descending())
                : PageRequest.of(page, amount, Sort.by(sortTyp).ascending());

        List<PostEntity> posts = (name != null)
                ? postRepository.findByParentPostIsNullAndMessageContainsIgnoreCaseAndAuthor(searchString, repositoryUtil.findUserByAll(name).getId(), pageable)
                : postRepository.findByParentPostIsNullAndMessageContainsIgnoreCase(searchString, pageable);

        List<PostDTO> postDTOS = posts.stream().map(postMapper::toPostDTO).toList();
        return ResponseEntity.ok(postDTOS);
    }

    public ResponseEntity<List<PostDTO>> getCommentsForPost(
            UUID postId,
            int amount,
            String userTag,
            String sortTyp,
            boolean descending,
            String searchString) {

        Comparator<PostEntity> comparator;

        switch (sortTyp == null ? "points" : sortTyp.toLowerCase()) {
            case "createdat" -> comparator = Comparator.comparing(PostEntity::getCreatedAt);
            case "lasteditedat" -> comparator = Comparator.comparing(PostEntity::getLastEditedAt);
            default -> comparator = Comparator.comparingInt(PostEntity::getPoints);
        }

        if (descending) {
            comparator = comparator.reversed();
        }

        PostEntity parentPost = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post does not exist"));
        UserEntity filterUser;
        if (userTag != null) {
            filterUser = repositoryUtil.findUserByAll(userTag);
        } else {
            filterUser = null;
        }

        List<PostDTO> postDTOS = parentPost.getComments().stream()
                .map(postRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(post -> filterUser == null ||
                        (post.getAuthor() != null && post.getAuthor().equals(filterUser.getId())))
                .filter(post -> searchString == null ||
                        (post.getMessage() != null && post.getMessage().toLowerCase().contains(searchString.toLowerCase())))
                .sorted(comparator)
                .limit(amount)
                .map(postMapper::toPostDTO)
                .toList();

        return ResponseEntity.ok(postDTOS);
    }
}
