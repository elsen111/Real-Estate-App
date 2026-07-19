package com.realestate.backend.service;

import com.realestate.backend.entity.MediaFileEntity;
import com.realestate.backend.enums.MediaFolder;
import org.springframework.web.multipart.MultipartFile;

public interface MediaService {

    MediaFileEntity upload(MultipartFile file, MediaFolder folder);

    void delete(MediaFileEntity mediaFile);

}
