package com.org.peysen.redisarticle.entity;

import lombok.Data;

/**
 * @Author : mengmeng.pei
 * @Date : 2020/6/23
 * @Desc : 用户发布的消息实体
 */

@Data
public class Inform {
    private Long id;
    private Integer uId;
    private String loginName;
    private String message;
    private Long postedTime;
}
