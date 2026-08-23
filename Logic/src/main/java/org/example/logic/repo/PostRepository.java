package org.example.logic.repo;

import org.example.logic.entity.PostEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, UUID> {
    @Query(value = "SELECT p FROM PostEntity p ORDER BY function('RANDOM')")
    List<PostEntity> findAllByRandom();

    List<PostEntity> findByParentPostIsNullAndMessageContainsIgnoreCase(String message, Pageable pageable);

    List<PostEntity> findByParentPostIsNullAndMessageContainsIgnoreCaseAndAuthor(String message, UUID id, Pageable pageable);
}
