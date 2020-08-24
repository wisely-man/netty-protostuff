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
        final String url = "http://localhost:8080/?name=";
        final Model header = new Model();
        header.set(NettyClient.HTTP_VERSION_KEY, "HTTP/1.0");

        for(int i=0; i<10; i++) {
            int finalI = i;
            executorService.submit(() -> {
                try {
                    final String u = url + "Netty_" + finalI;
                    String result = NettyClient.doHttpGet(u, header);
                    System.out.println("=======================result : " + result);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch2.countDown();
                }
            });
        }

//        latch2.await();

    }
}
