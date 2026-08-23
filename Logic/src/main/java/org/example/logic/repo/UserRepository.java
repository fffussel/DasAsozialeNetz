package org.example.logic.repo;

import org.example.logic.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    UserEntity findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    UserEntity findByEmail(String email);

    @Query("SELECT u FROM UserEntity u WHERE u.username = :input OR u.email = :input")
    UserEntity findByUsernameOrEmail(@Param("input") String input);

    Iterable<UserEntity> findByUsernameContainingIgnoreCase(String username);
}
