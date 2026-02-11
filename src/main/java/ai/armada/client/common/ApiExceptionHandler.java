package ai.armada.client.common;

import ai.armada.client.common.security.AuthenticationException;
import ai.armada.client.datapool.exception.DataPoolApiException;
import ai.armada.client.organization.exception.OrganizationApiException;
import ai.armada.client.serviceline.exception.ServiceLineApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@ControllerAdvice
@Slf4j
public class ApiExceptionHandler {

    @ExceptionHandler(OrganizationApiException.class)
    public ResponseEntity<ApiError> handleOrganizationApiException(
            OrganizationApiException ex, WebRequest request) {
        log.error("Organization API error: {}", ex.getMessage(), ex);

        ApiError error = new ApiError(
                ex.getErrorCode(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(error);
    }

    @ExceptionHandler(DataPoolApiException.class)
    public ResponseEntity<ApiError> handleDataPoolApiException(
            DataPoolApiException ex, WebRequest request) {
        log.error("DataPool API error: {}", ex.getMessage(), ex);

        ApiError error = new ApiError(
                ex.getErrorCode(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(error);
    }

    @ExceptionHandler(ServiceLineApiException.class)
    public ResponseEntity<ApiError> handleServiceLineApiException(
            ServiceLineApiException ex, WebRequest request) {
        log.error("ServiceLine API error: {}", ex.getMessage(), ex);

        ApiError error = new ApiError(
                ex.getErrorCode(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(error);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        log.error("Authentication error: {}", ex.getMessage(), ex);
        
        ApiError error = new ApiError(
                "AUTH_ERROR",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(error);
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ApiError> handleWebClientResponseException(
            WebClientResponseException ex, WebRequest request) {
        log.error("WebClient error: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
        
        ApiError error = new ApiError(
                "EXTERNAL_API_ERROR",
                "External API call failed: " + ex.getStatusText(),
                request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneralException(
            Exception ex, WebRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        
        ApiError error = new ApiError(
                "INTERNAL_ERROR",
                "An unexpected error occurred: " + ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
}