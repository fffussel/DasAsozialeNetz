package org.example.logic.repo;

import org.example.logic.entity.MediaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MediaRepository extends JpaRepository<MediaEntity, UUID> {
    MediaEntity findByFilename(String filename);

    void deleteByFilename(String filename);
}
