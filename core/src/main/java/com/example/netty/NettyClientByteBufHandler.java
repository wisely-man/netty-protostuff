package com.example.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@ChannelHandler.Sharable
public class NettyClientByteBufHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private Logger logger = LoggerFactory.getLogger(NettyClientByteBufHandler.class);

    private Promise<byte[]> promise;

    public void setPromise(Promise promise) {
        this.promise = promise;
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        logger.debug("message received");
        this.promise.setSuccess(msg.readBytes(msg.readableBytes()).array());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("NettyClientByteBufHandler error: {}", cause);
        this.promise.tryFailure(cause);
    }
}
