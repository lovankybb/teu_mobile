package com.teumobile.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {

    /**
     * Regex số điện thoại di động Việt Nam 10 số:
     * Đầu số 0 hoặc +84 kết hợp với các đầu mạng 3, 5, 7, 8, 9 và 7 chữ số tiếp theo.
     */
    private static final Pattern VIETNAM_PHONE_PATTERN = Pattern.compile(
            "^(?:\\+84|0)(?:3[2-9]|5[25689]|7[06-9]|8[1-9]|9[0-9])[0-9]{7}$"
    );

    private boolean required;

    @Override
    public void initialize(ValidPhoneNumber constraintAnnotation) {
        this.required = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return !required;
        }

        // Loại bỏ khoảng trắng và dấu gạch nối nếu client vô tình gửi vào (ví dụ: 0912-345-678)
        String sanitized = value.replaceAll("[\\s-]+", "");
        return VIETNAM_PHONE_PATTERN.matcher(sanitized).matches();
    }
}
