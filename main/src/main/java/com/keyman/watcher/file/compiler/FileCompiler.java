package com.keyman.watcher.file.compiler;

import com.keyman.watcher.exception.CompiledException;
import com.keyman.watcher.parser.util.FileUtil;
import com.keyman.watcher.parser.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Deprecated
public class FileCompiler extends ClassLoader
{
    private static final Logger log = LoggerFactory.getLogger(FileCompiler.class);
    private static final String CLASS_FILE = FileUtil.loadFile("FileTemplate");
    private static final String METHOD_FILE = FileUtil.loadFile("MethodTemplate");

    public FileCompiler()
    {
        super(ClassLoader.getSystemClassLoader());
    }

    public void compile(Map<String, String> input, String apiRootPath, Class<?> rootCLass, String targetClassName)
    {
        String packageName = rootCLass.getPackage().getName();
        String content = buildController(input, apiRootPath, packageName, targetClassName);
        try
        {
            String compilePath = rootCLass.getResource("").getPath();
            File javaFile = new File(compilePath + targetClassName + ".java");
            generateJavaFile(javaFile, content);
            invokeCompiler(compilePath  + targetClassName, packageName + "." + targetClassName);
        }
        catch (ClassNotFoundException | IOException e)
        {
            throw new CompiledException(e);
        }
    }

    void generateJavaFile(File javaFile, String content) throws IOException
    {
        FileOutputStream out = new FileOutputStream(javaFile);
        out.write(content.getBytes());
        out.close();
        javaFile.createNewFile();
    }

    public String buildController(Map<String, String> input, String rootPath, String packageName, String className) {
        StringBuilder builder = new StringBuilder();
        AtomicInteger index = new AtomicInteger(1);
        input.keySet().forEach(k -> {
            String url = k.substring(rootPath.length() + 1, k.lastIndexOf('.')).replaceAll("\\s", "");
            String tempMethod = METHOD_FILE.replace("{{methodUrl}}", url);
            tempMethod = tempMethod.replace("{{methodName}}", "get" + index.getAndIncrement());
            String methodReturn = "ResultStore.getGlobalResult()" + ".get(\"" + StringUtil.escapeRegex(k) + "\");";
            tempMethod = tempMethod.replace("{{methodReturn}}", methodReturn);
            builder.append(tempMethod);
        });
        String file = CLASS_FILE.replace("{{methods}}", builder.toString()).replace("{{className}}", className);
        file = file.replace("{{package}}", packageName);
        return file;
    }


    void invokeCompiler(String filePath, String classFullName) throws ClassNotFoundException {
        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
        try {
            int count = javaCompiler.run(null, null, null, filePath + ".java");
            log.debug("compile status: " + (count > 0));
        } catch (Exception e) {
            log.warn("compile error");
        }
        Class.forName(classFullName);
    }
}
