package com.org.peysen.redisarticle.service.chapter5;

import com.org.peysen.redisarticle.common.Common;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author : mengmeng.pei
 * @Date : 2020/6/20
 * @Desc : 使用redis实现计数器功能
 */

@Service
public class CounterService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Resource(name = "redisTemplate")
    private SetOperations<String, Object> setOperations;
    @Resource(name = "redisTemplate")
    private ZSetOperations<String, String> zSetOperations;
    @Resource(name = "redisTemplate")
    private HashOperations<String, String, Integer> hashOperations;

    private static final String NAME  = ":hits";
    private static final String KNOWN = "Known:";
    private static final String COUNT = "Count:";
    //1s、5s、1min、5min、1h、5h、1d
    private final static Integer[] PRECSION = {1,5,60,300,3600,18000,86400};

    /**
     * 创建各个时间精度的计数器
     */
    public void createCounter(){
        long currentSeconds = Common.getCurrentSeconds();
        Arrays.stream(PRECSION).forEach(prec -> {
            String hashName = prec + NAME;
            String pnow = String.valueOf((int)(currentSeconds / prec) * prec);

            zSetOperations.add(KNOWN, hashName, 0);
            hashOperations.put(COUNT + hashName, pnow, 0);
        });
    }

    /**
     * 获取指定计数器的值
     */
    public void getCounter(int prec){
        List<Integer> collect = Stream.of(PRECSION).collect(Collectors.toList());
        if (collect.contains(prec)){
            String hashKey = COUNT + prec + NAME;
            System.out.println("hashKey = " + hashKey);
            Set<String> keys = hashOperations.keys(hashKey);
            keys.stream().forEach(key -> {
                Integer integer = hashOperations.get(hashKey, key);
                System.out.println(key + " = " + integer);
            });
        }else{
            System.out.println("不存在：" + prec);
        }
    }

    /**
     * 获取所有计数器的值
     */
    public void getAllCounter(){
        Arrays.stream(PRECSION).forEach(prec -> {
            String hashKey = COUNT + prec + NAME;
            System.out.println("hashKey = " + hashKey);
            Set<String> keys = hashOperations.keys(hashKey);
            keys.stream().forEach(key -> {
                Integer integer = hashOperations.get(hashKey, key);
                System.out.println(key + " = " + integer);
            });
        });
    }

    public void cleanCounter(){
        int index = 1;
        int passes = 0;
        long currentSeconds = Common.getCurrentSeconds();
        Long zCard = zSetOperations.zCard(KNOWN);

        while(index < zCard){
            Set<String> range = zSetOperations.range(KNOWN, index, index);
            if (range != null && range.size() > 0){
                List<String> list = new ArrayList<>(range);
                String hashName = list.get(0);
                Integer prec = Integer.valueOf(hashName.split(":")[0]);
                System.out.println("hashName=" + hashName + ",prec=" + prec);

                int bprec = prec / 60 == 0 ? prec / 60 : 1;

                if (passes % bprec == 0){
                    System.out.println("todo");
                    continue;
                }else{
                    String hashKey = COUNT  + hashName;
                }
            }

            index++;
        }
    }

}
