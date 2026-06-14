package com.myonline.tenantservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Generic API response wrapper used by all endpoints in the Tenant Microservice.
 *
 * <p>Every response is wrapped in this structure for consistency:
 * <pre>
 * {
 *   "success": true,
 *   "message": "Operation successful",
 *   "data": { ... },
 *   "timestamp": "2024-01-01T10:00:00"
 * }
 * </pre>
 *
 * <p>Fields with {@code null} values are excluded from the JSON output via {@code @JsonInclude(NON_NULL)}.
 *
 * @param <T> the type of the response data payload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /** Indicates whether the operation was successful. */
    private boolean success;

    /** A human-readable message describing the outcome. */
    private String message;

    /** The response data payload (null for error responses or operations with no data). */
    private T data;

    /** Timestamp of when the response was generated. */
    private LocalDateTime timestamp;

    // ------------------------------------------------------------------
    // Static factory methods
    // ------------------------------------------------------------------

    /**
     * Creates a successful response with data.
     *
     * @param message a descriptive success message
     * @param data    the response payload
     * @param <T>     the data type
     * @return a successful {@link ApiResponse}
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a successful response without data (e.g., for delete operations).
     *
     * @param message a descriptive success message
     * @param <T>     the data type (typically Void)
     * @return a successful {@link ApiResponse} with no data
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates an error response with a message only.
     *
     * @param message a descriptive error message
     * @param <T>     the data type (typically Void)
     * @return an error {@link ApiResponse}
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates an error response with a message and additional error details.
     *
     * @param message a descriptive error message
     * @param data    additional error details (e.g., validation error map)
     * @param <T>     the data type
     * @return an error {@link ApiResponse} with error details
     */
    public static <T> ApiResponse<T> error(String message, T data) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
