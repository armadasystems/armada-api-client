package ai.armada.client.datapool.mapper;

import ai.armada.client.datapool.dto.DataPoolDto;
import ai.armada.client.datapool.dto.ExternalDataPoolDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DataPoolMapper {

    public DataPoolDto toDto(ExternalDataPoolDto external) {
        log.debug("Mapping external data pool: {} to internal DTO", external.id());
        
        return new DataPoolDto(
                external.id(),
                external.name(),
                external.country(),
                external.planType(),
                external.status(),
                external.startDate(),
                external.endDate(),
                external.dataAvailableGB(),
                external.dataUsedGB(),
                external.totalServiceLines()
        );
    }
}