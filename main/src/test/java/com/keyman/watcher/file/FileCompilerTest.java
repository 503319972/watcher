package com.keyman.watcher.file;

import com.keyman.watcher.controller.Temp;
import com.keyman.watcher.file.compiler.FileCompiler;
import com.keyman.watcher.parser.ResultStore;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Map;


public class FileCompilerTest {
    @Test
    @Ignore
    public void loadFile3() throws Exception {
        String rootPath = "C:\\Users\\khong\\Desktop\\api\\TS\\New folder\\New folder";
        FilePathHierarchyParser parser = new FilePathHierarchyParser(rootPath);
        Map<String, String> stringStringMap = parser.buildHierarchy();
        ResultStore.setGlobalResult(stringStringMap);
        new FileCompiler().compile(stringStringMap, rootPath, Temp.class, "CompiledController");

        Class<?> aClass = Class.forName("com.keyman.watcher.controller.CompiledController");
        Object obj = aClass.newInstance();
        Method get1 = aClass.getMethod("get1");
        Object invoke = get1.invoke(obj);
        System.out.println(invoke);
    }
}