package com.keyman.watcher.file.compiler;

import com.keyman.watcher.parser.ResultStore;
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
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MemCompiler extends ClassLoader {
    private static final Logger log = LoggerFactory.getLogger(MemCompiler.class);
    private static final String CLASS_FILE = FileUtil.loadFile("FileTemplate");
    private static final String METHOD_FILE = FileUtil.loadFile("MethodTemplate");

    public MemCompiler()
    {
        super(ClassLoader.getSystemClassLoader());
    }

    public Class<?> compile(String apiRootPath, Class<?> rootCLass, String targetClassName)
    {
        try {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            String packageName = rootCLass.getPackage().getName();
            String content = buildController(apiRootPath, packageName, targetClassName);

            StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(null, null, null);
            ControllerFileManager controllerFileManager = new ControllerFileManager(standardFileManager);
            ControllerStringObject stringObject = new ControllerStringObject(targetClassName + ".java",
                    JavaFileObject.Kind.SOURCE, content);

            DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();

            StringBuilder cp = new StringBuilder();
            URLClassLoader urlClassLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
            for (URL url : urlClassLoader.getURLs()) {
                cp.append(url.getFile()).append(File.pathSeparator);
            }
            List<String> options = new ArrayList<>();
            options.add("-classpath");
            options.add(cp.toString());

            JavaCompiler.CompilationTask task = compiler.getTask(null, controllerFileManager, diagnosticCollector,
                    options, null, Collections.singletonList(stringObject));
            if (Boolean.TRUE.equals(task.call())) {
                ControllerFileObject controllerFileObject = controllerFileManager.getControllerFileObject();
                ClassLoader classLoader = new ControllerClassLoader(controllerFileObject);
                return classLoader.loadClass(packageName + "." + targetClassName);
            } else {
                StringBuilder error = new StringBuilder();
                for (Diagnostic<?> diagnostic : diagnosticCollector.getDiagnostics()) {
                    error.append(compilePrint(diagnostic));
                }
                throw new UnsupportedOperationException(error.toString());
            }
        } catch (ClassNotFoundException e) {
            log.error("load auto-inject file controller failed", e);
        }
        return null;
    }

    public String buildController(String rootPath, String packageName, String className) {
        StringBuilder builder = new StringBuilder();
        AtomicInteger index = new AtomicInteger(1);
        Map<String, ?> input = ResultStore.getGlobalResult();
        input.keySet().forEach(k -> {
            String url = k.substring(rootPath.length() + 1, k.lastIndexOf('.')).replaceAll("\\s", "");
            String tempMethod = METHOD_FILE.replace("{{methodUrl}}", url.replace("\\", "/"));
            tempMethod = tempMethod.replace("{{methodName}}", "get" + index.getAndIncrement());
            String methodReturn = input.values().stream().findAny().orElse(null) instanceof byte[] ?
                    "GZIPUtil.decompress((byte[]) ResultStore.getGlobalResult()" + ".get(\"" + StringUtil.escapeRegex(k) + "\"));" :
                    "ResultStore.getGlobalResult()" + ".get(\"" + StringUtil.escapeRegex(k) + "\");";
            tempMethod = tempMethod.replace("{{methodReturn}}", methodReturn);
            builder.append(tempMethod);
        });
        String file = CLASS_FILE.replace("{{methods}}", builder.toString()).replace("{{className}}", className);
        file = file.replace("{{package}}", packageName);
        return file;
    }

    private String compilePrint(Diagnostic<?> diagnostic) {
        return "Code:[" + diagnostic.getCode() + "]\n" +
                "Kind:[" + diagnostic.getKind() + "]\n" +
                "Position:[" + diagnostic.getPosition() + "]\n" +
                "Start Position:[" + diagnostic.getStartPosition() + "]\n" +
                "End Position:[" + diagnostic.getEndPosition() + "]\n" +
                "Source:[" + diagnostic.getSource() + "]\n" +
                "Message:[" + diagnostic.getMessage(null) + "]\n" +
                "LineNumber:[" + diagnostic.getLineNumber() + "]\n" +
                "ColumnNumber:[" + diagnostic.getColumnNumber() + "]\n";
    }
}
