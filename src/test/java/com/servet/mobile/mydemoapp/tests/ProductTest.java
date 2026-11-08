package com.servet.mobile.mydemoapp.tests;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.SkipException;
import org.testng.annotations.Test;

import com.servet.mobile.framework.config.Platform;
import com.servet.mobile.framework.testing.BaseMobileTest;
import com.servet.mobile.mydemoapp.pages.CatalogPage;
import com.servet.mobile.mydemoapp.pages.ProductPage;

public class ProductTest extends BaseMobileTest {

    @Test(groups = "smoke")
    public void the_detail_page_shows_the_catalog_name() {
        CatalogPage catalog = new CatalogPage(driver());
        String name = catalog.productNames().get(0);

        ProductPage product = catalog.openProduct(0);

        assertThat(product.name()).isEqualTo(name);
    }

    @Test(groups = "smoke")
    public void the_detail_page_shows_the_catalog_price() {
        if (Platform.current() == Platform.IOS) {
            throw new SkipException("MDA-04: iOS does not expose the catalog price");
        }
        CatalogPage catalog = new CatalogPage(driver());
        String price = catalog.priceOf(0);

        ProductPage product = catalog.openProduct(0);

        assertThat(product.price()).isEqualTo(price);
    }

    @Test
    public void the_quantity_starts_at_one_and_can_be_changed() {
        ProductPage product = new CatalogPage(driver()).openProduct(0);
        assertThat(product.quantity()).isEqualTo(1);

        product.increase();
        product.increase();
        assertThat(product.quantity()).isEqualTo(3);

        product.decrease();
        assertThat(product.quantity()).isEqualTo(2);
    }
}
