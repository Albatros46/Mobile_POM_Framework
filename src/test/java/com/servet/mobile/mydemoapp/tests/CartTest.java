package com.servet.mobile.mydemoapp.tests;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

import com.servet.mobile.framework.testing.BaseMobileTest;
import com.servet.mobile.mydemoapp.pages.CartPage;
import com.servet.mobile.mydemoapp.pages.CatalogPage;
import com.servet.mobile.mydemoapp.pages.ProductPage;

public class CartTest extends BaseMobileTest {

    @Test(groups = "smoke")
    public void adding_a_product_puts_it_in_the_cart() {
        CatalogPage catalog = new CatalogPage(driver());
        String name = catalog.productNames().get(0);
        ProductPage product = catalog.openProduct(0);
        product.addToCart();

        CartPage cart = product.openCart();

        assertThat(cart.productNames()).containsExactly(name);
        assertThat(cart.itemCountText()).isEqualTo("1 Items");
    }

    @Test
    public void two_of_the_same_product_make_two_items() {
        CatalogPage catalog = new CatalogPage(driver());
        String name = catalog.productNames().get(0);
        ProductPage product = catalog.openProduct(0);
        product.increase();
        product.addToCart();

        CartPage cart = product.openCart();

        assertThat(cart.productNames()).containsExactly(name);
        assertThat(cart.itemCountText()).isEqualTo("2 Items");
    }

    @Test
    public void removing_the_only_product_empties_the_cart() {
        ProductPage product = new CatalogPage(driver()).openProduct(0);
        product.addToCart();
        CartPage cart = product.openCart();

        cart.removeFirst();

        assertThat(cart.isEmpty()).isTrue();
    }
}
