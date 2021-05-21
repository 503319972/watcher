package com.keyman.watcher.file;

import com.keyman.watcher.controller.Temp;
import com.keyman.watcher.file.compilation.FileCompiler;
import com.keyman.watcher.global.GlobalStore;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.Map;


public class FileCompilerTest {
    @Test
    @Ignore
    public void loadFile3() throws Exception {
        String rootPath = "C:\\Users\\khong\\Desktop\\api\\TS\\New folder\\New folder";
        FilePathHierarchyParser parser = new FilePathHierarchyParser(rootPath);
        Map<String, String> stringStringMap = parser.buildHierarchy();
        GlobalStore.putGlobalResult(Collections.singletonMap(rootPath, stringStringMap));
        new FileCompiler().compile(stringStringMap, rootPath, Temp.class, "CompiledController");

        Class<?> aClass = Class.forName("com.keyman.watcher.controller.CompiledController");
        Object obj = aClass.newInstance();
        Method get1 = aClass.getMethod("get1");
        Object invoke = get1.invoke(obj);
        System.out.println(invoke);
    }

    @Test
    public void dateStr() {
        Date date = new Date();
        System.out.println(date);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneOffset.ofHours(8));
        System.out.println(localDateTime.format(DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss")));
    }
}