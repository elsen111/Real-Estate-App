package com.realestate.backend.service;

import com.realestate.backend.dto.request.AccountPasswordRequest;
import com.realestate.backend.dto.request.AccountReactivationRequest;
import com.realestate.backend.dto.request.UpdateProfileRequest;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.exception.AccountStateException;
import com.realestate.backend.exception.ConflictException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.exception.UnauthorizedException;
import com.realestate.backend.mapper.UserMapper;
import com.realestate.backend.repository.RefreshTokenRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserMediaRepository userMediaRepository;

    @Mock
    private MediaService mediaService;

    @InjectMocks
    private UserServiceImpl service;

    private CustomUserDetails currentUser(String email) {

        return CustomUserDetails.from(
                UserEntity.builder()
                        .id(UUID.randomUUID())
                        .email(email)
                        .roles(new HashSet<>())
                        .build()
        );
    }

    @Test
    void updateProfile_throws_whenNewEmailAlreadyTaken() {

        UserEntity user =
                UserEntity.builder()
                        .email("old@test.com")
                        .build();

        UpdateProfileRequest request =
                new UpdateProfileRequest();

        request.setEmail("taken@test.com");

        when(userRepository.findByEmail("old@test.com"))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByEmail("taken@test.com"))
                .thenReturn(true);

        assertThatThrownBy(() ->
                service.updateProfile(
                        request,
                        currentUser("old@test.com")
                )
        )
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void deleteAccount_throws_whenPasswordIncorrect() {

        UserEntity user =
                UserEntity.builder()
                        .email("user@test.com")
                        .passwordHash("hashed")
                        .build();

        AccountPasswordRequest request =
                new AccountPasswordRequest();

        request.setPassword("wrong-password");

        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "wrong-password",
                "hashed"
        )).thenReturn(false);

        assertThatThrownBy(() ->
                service.deleteAccount(
                        request,
                        currentUser("user@test.com")
                )
        )
                .isInstanceOf(UnauthorizedException.class);

        verify(refreshTokenRepository, never())
                .deleteAllByUser(user);
    }

    @Test
    void deleteAccount_disablesAndSoftDeletesUser_whenPasswordCorrect() {

        UserEntity user =
                UserEntity.builder()
                        .id(UUID.randomUUID())
                        .email("user@test.com")
                        .passwordHash("hashed")
                        .enabled(true)
                        .deleted(false)
                        .build();

        AccountPasswordRequest request =
                new AccountPasswordRequest();

        request.setPassword("correct-password");

        CustomUserDetails currentUser =
                currentUser("user@test.com");

        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "correct-password",
                "hashed"
        )).thenReturn(true);

        service.deleteAccount(
                request,
                currentUser
        );

        assertThat(user.getEnabled())
                .isFalse();

        assertThat(user.getDeleted())
                .isTrue();

        verify(refreshTokenRepository)
                .deleteAllByUser(user);
    }

    @Test
    void disableAccount_throws_whenPasswordIncorrect() {

        UserEntity user =
                UserEntity.builder()
                        .email("user@test.com")
                        .passwordHash("hashed")
                        .enabled(true)
                        .build();

        AccountPasswordRequest request =
                new AccountPasswordRequest();

        request.setPassword("wrong-password");

        CustomUserDetails currentUser =
                currentUser("user@test.com");

        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "wrong-password",
                "hashed"
        )).thenReturn(false);

        assertThatThrownBy(() ->
                service.disableAccount(
                        request,
                        currentUser
                )
        )
                .isInstanceOf(UnauthorizedException.class);

        verify(refreshTokenRepository, never())
                .deleteAllByUser(user);
    }

    @Test
    void disableAccount_throws_whenAccountAlreadyDisabled() {

        UserEntity user =
                UserEntity.builder()
                        .email("user@test.com")
                        .passwordHash("hashed")
                        .enabled(false)
                        .build();

        AccountPasswordRequest request =
                new AccountPasswordRequest();

        request.setPassword("correct-password");

        CustomUserDetails currentUser =
                currentUser("user@test.com");

        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "correct-password",
                "hashed"
        )).thenReturn(true);

        assertThatThrownBy(() ->
                service.disableAccount(
                        request,
                        currentUser
                )
        )
                .isInstanceOf(AccountStateException.class)
                .hasMessage("Account is already disabled");

        verify(refreshTokenRepository, never())
                .deleteAllByUser(user);
    }

    @Test
    void disableAccount_disablesUser_whenPasswordCorrect() {

        UserEntity user =
                UserEntity.builder()
                        .id(UUID.randomUUID())
                        .email("user@test.com")
                        .passwordHash("hashed")
                        .enabled(true)
                        .build();

        AccountPasswordRequest request =
                new AccountPasswordRequest();

        request.setPassword("correct-password");

        CustomUserDetails currentUser =
                currentUser("user@test.com");

        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "correct-password",
                "hashed"
        )).thenReturn(true);

        UserEntity result =
                service.disableAccount(
                        request,
                        currentUser
                );

        assertThat(result)
                .isSameAs(user);

        assertThat(user.getEnabled())
                .isFalse();

        verify(refreshTokenRepository)
                .deleteAllByUser(user);
    }

    @Test
    void enableAccount_throws_whenUserDoesNotExist() {

        AccountReactivationRequest request =
                new AccountReactivationRequest();

        request.setEmail("missing@test.com");
        request.setPassword("Password1!");

        when(userRepository.findByEmailAndDeletedFalse(
                "missing@test.com"
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.enableAccount(request)
        )
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void enableAccount_throws_whenAccountAlreadyEnabled() {

        AccountReactivationRequest request =
                new AccountReactivationRequest();

        request.setEmail("user@test.com");
        request.setPassword("Password1!");

        UserEntity user =
                UserEntity.builder()
                        .email("user@test.com")
                        .enabled(true)
                        .deleted(false)
                        .build();

        when(userRepository.findByEmailAndDeletedFalse(
                "user@test.com"
        )).thenReturn(Optional.of(user));

        assertThatThrownBy(() ->
                service.enableAccount(request)
        )
                .isInstanceOf(AccountStateException.class)
                .hasMessage("Account is already enabled");

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());
    }

    @Test
    void enableAccount_throws_whenPasswordIsIncorrect() {

        AccountReactivationRequest request =
                new AccountReactivationRequest();

        request.setEmail("user@test.com");
        request.setPassword("wrong-password");

        UserEntity user =
                UserEntity.builder()
                        .email("user@test.com")
                        .passwordHash("hashed")
                        .enabled(false)
                        .deleted(false)
                        .build();

        when(userRepository.findByEmailAndDeletedFalse(
                "user@test.com"
        )).thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "wrong-password",
                "hashed"
        )).thenReturn(false);

        assertThatThrownBy(() ->
                service.enableAccount(request)
        )
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid email or password");

        assertThat(user.getEnabled())
                .isFalse();
    }

    @Test
    void enableAccount_enablesUser_whenPasswordIsCorrect() {

        AccountReactivationRequest request =
                new AccountReactivationRequest();

        request.setEmail("User@Test.com");
        request.setPassword("correct-password");

        UserEntity user =
                UserEntity.builder()
                        .id(UUID.randomUUID())
                        .email("user@test.com")
                        .passwordHash("hashed")
                        .enabled(false)
                        .deleted(false)
                        .build();

        when(userRepository.findByEmailAndDeletedFalse(
                "user@test.com"
        )).thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "correct-password",
                "hashed"
        )).thenReturn(true);

        UserEntity result =
                service.enableAccount(request);

        assertThat(result)
                .isSameAs(user);

        assertThat(user.getEnabled())
                .isTrue();

        verify(passwordEncoder)
                .matches(
                        "correct-password",
                        "hashed"
                );

        verify(userRepository)
                .findByEmailAndDeletedFalse(
                        "user@test.com"
                );

        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    void enableAccount_normalizesEmailBeforeLookup() {

        AccountReactivationRequest request =
                new AccountReactivationRequest();

        request.setEmail("  USER@TEST.COM  ");
        request.setPassword("correct-password");

        UserEntity user =
                UserEntity.builder()
                        .email("user@test.com")
                        .passwordHash("hashed")
                        .enabled(false)
                        .deleted(false)
                        .build();

        when(userRepository.findByEmailAndDeletedFalse(
                "user@test.com"
        )).thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "correct-password",
                "hashed"
        )).thenReturn(true);

        UserEntity result =
                service.enableAccount(request);

        assertThat(result)
                .isSameAs(user);

        assertThat(user.getEnabled())
                .isTrue();

        verify(userRepository)
                .findByEmailAndDeletedFalse(
                        "user@test.com"
                );
    }

    @Test
    void removeProfilePhoto_throws_whenNoPhotoExists() {

        UUID userId = UUID.randomUUID();

        CustomUserDetails user =
                CustomUserDetails.from(
                        UserEntity.builder()
                                .id(userId)
                                .email("user@test.com")
                                .roles(new HashSet<>())
                                .build()
                );

        when(userMediaRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.removeProfilePhoto(user)
        )
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void uploadProfilePhoto_throws_whenUserNotFound() {

        UUID userId = UUID.randomUUID();

        CustomUserDetails user =
                CustomUserDetails.from(
                        UserEntity.builder()
                                .id(userId)
                                .email("user@test.com")
                                .roles(new HashSet<>())
                                .build()
                );

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.uploadProfilePhoto(null, user)
        )
                .isInstanceOf(ResourceNotFoundException.class);
    }
}