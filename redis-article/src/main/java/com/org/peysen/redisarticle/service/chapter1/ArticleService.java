package com.org.peysen.redisarticle.service.chapter1;

import com.org.peysen.redisarticle.entity.Article;
import com.org.peysen.redisarticle.entity.User;

/**
 * @Author : mengmeng.pei
 * @Date : 2020/4/16
 * @Desc :
 */
public interface ArticleService {
    /**
     * 发布文章
     * @param article
     */
    void postArticle(Article article);

    /**
     * 给某一个文章点赞
     */
    void votedArticle(Article article, User user);

    void testHscan();

}
