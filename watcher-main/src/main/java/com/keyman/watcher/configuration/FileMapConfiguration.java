package com.keyman.watcher.configuration;

import com.keyman.watcher.controller.Temp;
import com.keyman.watcher.file.ControllerInjectCenter;
import com.keyman.watcher.file.compiler.FileCompiler;
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

    private void dynamicCompile() {
        FilePathHierarchyParser parser = new FilePathHierarchyParser(filePath);
        Map<String, ?> stringStringMap = compacted ? parser.buildHierarchy(true) : parser.buildHierarchy();
        ResultStore.setGlobalResult(stringStringMap);
        Class<?> compileClass = new MemCompiler().compile(filePath, Temp.class, controllerName);
        ControllerInjectCenter.controlCenter(compileClass, context, 1);
    }

    @Override
    public void afterPropertiesSet() {
        dynamicCompile();
    }
}
