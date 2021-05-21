package com.keyman.watcher.global;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChangedMap {
    private Map<String, Object> fileMap;
    private volatile boolean sent;
    private final AtomicBoolean fileMapChanged = new AtomicBoolean(false);

    public ChangedMap() {
        this(new HashMap<>(), true);
    }

    public ChangedMap(Map<String, Object> fileMap) {
        this(fileMap, true);
    }

    public ChangedMap(Map<String, Object> fileMap, boolean sent) {
        if (fileMap == null) {
            throw new IllegalArgumentException("file map cannot be null");
        }
        this.fileMap = fileMap;
        this.sent = sent;
    }

    public void setFileMap(Map<String, Object> fileMap) {
        this.fileMap = fileMap;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public Map<String, Object> getFileMap() {
        return fileMap;
    }

    public boolean isSent() {
        return sent;
    }

    public boolean isFileMapChanged() {
        return fileMapChanged.get();
    }

    public void setFileMapChanged(boolean changed) {
        fileMapChanged.compareAndSet(Boolean.FALSE.equals(changed), changed);
    }

    public void addFileMap(Map<String, Object> input) {
        while (true) { if (isSent()) break;}
        fileMap = input;
        setSent(false);
    }
}
