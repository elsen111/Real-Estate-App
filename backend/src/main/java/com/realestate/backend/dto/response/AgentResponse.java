package com.realestate.backend.dto.response;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AgentResponse extends UserResponse {

    private UUID memberId;
    private UUID agencyId;

}
