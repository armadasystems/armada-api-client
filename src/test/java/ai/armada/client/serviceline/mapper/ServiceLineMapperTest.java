package ai.armada.client.serviceline.mapper;

import ai.armada.client.serviceline.dto.ExternalServiceLineDto;
import ai.armada.client.serviceline.dto.ServiceLineDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServiceLineMapperTest {

    private ServiceLineMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ServiceLineMapper();
    }

    @Test
    void toDto_WhenValidExternalDto_ShouldMapToInternalDto() {
        // Arrange
        ExternalServiceLineDto externalDto = new ExternalServiceLineDto(
                "sl-001",
                "Test Service Line",
                "555-1234",
                "Active",
                LocalDate.of(2024, 1, 15),
                List.of("KIT001", "KIT002", "KIT003")
        );

        // Act
        ServiceLineDto result = mapper.toDto(externalDto);

        // Assert
        assertNotNull(result);
        assertEquals("sl-001", result.id());
        assertEquals("Test Service Line", result.serviceLineName());
        assertEquals("555-1234", result.serviceLineNumber());
        assertEquals("Active", result.status());
        assertEquals(LocalDate.of(2024, 1, 15), result.activationDate());
        assertNotNull(result.kitNumbers());
        assertEquals(3, result.kitNumbers().size());
        assertEquals("KIT001", result.kitNumbers().get(0));
        assertEquals("KIT002", result.kitNumbers().get(1));
        assertEquals("KIT003", result.kitNumbers().get(2));
    }

    @Test
    void toDto_WhenExternalDtoWithNullValues_ShouldMapNullValues() {
        // Arrange
        ExternalServiceLineDto externalDto = new ExternalServiceLineDto(
                "sl-002",
                "Service Line 2",
                null,
                "Inactive",
                null,
                null
        );

        // Act
        ServiceLineDto result = mapper.toDto(externalDto);

        // Assert
        assertNotNull(result);
        assertEquals("sl-002", result.id());
        assertEquals("Service Line 2", result.serviceLineName());
        assertNull(result.serviceLineNumber());
        assertEquals("Inactive", result.status());
        assertNull(result.activationDate());
        assertNull(result.kitNumbers());
    }

    @Test
    void toDto_WhenExternalDtoWithEmptyKitNumbers_ShouldMapEmptyList() {
        // Arrange
        ExternalServiceLineDto externalDto = new ExternalServiceLineDto(
                "sl-003",
                "Service Line 3",
                "555-5555",
                "Pending",
                LocalDate.of(2024, 6, 1),
                List.of()
        );

        // Act
        ServiceLineDto result = mapper.toDto(externalDto);

        // Assert
        assertNotNull(result);
        assertEquals("sl-003", result.id());
        assertEquals("Service Line 3", result.serviceLineName());
        assertNotNull(result.kitNumbers());
        assertTrue(result.kitNumbers().isEmpty());
    }

    @Test
    void toDto_WhenMultipleExternalDtos_ShouldMapEachIndependently() {
        // Arrange
        ExternalServiceLineDto external1 = new ExternalServiceLineDto(
                "sl-001",
                "Line 1",
                "555-0001",
                "Active",
                LocalDate.of(2024, 1, 1),
                List.of("KIT001")
        );

        ExternalServiceLineDto external2 = new ExternalServiceLineDto(
                "sl-002",
                "Line 2",
                "555-0002",
                "Active",
                LocalDate.of(2024, 2, 1),
                List.of("KIT002", "KIT003")
        );

        // Act
        ServiceLineDto result1 = mapper.toDto(external1);
        ServiceLineDto result2 = mapper.toDto(external2);

        // Assert
        assertNotEquals(result1.id(), result2.id());
        assertEquals("sl-001", result1.id());
        assertEquals("sl-002", result2.id());
        assertEquals("Line 1", result1.serviceLineName());
        assertEquals("Line 2", result2.serviceLineName());
        assertEquals(1, result1.kitNumbers().size());
        assertEquals(2, result2.kitNumbers().size());
    }

    @Test
    void toDto_WhenExternalDtoWithSingleKitNumber_ShouldMapCorrectly() {
        // Arrange
        ExternalServiceLineDto externalDto = new ExternalServiceLineDto(
                "sl-004",
                "Single Kit Line",
                "555-9999",
                "Active",
                LocalDate.of(2024, 3, 15),
                List.of("SINGLEKIT")
        );

        // Act
        ServiceLineDto result = mapper.toDto(externalDto);

        // Assert
        assertNotNull(result);
        assertEquals("sl-004", result.id());
        assertEquals(1, result.kitNumbers().size());
        assertEquals("SINGLEKIT", result.kitNumbers().get(0));
    }

    @Test
    void toDto_WhenDifferentStatuses_ShouldMapStatusCorrectly() {
        // Arrange
        String[] statuses = {"Active", "Inactive", "Pending", "Suspended", "Cancelled"};

        for (String status : statuses) {
            ExternalServiceLineDto externalDto = new ExternalServiceLineDto(
                    "sl-test",
                    "Test Line",
                    "555-0000",
                    status,
                    LocalDate.now(),
                    List.of()
            );

            // Act
            ServiceLineDto result = mapper.toDto(externalDto);

            // Assert
            assertEquals(status, result.status());
        }
    }
}
