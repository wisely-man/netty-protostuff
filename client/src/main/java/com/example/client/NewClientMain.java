package com.example.client;

import com.example.netty.NettyClient;

public class NewClientMain {

    public static void main(String[] args) {

        String url = "https://www.baidu.com";
        byte[] result = NettyClient.doHttpRequest(url, null, "");
        System.out.println(new String(result));
    }

}
