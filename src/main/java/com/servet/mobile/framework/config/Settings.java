package com.servet.mobile.framework.config;

import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;

/** Run settings from -D system properties, each with a default that works on the project's own setup. */
public record Settings(
        URI appiumUrl,
        Duration waitTime,
        Path androidApp,
        Path iosApp,
        String iosDevice,
        String iosVersion) {

    public static Settings current() {
        return new Settings(
                URI.create(property("appium.url", "http://127.0.0.1:4723")),
                Duration.ofSeconds(Long.parseLong(property("wait.seconds", "30"))),
                Path.of(property("android.app", "apps/mda-2.3.0-27.apk")).toAbsolutePath(),
                Path.of(property("ios.app", "apps/SauceLabs-Demo-App.app")).toAbsolutePath(),
                property("ios.device", "iPhone 16"),
                property("ios.version", "")); // empty: Appium picks the newest runtime
    }

    private static String property(String name, String fallback) {
        return System.getProperty(name, fallback).trim();
    }
}
