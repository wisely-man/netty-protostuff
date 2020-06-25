package com.example.client;

import com.example.client.handler.MyClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

public class ClientMain {

    private final static String HOST = "127.0.0.1";
    private final static Integer PORT = 8080;

    public static void main(String[] args) throws InterruptedException {

        Bootstrap b = new Bootstrap();

        EventLoopGroup workerGroup = new NioEventLoopGroup();

        b.group(workerGroup)
            .channel(NioSocketChannel.class)
            .remoteAddress(new InetSocketAddress(HOST, PORT))
            .handler(new ChannelInitializer<SocketChannel>(){
                @Override
                protected void initChannel(SocketChannel ch) {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast("handler", new MyClientHandler());
                }
            });


        ChannelFuture f = b.connect().sync();
        System.out.println("Client connected");
        f.channel().closeFuture().sync();
    }


}
