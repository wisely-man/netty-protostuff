package com.example.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;

@ChannelHandler.Sharable
public class NettyClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private Promise<byte[]> promise;

    public void setPromise(Promise promise) {
        this.promise = promise;
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        System.out.println("message received");
        this.promise.setSuccess(msg.readBytes(msg.readableBytes()).array());
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("exceptionCaught");
        cause.printStackTrace();
        this.promise.setFailure(cause);
        ctx.close();
    }
}
