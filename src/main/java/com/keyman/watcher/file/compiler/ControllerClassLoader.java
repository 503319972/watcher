package com.keyman.watcher.file.compiler;

public class ControllerClassLoader extends ClassLoader {
    private final ControllerFileObject controllerFileObject;

    public ControllerClassLoader(ControllerFileObject controllerFileObject) {
        this.controllerFileObject = controllerFileObject;
    }

    @Override
    protected Class<?> findClass(String name) {
        byte[] bytes = this.controllerFileObject.getBytes();
        return defineClass(name, bytes, 0, bytes.length);

    }
}
