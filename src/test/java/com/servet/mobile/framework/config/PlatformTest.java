package com.servet.mobile.framework.config;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

public class PlatformTest {

    private static final String KEY = "platform";
    private final Map<String, String> saved = new HashMap<>();

    @BeforeMethod(alwaysRun = true)
    public void remember() {
        saved.clear();
        saved.put(KEY, System.getProperty(KEY));
    }

    @AfterMethod(alwaysRun = true)
    public void restore() {
        saved.forEach((key, value) -> {
            if (value == null) {
                System.clearProperty(key);
            } else {
                System.setProperty(key, value);
            }
        });
    }

    @Test(groups = "unit")
    public void testDefaultIsAndroid() {
        System.clearProperty("platform");
        assertThat(Platform.current()).isEqualTo(Platform.ANDROID);
    }

    @Test(groups = "unit")
    public void testParsingIos() {
        System.setProperty("platform", "ios");
        assertThat(Platform.current()).isEqualTo(Platform.IOS);

        System.setProperty("platform", "IOS");
        assertThat(Platform.current()).isEqualTo(Platform.IOS);

        System.setProperty("platform", "  ios  ");
        assertThat(Platform.current()).isEqualTo(Platform.IOS);
    }

    @Test(groups = "unit")
    public void testUnknownValueThrowsException() {
        System.setProperty("platform", "windows");
        assertThatThrownBy(Platform::current)
                .isInstanceOf(IllegalArgumentException.class)
                .satisfies(e -> assertThat(e.getMessage().toLowerCase(Locale.ROOT))
                        .contains("android")
                        .contains("ios"));
    }
}
