package com.example.core;

import com.example.netty.NettyClient;
import com.example.util.ProtostuffUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ProxyHandler<T> implements InvocationHandler {

    private Class<T> interfaceClazz;

    public ProxyHandler(Class<T> interfaceClazz){
        this.interfaceClazz = interfaceClazz;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        // 这些应该从注册服务中拿取
        MethodParams request = new MethodParams();
        request.setServiceId(1);
        request.setServiceClazz(proxy.getClass());
        request.setMethodName(method.getName());
        request.setParams(args);

        byte[] protobuf = ProtostuffUtils.serializer(request);
        byte[] result = NettyClient.doHttpRequest("http://localhost:8080", null, new String(protobuf));

        return null;
    }

}
