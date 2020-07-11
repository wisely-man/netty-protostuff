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
        return uri.getPort();
    }

    public String getUrl(){
        return uri.toString();
    }

    public Boolean isSsl(){
        return StringUtils.equals(this.getUri().getScheme(), "https://");
    }


    /**
     * 重写hashCode方法
     *      相同scheme/host/port/handlers 的Pool不再重复创建新的连接池
     * @return
     */
    @Override
    public int hashCode() {
        StringBuffer  hash = new StringBuffer();
        hash.append(this.getScheme())
                .append(this.getHost())
                .append(this.getPort())
        ;
        if(this.getHandlers()!=null &&
                this.getHandlers().length>0){
            Arrays.stream(this.getHandlers()).forEach(x->{
                hash.append(x.getClass().toString());
            });
        }

        return hash.toString().hashCode();
    }

}
