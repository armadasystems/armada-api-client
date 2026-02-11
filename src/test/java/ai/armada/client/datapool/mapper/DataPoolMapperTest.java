package ai.armada.client.datapool.mapper;

import ai.armada.client.datapool.dto.DataPoolDto;
import ai.armada.client.datapool.dto.ExternalDataPoolDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DataPoolMapperTest {

    private DataPoolMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DataPoolMapper();
    }

    @Test
    void toDto_WhenValidExternalDto_ShouldMapToInternalDto() {
        // Arrange
        ExternalDataPoolDto externalDto = new ExternalDataPoolDto(
                "dp-001",
                "Test Data Pool",
                "USA",
                "Premium",
                "Active",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                500.0f,
                250.0f,
                10
        );

        // Act
        DataPoolDto result = mapper.toDto(externalDto);

        // Assert
        assertNotNull(result);
        assertEquals("dp-001", result.id());
        assertEquals("Test Data Pool", result.name());
        assertEquals("USA", result.country());
        assertEquals("Premium", result.planType());
        assertEquals("Active", result.status());
        assertEquals(LocalDate.of(2024, 1, 1), result.startDate());
        assertEquals(LocalDate.of(2024, 12, 31), result.endDate());
        assertEquals(500.0f, result.dataAvailableGB());
        assertEquals(250.0f, result.dataUsedGB());
        assertEquals(10, result.totalServiceLines());
    }

    @Test
    void toDto_WhenExternalDtoWithNullValues_ShouldMapNullValues() {
        // Arrange
        ExternalDataPoolDto externalDto = new ExternalDataPoolDto(
                "dp-002",
                "Test Data Pool 2",
                null,
                null,
                "Inactive",
                null,
                null,
                null,
                null,
                null
        );

        // Act
        DataPoolDto result = mapper.toDto(externalDto);

        // Assert
        assertNotNull(result);
        assertEquals("dp-002", result.id());
        assertEquals("Test Data Pool 2", result.name());
        assertNull(result.country());
        assertNull(result.planType());
        assertEquals("Inactive", result.status());
        assertNull(result.startDate());
        assertNull(result.endDate());
        assertNull(result.dataAvailableGB());
        assertNull(result.dataUsedGB());
        assertNull(result.totalServiceLines());
    }

    @Test
    void toDto_WhenExternalDtoWithZeroValues_ShouldMapZeroValues() {
        // Arrange
        ExternalDataPoolDto externalDto = new ExternalDataPoolDto(
                "dp-003",
                "Empty Data Pool",
                "CAN",
                "Standard",
                "Active",
                LocalDate.of(2024, 6, 1),
                LocalDate.of(2024, 12, 31),
                0.0f,
                0.0f,
                0
        );

        // Act
        DataPoolDto result = mapper.toDto(externalDto);

        // Assert
        assertNotNull(result);
        assertEquals("dp-003", result.id());
        assertEquals("Empty Data Pool", result.name());
        assertEquals(0.0f, result.dataAvailableGB());
        assertEquals(0.0f, result.dataUsedGB());
        assertEquals(0, result.totalServiceLines());
    }

    @Test
    void toDto_WhenMultipleExternalDtos_ShouldMapEachIndependently() {
        // Arrange
        ExternalDataPoolDto external1 = new ExternalDataPoolDto(
                "dp-001",
                "Pool 1",
                "USA",
                "Premium",
                "Active",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                100.0f,
                50.0f,
                5
        );

        ExternalDataPoolDto external2 = new ExternalDataPoolDto(
                "dp-002",
                "Pool 2",
                "CAN",
                "Standard",
                "Active",
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 12, 31),
                200.0f,
                75.0f,
                10
        );

        // Act
        DataPoolDto result1 = mapper.toDto(external1);
        DataPoolDto result2 = mapper.toDto(external2);

        // Assert
        assertNotEquals(result1.id(), result2.id());
        assertEquals("dp-001", result1.id());
        assertEquals("dp-002", result2.id());
        assertEquals("Pool 1", result1.name());
        assertEquals("Pool 2", result2.name());
    }
}
