package com.servet.mobile.framework.testing;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

import com.servet.mobile.framework.config.Platform;
import com.servet.mobile.framework.config.Settings;
import com.servet.mobile.framework.driver.Session;
import com.servet.mobile.framework.evidence.FailureEvidence;

import io.appium.java_client.AppiumDriver;

/** One fresh app session per test on the platform given by -Dplatform. */
@Listeners(FailureEvidence.class)
public abstract class BaseMobileTest {

    @BeforeMethod(alwaysRun = true)
    public void startSession() {
        Session.start(Platform.current(), Settings.current());
    }

    @AfterMethod(alwaysRun = true)
    public void endSession() {
        Session.quit();
    }

    protected AppiumDriver driver() {
        return Session.get();
    }
}
