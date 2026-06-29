package com.realestate.backend.dto.admin.agency.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminAgencyFilterRequest {

    String city;
    String email;
    Boolean status;
    String query;

}
