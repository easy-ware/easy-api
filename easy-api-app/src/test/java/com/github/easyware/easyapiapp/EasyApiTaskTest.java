package com.github.easyware.easyapiapp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.List;

@SpringBootTest
public class EasyApiTaskTest {

    @Autowired
    EasyApiTask task;
    @Autowired
    CommentParser commentParser;
    @Test
    public void svnUpdate() throws Exception {
        task.svnUpdate();
    }

    @Test
    public void parseFile() throws Exception {

        File file=new File("/Users/apple/workspace/ershouji/mobile-api/src/main/java/com/cehome/jishou/mobile_api/entity/ProductMeta.java");
        commentParser.parseFile("group1",file);

    }
}
