package com.example.client;

import com.example.core.ProxyHandler;
import com.example.entity.Person;
import com.example.netty.NettyClient;
import com.example.service.PersonService;
import com.wisely.core.helper.Model;
import io.netty.handler.codec.http.HttpHeaderNames;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RpcClientMain {

    public static void main(String[] args) throws InterruptedException {

//        final ExecutorService executorService = Executors.newFixedThreadPool(10);

//        final CountDownLatch latch = new CountDownLatch(10);

        // rpc test
//        for(int i=0; i<10; i++){
//            executorService.submit(() -> {
                try {
                    PersonService service = new ProxyHandler<>(PersonService.class).getProxy();
                    Person person = service.load(2);
                    System.out.println("load ： " + person);
                    person.setAge(31);
                    person = service.update(person);
                    System.out.println("update : " + person);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
//                    latch.countDown();
                }
//            });
//        }

//        latch.await();

    }
}
