package com.transitops.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * Standard API response wrapper — matches the { success, message, data } format
 * used by all endpoints in the Node.js backend.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private T user;
    private String token;
    private Long total;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder().success(true).data(data).build();
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return ApiResponse.<T>builder().success(true).message(message).data(data).build();
    }

    public static <T> ApiResponse<T> ok(T data, long total) {
        return ApiResponse.<T>builder().success(true).data(data).total(total).build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder().success(false).message(message).build();
    }
}
