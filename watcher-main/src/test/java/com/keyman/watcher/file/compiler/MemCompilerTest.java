package com.keyman.watcher.file.compiler;

import com.keyman.watcher.controller.Temp;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Collections;

public class MemCompilerTest {

    @Test
    public void compileTest() {
        MemCompiler memCompiler = new MemCompiler();
        Class<?> compiledController = memCompiler.compile(Collections.emptyMap(), "",
                Temp.class, "CompiledController");
        Method[] methods = compiledController.getMethods();
        Assert.assertNotNull(methods);
    }

}