package com.keyman.watcher.netty;

import com.keyman.watcher.util.StringUtil;
import org.springframework.stereotype.Component;

@Component
public class NettyConfig {
    public static volatile String MASTER_IP = "";
    private static final int MAX_THREADS = 1024;
    private static final int MAX_FRAME_LENGTH = 65535;
    private Integer port;
    public String getUrl() {
        return StringUtil.isBlank(MASTER_IP) ? "127.0.0.1" : MASTER_IP;
    }
    public Integer getPort() {
        return port == null || port == 0 ? 58898 : port;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public static int getMaxFrameLength() {
        return MAX_FRAME_LENGTH;
    }
    public static int getMaxThreads() {
        return MAX_THREADS;
    }
}
