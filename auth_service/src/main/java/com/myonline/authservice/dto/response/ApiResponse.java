package com.myonline.authservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Generic API response wrapper used across all endpoints.
 *
 * <p>Provides a consistent response structure:
 * <ul>
 *   <li>{@code success} - whether the operation succeeded</li>
 *   <li>{@code message} - human-readable message</li>
 *   <li>{@code data}    - response payload (null for error/no-content responses)</li>
 *   <li>{@code timestamp} - when the response was generated</li>
 * </ul>
 *
 * @param <T> the type of the data payload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Omit null fields from JSON output
@Schema(description = "Standard API response wrapper")
public class ApiResponse<T> {

    @Schema(description = "Indicates if the operation was successful", example = "true")
    private boolean success;

    @Schema(description = "Human-readable message", example = "Operation completed successfully")
    private String message;

    @Schema(description = "Response payload (null for errors or no-content responses)")
    private T data;

    @Schema(description = "Timestamp when the response was generated")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /** Convenience factory for successful responses with data */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /** Convenience factory for successful responses without data */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /** Convenience factory for error responses */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
