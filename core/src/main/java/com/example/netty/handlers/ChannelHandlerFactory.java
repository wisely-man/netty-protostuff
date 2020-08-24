package com.example.netty.handlers;

import io.netty.channel.ChannelHandler;

public interface ChannelHandlerFactory {


    ChannelHandler[] handlers();

}
