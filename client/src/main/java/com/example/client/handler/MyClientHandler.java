package com.example.client.handler;

import com.example.core.MethodParams;
import com.example.entity.Person;
import com.example.service.PersonService;
import io.netty.buffer.ByteBuf;
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
        MethodParams methodParams = new MethodParams();
        methodParams.setServiceId(1);
        methodParams.setServiceClazz(PersonService.class);
        methodParams.setMethodName("load");
        methodParams.setParams(2);
        methodParams.setReturnClazz(Person.class);
        LinkedBuffer buffer = LinkedBuffer.allocate();
        Schema<MethodParams> schema = RuntimeSchema.getSchema(MethodParams.class);
        byte[] protobuf = ProtobufIOUtil.toByteArray(methodParams, schema, buffer);
        ctx.writeAndFlush(Unpooled.copiedBuffer(protobuf));
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf o = (ByteBuf) msg;
        ByteBuf protobuf = o.readBytes(o.readableBytes());
        o.writeBytes(protobuf);
        Schema<Person> schema = RuntimeSchema.getSchema(Person.class);
        Person person = schema.newMessage();
        ProtobufIOUtil.mergeFrom(protobuf.array(), person, schema);
        System.out.println(person);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
