package com.iams.manage.validator;


import jakarta.validation.Constraint;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UserInfoValidator.class)
public @interface ValidUserInfo {

    String message() default "用户信息校验失败";
    Class<?>[] groups() default {};
    Class<?>[] payload() default {};
}
