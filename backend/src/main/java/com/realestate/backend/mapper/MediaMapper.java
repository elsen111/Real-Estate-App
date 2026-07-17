package com.realestate.backend.mapper;

import com.realestate.backend.entity.MediaFileEntity;
import com.realestate.backend.enums.MediaType;
import com.realestate.backend.exception.StorageException;
import com.realestate.backend.storage.UploadedFile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MediaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mediaType", expression = "java(resolveMediaType(uploadedFile.mimeType()))")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    MediaFileEntity toEntity(UploadedFile uploadedFile);

    default MediaType resolveMediaType(String mimeType) {

        if (mimeType == null) {
            throw new StorageException("Media type is missing.");
        }

        if (mimeType.startsWith("image/")) {
            return MediaType.IMAGE;
        }

        if (mimeType.startsWith("video/")) {
            return MediaType.VIDEO;
        }

        throw new StorageException(
                "Unsupported media type: " + mimeType
        );
    }

}
