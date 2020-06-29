package com.example.server.handler;

import com.example.core.Invoke;
import com.example.core.MethodParams;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

@ChannelHandler.Sharable
public class MyServerHandler extends SimpleChannelInboundHandler<ByteBuf> {


    private Invoke<byte[]> invoke = new InvokeImpl();


    protected void messageReceived(ChannelHandlerContext ctx, ByteBuf o) {
        ByteBuf protobuf = o.readBytes(o.readableBytes());
        o.writeBytes(protobuf);
        Schema<MethodParams> schema = RuntimeSchema.getSchema(MethodParams.class);
        MethodParams methodParams = schema.newMessage();
        ProtobufIOUtil.mergeFrom(protobuf.array(), methodParams, schema);
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
    }
}
