package de.muenchen.dave;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestConstants {

    public static final String SPRING_TEST_PROFILE = "unittest";

    public static final String SPRING_NO_SECURITY_PROFILE = "no-security";

}
