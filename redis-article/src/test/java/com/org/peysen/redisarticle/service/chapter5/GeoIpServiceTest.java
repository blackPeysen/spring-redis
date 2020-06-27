package com.org.peysen.redisarticle.service.chapter5;

import com.org.peysen.redisarticle.RedisArticleApplicationTests;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * @Author : mengmeng.pei
 * @Date : 2020/6/21
 * @Desc :
 */
public class GeoIpServiceTest extends RedisArticleApplicationTests {
    @Autowired
    private GeoIpService geoIpService;

    @Test
    public void importIpsToRedis() {
        String filePath = "/Users/peysen/AllThing/works/workSpaces/IdeaSpace/testSpace/spring-redis/redis-article/src/main/resources/geoIpLite/GeoLite2-City-Blocks-IPv4.csv";
        geoIpService.importIpsToRedis(filePath);
    }

    @Test
    public void importCityToRedis() {
        String filePath = "/Users/peysen/AllThing/works/workSpaces/IdeaSpace/testSpace/spring-redis/redis-article/src/main/resources/geoIpLite/GeoLite2-City-Locations-zh-CN.csv";
        geoIpService.importCityToRedis(filePath);
    }

    @Test
    public void findCityByIp(){
        String ipAddress = "5.150.136.0";
        System.out.println(geoIpService.findCityByIp(ipAddress));
    }
}