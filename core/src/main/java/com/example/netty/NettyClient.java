package com.example.netty;

import com.example.netty.handlers.ChannelHandlerFactory;
import com.example.netty.handlers.DefaultHttpChannelHandlerFactory;
import com.example.netty.handlers.DefaultRpcChannelHandlerFactory;
import com.wisely.core.exception.SystemException;
import com.wisely.core.helper.Model;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;
import io.netty.util.AttributeKey;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicInteger;

public class NettyClient {

    final static EventLoopGroup WORKER_GROUP = new NioEventLoopGroup(1);
    public static final AttributeKey NETTY_CLIENT_REQUEST = AttributeKey.newInstance("NETTY_CLIENT_REQUEST");
    public static final AttributeKey NETTY_CLIENT_PROMISE = AttributeKey.newInstance("NETTY_CLIENT_PROMISE");

    // 构造方法私有
    private NettyClient(NettyClientConfig config){
        this.config = config;
        this.init();
    }

    private AtomicInteger count = new AtomicInteger(0);
    private Bootstrap bootstrap;
    private NettyClientConfig config;
    private Channel channel;

    public NettyClientConfig getConfig() {
        return config;
    }

    public Channel getChannel() {
        return channel;
    }

    void increment(){
        count.getAndIncrement();
    }
    int getCount(){
        return this.count.get();
    }

    void init() {

        if(this.config == null){
            throw new SystemException("netty client config is null...");
        }

        this.bootstrap = new Bootstrap();
        this.bootstrap.group(WORKER_GROUP)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                    .handler(new ChannelInitializer() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                if(config.getHandlerFactory()!=null){
                    pipeline.addLast(config.getHandlerFactory().handlers());
                }
            }
        });

        this.connect();
    }

    void connect(){

        if(this.channel != null){
            this.channel.close();
        }

        // connect
        ChannelFuture future =
                this.bootstrap.connect(this.config.getHost(), this.config.getPort());
        // channel
        this.channel = future.channel();
    }


    // static Method ======================================================================

    /**
     * 获取NettyClient
     * @param url
     * @param handlerFactory
     * @return
     */
    public static NettyClient getNettyClient(final String url, final ChannelHandlerFactory handlerFactory){

        // 获取连接池
        NettyClientPool pool = NettyClientPool.getPool(url, handlerFactory);

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

        client.increment();
        System.out.println("client :" + client);
        System.out.println("client used count:" + client.getCount());

        return client;
    }


    /**
     * 返回NettyClient实例
     * @param url
     * @param handlerFactory
     * @return
     */
    static NettyClient newInstance(final String url, final ChannelHandlerFactory handlerFactory) {
        return new NettyClient(new NettyClientConfig(url, handlerFactory));
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


    public static String doHttpGet(String url){
        Model header = new Model();
        header.put(HTTP_METHOD_KEY, "GET");
        return doHttpGet(url, header);
    }

    public static String doHttpGet(String url, Model header){
        if(header == null){
            header = new Model();
        }
        header.put(HTTP_METHOD_KEY, "GET");
        return doHttpRequest(url, header, "");
    }

    public static String doHttpPost(String url, String message) {
        return doHttpPost(url, null, message);
    }

    public static String doHttpPost(String url, Model header, String message){
        if(header == null){
            header = new Model();
        }
        header.put(HTTP_METHOD_KEY, "POST");
        return doHttpRequest(url, header, message);
    }

    public static String doHttpRequest(String url, Model header, String message){
        return doHttpRequest(url, null, header, message);
    }

    public static String doHttpRequest(String url,
                   ChannelHandlerFactory handlerFactory, Model header, String message){
        if(handlerFactory == null){
            handlerFactory = new DefaultHttpChannelHandlerFactory();
        }
        NettyClient client = getNettyClient(url, handlerFactory);
        if(client == null){
            throw new SystemException("netty client init failed...");
        }

        if(!client.channel.isActive()){
            client.connect();
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

        String result;
        try {
            // 设置promise
            NettyResponse<String> response = new NettyResponse();
            client.channel.attr(NETTY_CLIENT_REQUEST).set(req);
            client.channel.attr(NETTY_CLIENT_PROMISE).set(response);

            // 阻塞获取请求结果
            result = response.get();

        } catch (Throwable e) {
            throw new SystemException("do request failed...", e);
        } finally {
            NettyClientPool.release(client);
        }

        return result;
    }


    // for rpc =============================================================================

    public static byte[] doRpcRequest(String url, byte[] message) {
        return doRpcRequest(url, null, message);
    }

    public static byte[] doRpcRequest(String url, ChannelHandlerFactory factory, byte[] message){
        if(factory == null){
            factory = new DefaultRpcChannelHandlerFactory();
        }
        NettyClient client = getNettyClient(url, factory);
        if(client == null){
            throw new SystemException("netty client init failed...");
        }

        if(!client.channel.isActive()){
        client.connect();
        }


        byte[] result;
        try {
            // 设置发送数据
            ByteBuf byteBuf = Unpooled.copiedBuffer(message);
            client.channel.attr(NETTY_CLIENT_REQUEST).set(byteBuf);


            // 设置响应
            NettyResponse<byte[]> response = new NettyResponse();
            client.channel.attr(NETTY_CLIENT_PROMISE).set(response);

            // 阻塞获取异步结果
            result = response.get();

        } catch (Throwable e) {
            throw new SystemException("do request failed...", e);
        } finally {
            NettyClientPool.release(client);
        }
        return result;
    }

}
