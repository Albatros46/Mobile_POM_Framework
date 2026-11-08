package com.servet.mobile.framework.driver;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import org.openqa.selenium.Capabilities;

import com.servet.mobile.framework.config.Platform;
import com.servet.mobile.framework.config.Settings;
import com.servet.mobile.mydemoapp.MyDemoApp;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;

public final class DriverFactory {

    private static final Duration NEW_COMMAND_TIMEOUT = Duration.ofSeconds(120);
    private static final String DOWNLOAD_HINT = "run scripts/download-apps.ps1 (Windows) or scripts/download-apps.sh";

    private DriverFactory() {
    }

    public static AppiumDriver create(Platform platform, Settings settings) {
        URL server = serverUrl(settings);
        Capabilities capabilities = options(platform, settings);
        return switch (platform) {
            case ANDROID -> new AndroidDriver(server, capabilities);
            case IOS -> new IOSDriver(server, capabilities);
        };
    }

    static Capabilities options(Platform platform, Settings settings) {
        return switch (platform) {
            case ANDROID -> androidOptions(settings);
            case IOS -> iosOptions(settings);
        };
    }

    private static UiAutomator2Options androidOptions(Settings settings) {
        Path app = requireApp(settings.androidApp());
        return new UiAutomator2Options()
                .setApp(app.toString())
                .setAppPackage(MyDemoApp.ANDROID_PACKAGE)
                .setAppActivity(".view.activities.SplashActivity")
                .setAppWaitActivity("*.MainActivity")
                .setNoReset(false)
                .setAutoGrantPermissions(true)
                .setNewCommandTimeout(NEW_COMMAND_TIMEOUT)
                .setUiautomator2ServerInstallTimeout(Duration.ofMinutes(3))
                .setAdbExecTimeout(Duration.ofMinutes(1));
    }

    private static XCUITestOptions iosOptions(Settings settings) {
        Path app = requireApp(settings.iosApp());
        XCUITestOptions options = new XCUITestOptions()
                .setApp(app.toString())
                .setDeviceName(settings.iosDevice())
                .setNoReset(false)
                .setWdaLaunchTimeout(Duration.ofMinutes(3))
                .setNewCommandTimeout(NEW_COMMAND_TIMEOUT);
        if (!settings.iosVersion().isEmpty()) {
            options.setPlatformVersion(settings.iosVersion());
        }
        return options;
    }

    private static Path requireApp(Path app) {
        if (!Files.exists(app)) {
            throw new IllegalStateException(app + " is missing: " + DOWNLOAD_HINT);
        }
        return app;
    }

    private static URL serverUrl(Settings settings) {
        try {
            return settings.appiumUrl().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("appium.url is not a URL: " + settings.appiumUrl(), e);
        }
    }
}
