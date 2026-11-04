package com.teumobile.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UsernameValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUsername {

    String message() default "Tên đăng nhập không hợp lệ (từ 3 đến 30 ký tự, bắt đầu bằng chữ cái hoặc chữ số, chỉ chứa chữ, số, dấu gạch dưới, gạch ngang hoặc chấm)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    boolean required() default true;
}
