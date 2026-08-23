package org.example.logic.utility;

import org.example.logic.dto.MediaDTO;
import org.example.logic.entity.MediaEntity;
import org.springframework.stereotype.Component;

@Component
public class MediaMapper {
    public MediaEntity toMediaEntity(MediaDTO mediaDTO) {
        return MediaEntity.builder()
                .id(mediaDTO.getId())
                .contentType(mediaDTO.getContentType())
                .originalFilename(mediaDTO.getOriginalFilename())
                .filename(mediaDTO.getFilename())
                .content(mediaDTO.getContent())
                .build();
    }

    public MediaDTO toMediaDTO(MediaEntity mediaEntity) {
        return MediaDTO.builder()
                .id(mediaEntity.getId())
                .contentType(mediaEntity.getContentType())
                .originalFilename(mediaEntity.getOriginalFilename())
                .filename(mediaEntity.getFilename())
                .content(mediaEntity.getContent())
                .build();
    }
}
