package ai.armada.client.organization.mapper;

import ai.armada.client.organization.dto.ExternalOrganizationDto;
import ai.armada.client.organization.dto.OrganizationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrganizationMapperTest {

    private OrganizationMapper organizationMapper;

    @BeforeEach
    void setUp() {
        organizationMapper = new OrganizationMapper();
    }

    @Test
    void toDto_WhenValidExternalOrganization_ShouldMapCorrectly() {
        // Arrange
        ExternalOrganizationDto external = new ExternalOrganizationDto();
        external.setId("org-123");
        external.setName("Test Organization");
        external.setDisplayName("Test Org");

        // Act
        OrganizationDto result = organizationMapper.toDto(external);

        // Assert
        assertNotNull(result);
        assertEquals("org-123", result.id());
        assertEquals("Test Org", result.displayName());
    }

    @Test
    void toDto_WhenExternalOrgWithNullDisplayName_ShouldMapWithNull() {
        // Arrange
        ExternalOrganizationDto external = new ExternalOrganizationDto();
        external.setId("org-456");
        external.setName("Another Organization");
        external.setDisplayName(null);

        // Act
        OrganizationDto result = organizationMapper.toDto(external);

        // Assert
        assertNotNull(result);
        assertEquals("org-456", result.id());
        assertNull(result.displayName());
    }

    @Test
    void toDto_WhenExternalOrgWithEmptyDisplayName_ShouldMapWithEmpty() {
        // Arrange
        ExternalOrganizationDto external = new ExternalOrganizationDto();
        external.setId("org-789");
        external.setName("Empty Display Organization");
        external.setDisplayName("");

        // Act
        OrganizationDto result = organizationMapper.toDto(external);

        // Assert
        assertNotNull(result);
        assertEquals("org-789", result.id());
        assertEquals("", result.displayName());
    }
}