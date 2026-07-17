package com.realestate.backend.storage;

import com.realestate.backend.enums.MediaFolder;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    UploadedFile upload(
            MultipartFile file,
            MediaFolder folder
    );

    void delete(String storageKey);

}