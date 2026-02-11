package ai.armada.client.serviceline.mapper;

import ai.armada.client.serviceline.dto.ExternalServiceLineDto;
import ai.armada.client.serviceline.dto.ServiceLineDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ServiceLineMapper {

    public ServiceLineDto toDto(ExternalServiceLineDto external) {
        log.debug("Mapping external service line: {} to internal DTO", external.id());
        
        return new ServiceLineDto(
                external.id(),
                external.serviceLineName(),
                external.serviceLineNumber(),
                external.status(),
                external.activationDate(),
                external.kitNumbers()
        );
    }
}