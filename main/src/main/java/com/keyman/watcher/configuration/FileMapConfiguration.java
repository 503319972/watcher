package com.keyman.watcher.configuration;

import com.keyman.watcher.controller.Temp;
import com.keyman.watcher.file.Compiler;
import com.keyman.watcher.file.FilePathHierarchyParser;
import com.keyman.watcher.parser.ResultStore;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(
        prefix = "watcher"
)
public class FileMapConfiguration {

    private boolean complied = false;
    private String filePath;
    private String controllerName;

    public void setFilePath(String filePath) {
        this.filePath = filePath;
        if (controllerName != null && !complied) {
            dynamicCompile();
        }
    }

    public String getFilePath() {
        return filePath;
    }

    public void setControllerName(String controllerName) {
        this.controllerName = controllerName;
        if (filePath != null && !complied) {
            dynamicCompile();
        }
    }

    public String getControllerName() {
        return controllerName;
    }

    private void dynamicCompile() {
        FilePathHierarchyParser parser = new FilePathHierarchyParser(filePath);
        Map<String, String> stringStringMap = parser.buildHierarchy();
        ResultStore.setGlobalResult(stringStringMap);
        new Compiler().compile(stringStringMap, filePath, Temp.class, controllerName);
        complied = false;
    }
}
