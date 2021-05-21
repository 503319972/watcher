package com.keyman.watcher.netty.strategy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.keyman.watcher.netty.client.Client;
import com.keyman.watcher.global.GlobalStore;
import com.keyman.watcher.util.GZIPUtil;
import com.keyman.watcher.util.JsonUtil;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class StarCopyStrategy implements Strategy {
    private static final Logger log = LoggerFactory.getLogger(StarCopyStrategy.class);

    private boolean compact;

    public StarCopyStrategy() {
        this(false);
    }

    public StarCopyStrategy(boolean compact) {
        this.compact = compact;
    }

    @Override
    public void distribute(Client client, byte[] msg) {
        List<String> channels = client.getConnectedChannels();
        channels.forEach(channel -> client.sendMsg(channel, msg));
    }

    @Override
    public BiConsumer<ChannelHandlerContext, Object> getServerHandler() {
        return (channel, msg) -> {
           if (msg instanceof byte[]) {
              byte[] message = GZIPUtil.decompressToByte((byte[]) msg);
              if (message.length > 0) {
                  Map<String, Map<String, byte[]>> remoteMap = JsonUtil.fromJson(message,
                          new TypeReference<Map<String, Map<String, byte[]>>>() {});
                  if (remoteMap != null && remoteMap.size() == 1) {
                      String host = remoteMap.keySet().iterator().next();
                      GlobalStore.putGlobalResult(host, remoteMap.get(host));
                      GlobalStore.setFileMapChanged(true);
                      channel.writeAndFlush("COPYOK");
                      log.info("receive remote file map");
                  }
                  if (remoteMap == null) {
                      channel.writeAndFlush("COPYFAIL");
                  }
              }
           }
        };
    }

    @Override
    public BiPredicate<ChannelHandlerContext, Object> getClientHandler() {
        return (ctx, obj) -> {
            String message = obj instanceof String ?
                    (String) obj : new String((byte[]) obj);
            if ("COPYOK".equals(message)) {
                log.info("copy data succeed");
                return true;
            } else if ("COPYFAIL".equals(message)) {
                ctx.writeAndFlush(GlobalStore.getLatestMoreMapForByte());
                log.info("receive copy fail, resend the copy data");
            }
            return false;
        };
    }
}
