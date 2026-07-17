package com.realestate.backend.service.media;

import com.realestate.backend.entity.MediaFileEntity;
import com.realestate.backend.enums.MediaType;
import com.realestate.backend.repository.MediaFileRepository;
import com.realestate.backend.storage.StorageService;
import com.realestate.backend.storage.UploadedFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class MediaServiceImpl implements MediaService {

    private final StorageService storageService;
    private final MediaFileRepository mediaFileRepository;

    @Override
    public MediaFileEntity upload(MultipartFile file) {

        UploadedFile uploaded = storageService.upload(file);

        MediaFileEntity entity = MediaFileEntity.builder()
                .storageKey(uploaded.storageKey())
                .fileUrl(uploaded.url())
                .originalName(uploaded.originalName())
                .mimeType(uploaded.mimeType())
                .fileSize(uploaded.fileSize())
                .mediaType(
                        String.valueOf(uploaded.mimeType().startsWith("image")
                                ? MediaType.IMAGE
                                : MediaType.VIDEO)
                )
                .build();

        return mediaFileRepository.saveAndFlush(entity);
    }

}
