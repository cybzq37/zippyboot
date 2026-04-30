package com.zippyboot.netty.server.udp;

import com.zippyboot.netty.config.NettyServerProperties;
import com.zippyboot.netty.server.NettyServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UdpNettyServer implements NettyServer {

    private final NettyServerProperties properties;

    private EventLoopGroup group;
    private Channel channel;
    private volatile boolean running;

    @Override
    public String protocol() {
        return "udp";
    }

    @Override
    public synchronized void start() throws Exception {
        if (running || !properties.getUdp().isEnabled()) {
            return;
        }

        group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, false)
                .handler(new UdpServerHandler());

        channel = bootstrap.bind(properties.getUdp().getHost(), properties.getUdp().getPort()).sync().channel();
        running = true;
        log.info("UDP server started on {}:{}", properties.getUdp().getHost(), properties.getUdp().getPort());
    }

    @Override
    public synchronized void stop() {
        if (!running) {
            return;
        }
        if (channel != null) {
            channel.close();
            channel = null;
        }
        if (group != null) {
            group.shutdownGracefully();
            group = null;
        }
        running = false;
        log.info("UDP server stopped");
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
