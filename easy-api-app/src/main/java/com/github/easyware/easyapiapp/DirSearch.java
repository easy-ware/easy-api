package com.github.easyware.easyapiapp;

import java.io.File;

public abstract class DirSearch {

    public DirSearch(File root) {
        search(0,  root);
    }

    abstract protected void handle(int level, File file);

    private void search(int level, File file) {
        handle( level, file);
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                search(level + 1, child);
            }
        }
    }

}