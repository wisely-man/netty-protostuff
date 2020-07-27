package com.example.netty;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class NettyClientFactory extends BasePooledObjectFactory<NettyClient> {

    public NettyClientFactory(NettyClientConfig config) {
        this.config = config;
    }

    private NettyClientConfig config;

    public NettyClientConfig getConfig() {
        return config;
    }

    public void setConfig(NettyClientConfig config) {
        this.config = config;
    }

    @Override
    public NettyClient create() throws Exception {
        return NettyClient.newInstance(config);
    }

    @Override
    public PooledObject<NettyClient> wrap(NettyClient nettyClient) {
        return new DefaultPooledObject<>(nettyClient);
    }

    @Override
    public void activateObject(PooledObject<NettyClient> p) {
        NettyClient client = p.getObject();
        if(!client.getChannel().isActive() || !client.getChannel().isOpen()){
            client.reconnect();
        }
    }
}
