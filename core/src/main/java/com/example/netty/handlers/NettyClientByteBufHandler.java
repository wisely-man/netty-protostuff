package com.example.netty.handlers;

import com.example.netty.NettyClient;
import com.example.netty.NettyResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;


@ChannelHandler.Sharable
public class NettyClientByteBufHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private Logger logger = LoggerFactory.getLogger(NettyClientByteBufHandler.class);

    private NettyResponse<byte[]> nettyResponse;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {

        System.out.println("rpc channel read");

        nettyResponse = (NettyResponse<byte[]>) ctx.channel().attr(NettyClient.NETTY_CLIENT_PROMISE).get();
        byte[] protobuf = new byte[msg.readableBytes()];
        msg.readBytes(protobuf);
        System.out.println(Arrays.toString(protobuf));
        this.nettyResponse.setSuccess(protobuf);
        ctx.close();
        ctx.channel().close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        this.nettyResponse.setError(cause);
        ctx.close();
        ctx.channel().close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        System.out.println("channel in active :" + ctx.channel().isActive());
    }
}
