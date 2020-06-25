package com.example.server.handler;

import com.example.entity.Person;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

@ChannelHandler.Sharable
public class MyServerHandler extends SimpleChannelInboundHandler<ByteBuf> {

    protected void messageReceived(ChannelHandlerContext channelHandlerContext, ByteBuf o) {
        ByteBuf protobuf = o.readBytes(o.readableBytes());
        o.writeBytes(protobuf);
        Schema<Person> schema = RuntimeSchema.getSchema(Person.class);
        Person person = schema.newMessage();
        ProtobufIOUtil.mergeFrom(protobuf.array(), person, schema);
        System.out.println(person);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }
}
