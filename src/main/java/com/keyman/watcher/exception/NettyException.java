package com.keyman.watcher.exception;

public class NettyException extends RuntimeException {
    public NettyException(String message) {
        super(message);
    }

    public NettyException() {
        super("unexpected netty error");
    }

    public NettyException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
