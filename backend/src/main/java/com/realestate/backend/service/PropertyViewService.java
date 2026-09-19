package com.realestate.backend.service;

import com.realestate.backend.entity.PropertyEntity;
import com.realestate.backend.security.CustomUserDetails;

public interface PropertyViewService {

    void recordView(PropertyEntity property, CustomUserDetails viewer);

}
