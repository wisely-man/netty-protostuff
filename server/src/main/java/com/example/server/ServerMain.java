package com.example.server;

import com.example.server.handler.MyServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class ServerMain {

    public static void main(String[] args) throws InterruptedException {

        ServerBootstrap bootstrap = new ServerBootstrap();

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();

        bootstrap.group(bossGroup, workGroup);
        bootstrap.channel(NioServerSocketChannel.class);

        //channel的属性配置
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) {
                ch.pipeline().addLast("myHandler", new MyServerHandler()); //请求匹配处理
            }
        })
        ;

        ChannelFuture channelFuture = bootstrap.bind(8080);
        channelFuture.addListener(future -> System.out.println("server start..."));

        //通过引入监听器对象监听future状态，当future任务执行完成后会调用-》{}内的方法
        channelFuture.channel().closeFuture().sync().addListener(future -> {
            System.out.println("server shut down...");

            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        });
    }


}
