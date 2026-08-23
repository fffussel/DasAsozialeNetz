package org.example.logic.utility;

import org.example.logic.dto.PostDTO;
import org.example.logic.entity.PostEntity;
import org.springframework.stereotype.Component;

@Component
public class PostMapper {
    public PostEntity toPostEntity(PostDTO postDTO) {
        return PostEntity.builder()
                .id(postDTO.getId())
                .message(postDTO.getMessage())
                .author(postDTO.getAuthor())
                .likes(postDTO.getLikes())
                .comments(postDTO.getComments())
                .media(postDTO.getMedia())
                .parentPost(postDTO.getParentPost())
                .points(postDTO.getPoints())
                .createdAt(postDTO.getCreatedAt())
                .lastEditedAt(postDTO.getLastEditedAt())
                .build();
    }

    public PostDTO toPostDTO(PostEntity postEntity) {
        return PostDTO.builder()
                .id(postEntity.getId())
                .message(postEntity.getMessage())
                .author(postEntity.getAuthor())
                .likes(postEntity.getLikes())
                .comments(postEntity.getComments())
                .media(postEntity.getMedia())
                .parentPost(postEntity.getParentPost())
                .points(postEntity.getPoints())
                .createdAt(postEntity.getCreatedAt())
                .lastEditedAt(postEntity.getLastEditedAt())
                .build();
    }
}
