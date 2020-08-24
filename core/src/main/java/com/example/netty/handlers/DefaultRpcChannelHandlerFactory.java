package com.example.netty.handlers;

import io.netty.channel.ChannelHandler;

public class DefaultRpcChannelHandlerFactory implements ChannelHandlerFactory{
    
    @Override
    public ChannelHandler[] handlers() {
        return new ChannelHandler[]{
//            new IdleStateHandler(2,2,2, TimeUnit.SECONDS), // 心跳
                new NettyClientByteBufHandler()
        };
    }
}
