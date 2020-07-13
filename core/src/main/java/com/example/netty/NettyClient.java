package com.example.netty;

import com.wisely.core.exception.SystemException;
import com.wisely.core.helper.Model;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.AsciiString;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.Promise;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class NettyClient {

    final static EventLoopGroup WORKER_GROUP = new NioEventLoopGroup();
    final static DefaultEventLoop NETTY_RESPONSE_PROMISE_NOTIFY_EVENT_LOOP =  new DefaultEventLoop();
    public final static String NETTY_CONNECTION_TIME_OUT = "NETTY_CONNECTION_TIME_OUT";
    final static Long DEFAULT_CONNECT_TIME_OUT = 90 * 1000l; // 默认连接超时时间

    // 构造方法私有
    private NettyClient(NettyClientConfig config){
        this.config = config;
        try {
            this.init();
        } catch (InterruptedException e) {
            throw new SystemException("NettyClient build error...", e);
        }
    }

    private NettyClientConfig config;
    private Channel channel;

    private void init() throws InterruptedException {

        if(this.config == null){
            throw new SystemException("netty client config is null...");
        }

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(WORKER_GROUP)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(config.getHost(), config.getPort()))
                    .handler(new ChannelInitializer() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                if(config.getHandlers()!=null && config.getHandlers().length>0){
                    for(ChannelHandler handler : config.getHandlers()){
                        pipeline.addLast(handler.getClass().getName(), handler);
                    }
                }
            }
        });

        ChannelFuture future =
                bootstrap.connect(this.config.getHost(), this.config.getPort());

        // channel
        this.channel = future.channel();
//        this.refreshChannel(future, this.channel);

//        future.channel().closeFuture().sync();
    }


    /**
     * 刷新channel
     * @param newFuture
     * @param oldChannel
     */
    private void refreshChannel(ChannelFuture newFuture, Channel oldChannel){
        try {
            if(oldChannel != null){
                oldChannel.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.channel = newFuture.channel();
        }

    }

    // static Method ======================================================================

    /**
     * 获取NettyClient
     * @param url
     * @param handlers
     * @return
     */
    public static NettyClient getNettyClient(final String url, final ChannelHandler...handlers){

        // 获取连接池
        NettyClientPool pool = NettyClientPool.getPool(url, handlers);

        Exception error = null;
        NettyClient client = null;
        try {
            client = pool.borrowObject();
        } catch (Exception e) {
            error = e;
        }

        if(client == null) {
            throw new SystemException("client get failed...", error);
        }
        return client;
    }


    /**
     * 返回NettyClient实例
     * @param url
     * @param handlers
     * @return
     */
    static NettyClient newInstance(final String url, final ChannelHandler...handlers) {
        return new NettyClient(new NettyClientConfig(url, handlers));
    }


    /**
     * 返回NettyClient实例
     * @param config
     * @return
     */
    static NettyClient newInstance(final NettyClientConfig config) {
        return new NettyClient(config);
    }






    // for http ============================================================================

    public final static String HTTP_VERSION_KEY = "HTTP_VERSION_KEY";
    public final static String HTTP_METHOD_KEY = "HTTP_METHOD_KEY";

    public final static ChannelHandler[] HTTP_HANDLERS = new ChannelHandler[]{
        new HttpClientCodec(), // 编解码器
        new HttpObjectAggregator(1024 * 10 * 1024), // 聚合
        new HttpContentDecompressor(), // 解压
        new ChunkedWriteHandler(), // 大数据
        new NettyClientHandler() // 自定义处理类
    };

    public static byte[] doHttpRequest(String url, Model header, String message){
        return doHttpRequest(url, null, header, message);
    }

    public static byte[] doHttpRequest(String url, ChannelHandler[] handlers, Model header, String message){
        if(handlers == null){
            handlers = HTTP_HANDLERS;
        }
        NettyClient client = getNettyClient(url, handlers);
        if(client == null){
            throw new SystemException("netty client init failed...");
        }

        if(header == null){
            header = new Model();
        }
        // http版本
        HttpVersion httpVersion = HttpVersion.valueOf(header.getString(HTTP_VERSION_KEY, "HTTP/1.0"));
        // 请求方式
        HttpMethod method = HttpMethod.valueOf(header.getString(HTTP_METHOD_KEY, "POST"));
        ByteBuf byteBuf;
        try {
            byteBuf = Unpooled.wrappedBuffer(message.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new SystemException("netty client : message convert failed...", e);
        }

        DefaultFullHttpRequest req =
                        new DefaultFullHttpRequest(httpVersion, method,
                            client.config.getUri().toASCIIString(), byteBuf);

        // 设置请求头
        req.headers()
            .set(HttpHeaderNames.HOST, client.config.getHost())
            .set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
            .set(HttpHeaderNames.CONTENT_LENGTH, String.valueOf(req.content().readableBytes()));

        if(!header.isEmpty()){
            for(Object key : header.keySet()){
                if(key instanceof AsciiString){
                    req.headers().set((AsciiString) key, header.getString(key));
                }
            }
        }

        byte[] result;
        try {
            // 设置promise
            Promise<byte[]> promise = NETTY_RESPONSE_PROMISE_NOTIFY_EVENT_LOOP.newPromise();
            client.channel.pipeline().get(NettyClientHandler.class).setPromise(promise);

            // 发送请求
            client.channel.writeAndFlush(req);

            Long timeout = header.getLong(NETTY_CONNECTION_TIME_OUT, DEFAULT_CONNECT_TIME_OUT);
            result = promise.get(timeout, TimeUnit.MILLISECONDS);

        } catch (Throwable e) {
            throw new SystemException("do request failed...", e);
        }

        return result;
    }


    // for rpc =============================================================================


    final static ChannelHandler[] RPC_HANDLERS = new ChannelHandler[]{
            new NettyClientHandler()
    };

    public static byte[] doRpcRequest(String url, byte[] message) {
        return doRpcRequest(url, null, message);
    }

    public static byte[] doRpcRequest(String url, ChannelHandler[] handlers, byte[] message){
        if(handlers == null){
            handlers = RPC_HANDLERS;
        }
        NettyClient client = getNettyClient(url, handlers);
        if(client == null){
            throw new SystemException("netty client init failed...");
        }

        byte[] result;
        try {
            // 设置promise
            Promise<byte[]> promise = NETTY_RESPONSE_PROMISE_NOTIFY_EVENT_LOOP.newPromise();
            client.channel.pipeline().get(NettyClientHandler.class).setPromise(promise);

            // 发送数据
            ByteBuf byteBuf = Unpooled.copiedBuffer(message);
            client.channel.writeAndFlush(byteBuf);

            result = promise.get(DEFAULT_CONNECT_TIME_OUT, TimeUnit.MILLISECONDS);

        } catch (Throwable e) {
            throw new SystemException("do request failed...", e);
        }
        return result;
    }

}
