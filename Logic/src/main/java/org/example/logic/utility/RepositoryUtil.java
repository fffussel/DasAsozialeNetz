package org.example.logic.utility;

import org.example.logic.entity.MediaEntity;
import org.example.logic.entity.UserEntity;
import org.example.logic.exception.NotFoundException;
import org.example.logic.repo.MediaRepository;
import org.example.logic.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class RepositoryUtil {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MediaRepository mediaRepository;

    public UserEntity findUserByAll(String input) {
        Optional<UserEntity> userEntity = Optional.ofNullable(userRepository.findByUsernameOrEmail(input));
        if (userEntity.isEmpty()) {
            try {
                UUID uuid = UUID.fromString(input);
                userEntity = userRepository.findById(uuid);
            } catch (Exception e) {
                throw new NotFoundException("User " + input + " does not exist");
            }
        }
        return userEntity.get();
    }

    public MediaEntity findMediaByIdOrFilename(String input) {
        MediaEntity mediaEntity = mediaRepository.findById(UUID.fromString(input)).orElse(null);
        if (mediaEntity == null) {
            mediaEntity = mediaRepository.findByFilename(input);
            if (mediaEntity == null) {
                throw new NotFoundException("Media " + input + " does not exist");
            }
        }
        return mediaEntity;
    }
}
