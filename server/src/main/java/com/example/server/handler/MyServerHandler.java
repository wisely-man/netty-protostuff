package com.example.server.handler;

import com.example.core.Invoke;
import com.example.core.MethodParams;
import com.example.util.SerializerUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class MyServerHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private Invoke<byte[]> invoke = new InvokeImpl();

    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf o) {

        byte[] protobuf = new byte[o.readableBytes()];
        o.readBytes(protobuf);
        MethodParams methodParams = SerializerUtils.deserializer(protobuf, MethodParams.class);
        System.out.println(methodParams);

        try {
            byte[] returns = invoke.invoke(methodParams);
            ctx.writeAndFlush(Unpooled.copiedBuffer(returns));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.writeAndFlush(cause);
    }
}
