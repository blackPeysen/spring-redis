package com.org.peysen.redisarticle.service;

import com.org.peysen.redisarticle.entity.Article;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author : mengmeng.pei
 * @Date : 2020/4/16
 * @Desc :
 */

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void postArticle(Article article) {
        Integer articleId = article.getArticleId();
        String articleStr = "article:"+article.getArticleId();
        //1、创建一个集合,将文章id作为key，保存该文章的投票者
        redisTemplate.boundSetOps("Voted:"+articleId).add(article.getUserId());

        //2、将文章信息添加到文章Hash中,key：id，value：article
        redisTemplate.boundHashOps("Article").put(articleStr, article);

        //3、将文章的评分和时间放入有序集合中，用于排序刷选
        redisTemplate.boundZSetOps("Score").add(articleStr, article.getVotes());
        redisTemplate.boundZSetOps("Time").add(articleStr, article.getTime());

    }

    @Override
    public void votedArticle(Article article) {

    }
}
