package com.servet.mobile.framework.driver;

import com.servet.mobile.framework.config.Platform;
import com.servet.mobile.framework.config.Settings;

import io.appium.java_client.AppiumDriver;

/** The Appium session of the current thread; one per thread so tests can later run in parallel. */
public final class Session {

    private static final ThreadLocal<AppiumDriver> CURRENT = new ThreadLocal<>();

    private Session() {
    }

    public static void start(Platform platform, Settings settings) {
        CURRENT.set(DriverFactory.create(platform, settings));
    }

    public static AppiumDriver get() {
        AppiumDriver driver = CURRENT.get();
        if (driver == null) {
            throw new IllegalStateException("No Appium session on this thread: start one first");
        }
        return driver;
    }

    public static boolean isActive() {
        return CURRENT.get() != null;
    }

    public static void quit() {
        AppiumDriver driver = CURRENT.get();
        CURRENT.remove();
        if (driver != null) {
            driver.quit();
        }
    }
}
