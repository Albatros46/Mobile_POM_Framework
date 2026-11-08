package com.servet.mobile.framework.pages;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.servet.mobile.framework.config.Platform;

/** Marks a page field that has a locator for one platform only; the reason says why. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OnlyOn {

    Platform value();

    String reason();
}
