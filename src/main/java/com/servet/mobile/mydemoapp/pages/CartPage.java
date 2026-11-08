package com.servet.mobile.mydemoapp.pages;

import java.util.List;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import com.servet.mobile.framework.config.Platform;
import com.servet.mobile.framework.pages.BasePage;
import com.servet.mobile.framework.pages.OnlyOn;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;

public final class CartPage extends BasePage {

    @AndroidFindBy(id = AndroidIds.PREFIX + "itemsTV")
    @iOSXCUITFindBy(iOSNsPredicate = "label MATCHES '[0-9]+ Items'") // ENDSWITH also matched the hidden "No Items"
    private WebElement itemCount;

    @OnlyOn(value = Platform.IOS, reason = "iOS waits for the screen id, Android waits for the item count")
    @iOSXCUITFindBy(accessibility = "Cart-screen")
    private WebElement iosCartScreen;

    @AndroidFindBy(id = AndroidIds.PREFIX + "titleTV")
    // iOS names have no id: the name is the visible text right before a "$ " price
    @iOSXCUITFindBy(xpath = "//XCUIElementTypeOther[@name='Cart-screen']"
            + "//XCUIElementTypeStaticText[@visible='true']"
            + "[following::XCUIElementTypeStaticText[1][starts-with(@name,'$ ')]]")
    private List<WebElement> names;

    @AndroidFindBy(id = AndroidIds.PREFIX + "removeBt")
    // a static text also reads "Remove Item", so only buttons are matched
    @iOSXCUITFindBy(iOSClassChain = "**/XCUIElementTypeButton[`name == \"Remove Item\"`]")
    private List<WebElement> removeButtons;

    @AndroidFindBy(uiAutomator = "new UiSelector().text(\"No Items\")")
    @iOSXCUITFindBy(accessibility = "No Items")
    private WebElement noItems;

    public CartPage(AppiumDriver driver) {
        super(driver);
    }

    /** Waits until the cart screen is shown, so the next read sees this screen. */
    public CartPage waitUntilShown() {
        if (isAndroid()) {
            visible(itemCount);
        } else {
            visible(iosCartScreen);
        }
        return this;
    }

    public String itemCountText() {
        return textOf(itemCount);
    }

    public List<String> productNames() {
        return textsOf(atLeast(names, 1));
    }

    /** Waits until the cart has one product less, so the next read sees the change. */
    public void removeFirst() {
        int before = atLeast(names, 1).size();
        tap(atLeast(removeButtons, 1).get(0));
        // reads names directly: productNames() waits for a product, which an emptied cart never has
        wait.until(d -> names.size() < before);
    }

    public boolean isEmpty() {
        try {
            visible(noItems);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }
}
