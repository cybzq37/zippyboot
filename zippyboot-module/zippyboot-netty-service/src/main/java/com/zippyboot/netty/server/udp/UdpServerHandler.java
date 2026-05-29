package com.zippyboot.netty.server.udp;

import io.netty.buffer.Unpooled;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

public class UdpServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    protected void channelRead0(io.netty.channel.ChannelHandlerContext ctx, DatagramPacket packet) {
        String payload = packet.content().toString(CharsetUtil.UTF_8).trim();
        String response = "udp-echo:" + payload;
        ctx.writeAndFlush(new DatagramPacket(
                Unpooled.copiedBuffer(response, CharsetUtil.UTF_8),
                packet.sender()
        ));
    }
}
