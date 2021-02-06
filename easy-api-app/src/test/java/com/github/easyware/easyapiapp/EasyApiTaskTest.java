package com.github.easyware.easyapiapp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EasyApiTaskTest {

    @Autowired
    EasyApiTask task;
    @Test
    public void svnUpdate() throws Exception {
        task.svnUpdate();
    }
}
