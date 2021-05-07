package com.keyman.watcher.file;

import org.junit.Test;

public class FileTemplateTest {

    @Test
    public void test(){
        String path = "C:\\api\\";
        path = path.substring(0, path.length() - 1);
        System.out.println(path);
    }

}