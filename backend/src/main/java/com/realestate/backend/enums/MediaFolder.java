package com.realestate.backend.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum MediaFolder {

    USER_PROFILE("users/profile"),
    AGENCY_LOGO("agencies/logo"),
    PROPERTY_IMAGE("properties/images"),
    PROPERTY_VIDEO("properties/videos"),
    SITE_LOGO("site/logo");

    private final String folderName;

}
