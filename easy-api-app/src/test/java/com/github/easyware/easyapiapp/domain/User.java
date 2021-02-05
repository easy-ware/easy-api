package com.github.easyware.easyapiapp.domain;

public class User {
    private String name;//名称
    /**
     * if you are man, then set man
     */
    private String sex;// man or woman

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }
}
