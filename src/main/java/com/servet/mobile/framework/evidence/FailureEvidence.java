package com.servet.mobile.framework.evidence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import org.openqa.selenium.OutputType;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.Reporter;

import com.servet.mobile.framework.config.Platform;
import com.servet.mobile.framework.driver.Session;

import io.appium.java_client.AppiumDriver;

/** Saves a screenshot and the page source of a failed test: the page source shows the locators to fix. */
public final class FailureEvidence implements ITestListener {

    private static final Path ROOT = Path.of("target", "evidence");

    @Override
    public void onTestFailure(ITestResult result) {
        if (!Session.isActive()) {
            return;
        }
        String name = result.getTestClass().getRealClass().getSimpleName() + "." + result.getName();
        try {
            AppiumDriver driver = Session.get();
            Path folder = Files.createDirectories(
                    ROOT.resolve(Platform.current().name().toLowerCase(Locale.ROOT)));
            Files.write(folder.resolve(name + ".png"), driver.getScreenshotAs(OutputType.BYTES));
            Files.writeString(folder.resolve(name + ".xml"), driver.getPageSource(), StandardCharsets.UTF_8);
        } catch (IOException | RuntimeException e) {
            // evidence must never hide the test failure that is being reported
            Reporter.log("Could not save evidence for " + name + ": " + e.getMessage(), true);
        }
    }
}
