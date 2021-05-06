package com.keyman.watcher.file;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JarHandlerTest {
    @Test
    public void jarTest(){
        String path = "file:\\C:\\Users\\khong\\Documents\\github\\watcher-demo" +
                "\\target\\watcher-demo-1.0-SNAPSHOT.jar!\\BOOT-INF";
        path = path.substring(0, path.lastIndexOf("!\\BOOT-INF"));
        path = path.replaceAll("file:\\\\|file:/", "");
        File file = new File(path);
        System.out.println(file.exists());
    }

    @Test
    @Ignore
    public void unjar() throws Exception {
        String target = "C:\\Users\\khong\\Desktop\\api\\TS\\Newfolder\\test";
        String path = "C:\\Users\\khong\\Desktop\\api\\TS\\Newfolder\\watcher-demo-1.0-SNAPSHOT.jar";
        JarHandler jarHandler = new JarHandler();
        jarHandler.decompress(path, target);
        System.out.println("finish");
        Thread.sleep(2000);
        jarHandler.clean(target);
    }

    @Test
    @Ignore
    public void pathTest() {
        String target = "C:\\Users\\khong\\Desktop\\api\\TS\\Newfolder\\test";
        Path path = Paths.get(target);
        System.out.println(path);

        System.out.println(new Object().hashCode());
        System.out.println(new Object().hashCode());
    }
}