package com.servet.mobile.framework;

import com.servet.mobile.framework.config.Platform;
import com.servet.mobile.framework.pages.OnlyOn;
import com.servet.mobile.mydemoapp.pages.*;
import io.appium.java_client.pagefactory.*;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class PageLocatorCoverageTest {

    @Test(groups = "unit")
    public void testPageLocatorCoverage() {
        List<Class<?>> pages = List.of(CatalogPage.class, ProductPage.class, CartPage.class);
        List<String> violations = new ArrayList<>();

        for (Class<?> pageClass : pages) {
            boolean hasField = false;
            for (Field field : pageClass.getDeclaredFields()) {
                if (isWebElementOrList(field)) {
                    hasField = true;
                    boolean hasAndroid = field.isAnnotationPresent(AndroidFindBy.class) ||
                                         field.isAnnotationPresent(AndroidFindBys.class) ||
                                         field.isAnnotationPresent(AndroidFindAll.class);
                    boolean hasIos = field.isAnnotationPresent(iOSXCUITFindBy.class) ||
                                     field.isAnnotationPresent(iOSXCUITFindBys.class) ||
                                     field.isAnnotationPresent(iOSXCUITFindAll.class);
                    String name = pageClass.getSimpleName() + "." + field.getName();
                    OnlyOn onlyOn = field.getAnnotation(OnlyOn.class);

                    if (onlyOn == null) {
                        if (!hasAndroid) violations.add(name + ": missing Android");
                        if (!hasIos) violations.add(name + ": missing iOS");
                    } else if (onlyOn.value() == Platform.ANDROID) {
                        if (!hasAndroid) violations.add(name + ": missing Android");
                        if (hasIos) violations.add(name + ": has iOS locator but is Android only");
                    } else {
                        if (!hasIos) violations.add(name + ": missing iOS");
                        if (hasAndroid) violations.add(name + ": has Android locator but is iOS only");
                    }
                }
            }
            if (!hasField) {
                fail("Page " + pageClass.getSimpleName() + " has no fields of type WebElement or List<WebElement>");
            }
        }

        assertThat(violations).isEmpty();
    }

    private boolean isWebElementOrList(Field field) {
        if (WebElement.class.isAssignableFrom(field.getType())) return true;
        if (field.getType() == List.class && field.getGenericType() instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) field.getGenericType();
            return pt.getActualTypeArguments()[0] == WebElement.class;
        }
        return false;
    }
}
