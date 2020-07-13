package com.example.client;

import com.example.core.ProxyHandler;
import com.example.entity.Person;
import com.example.netty.NettyClient;
import com.example.service.PersonService;
import com.wisely.core.helper.Model;
import io.netty.handler.codec.AsciiString;
import io.netty.handler.codec.http.HttpHeaderNames;

public class NewClientMain {

    public static void main(String[] args) {

//        String url = "ttp://101.132.174.118:8888/gts/gtsWeightBill/list.json?page=1&rows=15";
//        Model header = new Model();
//        header.set(HttpHeaderNames.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.100 Safari/537.36");
//        header.set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
//        header.set(HttpHeaderNames.COOKIE, "JSESSIONID=5E0D23C3661571E531387ABFF96C802B; jenkins-timestamper-offset=-28800000; UCENTER_USER_ACCOUNT=dounion; userName=%25u7ba1%25u7406%25u5458; theme_link=1; UCENTER_USER=616C94991416E5F748E7D3BB4B6E0557; UCENTER_USER_KD_ID=10; UCENTER_USER_KD=dounion118%2Cdounion118-2%2Cdounion118");
//        String result = NettyClient.doHttpGet(url, header);
//        System.out.println(result);

        PersonService service = new ProxyHandler<>(PersonService.class).getProxy();
        Person person = service.load(2);
        System.out.println(person);
        person.setAge(31);
        person = service.update(person);
        System.out.println(person);
    }
}
