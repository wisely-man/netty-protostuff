package com.example.netty.client;

import com.wisely.core.helper.CacheHelper;
import io.netty.channel.ChannelHandler;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class NettyClientPool extends GenericObjectPool<NettyClient> {


    private final static String CACHE_KEY = "NETTY_CLIENT_POOL_"; // 缓存KEY
    private final static Long ACTIVE_TIME = 60 * 1000l; // 最大存活时间
    private final static Object LOCK_OBJ = new Object();


    /**
     * 获取连接池
     *     根据NettyClientConfig的hash值存储在缓存中
     * @param url
     * @param handlers
     * @return
     */
    public static NettyClientPool getPool(final String url, final ChannelHandler...handlers){

        NettyClientConfig config = new NettyClientConfig(url, handlers);

        // 从缓存种获取连接池对象
        NettyClientPool pool = CacheHelper.get(CACHE_KEY + config.hashCode());
        if(pool != null){
            return pool;
        }

        synchronized (LOCK_OBJ) {
            // do double-check
            pool = CacheHelper.get(CACHE_KEY + config.hashCode());
            if(pool != null){
                return pool;
            }

            // 未初始化
            NettyClientFactory factory = new NettyClientFactory(config);
            pool = new NettyClientPool(factory);
            CacheHelper.set(CACHE_KEY+config.hashCode(), pool, ACTIVE_TIME);
            return pool;
        }
    }


    public static void release(final NettyClient client){
        if(client == null){
            return;
        }

        NettyClientPool pool = CacheHelper.get(CACHE_KEY + client.getConfig().hashCode());
        if(pool == null){
            return;
        }

        pool.returnObject(client);
    }

    public NettyClientPool(NettyClientFactory factory) {
        super(factory, new NettyClientPoolConfig());
    }

    public NettyClientPool(NettyClientFactory factory, NettyClientPoolConfig config) {
        super(factory, config);
    }


    /**
     * NettyClient连接池
     *          默认配置类
     */
    private static class NettyClientPoolConfig extends GenericObjectPoolConfig {

        public NettyClientPoolConfig(){

            // 最大空闲数
            setMaxIdle(5);
            // 最小空闲数, 池中只有一个空闲对象的时候，池会在创建一个对象，并借出一个对象，从而保证池中最小空闲数为1
            setMinIdle(1);
            // 最大池对象总数
            setMaxTotal(20);
            // 逐出连接的最小空闲时间 默认1800000毫秒(30分钟)
            setMinEvictableIdleTimeMillis(1800000);
            // 逐出扫描的时间间隔(毫秒) 如果为负数,则不运行逐出线程, 默认-1
            setTimeBetweenEvictionRunsMillis(1800000 * 2L);
            // 在获取对象的时候检查有效性, 默认false
            setTestOnBorrow(true);
            // 在归还对象的时候检查有效性, 默认false
            setTestOnReturn(false);
            // 在空闲时检查有效性, 默认false
            setTestWhileIdle(false);
            // 最大等待时间， 默认的值为-1，表示无限等待。
            setMaxWaitMillis(5000);
            // 是否启用后进先出, 默认true
            setLifo(true);
            // 连接耗尽时是否阻塞, false报异常,ture阻塞直到超时, 默认true
            setBlockWhenExhausted(true);
            // 每次逐出检查时 逐出的最大数目 默认3
            setNumTestsPerEvictionRun(3);

        }

    }
}
