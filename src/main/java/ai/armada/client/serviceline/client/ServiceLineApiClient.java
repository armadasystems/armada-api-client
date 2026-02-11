package ai.armada.client.serviceline.client;

import ai.armada.client.common.security.TokenProvider;
import ai.armada.client.config.ArmadaApiProperties;
import ai.armada.client.serviceline.dto.*;
import ai.armada.client.serviceline.exception.ServiceLineApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Component
@Slf4j
public class ServiceLineApiClient {

    private final WebClient webClient;
    private final TokenProvider tokenProvider;
    private final ArmadaApiProperties properties;

    public ServiceLineApiClient(
            WebClient webClient,
            TokenProvider tokenProvider,
            ArmadaApiProperties properties) {
        this.webClient = webClient;
        this.tokenProvider = tokenProvider;
        this.properties = properties;
    }

    public List<ExternalServiceLineDto> fetchServiceLines(String orgId, String dataPoolId) {
        log.debug("Fetching service lines for data pool: {}", dataPoolId);

        try {
            String accessToken = tokenProvider.getAccessToken();
            String uri = properties.getEndpoints().getOrganizations().getServiceLines()
                    .replace("{orgId}", orgId)
                    .replace("{dataPoolId}", dataPoolId);

            ServiceLineApiResponse response = webClient.get()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(ServiceLineApiResponse.class)
                    .doOnSuccess(r -> log.info("Received service line response - status: {}", r.status()))
                    .doOnError(e -> log.error("Failed to fetch service lines", e))
                    .block();

            if (response == null) {
                log.warn("Received null response when fetching service lines");
                return List.of();
            }

            if (!response.isSuccess()) {
                throw new ServiceLineApiException(
                        "SERVICELINE_FETCH_ERROR",
                        "API returned error status: " + response.status()
                );
            }

            if (response.data() == null) {
                log.info("No service line data in response");
                return List.of();
            }

            log.info("Successfully fetched {} service lines", response.data().size());
            return response.data();

        } catch (ai.armada.client.common.security.AuthenticationException e) {
            // Re-throw authentication exceptions to be handled by AuthenticationException handler
            throw e;
        } catch (WebClientResponseException e) {
            log.error("HTTP error fetching service lines: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new ServiceLineApiException(
                    "SERVICELINE_FETCH_ERROR",
                    "Failed to fetch service lines: " + e.getStatusText(),
                    e
            );
        } catch (Exception e) {
            log.error("Unexpected error fetching service lines", e);
            throw new ServiceLineApiException(
                    "SERVICELINE_FETCH_ERROR",
                    "Failed to fetch service lines: " + e.getMessage(),
                    e
            );
        }
    }

    public ExternalServiceLineDto fetchServiceLineById(String orgId, String dataPoolId, String serviceLineId) {
        log.debug("Fetching service line: {} for data pool: {}", serviceLineId, dataPoolId);

        try {
            String accessToken = tokenProvider.getAccessToken();
            String uri = properties.getEndpoints().getOrganizations().getServiceLineById()
                    .replace("{orgId}", orgId)
                    .replace("{dataPoolId}", dataPoolId)
                    .replace("{serviceLineId}", serviceLineId);

            ServiceLineSingleApiResponse response = webClient.get()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(ServiceLineSingleApiResponse.class)
                    .doOnSuccess(r -> log.info("Received service line response - status: {}", r.status()))
                    .doOnError(e -> log.error("Failed to fetch service line", e))
                    .block();

            if (response == null) {
                log.warn("Received null response when fetching service line");
                return null;
            }

            if (!response.isSuccess()) {
                throw new ServiceLineApiException(
                        "SERVICELINE_FETCH_ERROR",
                        "API returned error status: " + response.status()
                );
            }

            log.info("Successfully fetched service line: {}", serviceLineId);
            return response.data();

        } catch (ai.armada.client.common.security.AuthenticationException e) {
            // Re-throw authentication exceptions to be handled by AuthenticationException handler
            throw e;
        } catch (WebClientResponseException e) {
            log.error("HTTP error fetching service line: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new ServiceLineApiException(
                    "SERVICELINE_FETCH_ERROR",
                    "Failed to fetch service line: " + e.getStatusText(),
                    e
            );
        } catch (Exception e) {
            log.error("Unexpected error fetching service line", e);
            throw new ServiceLineApiException(
                    "SERVICELINE_FETCH_ERROR",
                    "Failed to fetch service line: " + e.getMessage(),
                    e
            );
        }
    }

