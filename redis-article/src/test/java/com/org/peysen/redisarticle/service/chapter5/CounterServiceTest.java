package com.org.peysen.redisarticle.service.chapter5;

import com.org.peysen.redisarticle.RedisArticleApplicationTests;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Author : mengmeng.pei
 * @Date : 2020/6/20
 * @Desc :
 */
public class CounterServiceTest extends RedisArticleApplicationTests {
    @Autowired
    private CounterService counterService;

    @Test
    public void createCounter() {
        counterService.createCounter();
    }

    @org.junit.jupiter.api.Test
    public void getAllCounter() {
        counterService.getAllCounter();
    }

    @Test
    public void getCounter(){
        counterService.getCounter(300);
    }

    @Test
    public void cleanCounter(){
        counterService.cleanCounter();
    }
}