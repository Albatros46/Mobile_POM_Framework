package com.servet.mobile.framework.pages;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.servet.mobile.framework.config.Settings;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.InteractsWithApps;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.appmanagement.ApplicationState;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import io.appium.java_client.support.PageFactory;

public abstract class BasePage {

    protected final AppiumDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(AppiumDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Settings.current().waitTime());
        this.wait.ignoring(StaleElementReferenceException.class);
        PageFactory.initElements(new AppiumFieldDecorator(driver, Duration.ZERO), this);
    }

    protected WebElement visible(WebElement element) {
        return wait.until(d -> {
            dismissNotResponding();
            return element.isDisplayed() ? element : null;
        });
    }

    protected WebElement visible(By locator) {
        return wait.until(d -> {
            dismissNotResponding();
            return d.findElements(locator).stream().filter(WebElement::isDisplayed).findFirst().orElse(null);
        });
    }

    protected void tap(WebElement element) {
        visible(element).click();
    }

    protected String textOf(WebElement element) {
        return visible(element).getText();
    }

    /** Waits until the list holds at least count elements, then returns it. */
    protected List<WebElement> atLeast(List<WebElement> elements, int count) {
        wait.until(d -> {
            dismissNotResponding();
            return elements.size() >= count;
        });
        return elements;
    }

    protected List<String> textsOf(List<WebElement> elements) {
        return elements.stream().map(WebElement::getText).toList();
    }

    protected boolean isAndroid() {
        return driver instanceof AndroidDriver;
    }

    /** Waits until the app is no longer in the foreground; false if it still is when the wait ends. */
    public boolean appLeftForeground(String appId) {
        try {
            wait.until(d -> ((InteractsWithApps) driver).queryAppState(appId) != ApplicationState.RUNNING_IN_FOREGROUND);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    // Android shows an "isn't responding" dialog over any app on a loaded emulator; its Wait button clears it
    private void dismissNotResponding() {
        if (!isAndroid()) {
            return;
        }
        List<WebElement> dialogs = driver.findElements(AppiumBy.id("android:id/aerr_wait"));
        if (!dialogs.isEmpty()) {
            dialogs.get(0).click();
        }
    }
}
