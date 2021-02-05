package com.github.easyware.easyapisdk;

public interface TypeVisitCallback<T> {
    T callback(T parent,String prop,Class propClazz,  boolean array,String baseName);
}
