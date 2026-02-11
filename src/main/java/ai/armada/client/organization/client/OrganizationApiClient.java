package ai.armada.client.organization.client;

import ai.armada.client.common.security.TokenProvider;
import ai.armada.client.config.ArmadaApiProperties;
import ai.armada.client.organization.dto.ExternalOrganizationDto;
import ai.armada.client.organization.exception.OrganizationApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Component
@Slf4j
public class OrganizationApiClient {

    private final WebClient webClient;
    private final TokenProvider tokenProvider;
    private final ArmadaApiProperties properties;

    public OrganizationApiClient(
            WebClient webClient,
            TokenProvider tokenProvider,
            ArmadaApiProperties properties) {
        this.webClient = webClient;
        this.tokenProvider = tokenProvider;
        this.properties = properties;
    }

    public List<ExternalOrganizationDto> fetchOrganizations() {
        log.debug("Fetching organizations from external API");

        try {
            String accessToken = tokenProvider.getAccessToken();

            ai.armada.client.organization.dto.OrganizationApiResponse response = webClient.get()
                    .uri(properties.getEndpoints().getOrganizations().getList())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(ai.armada.client.organization.dto.OrganizationApiResponse.class)
                    .doOnSuccess(r -> log.info("Received organization response - status: {}", r.status()))
                    .doOnError(e -> log.error("Failed to fetch organizations", e))
                    .block();

            if (response == null) {
                log.warn("Received null response when fetching organizations");
                return List.of();
            }

            if (!response.isSuccess()) {
                throw new OrganizationApiException(
                        "ORG_FETCH_ERROR",
                        "API returned error status: " + response.status()
                );
            }

            if (response.data() == null) {
                log.info("No organization data in response");
                return List.of();
            }

            // The API returns a single organization, wrap it in a list
            log.info("Successfully fetched organization: {}", response.data().getName());
            return List.of(response.data());

        } catch (ai.armada.client.common.security.AuthenticationException e) {
            // Re-throw authentication exceptions to be handled by AuthenticationException handler
            throw e;
        } catch (WebClientResponseException e) {
            log.error("HTTP error fetching organizations: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new OrganizationApiException(
                    "ORG_FETCH_ERROR",
                    "Failed to fetch organizations: " + e.getStatusText(),
                    e
            );
        } catch (Exception e) {
            log.error("Unexpected error fetching organizations", e);
            throw new OrganizationApiException(
                    "ORG_FETCH_ERROR",
                    "Failed to fetch organizations: " + e.getMessage(),
                    e
            );
        }
    }

}