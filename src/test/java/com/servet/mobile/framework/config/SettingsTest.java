package com.servet.mobile.framework.config;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

public class SettingsTest {

    private static final String[] KEYS = {
            "appium.url", "wait.seconds", "android.app", "ios.app", "ios.device", "ios.version"
    };
    private final Map<String, String> saved = new HashMap<>();

    @BeforeMethod(alwaysRun = true)
    public void remember() {
        saved.clear();
        for (String key : KEYS) {
            saved.put(key, System.getProperty(key));
        }
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
    public void testDefaults() {
        for (String key : KEYS) {
            System.clearProperty(key);
        }
        Settings settings = Settings.current();
        assertThat(settings.appiumUrl()).isEqualTo(URI.create("http://127.0.0.1:4723"));
        assertThat(settings.waitTime()).isEqualTo(Duration.ofSeconds(30));
        assertThat(settings.androidApp()).isEqualTo(Path.of("apps/mda-2.3.0-27.apk").toAbsolutePath());
        assertThat(settings.iosApp()).isEqualTo(Path.of("apps/SauceLabs-Demo-App.app").toAbsolutePath());
        assertThat(settings.iosDevice()).isEqualTo("iPhone 16");
        assertThat(settings.iosVersion()).isEmpty();
    }

    @Test(groups = "unit")
    public void testOverrides() {
        System.setProperty("appium.url", "http://192.168.1.1:4444");
        System.setProperty("wait.seconds", "10");
        System.setProperty("ios.device", "iPad Pro");
        System.setProperty("ios.version", "17.0");

        Settings settings = Settings.current();
        assertThat(settings.appiumUrl()).isEqualTo(URI.create("http://192.168.1.1:4444"));
        assertThat(settings.waitTime()).isEqualTo(Duration.ofSeconds(10));
        assertThat(settings.iosDevice()).isEqualTo("iPad Pro");
        assertThat(settings.iosVersion()).isEqualTo("17.0");
    }

    @Test(groups = "unit")
    public void testBlankIosVersionIsEmpty() {
        System.setProperty("ios.version", "  ");
        Settings settings = Settings.current();
        assertThat(settings.iosVersion()).isEmpty();
    }
}
