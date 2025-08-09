package com.likelion.cleopatra.global.exception.handler;

import com.likelion.cleopatra.global.exception.CleopatraException;
import com.likelion.cleopatra.global.exception.code.ErrorCode;
import com.likelion.cleopatra.global.exception.code.GlobalErrorCode;
import com.likelion.cleopatra.global.exception.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CleopatraException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessException(
            CleopatraException e, HttpServletRequest request) {
        log.warn("Business exception occurred: {}, URI: {}", e.getMessage(), request.getRequestURI(), e);
        return buildErrorResponse(e.getErrorCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        log.warn("Validation error occurred: URI: {}", request.getRequestURI(), e);

        Map<String, String> errors = new HashMap<>();
        BindingResult bindingResult = e.getBindingResult();
        bindingResult.getAllErrors().forEach(error -> {
            if (error instanceof FieldError fieldError) {
                errors.put(fieldError.getField(), fieldError.getDefaultMessage());
            } else {
                errors.put(error.getObjectName(), error.getDefaultMessage());
            }
        });

        return buildErrorResponse(GlobalErrorCode.BAD_REQUEST, errors);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<?>> handleMissingServletRequestParameter(
            MissingServletRequestParameterException e, HttpServletRequest request) {

        log.warn("Missing parameter: {}, URI: {}", e.getParameterName(), request.getRequestURI(), e);

        Map<String, String> details = new HashMap<>();
        details.put("parameter", e.getParameterName());

        return buildErrorResponse(GlobalErrorCode.BAD_REQUEST, details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException e, HttpServletRequest request) {

        log.warn("Message not readable: URI: {}", request.getRequestURI(), e);

        return buildErrorResponse(GlobalErrorCode.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException e, HttpServletRequest request) {

        log.warn("Method not supported: {}, URI: {}", e.getMethod(), request.getRequestURI(), e);

        Map<String, Object> details = new HashMap<>();
        details.put("unsupported", e.getMethod());
        details.put("supported", e.getSupportedHttpMethods());

        return buildErrorResponse(GlobalErrorCode.METHOD_NOT_ALLOWED, details);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException e, HttpServletRequest request) {

        log.warn("Media type not supported: {}, URI: {}", e.getContentType(), request.getRequestURI(), e);

        Map<String, Object> details = new HashMap<>();
        details.put("unsupported", e.getContentType());
        details.put("supported", e.getSupportedMediaTypes());

        return buildErrorResponse(GlobalErrorCode.UNSUPPORTED_MEDIA_TYPE, details);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException e, HttpServletRequest request) {

        log.warn("Type mismatch: {} (required: {}), URI: {}",
                e.getName(), e.getRequiredType(), request.getRequestURI(), e);

        Map<String, String> details = new HashMap<>();
        details.put("parameter", e.getName());
        details.put("requiredType", e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown");
        details.put("providedValue", String.valueOf(e.getValue()));

        return buildErrorResponse(GlobalErrorCode.BAD_REQUEST, details);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<?>> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException e, HttpServletRequest request) {

        log.warn("File size exceeded: URI: {}", request.getRequestURI(), e);

        return buildErrorResponse(GlobalErrorCode.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleDefaultException(Exception e, HttpServletRequest request) {

        log.error("Unhandled exception occurred: URI: {}", request.getRequestURI(), e);

        return buildErrorResponse(GlobalErrorCode.INTERNAL_ERROR);
    }

    private ResponseEntity<ApiResponse<?>> buildErrorResponse(ErrorCode errorCode) {
        ApiResponse<?> errorResponse = ApiResponse.fail(errorCode.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(errorCode.getStatus()));
    }

    private ResponseEntity<ApiResponse<?>> buildErrorResponse(ErrorCode errorCode, Object details) {
        ApiResponse<?> errorResponse = ApiResponse.fail(details, errorCode.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(errorCode.getStatus()));
    }
}
