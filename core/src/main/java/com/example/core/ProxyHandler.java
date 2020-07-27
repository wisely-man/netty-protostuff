package com.example.core;

import com.example.netty.NettyClient;
import com.example.util.SerializerUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyHandler<T> implements InvocationHandler {

    private Class<T> proxyInterface;

    public ProxyHandler(Class<T> proxyInterface){
        this.proxyInterface = proxyInterface;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        // 这些应该从注册服务中拿取
        MethodParams request = new MethodParams();
        request.setServiceId(1);
        request.setServiceClazz(proxyInterface);
        request.setMethodName(method.getName());
        request.setParams(args);

        byte[] protobuf = SerializerUtils.serializer(request);
        byte[] result = NettyClient.doRpcRequest("http://localhost:8080", protobuf);

        return SerializerUtils.deserializer(result, method.getReturnType());
    }


    public T getProxy(){
        return (T) Proxy.newProxyInstance(proxyInterface.getClassLoader(), new Class[]{proxyInterface}, this);
    }

}
