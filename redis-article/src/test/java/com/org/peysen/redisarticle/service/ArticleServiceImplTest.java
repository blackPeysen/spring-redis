package com.org.peysen.redisarticle.service;

import com.org.peysen.redisarticle.RedisArticleApplicationTests;
import com.org.peysen.redisarticle.entity.Article;
import com.org.peysen.redisarticle.entity.User;
import com.org.peysen.redisarticle.service.chapter1.ArticleService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * @Author : mengmeng.pei
 * @Date : 2020/6/15
 * @Desc :
 */
public class ArticleServiceImplTest extends RedisArticleApplicationTests {
    @Autowired
    private ArticleService articleService;

    @Test
    public void postArticle() {
        for(int i=0;i<10;i++){
            Article article = Article.createArticle();
            article.setArticleId(i+1);
            article.setLink("http://localhost:8080/link/" + (i+1));
            article.setTime(new Date().getTime() + i);
            article.setTitle("title_"+(i+1));
            article.setUserId((i+1));
            article.setVotes(0);

            articleService.postArticle(article);
        }
    }

    @Test
    public void votedArticle() {
        User user = new User(222);
        for(int i=0;i<10;i++){
            Article article = Article.createArticle();
            article.setArticleId(i+1);
            article.setLink("http://localhost:8080/link/" + (i+1));
            article.setTime(new Date().getTime() + i);
            article.setTitle("title_"+(i+1));
            article.setUserId((i+1));
            article.setVotes(0);

            articleService.votedArticle(article,user);
        }
    }

    @Test
    public void testHscan() {
        articleService.testHscan();
    }
}