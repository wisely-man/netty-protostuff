package com.example.service;

import com.example.core.annotation.RpcService;
import com.example.entity.Person;

@RpcService
public interface PersonService {

    Person load(Integer id);

    Person update(Person record);
}
