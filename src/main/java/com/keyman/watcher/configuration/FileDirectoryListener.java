package com.keyman.watcher.configuration;


import com.keyman.watcher.file.FilePathHierarchyParser;
import com.keyman.watcher.global.GlobalStore;
import com.keyman.watcher.netty.ConnectCenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class FileDirectoryListener {
    private static final Logger log = LoggerFactory.getLogger(FileDirectoryListener.class);
    private final String rootPath;
    private final ScheduledExecutorService executor;
    private final FileMapConfiguration configuration;
    private final boolean clusterCopy;

    public FileDirectoryListener(FileMapConfiguration configuration) {
        this.rootPath = configuration.getFilePath();
        this.configuration = configuration;
        this.executor = new ScheduledThreadPoolExecutor(2);
        this.clusterCopy = configuration.isClusterCopy();
    }

    public void listen() {
        FilePathHierarchyParser hierarchyParser = new FilePathHierarchyParser(rootPath);
        executor.scheduleWithFixedDelay(() -> {
            Map<String, Object> globalResult = GlobalStore.getLocalGlobalResult();
            boolean compacted = configuration.isCompacted();
            Map<String, ?> fileMap = compacted ?
                    hierarchyParser.buildHierarchy(true) : hierarchyParser.buildHierarchy();
            Set<String> files = fileMap.keySet();
            Set<String> oldFiles = globalResult.keySet();
            HashMap<String, Object> newMap = new HashMap<>();
            files.forEach(file -> {
                if(!oldFiles.contains(file)) {
                    newMap.put(file, fileMap.get(file));
                } else {
                    Object targetValue = fileMap.get(file);
                    Object curValue = globalResult.get(file);
                    if (compacted) {
                        if (!Arrays.equals((byte[]) targetValue, (byte[]) curValue)) {
                            newMap.put(file, targetValue);
                        }
                    } else {
                        if (!targetValue.equals(curValue)) {
                            newMap.put(file, targetValue);
                        }
                    }
                }
            });
            if (newMap.size() > 0) {
                GlobalStore.putGlobalResult(newMap);
                //configuration.compile(2); //need to compile after a while
                if (clusterCopy) {
                    GlobalStore.setLatestMap(newMap, false);
                    ConnectCenter center = ConnectCenter.getInstance();
                    center.distributeCopy();
                    GlobalStore.setLatestMoreMapSent();
                } else {
                    GlobalStore.setFileMapChanged(true);
                }
            }
        }, 0, 5000, TimeUnit.MILLISECONDS);


        executor.scheduleWithFixedDelay(() -> {
            Long latestTime = GlobalStore.getLatestTime();
            Optional.ofNullable(latestTime).ifPresent(last -> {
                long now = System.currentTimeMillis();
                if (now - last >= 6000 && GlobalStore.isFileMapChanged()) {
                    log.info("file map changed ---------------------------------");
                    configuration.compile(2, false);
                    GlobalStore.setFileMapChanged(false);
                }
            });
        }, 0, 5000, TimeUnit.MILLISECONDS);
    }


}
