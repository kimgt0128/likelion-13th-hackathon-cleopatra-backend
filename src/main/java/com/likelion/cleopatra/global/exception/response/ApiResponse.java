package com.likelion.cleopatra.global.exception.response;


import lombok.Getter;
import org.springdoc.api.ErrorMessage;

@Getter
public class ApiResponse<T> {

    private static final String SUCCESS_STATUS = "success";
    private static final String FAIL_STATUS = "fail";

    private String status;
    private T data;
    private ErrorMessage errorMessage;

    public static ApiResponse<?> success() {
        return new ApiResponse<>(SUCCESS_STATUS, null, null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(SUCCESS_STATUS, data, null);
    }

    // 메시지가 필요한 success의 경우 추가하기


    public static ApiResponse<?> fail(ErrorMessage errorMessage) {
        return new ApiResponse<>(FAIL_STATUS, null, errorMessage);
    }


    protected ApiResponse() {
    }

    private ApiResponse(String status, T data, ErrorMessage errorMessage) {
        this.status = status;
        this.data = data;
        this.errorMessage = errorMessage;
    }
}