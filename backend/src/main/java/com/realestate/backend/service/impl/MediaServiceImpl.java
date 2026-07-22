package com.realestate.backend.service.impl;

import com.realestate.backend.entity.MediaFileEntity;
import com.realestate.backend.enums.MediaFolder;
import com.realestate.backend.mapper.MediaMapper;
import com.realestate.backend.repository.MediaRepository;
import com.realestate.backend.service.MediaService;
import com.realestate.backend.storage.StorageService;
import com.realestate.backend.storage.UploadedFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final StorageService storageService;

    private final MediaRepository mediaRepository;
    private final MediaMapper mediaMapper;

    @Override
    @Transactional
    public MediaFileEntity upload(
            MultipartFile file,
            MediaFolder folder
    ) {

        UploadedFile uploadedFile =
                storageService.upload(file, folder);

        log.info(
                "File uploaded to storage. Folder: {}, Storage key: {}",
                folder,
                uploadedFile.storageKey()
        );

        MediaFileEntity media =
                mediaMapper.toEntity(uploadedFile);

        log.info(
                "Media record {} created for storage key {}",
                media.getId(),
                media.getStorageKey()
        );

        return mediaRepository.save(media);

    }

    @Override
    @Transactional
    public void delete(MediaFileEntity media) {

        storageService.delete(media.getStorageKey());

        mediaRepository.delete(media);

        log.info(
                "Media {} deleted successfully",
                media.getId()
        );

    }
}