    public ServiceLineUsageDto fetchServiceLineUsage(String orgId, String dataPoolId, String serviceLineId, Integer billingCycles) {
        log.debug("Fetching usage for service line: {}", serviceLineId);

        try {
            String accessToken = tokenProvider.getAccessToken();
            String uri = properties.getEndpoints().getOrganizations().getServiceLineUsage()
                    .replace("{orgId}", orgId)
                    .replace("{dataPoolId}", dataPoolId)
                    .replace("{serviceLineId}", serviceLineId);

            ServiceLineSingleUsageApiResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(uri)
                            .queryParam("billingCycles", billingCycles)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(ServiceLineSingleUsageApiResponse.class)
                    .doOnSuccess(r -> log.info("Received service line usage response - status: {}", r.status()))
                    .doOnError(e -> log.error("Failed to fetch service line usage", e))
                    .block();

            if (response == null) {
                log.warn("Received null response when fetching service line usage");
                return null;
            }

            if (!response.isSuccess()) {
                throw new ServiceLineApiException(
                        "SERVICELINE_USAGE_ERROR",
                        "API returned error status: " + response.status()
                );
            }

            log.info("Successfully fetched usage for service line: {}", serviceLineId);
            return response.data();

        } catch (ai.armada.client.common.security.AuthenticationException e) {
            // Re-throw authentication exceptions to be handled by AuthenticationException handler
            throw e;
        } catch (WebClientResponseException e) {
            log.error("HTTP error fetching service line usage: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new ServiceLineApiException(
                    "SERVICELINE_USAGE_ERROR",
                    "Failed to fetch service line usage: " + e.getStatusText(),
                    e
            );
        } catch (Exception e) {
            log.error("Unexpected error fetching service line usage", e);
            throw new ServiceLineApiException(
                    "SERVICELINE_USAGE_ERROR",
                    "Failed to fetch service line usage: " + e.getMessage(),
                    e
            );
        }
    }

    public ServiceLinesUsageDto fetchAllServiceLinesUsage(String orgId, String dataPoolId, Integer billingCycles) {
        log.debug("Fetching usage for all service lines in data pool: {}", dataPoolId);

        try {
            String accessToken = tokenProvider.getAccessToken();
            String uri = properties.getEndpoints().getOrganizations().getAllServiceLinesUsage()
                    .replace("{orgId}", orgId)
                    .replace("{dataPoolId}", dataPoolId);

            ServiceLinesUsageApiResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(uri)
                            .queryParam("billingCycles", billingCycles)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(ServiceLinesUsageApiResponse.class)
                    .doOnSuccess(r -> log.info("Received service lines usage response - status: {}", r.status()))
                    .doOnError(e -> log.error("Failed to fetch service lines usage", e))
                    .block();

            if (response == null) {
                log.warn("Received null response when fetching service lines usage");
                return null;
            }

            if (!response.isSuccess()) {
                throw new ServiceLineApiException(
                        "SERVICELINE_USAGE_ERROR",
                        "API returned error status: " + response.status()
                );
            }

            log.info("Successfully fetched usage for all service lines");
            return response.data();

        } catch (ai.armada.client.common.security.AuthenticationException e) {
            // Re-throw authentication exceptions to be handled by AuthenticationException handler
            throw e;
        } catch (WebClientResponseException e) {
            log.error("HTTP error fetching all service lines usage: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new ServiceLineApiException(
                    "SERVICELINE_USAGE_ERROR",
                    "Failed to fetch service lines usage: " + e.getStatusText(),
                    e
            );
        } catch (Exception e) {
            log.error("Unexpected error fetching service lines usage", e);
            throw new ServiceLineApiException(
                    "SERVICELINE_USAGE_ERROR",
                    "Failed to fetch service lines usage: " + e.getMessage(),
                    e
            );
        }
    }

