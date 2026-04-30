package com.zippyboot.netty.server.tcp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class TcpServerHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        ctx.writeAndFlush("tcp-echo:" + msg + System.lineSeparator());
    }
}
