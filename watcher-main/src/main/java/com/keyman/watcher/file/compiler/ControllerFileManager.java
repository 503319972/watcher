package com.keyman.watcher.file.compiler;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

public class ControllerFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
    private ControllerFileObject controllerFileObject;

    public ControllerFileManager(StandardJavaFileManager fileManager) {
        super(fileManager);
    }
    public ControllerFileObject getControllerFileObject() {
        return controllerFileObject;

    }

    @Override
    public JavaFileObject getJavaFileForOutput(JavaFileManager.Location location, String className,
                                               JavaFileObject.Kind kind, FileObject sibling) {
        controllerFileObject = new ControllerFileObject(className, kind);
        return controllerFileObject;
    }
}