    public ServiceLineSettingsDto fetchServiceLineSettings(String orgId, String dataPoolId, String serviceLineId) {
        log.debug("Fetching settings for service line: {}", serviceLineId);

        try {
            String accessToken = tokenProvider.getAccessToken();
            String uri = properties.getEndpoints().getOrganizations().getServiceLineSettings()
                    .replace("{orgId}", orgId)
                    .replace("{dataPoolId}", dataPoolId)
                    .replace("{serviceLineId}", serviceLineId);

            ServiceLineSettingsApiResponse response = webClient.get()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(ServiceLineSettingsApiResponse.class)
                    .doOnSuccess(r -> log.info("Received service line settings response - status: {}", r.status()))
                    .doOnError(e -> log.error("Failed to fetch service line settings", e))
                    .block();

            if (response == null) {
                log.warn("Received null response when fetching service line settings");
                return null;
            }

            if (!response.isSuccess()) {
                throw new ServiceLineApiException(
                        "SERVICELINE_SETTINGS_ERROR",
                        "API returned error status: " + response.status()
                );
            }

            log.info("Successfully fetched settings for service line: {}", serviceLineId);
            return response.data();

        } catch (ai.armada.client.common.security.AuthenticationException e) {
            // Re-throw authentication exceptions to be handled by AuthenticationException handler
            throw e;
        } catch (WebClientResponseException e) {
            log.error("HTTP error fetching service line settings: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new ServiceLineApiException(
                    "SERVICELINE_SETTINGS_ERROR",
                    "Failed to fetch service line settings: " + e.getStatusText(),
                    e
            );
        } catch (Exception e) {
            log.error("Unexpected error fetching service line settings", e);
            throw new ServiceLineApiException(
                    "SERVICELINE_SETTINGS_ERROR",
                    "Failed to fetch service line settings: " + e.getMessage(),
                    e
            );
        }
    }

    public List<ServiceLineSettingsDto> fetchAllServiceLinesSettings(String orgId, String dataPoolId) {
        log.debug("Fetching settings for all service lines in data pool: {}", dataPoolId);

        try {
            String accessToken = tokenProvider.getAccessToken();
            String uri = properties.getEndpoints().getOrganizations().getAllServiceLinesSettings()
                    .replace("{orgId}", orgId)
                    .replace("{dataPoolId}", dataPoolId);

            ServiceLineSettingsListApiResponse response = webClient.get()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(ServiceLineSettingsListApiResponse.class)
                    .doOnSuccess(r -> log.info("Received service lines settings response - status: {}", r.status()))
                    .doOnError(e -> log.error("Failed to fetch service lines settings", e))
                    .block();

            if (response == null) {
                log.warn("Received null response when fetching service lines settings");
                return List.of();
            }

            if (!response.isSuccess()) {
                throw new ServiceLineApiException(
                        "SERVICELINE_SETTINGS_ERROR",
                        "API returned error status: " + response.status()
                );
            }

            if (response.data() == null) {
                log.info("No service line settings data in response");
                return List.of();
            }

            log.info("Successfully fetched settings for {} service lines", response.data().size());
            return response.data();

        } catch (ai.armada.client.common.security.AuthenticationException e) {
            // Re-throw authentication exceptions to be handled by AuthenticationException handler
            throw e;
        } catch (WebClientResponseException e) {
            log.error("HTTP error fetching service lines settings: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new ServiceLineApiException(
                    "SERVICELINE_SETTINGS_ERROR",
                    "Failed to fetch service lines settings: " + e.getStatusText(),
                    e
            );
        } catch (Exception e) {
            log.error("Unexpected error fetching service lines settings", e);
            throw new ServiceLineApiException(
                    "SERVICELINE_SETTINGS_ERROR",
                    "Failed to fetch service lines settings: " + e.getMessage(),
                    e
            );
        }
    }

}