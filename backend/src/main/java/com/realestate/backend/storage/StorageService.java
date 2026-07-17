package com.realestate.backend.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    UploadedFile upload(MultipartFile file);

    void delete(String storageKey);

}