package com.keyman.watcher.parser;

import java.util.Arrays;

public enum ResultFormat {
    JSON("json"),
    TXT("txt"),
    XML("xml");

    private final String format;
    ResultFormat(String format) {
        this.format = format;
    }

    public String getFormat() {
        return format;
    }

    public static ResultFormat Of(String postfix) {
        return Arrays.stream(values()).filter(e -> e.format.equals(postfix)).findFirst().orElse(null);
    }
}
