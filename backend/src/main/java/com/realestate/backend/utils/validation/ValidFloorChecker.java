package com.realestate.backend.utils.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FloorValidator.class)
public @interface ValidFloorChecker {
    String message() default "Floor cannot be greater than total floors";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
