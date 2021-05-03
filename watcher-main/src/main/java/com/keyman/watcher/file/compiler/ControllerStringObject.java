package com.keyman.watcher.file.compiler;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import java.net.URI;

public class ControllerStringObject extends SimpleJavaFileObject {
    private final String content;

    public ControllerStringObject(String name, JavaFileObject.Kind kind, String content) {
        super(URI.create(name), kind);
        this.content = content;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return this.content;
    }
}
