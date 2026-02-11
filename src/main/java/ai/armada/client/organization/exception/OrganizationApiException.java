package ai.armada.client.organization.exception;

/**
 * Exception thrown when organization API calls fail
 */
public class OrganizationApiException extends RuntimeException {

    private final String errorCode;
    
    public OrganizationApiException(String message) {
        super(message);
        this.errorCode = "ORG_API_ERROR";
    }
    
    public OrganizationApiException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "ORG_API_ERROR";
    }
    
    public OrganizationApiException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public OrganizationApiException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}