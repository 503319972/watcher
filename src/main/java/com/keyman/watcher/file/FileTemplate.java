package com.keyman.watcher.file;

import com.keyman.watcher.global.GlobalStore;
import com.keyman.watcher.util.FileUtil;
import com.keyman.watcher.util.GZIPUtil;
import com.keyman.watcher.util.JsonUtil;
import com.keyman.watcher.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class FileTemplate {
    private static final Logger log = LoggerFactory.getLogger(FileTemplate.class);
    private static final String CLASS_FILE = FileUtil.loadFile("FileTemplate");
    private static final String METHOD_FILE = FileUtil.loadFile("MethodTemplate");

    private FileTemplate() {
    }

    public static String buildController(String packageName, String className) {
        StringBuilder builder = new StringBuilder();
        AtomicInteger index = new AtomicInteger(1);
        Map<String, Map<String, Object>> input = GlobalStore.getGlobalResultCopy();

        // fileKey: (ip, value)
        input.forEach((fileKey, v) -> {
            log.debug("cur path: {}", fileKey);
            String tempMethod = METHOD_FILE.replace("{{methodUrl}}", fileKey);
            tempMethod = tempMethod.replace("{{methodName}}", "get" + index.getAndIncrement());
            String methodReturn = "handleResult(\"" + fileKey + "\");";
            tempMethod = tempMethod.replace("{{methodReturn}}", methodReturn);
            builder.append(tempMethod);
        });
        String file = CLASS_FILE.replace("{{methods}}", builder.toString()).replace("{{className}}", className);
        file = file.replace("{{package}}", packageName);
        return file;
    }
}
