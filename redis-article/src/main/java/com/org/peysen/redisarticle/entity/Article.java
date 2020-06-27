package com.org.peysen.redisarticle.entity;

import lombok.Data;

/**
 * @Author : mengmeng.pei
 * @Date : 2020/4/16
 * @Desc : 文章实体类
 */

@Data
public class Article {
    private Integer articleId;      // 文章Id
    private String title;           // 文章标题
    private String link;            // 文章链接
    private Integer userId;         // 文章作者Id
    private Long time;              // 文章发布时间
    private Integer votes;          // 文章投票数

    public static Article createArticle(){
        return new Article();
    };

}
