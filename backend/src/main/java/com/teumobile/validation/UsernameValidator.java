package com.teumobile.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class UsernameValidator implements ConstraintValidator<ValidUsername, String> {

    /**
     * Username: 3-30 ký tự, bắt đầu bằng chữ cái hoặc chữ số,
     * cho phép chữ cái, chữ số, dấu gạch dưới, gạch ngang, dấu chấm. Không có khoảng trắng.
     */
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9][a-zA-Z0-9._-]{2,29}$"
    );

    private boolean required;

    @Override
    public void initialize(ValidUsername constraintAnnotation) {
        this.required = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return !required;
        }

        return USERNAME_PATTERN.matcher(value.trim()).matches();
    }
}
