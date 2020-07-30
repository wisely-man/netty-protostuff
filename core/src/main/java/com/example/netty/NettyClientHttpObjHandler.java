package com.example.netty;

import com.wisely.core.exception.SystemException;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class NettyClientHttpObjHandler extends SimpleChannelInboundHandler<HttpObject> {

    private Logger logger = LoggerFactory.getLogger(NettyClientHttpObjHandler.class);

    private StringBuffer result = new StringBuffer();

    private NettyResponse<String> nettyResponse;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        logger.debug("message received");

        this.nettyResponse = (NettyResponse<String>) ctx.channel().attr(NettyClient.NETTY_CLIENT_PROMISE).get();

        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;

            logger.debug("=================== head start =========================");

            logger.debug("STATUS: " + response.status());
            logger.debug("VERSION: " + response.protocolVersion());

            if(!HttpResponseStatus.OK.equals(response.status())){
                this.nettyResponse.setError(new SystemException("netty client no response"));
                return;
            }

            if (!response.headers().isEmpty()) {
                response.headers().forEach(x -> {
                    logger.debug(x.getKey() + ":" + x.getValue());
                });
                logger.debug("=================== head end =========================");
            }

            if (HttpUtil.isTransferEncodingChunked(response)) {
                logger.debug("CHUNKED CONTENT {");
            } else {
                logger.debug("CONTENT {");
            }
        }
        if (msg instanceof HttpContent) {
            HttpContent content = (HttpContent) msg;

            result.append(content.content().toString(CharsetUtil.UTF_8));
            logger.debug(content.content().toString(CharsetUtil.UTF_8));

            if (content instanceof LastHttpContent) {
                logger.debug("} END OF CONTENT");
                this.nettyResponse.setSuccess(result.toString());
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.debug("exceptionCaught");
        cause.printStackTrace();
        this.nettyResponse.setError(cause);
        ctx.close();
    }
}
