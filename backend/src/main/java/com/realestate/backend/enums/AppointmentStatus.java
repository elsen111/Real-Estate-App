package com.realestate.backend.enums;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum AppointmentStatus {

        PENDING,
        APPROVED,
        TENTATIVE,
        IN_PROGRESS,
        DELAYED,
        COMPLETED,
        CANCELLED,
        RESCHEDULED,
        NO_SHOW,
        REJECTED;

        private static final Map<AppointmentStatus, Set<AppointmentStatus>> ALLOWED_TRANSITIONS = Map.of(
                PENDING, EnumSet.of(
                        APPROVED,
                        TENTATIVE,
                        CANCELLED,
                        RESCHEDULED,
                        REJECTED
                ),

                TENTATIVE, EnumSet.of(
                        APPROVED,
                        CANCELLED,
                        RESCHEDULED,
                        REJECTED
                ),

                APPROVED, EnumSet.of(
                        IN_PROGRESS,
                        DELAYED,
                        CANCELLED,
                        RESCHEDULED,
                        NO_SHOW
                ),

                DELAYED, EnumSet.of(
                        IN_PROGRESS,
                        CANCELLED,
                        RESCHEDULED,
                        NO_SHOW
                ),

                IN_PROGRESS, EnumSet.of(
                        COMPLETED,
                        DELAYED
                ),

                RESCHEDULED, EnumSet.of(
                        PENDING
                ),

                COMPLETED, EnumSet.noneOf(AppointmentStatus.class),
                CANCELLED, EnumSet.noneOf(AppointmentStatus.class),
                NO_SHOW, EnumSet.noneOf(AppointmentStatus.class),
                REJECTED, EnumSet.noneOf(AppointmentStatus.class)
        );

        public boolean canTransitionTo(AppointmentStatus targetStatus) {
                return ALLOWED_TRANSITIONS
                        .getOrDefault(this, Set.of())
                        .contains(targetStatus);
        }
}