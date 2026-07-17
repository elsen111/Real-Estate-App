package com.realestate.backend.dto.user.response;

import lombok.Builder;

@Builder
public record UserProfilePhotoResponse(

        String photoUrl

) {
}
