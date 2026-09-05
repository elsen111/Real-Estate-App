package com.realestate.backend.service;

import com.realestate.backend.entity.MediaFileEntity;
import com.realestate.backend.mapper.MediaMapper;
import com.realestate.backend.repository.MediaRepository;
import com.realestate.backend.service.impl.MediaServiceImpl;
import com.realestate.backend.storage.FileValidator;
import com.realestate.backend.storage.MediaUploadPolicy;
import com.realestate.backend.storage.StorageService;
import com.realestate.backend.storage.UploadedFile;
import com.realestate.backend.storage.ValidatedFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaServiceImplTest {

    @Mock
    private StorageService storageService;

    @Mock
    private FileValidator fileValidator;

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private MediaMapper mediaMapper;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private MediaServiceImpl service;

    @Test
    void upload_validFile_uploadsToStorageAndSavesMedia() {

        ValidatedFile validatedFile =
                mock(ValidatedFile.class);

        UploadedFile uploadedFile =
                mock(UploadedFile.class);

        MediaFileEntity mediaEntity =
                MediaFileEntity.builder()
                        .id(UUID.randomUUID())
                        .storageKey(
                                "properties/images/abc.jpg"
                        )
                        .build();

        MediaFileEntity savedEntity =
                MediaFileEntity.builder()
                        .id(mediaEntity.getId())
                        .storageKey(
                                "properties/images/abc.jpg"
                        )
                        .build();

        when(
                fileValidator.validate(
                        file,
                        MediaUploadPolicy.PROPERTY_MEDIA
                )
        ).thenReturn(validatedFile);

        when(
                storageService.upload(
                        file,
                        validatedFile
                )
        ).thenReturn(uploadedFile);

        when(
                mediaMapper.toEntity(uploadedFile)
        ).thenReturn(mediaEntity);

        when(
                mediaRepository.save(mediaEntity)
        ).thenReturn(savedEntity);

        MediaFileEntity result =
                service.upload(
                        file,
                        MediaUploadPolicy.PROPERTY_MEDIA
                );

        assertThat(result)
                .isEqualTo(savedEntity);

        verify(fileValidator)
                .validate(
                        file,
                        MediaUploadPolicy.PROPERTY_MEDIA
                );

        verify(storageService)
                .upload(
                        file,
                        validatedFile
                );

        verify(mediaMapper)
                .toEntity(uploadedFile);

        verify(mediaRepository)
                .save(mediaEntity);

        verify(
                storageService,
                never()
        ).delete(anyString());
    }

    @Test
    void upload_validationFails_doesNotUploadToStorage() {

        RuntimeException validationException =
                new RuntimeException(
                        "Invalid file"
                );

        when(
                fileValidator.validate(
                        file,
                        MediaUploadPolicy.PROPERTY_MEDIA
                )
        ).thenThrow(validationException);

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> service.upload(
                                file,
                                MediaUploadPolicy.PROPERTY_MEDIA
                        )
                );

        assertThat(exception)
                .isSameAs(validationException);

        verify(
                storageService,
                never()
        ).upload(
                any(),
                any()
        );

        verify(
                mediaRepository,
                never()
        ).save(any());

        verify(
                storageService,
                never()
        ).delete(anyString());
    }

    @Test
    void upload_databaseSaveFails_cleansUpUploadedObject() {

        ValidatedFile validatedFile =
                mock(ValidatedFile.class);

        UploadedFile uploadedFile =
                mock(UploadedFile.class);

        MediaFileEntity mediaEntity =
                MediaFileEntity.builder()
                        .id(UUID.randomUUID())
                        .build();

        when(
                fileValidator.validate(
                        file,
                        MediaUploadPolicy.PROPERTY_MEDIA
                )
        ).thenReturn(validatedFile);

        when(
                storageService.upload(
                        file,
                        validatedFile
                )
        ).thenReturn(uploadedFile);

        when(
                uploadedFile.storageKey()
        ).thenReturn(
                "properties/images/orphan.jpg"
        );

        when(
                mediaMapper.toEntity(uploadedFile)
        ).thenReturn(mediaEntity);

        when(
                mediaRepository.save(mediaEntity)
        ).thenThrow(
                new RuntimeException(
                        "Database failure"
                )
        );

        assertThrows(
                RuntimeException.class,
                () -> service.upload(
                        file,
                        MediaUploadPolicy.PROPERTY_MEDIA
                )
        );

        verify(storageService)
                .delete(
                        "properties/images/orphan.jpg"
                );
    }

    @Test
    void delete_deletesDatabaseRecordThenStorageObject() {

        MediaFileEntity media =
                MediaFileEntity.builder()
                        .id(UUID.randomUUID())
                        .storageKey(
                                "properties/images/abc.jpg"
                        )
                        .build();

        service.delete(media);

        var inOrder =
                inOrder(
                        mediaRepository,
                        storageService
                );

        inOrder.verify(
                mediaRepository
        ).delete(media);

        inOrder.verify(
                storageService
        ).delete(
                "properties/images/abc.jpg"
        );
    }

    @Test
    void delete_storageFails_databaseDeleteWasAttempted() {

        MediaFileEntity media =
                MediaFileEntity.builder()
                        .id(UUID.randomUUID())
                        .storageKey(
                                "properties/images/abc.jpg"
                        )
                        .build();

        doThrow(
                new RuntimeException(
                        "MinIO unavailable"
                )
        ).when(storageService)
                .delete(
                        "properties/images/abc.jpg"
                );

        assertThrows(
                RuntimeException.class,
                () -> service.delete(media)
        );

        verify(
                mediaRepository
        ).delete(media);

        verify(
                storageService
        ).delete(
                "properties/images/abc.jpg"
        );
    }
}