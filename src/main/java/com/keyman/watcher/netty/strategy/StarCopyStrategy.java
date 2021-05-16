package com.keyman.watcher.netty.strategy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.keyman.watcher.netty.client.Client;
import com.keyman.watcher.parser.GlobalStore;
import com.keyman.watcher.util.GZIPUtil;
import com.keyman.watcher.util.JsonUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public class StarCopyStrategy implements Strategy {
    private static final Logger log = LoggerFactory.getLogger(StarCopyStrategy.class);

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
                  Map<String, Object> remoteMap = JsonUtil.fromJson(message, new TypeReference<Map<String, Object>>() {});
                  Optional.ofNullable(remoteMap).ifPresent(map -> {
                      GlobalStore.putGlobalResult(remoteMap);
                      channel.writeAndFlush("COPYOK");
                      log.info("receive remote file map");
                  });
                  if (remoteMap == null) {
                      channel.writeAndFlush("COPYFAIL");
                  }
              }
           }
        };
    }

    @Override
    public BiConsumer<ChannelHandlerContext, Object> getClientHandler() {
        return (ctx, obj) -> {
            String message = obj instanceof String ?
                    (String) obj : new String((byte[]) obj);
            if ("COPYOK".equals(message)) {
                log.info("copy data succeed");
            } else if ("COPYFAIL".equals(message)) {
                ctx.writeAndFlush(GlobalStore.getLatestMoreMapForByte());
                log.info("receive copy fail, resend the copy data");
            }
        };
    }
}
