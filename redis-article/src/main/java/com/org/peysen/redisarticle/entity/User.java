package com.org.peysen.redisarticle.entity;

import lombok.Data;

/**
 * @Author : mengmeng.pei
 * @Date : 2020/6/16
 * @Desc : 社交网络用户信息实体
 */
@Data
public class User {
    private int userId;
    private int age;
    // 用户名
    private String userName;
    // 登录名
    private String loginName;
    // 用户拥有的关注者人数
    private int followers;
    // 用户正在关注的人的人数
    private int following;
    // 用户已经发布的信息数量
    private int posts;
    // 用户注册日期
    private long signup;


    public User() {
    }

    public User(int userId) {
        this.userId = userId;
    }
}
