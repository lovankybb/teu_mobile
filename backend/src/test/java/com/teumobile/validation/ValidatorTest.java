package com.teumobile.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidatorTest {

    private final PhoneNumberValidator phoneValidator = new PhoneNumberValidator();
    private final UsernameValidator usernameValidator = new UsernameValidator();
    private final EmailValidator emailValidator = new EmailValidator();

    @Test
    @DisplayName("PhoneNumberValidator: Số điện thoại Việt Nam hợp lệ")
    void testValidPhoneNumbers() {
        assertTrue(phoneValidator.isValid("0912345678", null));
        assertTrue(phoneValidator.isValid("0381234567", null));
        assertTrue(phoneValidator.isValid("0791234567", null));
        assertTrue(phoneValidator.isValid("+84912345678", null));
        assertTrue(phoneValidator.isValid("0912-345-678", null));
    }

    @Test
    @DisplayName("PhoneNumberValidator: Số điện thoại không hợp lệ")
    void testInvalidPhoneNumbers() {
        assertFalse(phoneValidator.isValid("123456", null));
        assertFalse(phoneValidator.isValid("0123456789", null)); // Đầu 01 cũ 11 số
        assertFalse(phoneValidator.isValid("0212345678", null)); // Đầu 02 là máy bàn
        assertFalse(phoneValidator.isValid("abcdefghij", null));
    }

    @Test
    @DisplayName("UsernameValidator: Tên đăng nhập hợp lệ")
    void testValidUsernames() {
        assertTrue(usernameValidator.isValid("john_doe", null));
        assertTrue(usernameValidator.isValid("user.name", null));
        assertTrue(usernameValidator.isValid("admin2026", null));
        assertTrue(usernameValidator.isValid("teu-mobile", null));
    }

    @Test
    @DisplayName("UsernameValidator: Tên đăng nhập không hợp lệ")
    void testInvalidUsernames() {
        assertFalse(usernameValidator.isValid("ab", null)); // Quá ngắn (< 3)
        assertFalse(usernameValidator.isValid("user name", null)); // Có khoảng trắng
        assertFalse(usernameValidator.isValid(".username", null)); // Bắt đầu bằng dấu chấm
        assertFalse(usernameValidator.isValid("user@domain", null)); // Chứa ký tự @
    }

    @Test
    @DisplayName("EmailValidator: Email hợp lệ")
    void testValidEmails() {
        assertTrue(emailValidator.isValid("customer@gmail.com", null));
        assertTrue(emailValidator.isValid("admin.support@teumobile.vn", null));
        assertTrue(emailValidator.isValid("user+test@domain.co", null));
    }

    @Test
    @DisplayName("EmailValidator: Email không hợp lệ")
    void testInvalidEmails() {
        assertFalse(emailValidator.isValid("invalid-email", null));
        assertFalse(emailValidator.isValid("user@", null));
        assertFalse(emailValidator.isValid("@domain.com", null));
        assertFalse(emailValidator.isValid("user@domain", null));
    }
}
