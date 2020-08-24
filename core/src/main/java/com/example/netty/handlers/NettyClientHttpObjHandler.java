package com.example.netty.handlers;

import com.example.netty.NettyClient;
import com.example.netty.NettyResponse;
import com.wisely.core.exception.SystemException;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class NettyClientHttpObjHandler extends SimpleChannelInboundHandler<HttpObject> {

    private Logger logger = LoggerFactory.getLogger(NettyClientHttpObjHandler.class);

    private StringBuffer result = new StringBuffer();

    private NettyResponse<String> nettyResponse;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        System.out.println("message received");

        this.nettyResponse = (NettyResponse<String>) ctx.channel().attr(NettyClient.NETTY_CLIENT_PROMISE).get();

        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;

            System.out.println("=================== head start =========================");

            System.out.println("STATUS: " + response.status());
            System.out.println("VERSION: " + response.protocolVersion());

            if(!HttpResponseStatus.OK.equals(response.status())){
                this.nettyResponse.setError(new SystemException("netty client no response"));
                return;
            }

            if (!response.headers().isEmpty()) {
                response.headers().forEach(x -> {
                    System.out.println(x.getKey() + ":" + x.getValue());
                });
                System.out.println("=================== head end =========================");
            }

            if (HttpUtil.isTransferEncodingChunked(response)) {
                System.out.println("CHUNKED CONTENT {");
            } else {
                System.out.println("CONTENT {");
            }
        }
        if (msg instanceof HttpContent) {
            HttpContent content = (HttpContent) msg;

            result.append(content.content().toString(CharsetUtil.UTF_8));
            System.out.println(content.content().toString(CharsetUtil.UTF_8));

            if (content instanceof LastHttpContent) {
                System.out.println("} END OF CONTENT");
                this.nettyResponse.setSuccess(result.toString());
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("exceptionCaught");
        cause.printStackTrace();
        this.nettyResponse.setError(cause);
        ctx.close();
    }
}
