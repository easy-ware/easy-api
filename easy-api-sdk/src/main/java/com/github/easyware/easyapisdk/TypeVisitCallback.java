package com.github.easyware.easyapisdk;

import java.lang.reflect.Type;

public interface TypeVisitCallback<T> {
    T callback(T parent, String prop, Type sourceType, Class sourceClass, boolean array, String baseName);
}
