package ai.armada.client.datapool.client;

import ai.armada.client.common.security.TokenProvider;
import ai.armada.client.config.ArmadaApiProperties;
import ai.armada.client.datapool.dto.*;
import ai.armada.client.datapool.exception.DataPoolApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Component
@Slf4j
public class DataPoolApiClient {

    private final WebClient webClient;
    private final TokenProvider tokenProvider;
    private final ArmadaApiProperties properties;

    public DataPoolApiClient(
            WebClient webClient,
            TokenProvider tokenProvider,
            ArmadaApiProperties properties) {
        this.webClient = webClient;
        this.tokenProvider = tokenProvider;
        this.properties = properties;
    }

    public List<ExternalDataPoolDto> fetchDataPools(String orgId) {
        log.debug("Fetching data pools for organization: {}", orgId);

        try {
            String accessToken = tokenProvider.getAccessToken();
            String uri = properties.getEndpoints().getOrganizations().getDataPools()
                    .replace("{orgId}", orgId);

            DataPoolApiResponse response = webClient.get()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(DataPoolApiResponse.class)
                    .doOnSuccess(r -> log.info("Received data pool response - status: {}", r.status()))
                    .doOnError(e -> log.error("Failed to fetch data pools", e))
                    .block();

            if (response == null) {
                log.warn("Received null response when fetching data pools");
                return List.of();
            }

            if (!response.isSuccess()) {
                throw new DataPoolApiException(
                        "DATAPOOL_FETCH_ERROR",
                        "API returned error status: " + response.status()
                );
            }

            if (response.data() == null) {
                log.info("No data pool data in response");
                return List.of();
            }

            log.info("Successfully fetched {} data pools", response.data().size());
            return response.data();

        } catch (ai.armada.client.common.security.AuthenticationException e) {
            // Re-throw authentication exceptions to be handled by AuthenticationException handler
            throw e;
        } catch (WebClientResponseException e) {
            log.error("HTTP error fetching data pools: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new DataPoolApiException(
                    "DATAPOOL_FETCH_ERROR",
                    "Failed to fetch data pools: " + e.getStatusText(),
                    e
            );
        } catch (Exception e) {
            log.error("Unexpected error fetching data pools", e);
            throw new DataPoolApiException(
                    "DATAPOOL_FETCH_ERROR",
                    "Failed to fetch data pools: " + e.getMessage(),
                    e
            );
        }
    }

    public ExternalDataPoolDto fetchDataPoolById(String orgId, String dataPoolId) {
        log.debug("Fetching data pool: {} for organization: {}", dataPoolId, orgId);

        try {
            String accessToken = tokenProvider.getAccessToken();
            String uri = properties.getEndpoints().getOrganizations().getDataPoolById()
                    .replace("{orgId}", orgId)
                    .replace("{dataPoolId}", dataPoolId);

            DataPoolSingleApiResponse response = webClient.get()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(DataPoolSingleApiResponse.class)
                    .doOnSuccess(r -> log.info("Received data pool response - status: {}", r.status()))
                    .doOnError(e -> log.error("Failed to fetch data pool", e))
                    .block();

            if (response == null) {
                log.warn("Received null response when fetching data pool");
                return null;
            }

            if (!response.isSuccess()) {
                throw new DataPoolApiException(
                        "DATAPOOL_FETCH_ERROR",
                        "API returned error status: " + response.status()
                );
            }

            log.info("Successfully fetched data pool: {}", dataPoolId);
            return response.data();

        } catch (ai.armada.client.common.security.AuthenticationException e) {
            // Re-throw authentication exceptions to be handled by AuthenticationException handler
            throw e;
        } catch (WebClientResponseException e) {
            log.error("HTTP error fetching data pool: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new DataPoolApiException(
                    "DATAPOOL_FETCH_ERROR",
                    "Failed to fetch data pool: " + e.getStatusText(),
                    e
            );
        } catch (Exception e) {
            log.error("Unexpected error fetching data pool", e);
            throw new DataPoolApiException(
                    "DATAPOOL_FETCH_ERROR",
                    "Failed to fetch data pool: " + e.getMessage(),
                    e
            );
        }
    }

