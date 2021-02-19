package com.github.easyware.easyapiapp;

import com.github.easyware.easyapiapp.object.MethodComment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

@RestController
public class Controller {

    @Autowired
    CommentParser commentParser;

    @RequestMapping("/method")
    public MethodComment method(String g,String m){
       return commentParser.getMethodComments(g).get(m);
    }

    @RequestMapping("/methodNames")
    public Set<String> methodNames(String g){
        return commentParser.getMethodComments(g).keySet();
    }

    @RequestMapping("/methods")
    public Map<String, MethodComment>  methods(String g){
        return commentParser.getMethodComments(g);
    }


    @RequestMapping("/class")
    public Map<String, String> getClassComment(String g,String c){
        return commentParser.getClassComments(g).get(c);
    }

    @RequestMapping("/classNames")
    public Set<String> classNames(String g){
        return commentParser.getClassComments(g).keySet();
    }

    @RequestMapping("/classes")
    public Map<String, Map<String, String>> classes(String g){
        return commentParser.getClassComments(g);
    }
}
