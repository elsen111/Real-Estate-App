package com.realestate.backend.service.media;

import com.realestate.backend.entity.MediaFileEntity;
import org.springframework.web.multipart.MultipartFile;

public interface MediaService {

    MediaFileEntity upload(MultipartFile file);

}
