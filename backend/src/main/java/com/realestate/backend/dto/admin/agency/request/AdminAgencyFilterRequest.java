package com.realestate.backend.dto.admin.agency.request;

import com.realestate.backend.dto.agency.request.AgencyFilterRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminAgencyFilterRequest extends AgencyFilterRequest {

    String email;
    Boolean status;
    Boolean isDeleted;

}
