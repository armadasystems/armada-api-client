# armada-api-client

A Spring Boot application for consuming the Armada API with automatic token management and refresh.

## Features

- **Automatic Token Management**: Scheduled token refresh based on configurable threshold
- **Multi-Environment Support**: Easily switch between dev, staging, and production
- **Comprehensive Error Handling**: Custom exceptions with detailed error responses
- **Logging**: Structured logging with SLF4J and Logback
- **Records**: Modern Java records for immutable DTOs
- **Best Practices**: Following Spring Boot and Java best practices

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Armada API credentials (API Key ID and API Key)

## Configuration

### Environment Variables

Set the following environment variables:

```bash
export ARMADA_API_BASE_URL=https://api.dev.armada.ai
export ARMADA_API_KEY_ID=your-api-key-id
export ARMADA_API_KEY=your-api-key
```

Or use the `.env.armada` file as a template.

### application.yml

The application can be configured via `application.yml`. Key configurations:

- **Base URL**: Change based on environment (dev/stage/prod)
- **Token Refresh Cron**: Default is every 5 minutes (`0 */5 * * * *`)
- **Expiry Threshold**: Default is 50% (token refreshes when it has passed half its lifetime)

## Token Refresh Strategy

The application uses a smart token refresh strategy:

1. **Scheduled Job**: Runs every 5 minutes (configurable via cron expression)
2. **Threshold Check**: Only refreshes if token has passed 50% of its lifetime
3. **Lazy Refresh**: Also checks and refreshes on-demand when accessing APIs
4. **Thread-Safe**: Uses synchronized blocks to prevent concurrent refreshes

### Token Refresh Flow

```
Token Issued (t=0) ─────────── Threshold (50%) ─────────── Expires (100%)
                                     ↑
                              Refresh happens here
```

## Project Structure

```
ai.armada.client/
├── common/
│   ├── security/
│   │   ├── AccessToken.java (record)
│   │   ├── TokenRequest.java (record)
│   │   ├── TokenResponse.java (record)
│   │   ├── AuthApiClient.java
│   │   ├── TokenProvider.java
│   │   ├── TokenRefreshScheduler.java
│   │   └── AuthenticationException.java
│   ├── ApiError.java (record)
│   └── ApiExceptionHandler.java
├── config/
│   ├── ArmadaApiProperties.java
│   └── WebClientConfig.java
└── organization/
    ├── controller/
    │   └── OrganizationController.java
    ├── service/
    │   └── OrganizationService.java
    ├── client/
    │   └── OrganizationApiClient.java
    ├── dto/
    │   ├── OrganizationDto.java (record)
    │   └── ExternalOrganizationDto.java
    ├── mapper/
    │   └── OrganizationMapper.java
    └── exception/
        └── OrganizationApiException.java
```

## Running the Application

### Using Maven

```bash
mvn clean install
mvn spring-boot:run
```

### Using Java

```bash
java -jar target/armada-api-client-1.0.0-SNAPSHOT.jar
```

## API Endpoints

### Get Organizations

```http
GET /api/orgs
```

**Response:**
```json
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "display_name": "My Organization"
  }
]
```

## Environment Configurations

### Development
```yaml
armada.api.base-url: https://api.dev.armada.ai
```

### Staging
```yaml
armada.api.base-url: https://api.stage.armada.ai
```

### Production
```yaml
armada.api.base-url: https://api.armada.ai
```

## Logging

The application uses structured logging with different levels:

- **DEBUG**: Detailed flow information
- **INFO**: Important events (token refresh, API calls)
- **WARN**: Warning conditions (token expiring)
- **ERROR**: Error conditions with stack traces

Example log output:
```
2024-12-23 10:15:00 - Refreshing OAuth access token
2024-12-23 10:15:01 - Successfully obtained access token, expires in 3600 seconds
2024-12-23 10:15:01 - Access token refreshed successfully. Valid until: 2024-12-23T11:15:01Z, Remaining: 3600s
```

## Error Handling

The application provides detailed error responses:

```json
{
  "errorCode": "ORG_API_ERROR",
  "message": "Failed to fetch organizations: Unauthorized",
  "timestamp": "2024-12-23T10:15:00Z",
  "path": "/api/orgs"
}
```

## Monitoring Token Status

The `TokenProvider` exposes methods to check token status:

- `shouldRefreshToken()`: Check if refresh is needed
- `getCurrentToken()`: Get current token info including expiration
- `forceRefresh()`: Manually trigger a refresh

## Best Practices Implemented

1. **Records for DTOs**: Immutable data transfer objects
2. **Lombok**: Reduces boilerplate code
3. **Centralized Configuration**: All endpoints in YAML
4. **Exception Handling**: Global exception handler with custom exceptions
5. **Logging**: Comprehensive logging at all levels
6. **Thread Safety**: Synchronized token refresh
7. **WebClient**: Non-blocking reactive HTTP client
8. **Configuration Properties**: Type-safe configuration binding

## Troubleshooting

### Token Not Refreshing

Check the logs for token refresh attempts:
```
Token has passed 50% of its lifetime, refresh needed. Remaining: 1800s
```

### Authentication Errors

Verify your credentials are correct in environment variables:
```bash
echo $ARMADA_API_KEY_ID
echo $ARMADA_API_KEY
```

### Connection Timeouts

Adjust timeouts in `application.yml`:
```yaml
webclient:
  connection-timeout: 10000
  read-timeout: 10000
```

