package com.servet.mobile.mydemoapp.pages;

import java.util.List;

import org.openqa.selenium.WebElement;

import com.servet.mobile.framework.config.Platform;
import com.servet.mobile.framework.pages.BasePage;
import com.servet.mobile.framework.pages.OnlyOn;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;

public final class ProductPage extends BasePage {

    @OnlyOn(value = Platform.ANDROID, reason = "Android shows the name in its own text view")
    @AndroidFindBy(id = AndroidIds.PREFIX + "productTV")
    private WebElement androidName;

    // iOS gives the name no id: it is the first static text of this screen, see name()
    @OnlyOn(value = Platform.IOS, reason = "the screen has an accessibility id, the name has none")
    @iOSXCUITFindBy(accessibility = "ProductDetails-screen")
    private WebElement iosDetailsScreen;

    @AndroidFindBy(id = AndroidIds.PREFIX + "priceTV")
    @iOSXCUITFindBy(accessibility = "Price")
    private WebElement priceLabel;

    @AndroidFindBy(id = AndroidIds.PREFIX + "noTV")
    @iOSXCUITFindBy(accessibility = "Amount")
    private WebElement quantityLabel;

    @AndroidFindBy(id = AndroidIds.PREFIX + "plusIV")
    @iOSXCUITFindBy(accessibility = "AddPlus Icons")
    private WebElement plusButton;

    @AndroidFindBy(id = AndroidIds.PREFIX + "minusIV")
    @iOSXCUITFindBy(accessibility = "SubtractMinus Icons")
    private WebElement minusButton;

    @AndroidFindBy(id = AndroidIds.PREFIX + "cartBt")
    @iOSXCUITFindBy(accessibility = "Add To Cart")
    private WebElement addToCartButton;

    @AndroidFindBy(id = AndroidIds.PREFIX + "cartIV")
    @iOSXCUITFindBy(accessibility = "Cart-tab-item")
    private WebElement cartIcon;

    @OnlyOn(value = Platform.IOS, reason = "iOS has a tab bar; Android goes back through the app menu")
    @iOSXCUITFindBy(accessibility = "Catalog-tab-item")
    private WebElement catalogTab;

    @OnlyOn(value = Platform.ANDROID, reason = "iOS has no header menu")
    @AndroidFindBy(id = AndroidIds.PREFIX + "menuIV")
    private WebElement androidMenuButton;

    @OnlyOn(value = Platform.ANDROID, reason = "entry of the header menu that opens the catalog")
    @AndroidFindBy(uiAutomator = "new UiSelector().text(\"Catalog\")")
    private WebElement androidCatalogMenuItem;

    public ProductPage(AppiumDriver driver) {
        super(driver);
    }

    public String name() {
        if (isAndroid()) {
            return textOf(androidName);
        }
        // the first static text of the screen is the product title, the price and description follow it
        return visible(iosDetailsScreen)
                .findElements(AppiumBy.className("XCUIElementTypeStaticText"))
                .get(0)
                .getText();
    }

    public String price() {
        return textOf(priceLabel);
    }

    public int quantity() {
        return Integer.parseInt(textOf(quantityLabel).trim());
    }

    /** Waits until the quantity has changed, so the next read sees the new value. */
    public void increase() {
        changeQuantity(plusButton);
    }

    public void decrease() {
        changeQuantity(minusButton);
    }

    /** Waits until the detail screen is shown, so taps hit this screen and not the previous one. */
    public ProductPage waitUntilShown() {
        if (isAndroid()) {
            visible(androidName);
            visible(addToCartButton);
        } else {
            visible(iosDetailsScreen);
        }
        return this;
    }

    /** Waits until the cart badge shows the expected count, so the add is confirmed. */
    public void addToCart() {
        int expected = cartCount() + quantity();
        tap(addToCartButton);
        wait.until(d -> cartCount() == expected);
    }

    public CartPage openCart() {
        tap(cartIcon);
        return new CartPage(driver).waitUntilShown();
    }

    public CatalogPage backToCatalog() {
        if (isAndroid()) {
            // the system back key would leave the app from this screen
            tap(androidMenuButton);
            tap(androidCatalogMenuItem);
        } else {
            tap(catalogTab);
        }
        return new CatalogPage(driver).waitUntilShown();
    }

    private void changeQuantity(WebElement button) {
        int before = quantity();
        tap(button);
        wait.until(d -> quantity() != before);
    }

    /** Cart badge count; 0 when the badge is absent. Uses findElements so absence does not throw. */
    private int cartCount() {
        if (isAndroid()) {
            List<WebElement> badges = driver.findElements(AppiumBy.id(AndroidIds.PREFIX + "cartTV"));
            return badges.isEmpty() ? 0 : parseCount(badges.get(0).getText());
        }
        List<WebElement> tabs = driver.findElements(AppiumBy.accessibilityId("Cart-tab-item"));
        return tabs.isEmpty() ? 0 : parseCount(tabs.get(0).getAttribute("value"));
    }

    private static int parseCount(String text) {
        return text == null || text.isBlank() ? 0 : Integer.parseInt(text.trim());
    }
}
