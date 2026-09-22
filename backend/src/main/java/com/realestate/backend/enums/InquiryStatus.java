package com.realestate.backend.enums;

import lombok.Getter;

@Getter
public enum InquiryStatus {
    NEW(0),
    CONTACTED(1),
    CLOSED(2);

    private final int level;

    InquiryStatus(int level) {
        this.level = level;
    }

    public boolean canTransitionTo(InquiryStatus targetStatus) {
        return targetStatus != null && this.level < targetStatus.getLevel();
    }
}
