package com.zippyboot.netty.server;

public interface NettyServer {

    String protocol();

    void start() throws Exception;

    void stop();

    boolean isRunning();
}
