package com.keyman.watcher.exception;

public class UnknownResultFormatException extends RuntimeException {
    public UnknownResultFormatException(String message) {
        super(message);
    }

    public UnknownResultFormatException() {
        super("unknown file parse result format");
    }
}
