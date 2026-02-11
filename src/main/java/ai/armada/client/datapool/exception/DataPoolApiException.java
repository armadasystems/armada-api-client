package ai.armada.client.datapool.exception;

/**
 * Exception thrown when data pool API calls fail
 */
public class DataPoolApiException extends RuntimeException {

    private final String errorCode;
    
    public DataPoolApiException(String message) {
        super(message);
        this.errorCode = "DATAPOOL_API_ERROR";
    }
    
    public DataPoolApiException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "DATAPOOL_API_ERROR";
    }
    
    public DataPoolApiException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public DataPoolApiException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}