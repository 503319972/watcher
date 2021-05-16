package com.keyman.watcher.netty;

import java.util.List;

public class NettyConfig {
    private static final int MAX_THREADS = 1024;
    private static final int MAX_FRAME_LENGTH = 65535;
    private Integer port;
    private List<String> hosts;
    public static int getMaxFrameLength() {
        return MAX_FRAME_LENGTH;
    }
    public static int getMaxThreads() {
        return MAX_THREADS;
    }
    public Integer getPort() {
        return port == null || port == 0 ? 58898 : port;
    }

    public NettyConfig() {
    }

    public NettyConfig(Integer port) {
        this.port = port;
    }

    public NettyConfig(List<String> hosts) {
        this.hosts = hosts;
    }

    public List<String> getHosts() {
        return hosts;
    }
}
