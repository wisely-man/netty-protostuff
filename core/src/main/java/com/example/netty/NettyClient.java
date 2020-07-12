package com.example.netty;

import com.wisely.core.exception.SystemException;
import com.wisely.core.helper.Model;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.AsciiString;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class NettyClient {

    final static EventLoopGroup WORKER_GROUP = new NioEventLoopGroup();
    final static Long CONNECT_TIME_OUT_SECOND = 90 * 1000l;

    // 构造方法私有
    private NettyClient(NettyClientConfig config){
        this.config = config;
        this.init();
    }

    private NettyClientConfig config;
    private Channel channel;

    private void init(){

        if(this.config == null){
            throw new SystemException("netty client config is null...");
        }

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(WORKER_GROUP)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                if(config.getHandlers()!=null && config.getHandlers().length>0){
                    pipeline.addLast(config.getHandlers());
                }
            }
        });

        ChannelFuture future = bootstrap.connect(this.config.getHost(), this.config.getPort());
        boolean flag = future.awaitUninterruptibly(2000, TimeUnit.MILLISECONDS);
        if(!flag){
            throw new SystemException(this.config.getUri() + " connect failed...");
        }

        // channel
        this.channel = future.channel();
//        this.refreshChannel(future, this.channel);
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
        final CountDownLatch latch = new CountDownLatch(1);
        try {

            Promise<byte[]> promise = new DefaultPromise<>(new DefaultEventLoop());

            // 发送请求并添加监听，等待请求完成
            client.channel.pipeline().get(NettyClientHandler.class).setPromise(promise);
            client.channel.writeAndFlush(req).addListener(future -> {
                if(future.isDone()){
                    latch.countDown();
                }
            });
            // 阻塞等待异步结果
            latch.await();

            result = promise.get();

        } catch (Throwable e) {
            throw new SystemException("do request failed...", e);
        } finally {
            latch.countDown();
        }

        return result;
    }


}
