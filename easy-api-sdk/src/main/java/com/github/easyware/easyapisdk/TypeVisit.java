package com.github.easyware.easyapisdk;

import com.google.common.reflect.TypeResolver;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TypeVisit<T> {
    TypeVisitCallback<T> typeVisitCallback;
    Set<String> classSet=new HashSet<>();

    public TypeVisit(TypeVisitCallback<T> typeVisitCallback) {
        this.typeVisitCallback = typeVisitCallback;
    }
    public void visit(Type type) throws IntrospectionException {
        visit(null,type,false,null);
    }
    protected void visit(T parent, Type type, boolean array, String prop) throws IntrospectionException {

        Class clazz;
        Type[] argTypes = null;
        //-- 非泛型
        if (type instanceof Class) {
            clazz = (Class) type;
            if (clazz.isArray()) {
                visit(parent, clazz.getComponentType(), true, prop);
            } else {
                String base = Global.getBase(clazz);
                if (base != null) {
                    //System.out.println(prefix + prop + ":" + base);
                    typeVisitCallback.callback(parent, prop, clazz, array, base);

                } else {
                    visitObject(parent, prop, clazz, array,argTypes);
                }
            }


        }
        //泛型
        else if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            //真实类
            clazz = (Class) pType.getRawType();
            //泛型信息
            argTypes = pType.getActualTypeArguments();
            visitObject(parent, prop, clazz, array,argTypes);
        }//数组泛型
        else if (type instanceof GenericArrayType) {
            GenericArrayType aType = (GenericArrayType) type;
            System.out.println("is array");
            visit(parent, aType.getGenericComponentType(), true, prop);
        } else {
            System.out.println("not not");
            // return;
        }

    }


    private void visitObject(T parent, String propName, Class propClass,  boolean array, Type[] argTypes) throws IntrospectionException {
        System.out.println("--------class:"+propClass.getName());
        if(propClass.equals(Class.class)){
            System.out.println("ignore Class.class");
            return;
        }
        //类的泛型参数 T K
        TypeVariable[] varTypes = propClass.getTypeParameters();
        TypeResolver typeResolver = getTypeResolver(varTypes, argTypes);


        if (Collection.class.isAssignableFrom(propClass)) {
            //System.out.println("array");
            if (argTypes != null) {
                visit(parent, argTypes[0], true, propName + "[]");
            } else {
                visit(parent, Object.class, true, propName + "[]");
                //System.out.println(prefix + prop + "[]:Object");
            }
            return;

        } else if (Map.class.isAssignableFrom(propClass)) {
            System.out.println("not support Map");
            return;
        }
        //System.out.println(prefix + propName + ":" + propClass.getName());

        T current=typeVisitCallback.callback(parent, propName, propClass, array, null);
        //avoid loop reference
        if(!classSet.add(propClass.getName())){
            System.out.println("class exists: "+propClass.getName());
            return;
        }


        BeanInfo bi = Introspector.getBeanInfo(propClass);
        PropertyDescriptor[] pds = bi.getPropertyDescriptors();
        //遍历类属性
        for (PropertyDescriptor pd : pds) {
            String name = pd.getName();
            if (name.equals("class")) continue;

            Class pc = pd.getPropertyType();
            if(pc.equals(Class.class)) continue;
            if (name.equals("list3")) {
                System.out.println("");
            }
            //-- 是否基础类型
            /*String base = getBase(pc); //String[]  Demo<String>[]
            if (base != null) {
                System.out.println(prefix + "    " + pd.getName() + ":" + base);
                callback.callback(current, prop, clazz, false, base);
            } else {*/
            Method readMethod = pd.getReadMethod();
            if (readMethod != null) {
                //-- 属性类型
                Type propType = readMethod.getGenericReturnType();

                propType = typeResolver.resolveType(propType);

                visit(current, propType, false, name );


            } else {
                System.out.println("ignore prop " + name+" of "+pc.getName());
            }


        }
    }

    private TypeResolver getTypeResolver(TypeVariable[] varTypes, Type[] argTypes) {
        TypeResolver typeResolver = new TypeResolver();
        if (varTypes != null && argTypes != null) {
            int end = Math.min(varTypes.length, argTypes.length);
            for (int i = 0; i < end; i++) {
                typeResolver = typeResolver.where(varTypes[i], argTypes[i]);
            }
        }
        return typeResolver;
    }




}
