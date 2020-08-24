package com.example.netty.handlers;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.stream.ChunkedWriteHandler;

public class DefaultHttpChannelHandlerFactory implements ChannelHandlerFactory{

    @Override
    public ChannelHandler[] handlers() {
        return new ChannelHandler[]{
                new HttpClientCodec(), // 编解码器
                new HttpObjectAggregator(1024 * 10 * 1024), // 聚合
                new HttpContentDecompressor(), // 解压
                new ChunkedWriteHandler(), // 大数据
//        new IdleStateHandler(2,2,2, TimeUnit.SECONDS), // 心跳
                new NettyClientHttpObjHandler(), // 自定义处理类
        };
    }
}
