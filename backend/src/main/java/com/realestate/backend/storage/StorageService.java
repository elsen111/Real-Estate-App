package com.realestate.backend.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    UploadedFile upload(
            MultipartFile file,
            ValidatedFile validatedFile
    );

    void delete(String storageKey);

}