package com.servet.mobile.framework.config;

import java.util.Arrays;
import java.util.Locale;

public enum Platform {
    ANDROID, IOS;

    /** The platform named by -Dplatform, case-insensitive; Android when the property is not set. */
    public static Platform current() {
        String value = System.getProperty("platform", "android").trim();
        for (Platform platform : values()) {
            if (platform.name().equalsIgnoreCase(value)) {
                return platform;
            }
        }
        throw new IllegalArgumentException("Unknown platform '" + value + "'; allowed values: "
                + Arrays.stream(values()).map(p -> p.name().toLowerCase(Locale.ROOT)).toList());
    }
}
