package com.keyman.watcher.configuration;

import com.keyman.watcher.file.ControllerInjectCenter;
import com.keyman.watcher.file.FilePathHierarchyParser;
import com.keyman.watcher.file.FileTemplate;
import com.keyman.watcher.file.jar.JarHandler;
import com.keyman.watcher.file.compilation.MemCompiler;
import com.keyman.watcher.netty.ConnectCenter;
import com.keyman.watcher.netty.strategy.LeaderCopyStrategy;
import com.keyman.watcher.netty.strategy.StarCopyStrategy;
import com.keyman.watcher.netty.strategy.Strategy;
import com.keyman.watcher.parser.GlobalStore;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@ConfigurationProperties(
        prefix = "watcher"
)
public class FileMapConfiguration implements InitializingBean {

    private String filePath;
    private String controllerName;
    private boolean listened = false;
    private boolean compacted = false;
    private List<String> hosts;
    private boolean clusterCopy = false;
    private ClusterStrategy clusterStrategy;

    @Autowired
    ApplicationContext context;

    @Autowired
    private JarHandler jarHandler;

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setControllerName(String controllerName) {
        this.controllerName = controllerName;
    }

    public boolean isCompacted() {
        return compacted;
    }

    public void setCompacted(boolean compacted) {
        this.compacted = compacted;
    }

    public String getControllerName() {
        return controllerName;
    }

    public boolean isListened() {
        return listened;
    }

    public void setListened(boolean listened) {
        this.listened = listened;
    }

    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public ClusterStrategy getClusterStrategy() {
        return clusterStrategy;
    }

    public void setClusterStrategy(ClusterStrategy clusterStrategy) {
        this.clusterStrategy = clusterStrategy;
    }

    public boolean isClusterCopy() {
        return clusterCopy;
    }

    public void setClusterCopy(boolean clusterCopy) {
        this.clusterCopy = clusterCopy;
    }

    // init
    private void dynamicCompile() {
        FilePathHierarchyParser parser = new FilePathHierarchyParser(filePath);
        Map<String, ?> stringStringMap = compacted ? parser.buildHierarchy(true) : parser.buildHierarchy();
        GlobalStore.putGlobalResult(stringStringMap);
        Map<String, Object> moreMap = Collections.unmodifiableMap(stringStringMap);
        boolean isCluster = !CollectionUtils.isEmpty(hosts) && hosts.size() > 1 && clusterCopy;
        compile(1);
        if (isCluster) {
            GlobalStore.setLatestMap(moreMap);
            startNetty();
        }
    }

    public void compile(int type) {
        String content = FileTemplate.buildController(filePath, "", controllerName);
        Class<?> compileClass = new MemCompiler(jarHandler).compile(content, "", controllerName);
        ControllerInjectCenter.controlCenter(compileClass, context, type);
    }

    private void startNetty() {
        Strategy strategy = Optional.ofNullable(getClusterStrategy()).map(c -> c.strategy).orElse(new StarCopyStrategy());
        ConnectCenter connectCenter = ConnectCenter.getInstance(strategy);
        connectCenter.startServer();
        connectCenter.startClient(getHosts());
    }

    @Override
    public void afterPropertiesSet() {
        String commandLine = System.getProperty("sun.java.command");
        if (commandLine.contains("boot=jar")){
            GlobalStore.setJarBoot();
        }
        dynamicCompile();
        if (listened) {
            FileDirectoryListener listener = new FileDirectoryListener(this, filePath, clusterCopy);
            listener.listen();
        }
    }

    public static class ClusterStrategy {
        private boolean enableStarCopy = false;
        private boolean enableLeaderCopy = false;
        private Strategy strategy;

        public boolean isEnableStarCopy() {
            return enableStarCopy;
        }

        public void setEnableStarCopy(boolean enableStarCopy) {
            this.enableStarCopy = enableStarCopy;
            if (enableStarCopy) {
                strategy = new StarCopyStrategy();
            }
        }

        public boolean isEnableLeaderCopy() {
            return enableLeaderCopy;
        }

        public void setEnableLeaderCopy(boolean enableLeaderCopy) {
            this.enableLeaderCopy = enableLeaderCopy;
            if (enableLeaderCopy) {
                strategy = new LeaderCopyStrategy();
            }
        }

        public Strategy getStrategy() {
            if (strategy == null) {
                return new StarCopyStrategy();
            }
            return strategy;
        }
    }
}
