package com.keyman.watcher.file.compilation;

import com.keyman.watcher.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControllerClassLoader extends ClassLoader {
    private static final Logger log = LoggerFactory.getLogger(ControllerClassLoader.class);
    private final ControllerFileObject controllerFileObject;
    private final String specifiedClass;

    public ControllerClassLoader(ControllerFileObject controllerFileObject,
                                 String specifiedClass) {
        this.controllerFileObject = controllerFileObject;
        if (StringUtil.isEmpty(specifiedClass)) {
            throw new IllegalArgumentException("target compiling class cannot be null");
        }
        this.specifiedClass = specifiedClass;
    }

    @Override
    protected Class<?> findClass(String name) {
        if (specifiedClass.equals(name)) {
            byte[] bytes = this.controllerFileObject.getBytes();
            return defineClass(name, bytes, 0, bytes.length);
        }
        else {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException e) {
                log.error("cannot define class", e);
            }
        }
        return null;
    }
}
