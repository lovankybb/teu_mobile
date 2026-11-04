package com.teumobile.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PhoneNumberValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhoneNumber {

    String message() default "Số điện thoại không hợp lệ (yêu cầu định dạng số điện thoại Việt Nam 10 chữ số, ví dụ: 0912345678 hoặc +84912345678)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Nếu true: cho phép giá trị null hoặc rỗng (chỉ validate khi có truyền dữ liệu)
     */
    boolean required() default false;
}
