package com.keyman.watcher.file.compilation;

import com.keyman.watcher.file.JarHandler;
import com.keyman.watcher.parser.GlobalStore;
import com.keyman.watcher.util.FileUtil;
import com.keyman.watcher.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MemCompiler {
    private static final Logger log = LoggerFactory.getLogger(MemCompiler.class);
    private static final String CLASS_FILE = FileUtil.loadFile("FileTemplate");
    private static final String METHOD_FILE = FileUtil.loadFile("MethodTemplate");
    private final JarHandler jarHandler;


    public MemCompiler(JarHandler jarHandler) {
        this.jarHandler = jarHandler;
    }

    public Class<?> compile(String apiRootPath, String packageName, String targetClassName)
    {
        try {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            String content = buildController(apiRootPath, packageName, targetClassName);
            log.info("class content: {}", content);
            StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(null, null, null);
            ControllerFileManager controllerFileManager = new ControllerFileManager(standardFileManager);
            ControllerStringObject stringObject = new ControllerStringObject(targetClassName + ".java",
                    JavaFileObject.Kind.SOURCE, content);

            DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();

            List<String> options = new ArrayList<>();
            handleJar(options);
            JavaCompiler.CompilationTask task = compiler.getTask(null, controllerFileManager, diagnosticCollector,
                    options, null, Collections.singletonList(stringObject));
            if (Boolean.TRUE.equals(task.call())) {
                ControllerFileObject controllerFileObject = controllerFileManager.getControllerFileObject();
                ClassLoader classLoader = new ControllerClassLoader(controllerFileObject, targetClassName);
                Class<?> targetClass = classLoader.loadClass(StringUtil.isEmpty(packageName) ?
                        targetClassName : packageName+ "." + targetClassName);
                log.info("finish load class: {}", targetClass.getName());
                return targetClass;
            } else {
                StringBuilder error = new StringBuilder();
                for (Diagnostic<?> diagnostic : diagnosticCollector.getDiagnostics()) {
                    error.append(compilePrint(diagnostic));
                }
                throw new UnsupportedOperationException(error.toString());
            }
        } catch (Exception e) {
            log.error("load auto-inject file controller failed", e);
        }
        return null;
    }

    private void handleJar(List<String> options) {
        if (GlobalStore.getJarBoot()) {
            String libs = jarHandler.getLibsInJar();
            options.add("-classpath");
            options.add(libs);
        }
    }

    public String buildController(String rootPath, String packageName, String className) {
        StringBuilder builder = new StringBuilder();
        AtomicInteger index = new AtomicInteger(1);
        Map<String, ?> input = GlobalStore.getGlobalResult();
        input.keySet().forEach(k -> {
            String url = k.substring(rootPath.length() + 1, k.lastIndexOf('.')).replaceAll("\\s", "");
            String tempMethod = METHOD_FILE.replace("{{methodUrl}}", url.replace("\\", "/"));
            tempMethod = tempMethod.replace("{{methodName}}", "get" + index.getAndIncrement());
            String methodReturn = input.values().stream().findAny().orElse(null) instanceof byte[] ?
                    "GZIPUtil.decompress((byte[]) GlobalStore.getGlobalResult().get(\"" + StringUtil.escapeRegex(k) + "\"));" :
                    "GlobalStore.getGlobalResult().get(\"" + StringUtil.escapeRegex(k) + "\");";
            tempMethod = tempMethod.replace("{{methodReturn}}", methodReturn);
            builder.append(tempMethod);
        });
        String file = CLASS_FILE.replace("{{methods}}", builder.toString()).replace("{{className}}", className);
        file = file.replace("{{package}}", packageName);
        return file;
    }

    private String compilePrint(Diagnostic<?> diagnostic) {
        return "Source:[" + diagnostic.getSource() + "]\n" +
                "Message:[" + diagnostic.getMessage(null) + "]\n" +
                "LineNumber:[" + diagnostic.getLineNumber() + "]\n" +
                "ColumnNumber:[" + diagnostic.getColumnNumber() + "]\n";
    }
}
