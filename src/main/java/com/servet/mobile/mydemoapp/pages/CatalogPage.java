package com.servet.mobile.mydemoapp.pages;

import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebElement;

import com.servet.mobile.framework.pages.BasePage;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;

public final class CatalogPage extends BasePage {

    @AndroidFindBy(uiAutomator = "new UiSelector().text(\"Products\")")
    @iOSXCUITFindBy(accessibility = "Catalog-screen")
    private WebElement screen;

    @AndroidFindBy(id = AndroidIds.PREFIX + "titleTV")
    @iOSXCUITFindBy(accessibility = "Product Name")
    private List<WebElement> names;

    @AndroidFindBy(id = AndroidIds.PREFIX + "priceTV")
    @iOSXCUITFindBy(accessibility = "Product Price")
    private List<WebElement> prices;

    // Android opens a product from its picture, iOS from the whole cell
    @AndroidFindBy(id = AndroidIds.PREFIX + "productIV")
    @iOSXCUITFindBy(accessibility = "ProductItem")
    private List<WebElement> items;

    public CatalogPage(AppiumDriver driver) {
        super(driver);
    }

    public CatalogPage waitUntilShown() {
        visible(screen);
        return this;
    }

    public List<String> productNames() {
        waitUntilShown();
        return textsOf(atLeast(names, 1));
    }

    public String priceOf(int index) {
        WebElement price = atLeast(prices, index + 1).get(index);
        if (isAndroid()) {
            return textOf(price);
        }
        // on iOS the accessibility id is the name, so the price is read from value or label
        String value = visible(price).getAttribute("value");
        if (value != null && value.startsWith("$")) {
            return value;
        }
        return visible(price).getAttribute("label");
    }

    public ProductPage openProduct(int index) {
        tapCard(index);
        return new ProductPage(driver).waitUntilShown();
    }

    /** Taps the card at index without expecting a detail page, for taps that may not open one. */
    public void tapCard(int index) {
        tap(atLeast(items, index + 1).get(index));
    }

    /** Scrolls until the card with exactly this name is on screen, then opens it. */
    public ProductPage openProduct(String name) {
        tapCard(name);
        return new ProductPage(driver).waitUntilShown();
    }

    /** Scrolls until the card with exactly this name is on screen and taps it, without waiting for a detail page. */
    public void tapCard(String name) {
        // the name goes into a locator string, so quotes would break it
        if (name.contains("\"") || name.contains("'")) {
            throw new IllegalArgumentException("Product name must not contain quotes: " + name);
        }
        if (isAndroid()) {
            // tapping the title did not open the product, so the picture beside it is tapped
            String title = "new UiSelector().resourceId(\"" + AndroidIds.PREFIX + "titleTV\")"
                    + ".text(\"" + name + "\")";
            driver.findElement(AppiumBy.androidUIAutomator(
                    "new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(" + title + ")"));
            tap(driver.findElement(AppiumBy.androidUIAutomator(
                    title + ".fromParent(new UiSelector().resourceId(\"" + AndroidIds.PREFIX + "productIV\"))")));
        } else {
            driver.executeScript("mobile: scroll",
                    Map.of("direction", "down", "predicateString", "label == '" + name + "'"));
            tap(visible(AppiumBy.iOSNsPredicateString("label == '" + name + "'")));
        }
    }
}
