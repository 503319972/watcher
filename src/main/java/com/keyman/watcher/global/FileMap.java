package com.keyman.watcher.global;

import com.keyman.watcher.util.StringUtil;

import java.net.InetAddress;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class FileMap {
    /**
     * fileKey: (ip, value)
     */
    private final Map<String, Map<String, Object>> resultMap = new ConcurrentHashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    /**
     *
     * @param host the node flag
     * @param input file map
     */
    public void inputFileMap(String host, Map<String, ?> input) {
        String localHost = GlobalStore.getLocalHost();
        if (StringUtil.isEmpty(host) || host.equals(GlobalStore.getHost())) {
            host = localHost;
        }
        String finalHost = host;
        input.forEach((key, value) -> {
            Map<String, Object> map = resultMap.get(key);
            if (map != null) {
                map.put(finalHost, value);
            } else {
                try {
                    lock.lock();
                    map = resultMap.get(key);
                    if (map == null) {
                        map = new ConcurrentHashMap<>();
                        map.put(finalHost, value);
                        resultMap.put(key, map);
                    } else {
                        map.put(finalHost, value);
                    }
                }
                finally {
                    lock.unlock();
                }
            }
        });
    }

    public Map<String, Map<String, Object>> getResultMap() {
        return resultMap;
    }

    public Map<String, Object> getLocalResultMap() {
        Map<String, Object> map = resultMap.entrySet().stream().
                collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().get(GlobalStore.LOCAL_IP)));
        if (map.size() == 0){
            return Collections.emptyMap();
        }
        return map;
    }
}

