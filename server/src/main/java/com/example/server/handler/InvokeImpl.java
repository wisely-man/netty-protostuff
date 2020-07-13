package com.example.server.handler;

import com.example.core.Invoke;
import com.example.core.MethodParams;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.lang.reflect.Method;

public class InvokeImpl implements Invoke<byte[]> {

    @Override
    public byte[] invoke(MethodParams methodParams) throws Exception{

        Object obj = CtClassFactory.getInstance(methodParams.getServiceClazz());
        if(obj == null){
            throw new IllegalAccessException("服务实例未注册");
        }

        Class[] paramsClazz = null;
        if(methodParams.getParams()!=null && methodParams.getParams().length>0){
            paramsClazz = new Class[methodParams.getParams().length];
            for(int i=0; i<methodParams.getParams().length; i++) {
                paramsClazz[i] = methodParams.getParams()[i].getClass();
            };
        }

        Method method;
        if(paramsClazz != null){
            method = methodParams.getServiceClazz().getDeclaredMethod(methodParams.getMethodName(), paramsClazz);
        } else {
            method = methodParams.getServiceClazz().getDeclaredMethod(methodParams.getMethodName());
        }

        // 执行方法
        Object result = method.invoke(obj, methodParams.getParams());

        // 返回结果
        Class backClazz = result.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate();
        Schema backSchema = RuntimeSchema.getSchema(backClazz);
        return ProtobufIOUtil.toByteArray(result, backSchema, buffer);
    }

}
