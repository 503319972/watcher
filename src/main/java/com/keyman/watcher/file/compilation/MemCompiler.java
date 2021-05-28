package com.keyman.watcher.file.compilation;

import com.keyman.watcher.file.jar.JarHandler;
import com.keyman.watcher.global.GlobalStore;
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

public class MemCompiler {
    private static final Logger log = LoggerFactory.getLogger(MemCompiler.class);
    private static final List<String> optionLibs = new ArrayList<>();
    private final JarHandler jarHandler;

    public MemCompiler(JarHandler jarHandler) {
        this.jarHandler = jarHandler;
    }
    public Class<?> compile(String content, String packageName, String targetClassName, boolean init)
    {
        try {
            log.debug("class content: {}", content);
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(null, null, null);
            ControllerFileManager controllerFileManager = new ControllerFileManager(standardFileManager);
            ControllerStringObject stringObject = new ControllerStringObject(targetClassName + ".java",
                    JavaFileObject.Kind.SOURCE, content);

            DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();

            if (init) {
                handleJar();
            }
            JavaCompiler.CompilationTask task = compiler.getTask(null, controllerFileManager, diagnosticCollector,
                    optionLibs, null, Collections.singletonList(stringObject));
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

    private void handleJar() {
        if (GlobalStore.getJarBoot()) {
            String libs = jarHandler.getLibsInJar();
            optionLibs.add("-classpath");
            optionLibs.add(libs);
        }
    }

    private String compilePrint(Diagnostic<?> diagnostic) {
        return "Source:[" + diagnostic.getSource() + "]\n" +
                "Message:[" + diagnostic.getMessage(null) + "]\n" +
                "LineNumber:[" + diagnostic.getLineNumber() + "]\n" +
                "ColumnNumber:[" + diagnostic.getColumnNumber() + "]\n";
    }
}