    public DataPoolDataUsageDto fetchDataPoolUsage(String orgId, String dataPoolId, Integer billingCycles) {
        log.debug("Fetching data usage for data pool: {} with {} billing cycles", dataPoolId, billingCycles);

        try {
            String accessToken = tokenProvider.getAccessToken();
            String uri = properties.getEndpoints().getOrganizations().getDataPoolUsage()
                    .replace("{orgId}", orgId)
                    .replace("{dataPoolId}", dataPoolId);

            DataPoolUsageApiResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(uri)
                            .queryParam("billingCycles", billingCycles)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(DataPoolUsageApiResponse.class)
                    .doOnSuccess(r -> log.info("Received data pool usage response - status: {}", r.status()))
                    .doOnError(e -> log.error("Failed to fetch data pool usage", e))
                    .block();

            if (response == null) {
                log.warn("Received null response when fetching data pool usage");
                return null;
            }

            if (!response.isSuccess()) {
                throw new DataPoolApiException(
                        "DATAPOOL_USAGE_ERROR",
                        "API returned error status: " + response.status()
                );
            }

            log.info("Successfully fetched data usage for data pool: {}", dataPoolId);
            return response.data();

        } catch (ai.armada.client.common.security.AuthenticationException e) {
            // Re-throw authentication exceptions to be handled by AuthenticationException handler
            throw e;
        } catch (WebClientResponseException e) {
            log.error("HTTP error fetching data pool usage: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new DataPoolApiException(
                    "DATAPOOL_USAGE_ERROR",
                    "Failed to fetch data pool usage: " + e.getStatusText(),
                    e
            );
        } catch (Exception e) {
            log.error("Unexpected error fetching data pool usage", e);
            throw new DataPoolApiException(
                    "DATAPOOL_USAGE_ERROR",
                    "Failed to fetch data pool usage: " + e.getMessage(),
                    e
            );
        }
    }

    public DataPoolSettingsDto fetchDataPoolSettings(String orgId, String dataPoolId) {
        log.debug("Fetching settings for data pool: {}", dataPoolId);

        try {
            String accessToken = tokenProvider.getAccessToken();
            String uri = properties.getEndpoints().getOrganizations().getDataPoolSettings()
                    .replace("{orgId}", orgId)
                    .replace("{dataPoolId}", dataPoolId);

            DataPoolSettingsApiResponse response = webClient.get()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(DataPoolSettingsApiResponse.class)
                    .doOnSuccess(r -> log.info("Received data pool settings response - status: {}", r.status()))
                    .doOnError(e -> log.error("Failed to fetch data pool settings", e))
                    .block();

            if (response == null) {
                log.warn("Received null response when fetching data pool settings");
                return null;
            }

            if (!response.isSuccess()) {
                throw new DataPoolApiException(
                        "DATAPOOL_SETTINGS_ERROR",
                        "API returned error status: " + response.status()
                );
            }

            log.info("Successfully fetched settings for data pool: {}", dataPoolId);
            return response.data();

        } catch (ai.armada.client.common.security.AuthenticationException e) {
            // Re-throw authentication exceptions to be handled by AuthenticationException handler
            throw e;
        } catch (WebClientResponseException e) {
            log.error("HTTP error fetching data pool settings: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new DataPoolApiException(
                    "DATAPOOL_SETTINGS_ERROR",
                    "Failed to fetch data pool settings: " + e.getStatusText(),
                    e
            );
        } catch (Exception e) {
            log.error("Unexpected error fetching data pool settings", e);
            throw new DataPoolApiException(
                    "DATAPOOL_SETTINGS_ERROR",
                    "Failed to fetch data pool settings: " + e.getMessage(),
                    e
            );
        }
    }

}