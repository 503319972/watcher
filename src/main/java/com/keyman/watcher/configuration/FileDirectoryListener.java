package com.keyman.watcher.configuration;


import com.keyman.watcher.file.FilePathHierarchyParser;
import com.keyman.watcher.netty.ConnectCenter;
import com.keyman.watcher.parser.GlobalStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class FileDirectoryListener {
    private static final Logger log = LoggerFactory.getLogger(FileDirectoryListener.class);
    private final String rootPath;
    private final ScheduledExecutorService executor;
    private final FileMapConfiguration configuration;
    private final boolean clusterCopy;

    public FileDirectoryListener(FileMapConfiguration configuration,
                                 String rootPath,
                                 boolean clusterCopy) {
        this.rootPath = rootPath;
        this.configuration = configuration;
        this.executor = new ScheduledThreadPoolExecutor(2);
        this.clusterCopy = clusterCopy;
    }

    public void listen() {
        FilePathHierarchyParser hierarchyParser = new FilePathHierarchyParser(rootPath);
        executor.scheduleWithFixedDelay(() -> {
            AtomicBoolean changed = new AtomicBoolean(false);
            Map<String, Object> globalResult = GlobalStore.getGlobalResultCopy();
            Map<String, ?> fileMap = configuration.isCompacted() ?
                    hierarchyParser.buildHierarchy(true) : hierarchyParser.buildHierarchy();
            Set<String> files = fileMap.keySet();
            Set<String> oldFiles = globalResult.keySet();
            HashMap<String, Object> newMap = new HashMap<>();
            files.stream().filter(file -> !oldFiles.contains(file)).forEach(file -> {
                newMap.put(file, fileMap.get(file));
                changed.set(true);
            });
            if (changed.get()) {
                GlobalStore.putGlobalResult(newMap);
                //configuration.compile(2); //need to compile after a while
                if (clusterCopy) {
                    GlobalStore.setLatestMap(newMap);
                    ConnectCenter center = ConnectCenter.getInstance();
                    center.distributeCopy();
                    GlobalStore.setLatestMoreMapSent();
                }
            }
        }, 0, 5000, TimeUnit.MILLISECONDS);


        executor.scheduleWithFixedDelay(() -> {
            Long latestTime = GlobalStore.getLatestTime();
            Optional.ofNullable(latestTime).ifPresent(last -> {
                long now = System.currentTimeMillis();
                if (now - last >= 6000) {
                    configuration.compile(2);
                }
            });
        }, 0, 5000, TimeUnit.MILLISECONDS);
    }


}
