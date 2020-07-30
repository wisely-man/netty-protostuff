package com.example.client;

import com.example.netty.NettyClient;
import com.wisely.core.helper.Model;
import io.netty.handler.codec.http.HttpHeaderNames;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpClientMain {

    public static void main(String[] args) throws InterruptedException {


        final ExecutorService executorService = Executors.newFixedThreadPool(10);
        final CountDownLatch latch2 = new CountDownLatch(10);

        // http test
        final String url = "https://www.baidu.com/?wd=aaa";
        final Model header = new Model();
        header.set(NettyClient.HTTP_VERSION_KEY, "HTTP/1.1");
        header.set(HttpHeaderNames.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.100 Safari/537.36");
        header.set(HttpHeaderNames.CONTENT_TYPE, "text/html;charset=UTF-8");


        for(int i=0; i<10; i++) {

//            executorService.submit(() -> {
                try {
                    String result = NettyClient.doHttpGet(url, header);
                    System.out.println(result);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
//                    latch2.countDown();
                }
//            });
        }

//        latch2.await();

    }
}
