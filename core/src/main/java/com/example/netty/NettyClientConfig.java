package com.example.netty;

import com.wisely.core.exception.SystemException;
import io.netty.channel.ChannelHandler;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

public class NettyClientConfig {

    public NettyClientConfig(String url) throws SystemException {
        try {
            this.uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new SystemException("url:[" + url + "] syntax error...", e);
        }
    }

    public NettyClientConfig(String url, ChannelHandler...handlers) throws SystemException {
        this(url);
        this.handlers = handlers;
    }

    private URI uri;
    private ChannelHandler[] handlers;


    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public ChannelHandler[] getHandlers() {
        return handlers;
    }

    public void setHandlers(ChannelHandler[] handlers) {
        this.handlers = handlers;
    }

    public String getScheme(){
        return this.getUri().getScheme();
    }

    public String getHost(){
        return uri.getHost();
    }

    public Integer getPort(){
        if(uri.getPort() == -1){
            return isSsl() ? 443 : 80;
        }
        return uri.getPort();
    }

    public String getUrl(){
        return uri.toString();
    }

    public Boolean isSsl(){
        return StringUtils.equals(this.getUri().getScheme(), "https");
    }


    /**
     * 重写hashCode方法
     *      相同 host/port/handlers 的Pool不再重复创建新的连接池
     * @return
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        return (this.getHost() + ":" + this.getPort()).hashCode() * prime
                + Arrays.hashCode(this.handlers);
    }

}
