package com.example.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;


@ChannelHandler.Sharable
public class NettyClientByteBufHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private Logger logger = LoggerFactory.getLogger(NettyClientByteBufHandler.class);

    private NettyResponse<byte[]> nettyResponse;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        nettyResponse = (NettyResponse<byte[]>) ctx.channel().attr(NettyClient.NETTY_CLIENT_PROMISE).get();

        byte[] protobuf = new byte[msg.readableBytes()];
        msg.readBytes(protobuf);

        System.out.println(Arrays.toString(protobuf));

        this.nettyResponse.set(protobuf);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        this.nettyResponse.set(null);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        System.out.println("channel in active");
    }


}
