package ai.armada.client.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        String errorCode,
        String message,
        Instant timestamp,
        String path
) {
    public ApiError(String errorCode, String message) {
        this(errorCode, message, Instant.now(), null);
    }
    
    public ApiError(String errorCode, String message, String path) {
        this(errorCode, message, Instant.now(), path);
    }
}