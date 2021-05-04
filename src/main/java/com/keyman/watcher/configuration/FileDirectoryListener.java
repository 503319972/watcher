package com.keyman.watcher.configuration;


import com.keyman.watcher.file.FilePathHierarchyParser;
import com.keyman.watcher.parser.ResultStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class FileDirectoryListener {
    private static final Logger log = LoggerFactory.getLogger(FileDirectoryListener.class);
    private final String rootPath;
    private Map<String, ?> globalMap;
    private final ScheduledExecutorService executor;
    private final FileMapConfiguration configuration;

    public FileDirectoryListener(FileMapConfiguration configuration,
                                 String rootPath) {
        this.rootPath = rootPath;
        this.configuration = configuration;
        Map<String, ?> globalResult = ResultStore.getGlobalResult();
        if (globalResult.size() > 0) {
            globalMap = globalResult;
        }
        executor = new ScheduledThreadPoolExecutor(1);
    }

    public void listen() {
        FilePathHierarchyParser hierarchyParser = new FilePathHierarchyParser(rootPath);
        executor.scheduleWithFixedDelay(() -> {
            AtomicBoolean changed = new AtomicBoolean(false);
            Map<String, Object> globalResult = ResultStore.getGlobalResult();
            Map<String, ?> fileMap = configuration.isCompacted() ? hierarchyParser.buildHierarchy(true) : hierarchyParser.buildHierarchy();
            Set<String> files = fileMap.keySet();
            Set<String> oldFiles = globalMap.keySet();
            files.stream().filter(file -> !oldFiles.contains(file)).forEach(file -> {
                globalResult.put(file, fileMap.get(file));
                changed.set(true);
            });
            if (changed.get()) {
                ResultStore.setGlobalResult(globalResult);
                configuration.compile(2);
            }
        }, 0, 5000, TimeUnit.MILLISECONDS);
    }
}
