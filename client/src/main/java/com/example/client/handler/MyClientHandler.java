package com.example.client.handler;

import com.example.entity.Person;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

public class MyClientHandler extends SimpleChannelInboundHandler {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 组装数据
        Person person = new Person("张三", 18);
        LinkedBuffer buffer = LinkedBuffer.allocate();
        Schema<Person> schema = RuntimeSchema.getSchema(Person.class);
        byte[] protobuf = ProtobufIOUtil.toByteArray(person, schema, buffer);
        ctx.writeAndFlush(Unpooled.copiedBuffer(protobuf));
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
