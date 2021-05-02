package com.keyman.watcher.parser;

import java.util.HashMap;
import java.util.Map;

public class ResultStore {
    private static Map<String, String> resultMap = null;

    public static void setGlobalResult(Map<String, String> input) {
        if (resultMap == null) {
            synchronized (ResultStore.class) {
                if (resultMap == null) {
                    resultMap = input;
                }
            }
        }
    }

    public static Map<String, String> getGlobalResult() {
        return resultMap == null ? new HashMap<>() : resultMap;
    }
}
