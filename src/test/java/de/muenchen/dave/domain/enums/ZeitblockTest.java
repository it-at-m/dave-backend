package de.muenchen.dave.domain.enums;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

class ZeitblockTest {

    @Test
    void isThisZeitblockWithinZeitblockToCompare() {
        // givenZeitblock encompasses whole day -> inner block should be within
        assertThat(Zeitblock.ZB_06_10.isThisZeitblockWithinZeitblockToCompare(Zeitblock.ZB_00_24), is(true));

        // identical blocks -> within
        assertThat(Zeitblock.ZB_06_10.isThisZeitblockWithinZeitblockToCompare(Zeitblock.ZB_06_10), is(true));

        // block starting before given start -> not within
        assertThat(Zeitblock.ZB_00_06.isThisZeitblockWithinZeitblockToCompare(Zeitblock.ZB_06_10), is(false));

        // block ending after given end -> not within
        assertThat(Zeitblock.ZB_19_24.isThisZeitblockWithinZeitblockToCompare(Zeitblock.ZB_06_19), is(false));

        // null parameters -> false
        assertThat(Zeitblock.ZB_06_10.isThisZeitblockWithinZeitblockToCompare(null), is(false));
    }
}
