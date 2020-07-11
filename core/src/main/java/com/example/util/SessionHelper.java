package com.example.util;

import com.example.request.HttpSession;
import com.wisely.core.helper.RandomHelper;

import java.util.concurrent.ConcurrentHashMap;

public class SessionHelper {

    private final static ConcurrentHashMap<String, HttpSession> SESSION_MAP = new ConcurrentHashMap<>();

    /**
     * 注册Session，并返回新注册的HttpSession对象
     * @return
     */
    public static HttpSession addNewSession() {
        String sessionId = RandomHelper.getUUID();
        HttpSession session = new HttpSession(sessionId);
        SESSION_MAP.putIfAbsent(sessionId, session);
        return session;
    }

    /**
     * 判断当前服务端是否有该 session id 的记录
     */
    public static boolean containsSession(String sessionId){
        return SESSION_MAP.containsKey(sessionId);
    }

    public static HttpSession getSession(String sessionId) {
        return SESSION_MAP.get(sessionId);
    }

}
