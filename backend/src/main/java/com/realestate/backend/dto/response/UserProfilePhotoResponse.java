package com.realestate.backend.dto.response;

import lombok.Builder;

@Builder
public record UserProfilePhotoResponse(

        String photoUrl

) {
}
