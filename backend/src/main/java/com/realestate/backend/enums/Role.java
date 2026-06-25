package com.realestate.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public enum Role {

    SUPER_ADMIN("Super Admin"),
    AGENCY_OWNER("Agency Owner"),
    AGENT("Agent"),
    LANDLORD("Landlord"),
    CLIENT("Client");

    private String label;

}