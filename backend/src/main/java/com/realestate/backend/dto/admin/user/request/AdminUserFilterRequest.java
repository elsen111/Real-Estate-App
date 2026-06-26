package com.realestate.backend.dto.admin.user.request;

import com.realestate.backend.enums.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserFilterRequest {

    Role role;

    Boolean enabled;

    String query;

}
