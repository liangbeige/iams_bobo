package com.iams.manage.validator;


import jakarta.validation.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = BorrowTimeValidator.class)
public @interface ValidBorrowTime {

    String message() default "时间范围有误";
    Class<?>[] groups() default {};
    Class<?>[] payload() default {};
}
