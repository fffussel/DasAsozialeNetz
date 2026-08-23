package org.example.logic.services;

import org.example.logic.dto.MediaDTO;
import org.example.logic.entity.MediaEntity;
import org.example.logic.exception.AccessDeniedException;
import org.example.logic.exception.BadRequestException;
import org.example.logic.repo.MediaRepository;
import org.example.logic.security.MyUserDetails;
import org.example.logic.utility.MediaMapper;
import org.example.logic.utility.RepositoryUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

@Service
public class MediaService {
    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private MediaMapper mediaMapper;

    @Autowired
    private RepositoryUtil repositoryUtil;

    public MediaDTO uploadMedia(MyUserDetails userDetails, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File cannot be empty");
        }
        try {
            MediaEntity mediaEntity = MediaEntity.builder()
                    .contentType(file.getContentType())
                    .originalFilename(file.getOriginalFilename())
                    .authorId(userDetails.getId())
                    .build();

            mediaEntity = mediaRepository.save(mediaEntity);

            String newFileName = mediaEntity.getId().toString() + "." + file.getContentType().split("/")[1];
            mediaEntity.setFilename(newFileName);
            mediaEntity.setContent(Base64.getEncoder().encodeToString(file.getBytes()));

            mediaRepository.save(mediaEntity);

            return mediaMapper.toMediaDTO(mediaEntity);
        } catch (Exception e) {
            throw new BadRequestException("File cannot be upload. " + e.getMessage());
        }
    }

    public Resource getMedia(String media) {
        MediaEntity mediaEntity = repositoryUtil.findMediaByIdOrFilename(media);
        return new ByteArrayResource(Base64.getDecoder().decode(mediaEntity.getContent()));
    }

    public String getMediaFilename(String media) {
        return repositoryUtil.findMediaByIdOrFilename(media).getFilename();
    }

    public String deleteMedia(MyUserDetails userDetails, String media) {
        MediaEntity mediaEntity = repositoryUtil.findMediaByIdOrFilename(media);
        if (!userDetails.getRole().equals("ADMIN") && !userDetails.getId().equals(userDetails.getId())) {
            throw new AccessDeniedException("Your can only delete your own media");
        }

        mediaRepository.delete(mediaEntity);

        return "Image " + mediaEntity.getOriginalFilename() + " has been deleted. ID: " + mediaEntity.getId();
    }
}
