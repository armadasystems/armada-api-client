package ai.armada.client.common.security;

import ai.armada.client.config.ArmadaApiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@Slf4j
public class AuthApiClient {

    private final WebClient webClient;
    private final ArmadaApiProperties properties;

    public AuthApiClient(WebClient webClient, ArmadaApiProperties properties) {
        this.webClient = webClient;
        this.properties = properties;
    }

    public TokenResponse fetchAuthToken() {
        log.debug("Fetching new authentication token from {}", 
                properties.getBaseUrl() + properties.getEndpoints().getAuth().getToken());
        
        try {
            TokenRequest request = new TokenRequest(
                    properties.getCredentials().getApiKey(),
                    properties.getCredentials().getApiKeyId()
            );

            TokenResponse response = webClient.post()
                    .uri(properties.getEndpoints().getAuth().getToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(TokenResponse.class)
                    .doOnSuccess(r -> {
                        log.info("Received token response - status: {}, expiresIn: {}, organizationId: {}",
                                r.status(), r.getExpiresIn(), r.getOrganizationId());
                        if (r.getAccessToken() != null) {
                            log.debug("Access token length: {}", r.getAccessToken().length());
                        }
                    })
                    .doOnError(e -> log.error("Failed to obtain access token", e))
                    .block();

            if (response == null) {
                throw new AuthenticationException("Failed to authenticate: No response");
            }

            if (!response.isSuccess()) {
                throw new AuthenticationException("Failed to authenticate: API returned status '" +
                        response.status() + "'");
            }

            if (response.getAccessToken() == null || response.getAccessToken().isEmpty()) {
                throw new AuthenticationException("Failed to authenticate: No access token in response");
            }

            return response;
        } catch (WebClientResponseException e) {
            log.error("HTTP error during token fetch: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AuthenticationException("Authentication failed with status: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("Unexpected error during token fetch", e);
            throw new AuthenticationException("Authentication failed: " + e.getMessage(), e);
        }
    }
}