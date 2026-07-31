package com.realestate.backend.service;

import com.realestate.backend.entity.MediaFileEntity;
import com.realestate.backend.enums.MediaFolder;
import com.realestate.backend.mapper.MediaMapper;
import com.realestate.backend.repository.MediaRepository;
import com.realestate.backend.service.impl.MediaServiceImpl;
import com.realestate.backend.storage.StorageService;
import com.realestate.backend.storage.UploadedFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaServiceImplTest {

    @Mock private StorageService storageService;
    @Mock private MediaRepository mediaRepository;
    @Mock private MediaMapper mediaMapper;
    @Mock private MultipartFile file;

    @InjectMocks private MediaServiceImpl service;

    @Test
    void upload_storesFile_andSavesMediaRecord() {
        UploadedFile uploadedFile = mock(UploadedFile.class);
        when(uploadedFile.storageKey()).thenReturn("properties/abc.jpg");
        MediaFileEntity mediaEntity = MediaFileEntity.builder().id(UUID.randomUUID())
                .storageKey("properties/abc.jpg").build();
        MediaFileEntity savedEntity = MediaFileEntity.builder().id(mediaEntity.getId())
                .storageKey("properties/abc.jpg").build();

        when(storageService.upload(file, MediaFolder.PROPERTY_IMAGE)).thenReturn(uploadedFile);
        when(mediaMapper.toEntity(uploadedFile)).thenReturn(mediaEntity);
        when(mediaRepository.save(mediaEntity)).thenReturn(savedEntity);

        MediaFileEntity result = service.upload(file, MediaFolder.PROPERTY_IMAGE);

        assertThat(result).isEqualTo(savedEntity);
        verify(mediaRepository).save(mediaEntity);
    }

    @Test
    void delete_removesFromStorageAndRepository() {
        MediaFileEntity media = MediaFileEntity.builder().id(UUID.randomUUID())
                .storageKey("properties/abc.jpg").build();

        service.delete(media);

        verify(storageService).delete("properties/abc.jpg");
        verify(mediaRepository).delete(media);
    }
}