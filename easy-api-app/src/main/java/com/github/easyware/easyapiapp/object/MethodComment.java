package com.github.easyware.easyapiapp.object;

import java.util.HashMap;
import java.util.Map;

public class MethodComment {
    //private String fullName;//类名#方法名
    private String summary;//概要
    private String desc;//说明
    private Map<String,String> params =new HashMap<>();//参数说明
    private String returnComment;//返回说明

    /*public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }*/

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public String getReturnComment() {
        return returnComment;
    }

    public void setReturnComment(String returnComment) {
        this.returnComment = returnComment;
    }
}
