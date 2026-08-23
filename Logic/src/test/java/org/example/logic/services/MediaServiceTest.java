package org.example.logic.services;

import org.example.logic.AbstractIntegrationTest;
import org.example.logic.dto.MediaDTO;
import org.example.logic.entity.UserEntity;
import org.example.logic.exception.BadRequestException;
import org.example.logic.exception.NotFoundException;
import org.example.logic.repo.MediaRepository;
import org.example.logic.repo.UserRepository;
import org.example.logic.security.MyUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MediaServiceTest extends AbstractIntegrationTest {

    @Autowired
    private MediaService mediaService;

    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private UserRepository userRepository;

    private UserEntity persistUser() {
        return userRepository.save(UserEntity.builder()
                .username("alice").email("alice@example.com").password("secret").role("USER").build());
    }

    @Test
    void uploadMedia_savesEntityWithBase64Content() throws Exception {
        UserEntity author = persistUser();
        MockMultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", "content".getBytes());

        MediaDTO dto = mediaService.uploadMedia(new MyUserDetails(author), file);

        assertThat(dto.getId()).isNotNull();
        assertThat(dto.getOriginalFilename()).isEqualTo("photo.png");
        var saved = mediaRepository.findById(dto.getId()).orElseThrow();
        assertThat(saved.getContent()).isEqualTo(Base64.getEncoder().encodeToString("content".getBytes()));
    }

    @Test
    void uploadMedia_emptyFile_throwsBadRequest() {
        UserEntity author = persistUser();
        MockMultipartFile empty = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);

        assertThatThrownBy(() -> mediaService.uploadMedia(new MyUserDetails(author), empty))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void getMedia_byId_returnsReadableResource() throws Exception {
        UserEntity author = persistUser();
        MockMultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", "content".getBytes());
        MediaDTO dto = mediaService.uploadMedia(new MyUserDetails(author), file);

        Resource resource = mediaService.getMedia(dto.getId().toString());

        assertThat(resource.exists()).isTrue();
        assertThat(resource.getInputStream().readAllBytes()).isEqualTo("content".getBytes());
    }

    @Test
    void getMedia_unknownButValidUuid_throwsNotFound() {
        assertThatThrownBy(() -> mediaService.getMedia(UUID.randomUUID().toString()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getMedia_nonUuidInput_throwsIllegalArgument() {
        // RepositoryUtil.findMediaByIdOrFilename calls UUID.fromString(input) unconditionally,
        // so a plain (non-UUID) filename never reaches the findByFilename fallback.
        assertThatThrownBy(() -> mediaService.getMedia("not-a-uuid.png"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getMediaFilename_returnsStoredLogicalFilename() throws Exception {
        UserEntity author = persistUser();
        MockMultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", "content".getBytes());
        MediaDTO dto = mediaService.uploadMedia(new MyUserDetails(author), file);

        String filename = mediaService.getMediaFilename(dto.getId().toString());

        assertThat(filename).isEqualTo(dto.getId() + ".png");
    }

    @Test
    void deleteMedia_removesEntity() throws Exception {
        UserEntity author = persistUser();
        MockMultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", "content".getBytes());
        MediaDTO dto = mediaService.uploadMedia(new MyUserDetails(author), file);

        mediaService.deleteMedia(new MyUserDetails(author), dto.getId().toString());

        assertThat(mediaRepository.findById(dto.getId())).isEmpty();
    }

    @Test
    void deleteMedia_unknown_throwsNotFound() {
        UserEntity author = persistUser();

        assertThatThrownBy(() -> mediaService.deleteMedia(new MyUserDetails(author), UUID.randomUUID().toString()))
                .isInstanceOf(NotFoundException.class);
    }
}
