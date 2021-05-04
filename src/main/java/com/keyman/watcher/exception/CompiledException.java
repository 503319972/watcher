package com.keyman.watcher.exception;

public class CompiledException extends RuntimeException{
    public CompiledException(String message) {
        super(message);
    }

    public CompiledException(Throwable cause) {
        super(cause);
    }

    public CompiledException() {
        super("compiled fail");
    }
}
