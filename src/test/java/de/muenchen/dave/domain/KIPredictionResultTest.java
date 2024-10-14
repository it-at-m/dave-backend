package de.muenchen.dave.domain;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KIPredictionResultTest {

    @Test
    void testFromArray() {
        // Arrange
        long[] tagessummen = { 6 };

        // Act
        KIPredictionResult result = KIPredictionResult.fromArray(tagessummen);

        // Assert
        assertThat(result, hasProperty("radTagessumme", equalTo(6)));
    }

    @Test()
    void testFromArrayWrongSize() {
        // Arrange
        long[] tagessummen = { 1, 2, 3, 4, 5, 6, 7 };

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> KIPredictionResult.fromArray(tagessummen));

        String expectedMessage = "Incorrect amount of elements provided. Array must contain 1 element.";
        String actualMessage = exception.getMessage();

        assertThat(actualMessage, equalTo(expectedMessage));
    }

}
