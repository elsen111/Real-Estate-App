package com.realestate.backend.service;

import com.realestate.backend.entity.MediaFileEntity;
import com.realestate.backend.storage.MediaUploadPolicy;
import org.springframework.web.multipart.MultipartFile;

public interface MediaService {

    MediaFileEntity upload(MultipartFile file, MediaUploadPolicy policy);

    void delete(MediaFileEntity mediaFile);

}
