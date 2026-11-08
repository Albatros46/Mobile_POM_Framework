package com.servet.mobile.framework.driver;

import com.servet.mobile.framework.config.Platform;
import com.servet.mobile.framework.config.Settings;
import org.openqa.selenium.Capabilities;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;

public class DriverFactoryTest {

    private static final String[] KEYS = {"android.app", "ios.app", "ios.version"};
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
    public void testAndroidCapabilities() throws Exception {
        Path tempApp = Files.createTempFile("test-android", ".apk");
        System.setProperty("android.app", tempApp.toAbsolutePath().toString());
        Settings settings = Settings.current();

        Capabilities caps = DriverFactory.options(Platform.ANDROID, settings);

        assertThat(caps.getPlatformName()).isEqualTo(org.openqa.selenium.Platform.ANDROID);
        assertThat(String.valueOf(caps.getCapability("appium:automationName"))).isEqualToIgnoringCase("UiAutomator2");
        assertThat(caps.getCapability("appium:appPackage")).isEqualTo("com.saucelabs.mydemoapp.android");
        assertThat(caps.getCapability("appium:noReset")).isEqualTo(false);
        assertThat(caps.getCapability("appium:app")).isEqualTo(tempApp.toAbsolutePath().toString());
    }

    @Test(groups = "unit")
    public void testIosCapabilities() throws Exception {
        Path tempApp = Files.createTempFile("test-ios", ".app");
        System.setProperty("ios.app", tempApp.toAbsolutePath().toString());
        System.setProperty("ios.version", "18.2");
        Settings settings = Settings.current();

        Capabilities caps = DriverFactory.options(Platform.IOS, settings);

        assertThat(caps.getPlatformName()).isEqualTo(org.openqa.selenium.Platform.IOS);
        assertThat(String.valueOf(caps.getCapability("appium:automationName"))).isEqualToIgnoringCase("XCUITest");
        assertThat(caps.getCapability("appium:deviceName")).isEqualTo("iPhone 16");
        assertThat(caps.getCapability("appium:platformVersion")).isEqualTo("18.2");
        assertThat(((Number) caps.getCapability("appium:wdaStartupRetries")).intValue()).isEqualTo(2);
        assertThat(((Number) caps.getCapability("appium:wdaStartupRetryInterval")).intValue()).isEqualTo(20_000);
    }

    @Test(groups = "unit")
    public void testMissingFileThrowsException() {
        System.setProperty("android.app", "/non/existent/path");
        Settings settings = Settings.current();

        assertThatThrownBy(() -> DriverFactory.options(Platform.ANDROID, settings))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("download-apps");
    }
}
