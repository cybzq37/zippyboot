package com.zippyboot.netty.server.tcp;

import com.zippyboot.netty.config.NettyServerProperties;
import com.zippyboot.netty.server.NettyServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TcpNettyServer implements NettyServer {

    private final NettyServerProperties properties;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel channel;
    private volatile boolean running;

    @Override
    public String protocol() {
        return "tcp";
    }

    @Override
    public synchronized void start() throws Exception {
        if (running || !properties.getTcp().isEnabled()) {
            return;
        }

        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new StringDecoder(), new StringEncoder(), new TcpServerHandler());
                    }
                })
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        channel = bootstrap.bind(properties.getTcp().getHost(), properties.getTcp().getPort()).sync().channel();
        running = true;
        log.info("TCP server started on {}:{}", properties.getTcp().getHost(), properties.getTcp().getPort());
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
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
            bossGroup = null;
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
            workerGroup = null;
        }
        running = false;
        log.info("TCP server stopped");
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
