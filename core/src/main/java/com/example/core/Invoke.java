package com.example.core;

public interface Invoke<R> {

    R invoke(MethodParams methodParams) throws Exception;

}
