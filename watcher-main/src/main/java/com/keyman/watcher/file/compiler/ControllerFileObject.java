package com.keyman.watcher.file.compiler;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;

public class ControllerFileObject extends SimpleJavaFileObject {
    ByteArrayOutputStream outputStream;

    public ControllerFileObject(String className, Kind kind) {
        super(URI.create(className + kind.extension), kind);
        this.outputStream = new ByteArrayOutputStream();
    }

    @Override
    public OutputStream openOutputStream() {
        return this.outputStream;
    }

    public byte[] getBytes() {
        return this.outputStream.toByteArray();
    }
}
