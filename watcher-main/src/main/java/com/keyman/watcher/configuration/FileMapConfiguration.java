package com.keyman.watcher.configuration;

import com.keyman.watcher.controller.Temp;
import com.keyman.watcher.file.ControllerInjectCenter;
import com.keyman.watcher.file.FilePathHierarchyParser;
import com.keyman.watcher.file.compiler.MemCompiler;
import com.keyman.watcher.parser.ResultStore;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(
        prefix = "watcher"
)
public class FileMapConfiguration implements InitializingBean {

    private String filePath;
    private String controllerName;
    private boolean listened = false;
    private boolean compacted = false;

    @Autowired
    ApplicationContext context;

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setControllerName(String controllerName) {
        this.controllerName = controllerName;
    }

    public boolean isCompacted() {
        return compacted;
    }

    public void setCompacted(boolean compacted) {
        this.compacted = compacted;
    }

    public String getControllerName() {
        return controllerName;
    }

    public boolean isListened() {
        return listened;
    }

    public void setListened(boolean listened) {
        this.listened = listened;
    }

    private void dynamicCompile() {
        FilePathHierarchyParser parser = new FilePathHierarchyParser(filePath);
        Map<String, ?> stringStringMap = compacted ? parser.buildHierarchy(true) : parser.buildHierarchy();
        ResultStore.setGlobalResult(stringStringMap);
        compile(1);
    }

    public void compile(int type) {
        Class<?> compileClass = new MemCompiler().compile(filePath, Temp.class, controllerName);
        ControllerInjectCenter.controlCenter(compileClass, context, type);
    }

    @Override
    public void afterPropertiesSet() {
        dynamicCompile();
        if (listened) {
            FileDirectoryListener listener = new FileDirectoryListener(this, filePath);
            listener.listen();
        }
    }
}
