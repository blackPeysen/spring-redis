package com.org.peysen.redisarticle.service.chapter1;

import com.org.peysen.redisarticle.entity.Article;
import com.org.peysen.redisarticle.entity.User;
import com.org.peysen.redisarticle.service.chapter1.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author : mengmeng.pei
 * @Date : 2020/4/16
 * @Desc :
 */

@Service
public class ArticleServiceImpl implements ArticleService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Resource(name = "redisTemplate")
    private SetOperations<String, Object> setOperations;
    @Resource(name = "redisTemplate")
    private ZSetOperations<String, String> zSetOperations;
    @Resource(name = "redisTemplate")
    private HashOperations<String, String, Article> hashOperations;

    private static final long ONE_WEEK_IN_SECONDS = 7 * 86400;
    private static final int VOTE_SCORE = 432;
    private static final String ARTICLE = "Article";
    private static final String VOTED = "Voted:";
    private static final String SCORE = "Score";
    private static final String TIME = "Time";

    /**
     * 作者发布一篇文章
     *  1、将自己的文章添加到article Hash散列表中
     *  2、将作者自己的id添加到投票者集合中
     *  3、将文章初始的得分和时间加入到有序集合中
     * @param article
     */
    @Override
    public void postArticle(Article article) {
        String articleId = article.getArticleId().toString();

        //1、将文章信息添加到文章Hash中,key：id，value：article
        hashOperations.put(ARTICLE, ARTICLE + ":" + articleId, article);

        //2、创建一个集合,将文章id作为key，保存该文章的投票者
        setOperations.add(VOTED + articleId, article.getUserId());
        // 设置过期时间
        redisTemplate.expire(VOTED + articleId, ONE_WEEK_IN_SECONDS, TimeUnit.SECONDS);

        //3、将文章的评分和时间放入有序集合中，用于排序刷选
        zSetOperations.add(TIME, articleId, article.getTime());
        redisTemplate.boundZSetOps(SCORE).add(articleId, article.getVotes());
    }

    /**
     * 其他人给该文章进行点赞
     *  1、判断该文章的有效期，是否还可以进行点赞
     *  2、如果有效，则将该点赞人的id加入到该文章对应的投票人集合中，防止重复点赞
     *  3、得分+432、投票数+1
     * @param article
     */
    @Override
    public void votedArticle(Article article, User user) {
        String articleId = article.getArticleId().toString();
        // 1、根据文章的id获取到该文章的发布时间
        Long cutOff = new Date().getTime() - ONE_WEEK_IN_SECONDS;
        Double acticleTime = zSetOperations.score(TIME, articleId);
        if (acticleTime < cutOff){
            System.out.println("该文章已经过期，不可以投票");
            return;
        }

        // 2、判断该用户是否已经投票过
        Boolean isExists = setOperations.isMember(VOTED + articleId, user.getUserId());
        if (!isExists){
            // 3、将该用户的id加入投票集合中，并加分
            setOperations.add(VOTED + articleId, user.getUserId());

            Article articleOld = hashOperations.get(ARTICLE, articleId);
            articleOld.setVotes(articleOld.getVotes()+1);
            hashOperations.put(ARTICLE, articleId, articleOld);

            zSetOperations.incrementScore(SCORE, articleId, VOTE_SCORE);
        }else{
            System.out.println("该用户已经给该文章投票过，不允许重复投票");
        }


    }

    @Override
    public void testHscan(){
        ScanOptions scanOptions = new ScanOptions.ScanOptionsBuilder().count(4).match(ARTICLE+"*").build();
        Cursor<Map.Entry<String, Article>> entryCursor = hashOperations.scan(ARTICLE, scanOptions);

        while(entryCursor.hasNext()){
            Map.Entry<String, Article> articleEntry = entryCursor.next();
            System.out.println(articleEntry.getKey() + "=" + articleEntry.getValue().toString());
        }
    }
}
