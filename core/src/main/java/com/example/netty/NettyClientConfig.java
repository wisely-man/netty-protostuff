package com.example.netty;

import com.example.netty.handlers.ChannelHandlerFactory;
import com.wisely.core.exception.SystemException;
import com.wisely.core.helper.AssertHelper;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;

public class NettyClientConfig {

    public NettyClientConfig(String url) throws SystemException {
        AssertHelper.isEmpty(url, "url can not be null");
        try {
            this.uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new SystemException("url:[" + url + "] syntax error...", e);
        }
    }

    public NettyClientConfig(String url, ChannelHandlerFactory handlerFactory) throws SystemException {
        this(url);
        this.handlerFactory = handlerFactory;
    }

    private URI uri;
    private ChannelHandlerFactory handlerFactory;


    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public ChannelHandlerFactory getHandlerFactory() {
        return handlerFactory;
    }

    public void setHandlerFactory(ChannelHandlerFactory handlerFactory) {
        this.handlerFactory = handlerFactory;
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
        int factoryHash = this.handlerFactory==null ? 0 : this.handlerFactory.hashCode();
        return (this.getHost() + ":" + this.getPort()).hashCode() * prime
                + factoryHash;
    }

}
