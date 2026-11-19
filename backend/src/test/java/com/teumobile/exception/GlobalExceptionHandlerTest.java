package com.teumobile.exception;

import com.teumobile.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Xử lý ResourceNotFoundException trả về HTTP 404 NOT FOUND và error message")
    void testHandleResourceNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("User không tồn tại");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleResourceNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("User không tồn tại", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Xử lý IllegalArgumentException trả về HTTP 400 BAD REQUEST và error message")
    void testHandleIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("Tham số không hợp lệ");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Tham số không hợp lệ", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Xử lý AccessDeniedException trả về HTTP 403 FORBIDDEN")
    void testHandleAccessDenied() {
        AccessDeniedException ex = new AccessDeniedException("Forbidden");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleAccessDenied(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Bạn không có quyền thực hiện thao tác này", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Xử lý Exception chung trả về HTTP 500 INTERNAL SERVER ERROR")
    void testHandleGeneralException() {
        Exception ex = new RuntimeException("Lỗi CSDL");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleGeneralException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Đã xảy ra lỗi hệ thống: Lỗi CSDL"));
    }

    @Test
    @DisplayName("Xử lý MethodArgumentNotValidException trả về HTTP 400 BAD REQUEST kèm danh sách lỗi trường dữ liệu")
    void testHandleValidationExceptions() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("userDto", "email", "Email sai định dạng");
        FieldError fieldError2 = new FieldError("userDto", "phoneNumber", "Số điện thoại không hợp lệ");

        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ApiResponse<Map<String, String>>> response = exceptionHandler.handleValidationExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Dữ liệu đầu vào không hợp lệ", response.getBody().getMessage());

        Map<String, String> errors = response.getBody().getData();
        assertNotNull(errors);
        assertEquals(2, errors.size());
        assertEquals("Email sai định dạng", errors.get("email"));
        assertEquals("Số điện thoại không hợp lệ", errors.get("phoneNumber"));
    }
}
