package com.example.core;

import java.util.Arrays;

public class MethodParams {

    private int serviceId;
    private Class serviceClazz;
    private String methodName;
    private Object[] params;
    private Class returnClazz;


    @Override
    public String toString() {
        return "MethodParams:{" +
                    "serviceId:" + this.serviceId +
                    ", methodName:" + this.methodName +
                    ", serviceClazz:" + this.serviceClazz +
                    ", params:" + Arrays.toString(this.params) +
                    ", returnClazz:" + this.returnClazz +
                "}";
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public Class getServiceClazz() {
        return serviceClazz;
    }

    public void setServiceClazz(Class serviceClazz) {
        this.serviceClazz = serviceClazz;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object...params) {
        this.params = params;
    }

    public Class getReturnClazz() {
        return returnClazz;
    }

    public void setReturnClazz(Class returnClazz) {
        this.returnClazz = returnClazz;
    }
}
