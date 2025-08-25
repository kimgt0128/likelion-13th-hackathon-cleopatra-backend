package com.likelion.cleopatra.global.exception.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Setter
@Getter
public class ApiResponse<T> {

    private static final String SUCCESS_STATUS = "success";
    private static final String FAIL_STATUS = "fail";

    private String status;
    private T data;
    private String message;

    public static ApiResponse<?> success() {
        return new ApiResponse<>(SUCCESS_STATUS, null, null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(SUCCESS_STATUS, data, null);
    }


    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(SUCCESS_STATUS, data, message);
    }


    public static ApiResponse<?> fail(String message) {
        return new ApiResponse<>(FAIL_STATUS, null, message);
    }

    public static <T> ApiResponse<T> fail(T data, String message) {
        return new ApiResponse<>(FAIL_STATUS, data, message);
    }

    protected ApiResponse() {
    }

    private ApiResponse(String status, T data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }
}