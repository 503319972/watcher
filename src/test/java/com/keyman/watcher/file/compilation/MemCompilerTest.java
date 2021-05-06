package com.keyman.watcher.file.compilation;

import com.keyman.watcher.controller.Temp;
import com.keyman.watcher.file.JarHandler;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.Method;

public class MemCompilerTest {

    @Test
    @Ignore("ignore maven build")
    public void compileTest() {
        MemCompiler memCompiler = new MemCompiler(new JarHandler());
        Class<?> compiledController = memCompiler.compile("",
                Temp.class.getPackage().getName(), "CompiledController");
        Method[] methods = compiledController.getMethods();
        Assert.assertNotNull(methods);
    }

    @Test
    public void test() {
        String realPath = MemCompilerTest.class.getClassLoader().getResource("")
                .getFile();
        java.io.File file = new java.io.File(realPath);
        realPath = file.getParentFile().getAbsolutePath();
        System.out.println(realPath);
    }

}