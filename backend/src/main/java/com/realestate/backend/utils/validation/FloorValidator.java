package com.realestate.backend.utils.validation;

import com.realestate.backend.dto.request.PropertyRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FloorValidator implements ConstraintValidator<ValidFloorChecker, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (!(value instanceof PropertyRequest target)) {
            return true;
        }

        Integer floor = target.getFloor();
        Integer totalFloors = target.getTotalFloors();

        if (floor == null || totalFloors == null) {
            return true;
        }

        return floor <= totalFloors;
    }
}
