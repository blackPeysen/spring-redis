package com.org.peysen.redisarticle.service.chapter11;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.naming.Reference;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @Author : mengmeng.pei
 * @Date : 2020/6/27
 * @Desc :
 */


@Service
public class LuaService {
    private static final String LOCK_PREFIX = "PEYSEN";
    @Autowired
    private RedisTemplate redisTemplate;
    private ThreadLocal<String> localKeys;
    private ThreadLocal<String> localRequestIds;


    //定义获取锁的lua脚本
    private static DefaultRedisScript<Long> LOCK_LUA_SCRIPT;

    //定义获取锁的lua脚本
    private static DefaultRedisScript<Long> UNLOCK_LUA_SCRIPT;

    @PostConstruct
    public void initScript(){
        LOCK_LUA_SCRIPT = new DefaultRedisScript<>();
        LOCK_LUA_SCRIPT.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/LOCK_LUA.lua")));
        LOCK_LUA_SCRIPT.setResultType(Long.class);

        UNLOCK_LUA_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_LUA_SCRIPT.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/UN_LOCK_LUA.lua")));
        UNLOCK_LUA_SCRIPT.setResultType(Long.class);
    }

    /**
     * 加锁
     * @param key Key
     * @param timeout 过期时间
     * @param retryTimes 重试次数
     * @return
     */
    public boolean lock(String key, long timeout, int retryTimes) {
        try {
            final String redisKey = this.getRedisKey(key);
            final String requestId = this.getRequestId();
            System.out.println("lock :::: redisKey = " + redisKey + " requestid = " + requestId);
            //组装lua脚本参数
            List<String> keys = Arrays.asList(redisKey, requestId, String.valueOf(timeout));
            //执行脚本
            Long result = (Long) redisTemplate.execute(LOCK_LUA_SCRIPT, keys);
            //存储本地变量
            if(result != null) {
                localRequestIds.set(requestId);
                localKeys.set(redisKey);
                System.out.println("success to acquire lock:" + Thread.currentThread().getName() + ", Status code reply:" + result);
                return true;
            } else if (retryTimes == 0) {
                // 重试次数为0直接返回失败
                return false;
            } else {
                //重试获取锁
                System.out.println("retry to acquire lock:" + Thread.currentThread().getName() + ", Status code reply:" + result);
                int count = 0;
                while(true) {
                    try {
                        //休眠一定时间后再获取锁，这里时间可以通过外部设置
                        Thread.sleep(100);
                        result = (Long) redisTemplate.execute(LOCK_LUA_SCRIPT, keys);
                        if(result != null) {
                            localRequestIds.set(requestId);
                            localKeys.set(redisKey);
                            System.out.println("success to acquire lock:" + Thread.currentThread().getName() + ", Status code reply:" + result);
                            return true;
                        } else {
                            count++;
                            if (retryTimes == count) {
                                System.out.println("fail to acquire lock for " + Thread.currentThread().getName() + ", Status code reply:" + result);
                                return false;
                            } else {
                                System.out.println(count + " times try to acquire lock for " + Thread.currentThread().getName() + ", Status code reply:" + result);
                                continue;
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("acquire redis occured an exception:" + Thread.currentThread().getName()+ e);
                        break;
                    }
                }
            }
        } catch (Exception e1) {
            System.out.println("acquire redis occured an exception:" + Thread.currentThread().getName() + e1);
        }
        return false;
    }

    /**
     * 释放KEY
     * @param key
     * @return
     */
    public boolean unlock(String key) {
        try {
            String localKey = localKeys.get();
            //如果本地线程没有KEY，说明还没加锁，不能释放
            if(StringUtils.isEmpty(localKey)) {
                System.out.println("release lock occured an error: lock key not found");
                return false;
            }
            String redisKey = getRedisKey(key);
            //判断KEY是否正确，不能释放其他线程的KEY
            if(!StringUtils.isEmpty(localKey) && !localKey.equals(redisKey)) {
                System.out.println("release lock occured an error: illegal key:" + key);
                return false;
            }
            //组装lua脚本参数
            List<String> keys = Arrays.asList(redisKey, localRequestIds.get());
            System.out.println("unlock :::: redisKey = " + redisKey + " requestid = " + localRequestIds.get());
            // 使用lua脚本删除redis中匹配value的key，可以避免由于方法执行时间过长而redis锁自动过期失效的时候误删其他线程的锁
            Long result = (Long) redisTemplate.execute(UNLOCK_LUA_SCRIPT, keys);
            //如果这里抛异常，后续锁无法释放
            if (result != null) {
                System.out.println("release lock success:" + Thread.currentThread().getName() + ", Status code reply=" + result);
                return true;
            } else if (result != null) {
                //返回-1说明获取到的KEY值与requestId不一致或者KEY不存在，可能已经过期或被其他线程加锁
                // 一般发生在key的过期时间短于业务处理时间，属于正常可接受情况
                System.out.println("release lock exception:" + Thread.currentThread().getName() + ", key has expired or released. Status code reply=" + result);
            } else {
                //其他情况，一般是删除KEY失败，返回0
                System.out.println("release lock failed:" + Thread.currentThread().getName() + ", del key failed. Status code reply=" + result);
            }
        } catch (Exception e) {
            System.out.println("release lock occured an exception"+ e);
        } finally {
            //清除本地变量
            this.clean();
        }
        return false;
    }

    /**
     * 清除本地线程变量，防止内存泄露
     */
    private void clean() {
        localRequestIds.remove();
        localKeys.remove();
    }

    /**
     * 获取RedisKey
     * @param key 原始KEY，如果为空，自动生成随机KEY
     * @return
     */
    private String getRedisKey(String key) {
        //如果Key为空且线程已经保存，直接用，异常保护
        if (StringUtils.isEmpty(key) && !StringUtils.isEmpty(localKeys.get())) {
            return localKeys.get();
        }
        //如果都是空那就抛出异常
        if (StringUtils.isEmpty(key) && StringUtils.isEmpty(localKeys.get())) {
            throw new RuntimeException("key is null");
        }
        return LOCK_PREFIX + key;
    }

    /**
     * 获取随机请求ID
     * @return
     */
    private String getRequestId() {
        return UUID.randomUUID().toString();
    }
}
