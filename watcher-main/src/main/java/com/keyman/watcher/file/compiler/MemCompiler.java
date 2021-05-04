package com.keyman.watcher.file.compiler;

import com.keyman.watcher.parser.util.FileUtil;
import com.keyman.watcher.parser.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MemCompiler extends ClassLoader {
    private static final Logger log = LoggerFactory.getLogger(MemCompiler.class);
    private static final JavaCompiler COMPILER = ToolProvider.getSystemJavaCompiler();
    private static final String CLASS_FILE = FileUtil.loadFile("FileTemplate");
    private static final String METHOD_FILE = FileUtil.loadFile("MethodTemplate");

    public MemCompiler()
    {
        super(ClassLoader.getSystemClassLoader());
    }

    public Class<?> compile(Map<String, String> input, String apiRootPath, Class<?> rootCLass, String targetClassName)
    {
        try {
            String packageName = rootCLass.getPackage().getName();
            String content = buildController(input, apiRootPath, packageName, targetClassName);

            StandardJavaFileManager standardFileManager = COMPILER.getStandardFileManager(null, null, null);
            ControllerFileManager controllerFileManager = new ControllerFileManager(standardFileManager);
            ControllerStringObject stringObject = new ControllerStringObject(targetClassName + ".java",
                    JavaFileObject.Kind.SOURCE, content);

            JavaCompiler.CompilationTask task = COMPILER.getTask(null, controllerFileManager, null,
                    null, null, Collections.singletonList(stringObject));
            if (Boolean.TRUE.equals(task.call())) {
                ControllerFileObject controllerFileObject = controllerFileManager.getControllerFileObject();
                ClassLoader classLoader = new ControllerClassLoader(controllerFileObject);
                return classLoader.loadClass(packageName + "." + targetClassName);
            }
        } catch (ClassNotFoundException e) {
            log.error("load auto-inject file controller failed", e);
        }
        return null;
    }

    public String buildController(Map<String, String> input, String rootPath, String packageName, String className) {
        StringBuilder builder = new StringBuilder();
        AtomicInteger index = new AtomicInteger(1);
        input.keySet().forEach(k -> {
            String url = k.substring(rootPath.length() + 1, k.lastIndexOf('.')).replaceAll("\\s", "");
            String tempMethod = METHOD_FILE.replace("{{methodUrl}}", url.replace("\\", "/"));
            tempMethod = tempMethod.replace("{{methodName}}", "get" + index.getAndIncrement());
            String methodReturn = "ResultStore.getGlobalResult()" + ".get(\"" + StringUtil.escapeRegex(k) + "\");";
            tempMethod = tempMethod.replace("{{methodReturn}}", methodReturn);
            builder.append(tempMethod);
        });
        String file = CLASS_FILE.replace("{{methods}}", builder.toString()).replace("{{className}}", className);
        file = file.replace("{{package}}", packageName);
        return file;
    }
}
