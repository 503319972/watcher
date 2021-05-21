package com.keyman.watcher.global;

import com.keyman.watcher.queue.LRUQueue;
import com.keyman.watcher.util.GZIPUtil;
import com.keyman.watcher.util.JsonUtil;
import com.keyman.watcher.util.Retry;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class GlobalStore {
    private GlobalStore() {
    }

    private static String rootPath;
    private static final FileMap RESULT_MAP = new FileMap();
    private static final ChangedMap LATEST_MAP = new ChangedMap();
    private static boolean jarBoot = false;
    private static final LRUQueue<Long> UPDATE_TIME_SERIES = new LRUQueue<>(10);
    public static final String LOCAL_IP = InetAddress.getLoopbackAddress().getHostAddress();
    public static final String LOCAL_HOST_IP;
    private static Integer port = -1;

    static {
        String localHostIp;
        try {
            localHostIp = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            localHostIp = LOCAL_IP;
        }
        LOCAL_HOST_IP = localHostIp;
    }

    public static Integer getPort() {
        return port;
    }

    public static void setPort(Integer port) {
        if (GlobalStore.port.equals(-1)) {
            GlobalStore.port = port;
        }
    }

    public static String getLocalHost() {
        return LOCAL_HOST_IP + ":" + port;
    }

    public static String getHost() {
        return LOCAL_IP + ":" + port;
    }

    public static String getRootPath() {
        return rootPath;
    }

    public static void setRootPath(String rootPath) {
        GlobalStore.rootPath = rootPath;
    }

    public static boolean isFileMapChanged() {
        return LATEST_MAP.isFileMapChanged();
    }

    public static void setFileMapChanged(boolean changed) {
        LATEST_MAP.setFileMapChanged(changed);
    }

    public static void putGlobalResult(String host, Map<String, ?> input) {
        if (input != null && input.size() > 0) {
            RESULT_MAP.inputFileMap(host, input);
            Retry.tryAsync(GlobalStore::putTimeFlag);
        }
    }

    public static void putGlobalResult(Map<String, ?> input) {
        putGlobalResult(null, input);
    }

    public static void setLatestMap(Map<String, Object> input, boolean init) {
        handleLatestMap(input, false);
        if (!init) {
            putTimeFlag();
            setFileMapChanged(true);
        }
    }

    private static void handleLatestMap(Map<String, Object> input, boolean async) {
        if (input != null && input.size() > 0) {
            if (async) {
                Retry.tryAsync(() -> LATEST_MAP.addFileMap(input));
            } else {
                LATEST_MAP.addFileMap(input);
            }
        }
    }

    public static void setLatestMoreMapSent() {
        LATEST_MAP.setSent(true);
    }

    public static boolean getLatestMoreMapSent() {
        return LATEST_MAP.isSent() && LATEST_MAP.getFileMap() != null && LATEST_MAP.getFileMap().size() > 0;
    }

    /**
     * @return the data to send to other node in byte array
     */
    public static byte[] getLatestMoreMapForByte() {
        Map<String, Map<String, Object>> copyMap = new HashMap<>();
        copyMap.put(getLocalHost(), LATEST_MAP.getFileMap());
        return GZIPUtil.compress(JsonUtil.writeToByte(copyMap));
    }

    private static void putTimeFlag() {
        UPDATE_TIME_SERIES.add(System.currentTimeMillis());
    }

    public static Long getLatestTime(){
        return UPDATE_TIME_SERIES.get();
    }

    public static Map<String, Map<String, Object>> getGlobalResultCopy() {
        return new HashMap<>(RESULT_MAP.getResultMap());
    }

    public static Map<String, Object> getLocalGlobalResult() {
        Map<String, Map<String, Object>> input = getGlobalResult();
        Map<String, Object> ipValue = new HashMap<>();
        input.forEach((key, valueMap) -> valueMap.forEach((ip, val) -> {
            if (ip.equals(getHost()) || ip.equals(getLocalHost())) {
                ipValue.put(key, val);
            }
        }));
        return ipValue;
    }

    /**
     * the compiled class will invoke this function
     * @return the file content
     */
    public static Map<String, Map<String, Object>> getGlobalResult() {
        return RESULT_MAP.getResultMap();
    }

    public static void setJarBoot() {
        jarBoot = true;
    }

    public static boolean getJarBoot() {
        return jarBoot;
    }
}
