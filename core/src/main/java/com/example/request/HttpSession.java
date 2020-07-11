package com.example.request;

import com.wisely.core.helper.Model;

public class HttpSession {

    public HttpSession(String id) {
        this.id = id;
    }


    private String id;

    private Model attributes = new Model();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void addAttribute(String key, Object obj){
        attributes.set(key, obj);
    }

    public <T> T getAttribute(String key){
        return (T) attributes.get(key);
    }

}
