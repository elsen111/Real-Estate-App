package com.realestate.backend.service.impl;

import com.realestate.backend.entity.MediaFileEntity;
import com.realestate.backend.exception.StorageException;
import com.realestate.backend.mapper.MediaMapper;
import com.realestate.backend.repository.MediaRepository;
import com.realestate.backend.service.MediaService;
import com.realestate.backend.storage.FileValidator;
import com.realestate.backend.storage.MediaUploadPolicy;
import com.realestate.backend.storage.StorageService;
import com.realestate.backend.storage.UploadedFile;
import com.realestate.backend.storage.ValidatedFile;
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
    private final FileValidator fileValidator;

    private final MediaRepository mediaRepository;
    private final MediaMapper mediaMapper;

    @Override
    @Transactional
    public MediaFileEntity upload(
            MultipartFile file,
            MediaUploadPolicy policy
    ) {

        ValidatedFile validatedFile =
                fileValidator.validate(
                        file,
                        policy
                );

        UploadedFile uploadedFile = null;

        try {

            uploadedFile =
                    storageService.upload(
                            file,
                            validatedFile
                    );

            log.atInfo()
                    .setMessage("File uploaded to storage")
                    .addKeyValue(
                            "storageKey",
                            uploadedFile.storageKey()
                    )
                    .addKeyValue(
                            "mediaType",
                            uploadedFile.mimeType()
                    )
                    .addKeyValue(
                            "fileSize",
                            uploadedFile.fileSize()
                    )
                    .log();

            MediaFileEntity media =
                    mediaMapper.toEntity(uploadedFile);

            MediaFileEntity savedMedia =
                    mediaRepository.save(media);

            log.atInfo()
                    .setMessage("Media record created")
                    .addKeyValue(
                            "mediaId",
                            savedMedia.getId()
                    )
                    .addKeyValue(
                            "storageKey",
                            savedMedia.getStorageKey()
                    )
                    .log();

            return savedMedia;

        } catch (Exception e) {

            if (uploadedFile != null) {

                cleanupUploadedObject(
                        uploadedFile.storageKey()
                );
            }

            if (e instanceof StorageException storageException) {
                throw storageException;
            }

            throw new StorageException(
                    "Failed to create media record.",
                    e
            );
        }
    }

    @Override
    @Transactional
    public void delete(
            MediaFileEntity media
    ) {

        String storageKey =
                media.getStorageKey();

        mediaRepository.delete(media);

        storageService.delete(storageKey);

        log.atInfo()
                .setMessage("Media deleted")
                .addKeyValue(
                        "mediaId",
                        media.getId()
                )
                .addKeyValue(
                        "storageKey",
                        storageKey
                )
                .log();
    }

    private void cleanupUploadedObject(
            String storageKey
    ) {

        try {

            storageService.delete(
                    storageKey
            );

            log.atWarn()
                    .setMessage(
                            "Cleaned up orphaned storage object"
                    )
                    .addKeyValue(
                            "storageKey",
                            storageKey
                    )
                    .log();

        } catch (Exception cleanupException) {

            log.atError()
                    .setMessage(
                            "Failed to clean up orphaned storage object"
                    )
                    .addKeyValue(
                            "storageKey",
                            storageKey
                    )
                    .setCause(cleanupException)
                    .log();
        }
    }
}