package com.keyman.watcher.file;

import com.keyman.watcher.parser.GlobalStore;
import com.keyman.watcher.util.FileUtil;
import com.keyman.watcher.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class FileTemplate {
    private static final Logger log = LoggerFactory.getLogger(FileTemplate.class);
    private static final String CLASS_FILE = FileUtil.loadFile("FileTemplate");
    private static final String METHOD_FILE = FileUtil.loadFile("MethodTemplate");

    private FileTemplate() {
    }

    public static String buildController(String rootPath, String packageName, String className) {
        StringBuilder builder = new StringBuilder();
        AtomicInteger index = new AtomicInteger(1);
        Map<String, ?> input = GlobalStore.getGlobalResult();
        log.debug("root path: {}", rootPath);
        if (rootPath.endsWith("/") || rootPath.endsWith("\\")) {
            rootPath = rootPath.substring(0, rootPath.length() - 1);
        }
        String finalRootPath = rootPath;
        input.keySet().forEach(k -> {
            log.debug("cur path: {}", k);
            String url = k.substring(finalRootPath.length() + 1, k.lastIndexOf('.')).replaceAll("\\s", "");
            String tempMethod = METHOD_FILE.replace("{{methodUrl}}", url.replace("\\", "/"));
            tempMethod = tempMethod.replace("{{methodName}}", "get" + index.getAndIncrement());
            String methodReturn = input.values().stream().findAny().orElse(null) instanceof byte[] ?
                    "GZIPUtil.decompress((byte[]) GlobalStore.getGlobalResult().get(\"" + StringUtil.escapeRegex(k) + "\"));" :
                    "GlobalStore.getGlobalResult().get(\"" + StringUtil.escapeRegex(k) + "\");";
            tempMethod = tempMethod.replace("{{methodReturn}}", methodReturn);
            builder.append(tempMethod);
        });
        String file = CLASS_FILE.replace("{{methods}}", builder.toString()).replace("{{className}}", className);
        file = file.replace("{{package}}", packageName);
        return file;
    }
}
