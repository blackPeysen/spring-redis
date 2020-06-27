package com.org.peysen.redisarticle.service.chapter8;

import com.org.peysen.redisarticle.RedisArticleApplicationTests;
import com.org.peysen.redisarticle.entity.Inform;
import com.org.peysen.redisarticle.entity.User;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @Author : mengmeng.pei
 * @Date : 2020/6/22
 * @Desc :
 */
public class UserServiceTest extends RedisArticleApplicationTests {
    @Autowired
    private UserService userService;

    private User user;
    private User otherUser;

    @Before
    public void getUser(){
        user = new User();
        user.setUserId(1);
        user.setAge(22);
        user.setLoginName("pmm");
        user.setUserName("peysen");
        user.setSignup(System.currentTimeMillis());

        otherUser = new User();
        otherUser.setUserId(2);
        otherUser.setAge(222);
        otherUser.setLoginName("pmm2");
        otherUser.setUserName("peysen2");
        otherUser.setSignup(System.currentTimeMillis());
    }

    @Test
    public void createUser() throws IllegalAccessException {
        userService.createUser(user);
        userService.createUser(otherUser);
    }

    @Test
    public void getUserInfo() {
        System.out.println(userService.getUserInfo(String.valueOf(user.getUserId()), "loginName"));
        System.out.println(userService.getUserInfo(String.valueOf(user.getUserId()), "followers"));
    }

    @Test
    public void createInform() {
        for(int i=0; i<20;i++){
            Inform inform = new Inform();
            inform.setUId(otherUser.getUserId());
            inform.setLoginName(otherUser.getLoginName());
            inform.setMessage("第"+(i+1) + "次发布信息");
            inform.setPostedTime(System.currentTimeMillis()/1000);
            userService.createInform(inform);
        }
    }

    @Test
    public void getUserStatus(){
        List<Inform> userStatus = userService.getUserStatus(otherUser,UserService.PROFILE,1, 10);

        System.out.println("size:" + userStatus.size());
    }

    @Test
    public void followUser(){
        userService.followUser(user, otherUser);
    }

    @Test
    public void unFollowUser(){
        userService.unFollowUser(user, otherUser);
    }
}