package com.zhuinden.servicetreeconductorexample.injection;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

/**
 * Created by Owner on 2017. 03. 07..
 */
@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface ControllerScope {
    Class<?> value();
}
