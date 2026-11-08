package com.servet.mobile.mydemoapp.tests;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.servet.mobile.framework.config.Platform;
import com.servet.mobile.framework.testing.BaseMobileTest;
import com.servet.mobile.mydemoapp.MyDemoApp;
import com.servet.mobile.mydemoapp.pages.CatalogPage;

public class CatalogTest extends BaseMobileTest {

    @Test(groups = "smoke")
    public void the_catalog_shows_more_than_one_product() {
        CatalogPage catalog = new CatalogPage(driver());

        assertThat(catalog.productNames()).hasSizeGreaterThan(1);
    }

    @Test
    public void the_first_product_is_sauce_labs_backpack() {
        CatalogPage catalog = new CatalogPage(driver());

        // iOS appends " - Black" to the name; the prefix holds on both platforms
        assertThat(catalog.productNames().get(0)).startsWith("Sauce Labs Backpack");
    }

    @DataProvider(name = "cardsOtherThanTheFirst")
    public Object[][] cardsOtherThanTheFirst() {
        return new Object[][] {
            {"Sauce Labs Backpack (green)"},
            {"Sauce Labs Bike Light"}
        };
    }

    /**
     * KNOWN BUG MDA-03: on Android, tapping any card other than the first crashes My Demo App 2.3.0.
     * "Sauce Labs Backpack (green)" (index 1) throws NullPointerException (Integer.intValue() on null),
     * and "Sauce Labs Bike Light" (below the fold) throws ArrayIndexOutOfBoundsException (length=6; index=6),
     * both at ProductCatalogFragment.java:156.
     * This test pins the current behaviour; it turns red when the app is fixed, and then it must change.
     */
    @Test(dataProvider = "cardsOtherThanTheFirst", groups = "known-bug")
    public void known_bug_mda_03_a_card_other_than_the_first_crashes_the_app(String card) {
        if (Platform.current() == Platform.IOS) {
            throw new SkipException("MDA-03 is an Android defect");
        }
        CatalogPage catalog = new CatalogPage(driver());

        catalog.tapCard(card);

        assertThat(catalog.appLeftForeground(MyDemoApp.ANDROID_PACKAGE)).isTrue();
    }

    /**
     * KNOWN BUG MDA-04: on iOS, the catalog price element exposes only "Product Price" as its text, value,
     * label and name, because the storyboard sets that accessibility label on the price label. The amount
     * is therefore not available to VoiceOver or to automation.
     * This test pins the current behaviour; it turns red when the app is fixed, and then it must change.
     */
    @Test(groups = "known-bug")
    public void known_bug_mda_04_the_ios_catalog_price_is_not_exposed() {
        if (Platform.current() == Platform.ANDROID) {
            throw new SkipException("MDA-04 is an iOS defect");
        }
        CatalogPage catalog = new CatalogPage(driver());

        assertThat(catalog.priceLabelOf(0)).isEqualTo("Product Price");
    }
}
