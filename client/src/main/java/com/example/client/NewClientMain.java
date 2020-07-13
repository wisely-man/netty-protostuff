package com.example.client;

import com.example.core.ProxyHandler;
import com.example.entity.Person;
import com.example.service.PersonService;

public class NewClientMain {

    public static void main(String[] args) {

//        String url = "https://www.baidu.com";
//        byte[] result = NettyClient.doHttpRequest(url, null, "");
//        System.out.println(new String(result));

        PersonService service = new ProxyHandler<>(PersonService.class).getProxy();
        Person person = service.load(2);
        System.out.println(person);
    }
}
