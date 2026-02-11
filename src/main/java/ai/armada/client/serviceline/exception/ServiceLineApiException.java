package ai.armada.client.serviceline.exception;

/**
 * Exception thrown when service line API calls fail
 */
public class ServiceLineApiException extends RuntimeException {

    private final String errorCode;
    
    public ServiceLineApiException(String message) {
        super(message);
        this.errorCode = "SERVICELINE_API_ERROR";
    }
    
    public ServiceLineApiException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "SERVICELINE_API_ERROR";
    }
    
    public ServiceLineApiException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public ServiceLineApiException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}