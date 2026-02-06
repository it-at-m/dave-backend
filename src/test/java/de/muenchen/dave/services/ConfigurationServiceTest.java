package de.muenchen.dave.services;

import de.muenchen.dave.domain.ConfigurationEntity;
import de.muenchen.dave.domain.enums.ConfigDataTypes;
import de.muenchen.dave.repositories.relationaldb.ConfigurationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ConfigurationServiceTest {

    @Mock
    private ConfigurationRepository repository;

    @InjectMocks
    private ConfigurationService configurationService;

    @Test
    void testTypeCorrectness_ValidInteger_ShouldNotThrow() {
        ConfigurationEntity config = ConfigurationEntity.builder()
                .keyname("test_key")
                .valuefield("123")
                .category("test")
                .datatype(ConfigDataTypes.INTEGER)
                .build();

        assertDoesNotThrow(() -> configurationService.saveOrUpdate(config));
    }

    @Test
    void testTypeCorrectness_InvalidInteger_ShouldThrowException() {
        ConfigurationEntity config = ConfigurationEntity.builder()
                .keyname("test_key")
                .valuefield("not_a_number")
                .category("test")
                .datatype(ConfigDataTypes.INTEGER)
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> configurationService.saveOrUpdate(config));
        assert(exception.getMessage().contains("is not a valid INTEGER"));
    }

    @Test
    void testTypeCorrectness_ValidDouble_ShouldNotThrow() {
        ConfigurationEntity config = ConfigurationEntity.builder()
                .keyname("test_key")
                .valuefield("123.45")
                .category("test")
                .datatype(ConfigDataTypes.DOUBLE)
                .build();

        assertDoesNotThrow(() -> configurationService.saveOrUpdate(config));
    }

    @Test
    void testTypeCorrectness_InvalidDouble_ShouldThrowException() {
        ConfigurationEntity config = ConfigurationEntity.builder()
                .keyname("test_key")
                .valuefield("not_a_double")
                .category("test")
                .datatype(ConfigDataTypes.DOUBLE)
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> configurationService.saveOrUpdate(config));
        assert(exception.getMessage().contains("is not a valid DOUBLE"));
    }

    @Test
    void testTypeCorrectness_ValidBooleanTrue_ShouldNotThrow() {
        ConfigurationEntity config = ConfigurationEntity.builder()
                .keyname("test_key")
                .valuefield("true")
                .category("test")
                .datatype(ConfigDataTypes.BOOLEAN)
                .build();

        assertDoesNotThrow(() -> configurationService.saveOrUpdate(config));
    }

    @Test
    void testTypeCorrectness_ValidBooleanFalse_ShouldNotThrow() {
        ConfigurationEntity config = ConfigurationEntity.builder()
                .keyname("test_key")
                .valuefield("false")
                .category("test")
                .datatype(ConfigDataTypes.BOOLEAN)
                .build();

        assertDoesNotThrow(() -> configurationService.saveOrUpdate(config));
    }

    @Test
    void testTypeCorrectness_ValidBooleanCaseInsensitive_ShouldNotThrow() {
        ConfigurationEntity config = ConfigurationEntity.builder()
                .keyname("test_key")
                .valuefield("TRUE")
                .category("test")
                .datatype(ConfigDataTypes.BOOLEAN)
                .build();

        assertDoesNotThrow(() -> configurationService.saveOrUpdate(config));
    }

    @Test
    void testTypeCorrectness_InvalidBoolean_ShouldThrowException() {
        ConfigurationEntity config = ConfigurationEntity.builder()
                .keyname("test_key")
                .valuefield("not_a_boolean")
                .category("test")
                .datatype(ConfigDataTypes.BOOLEAN)
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> configurationService.saveOrUpdate(config));
        assert(exception.getMessage().contains("is not a valid BOOLEAN"));
    }

    @Test
    void testTypeCorrectness_StringType_ShouldAlwaysPass() {
        ConfigurationEntity config = ConfigurationEntity.builder()
                .keyname("test_key")
                .valuefield("any string value")
                .category("test")
                .datatype(ConfigDataTypes.STRING)
                .build();

        assertDoesNotThrow(() -> configurationService.saveOrUpdate(config));
    }
}