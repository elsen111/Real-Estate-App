package com.realestate.backend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminAgencyFilterRequest extends AgencyFilterRequest {

    String email;
    Boolean status;
    Boolean isDeleted;

}
