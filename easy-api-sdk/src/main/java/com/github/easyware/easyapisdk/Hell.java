package com.github.easyware.easyapisdk;

import java.lang.reflect.Type;

public class Hell {

    class A<T>{
        public T data;

    }

    public void ddd(A<Integer> a){

    }

    public static void main(String[] args) throws Exception{
        Type type=Hell.class.getMethod("ddd",A.class).getGenericParameterTypes()[0];
        System.out.println(type);
    }
}
