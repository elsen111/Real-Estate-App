package com.realestate.backend.service;

import com.realestate.backend.dto.request.DeleteAccountRequest;
import com.realestate.backend.dto.request.UpdateProfileRequest;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.exception.ConflictException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.exception.UnauthorizedException;
import com.realestate.backend.repository.UserMediaRepository;
import com.realestate.backend.repository.UserRepository;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserMediaRepository userMediaRepository;

    @InjectMocks private UserServiceImpl service;

    private CustomUserDetails currentUser(String email) {
        return CustomUserDetails.from(UserEntity.builder().id(UUID.randomUUID())
                .email(email).roles(new HashSet<>()).build());
    }

    @Test
    void updateProfile_throws_whenNewEmailAlreadyTaken() {
        UserEntity user = UserEntity.builder().email("old@test.com").build();
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setEmail("taken@test.com");

        when(userRepository.findByEmail("old@test.com")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

        assertThatThrownBy(() -> service.updateProfile(request, currentUser("old@test.com")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void deleteAccount_throws_whenPasswordIncorrect() {
        UserEntity user = UserEntity.builder().email("user@test.com").passwordHash("hashed").build();
        DeleteAccountRequest request = new DeleteAccountRequest();
        request.setPassword("wrong-password");

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> service.deleteAccount(request, currentUser("user@test.com")))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void removeProfilePhoto_throws_whenNoPhotoExists() {
        UUID userId = UUID.randomUUID();
        CustomUserDetails user = CustomUserDetails.from(UserEntity.builder().id(userId)
                .email("user@test.com").roles(new HashSet<>()).build());

        when(userMediaRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.removeProfilePhoto(user))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void uploadProfilePhoto_throws_whenUserNotFound() {
        UUID userId = UUID.randomUUID();
        CustomUserDetails user = CustomUserDetails.from(UserEntity.builder().id(userId)
                .email("user@test.com").roles(new HashSet<>()).build());

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.uploadProfilePhoto(null, user))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}