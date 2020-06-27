package com.org.peysen.redisarticle.service.chapter8;

import com.org.peysen.redisarticle.common.Common;
import com.org.peysen.redisarticle.entity.Inform;
import com.org.peysen.redisarticle.entity.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author : mengmeng.pei
 * @Date : 2020/6/22
 * @Desc :
 */

@Service
public class UserService {
//    @Autowired
//    private Redisson redisson;
    @Autowired
    private RedisTemplate redisTemplate;
    @Resource(name = "redisTemplate")
    private SetOperations<String, Object> setOperations;
    @Resource(name = "redisTemplate")
    private ZSetOperations<String, String> zSetOperations;

    private static final String STATUS_ID = "STATUS_ID";
    private static final String USER = "Users:";
    private static final String STATUS = "Status";
    public static final String HOME = "Home:";
    public static final String PROFILE = "Profile:";
    // 某用户关注的用户列表
    private static final String FOLLOWING = "Following:";
    // 某用户被关注的用户列表
    private static final String FOLLOWERS = "Followers:";


    /**
     * 创建用户
     * @param user
     * @throws InterruptedException
     * @throws IllegalAccessException
     */
    public void createUser(User user) throws IllegalAccessException {
        if (user != null){
            String hashKey = USER + user.getUserId();
            if(!redisTemplate.hasKey(hashKey)){
                redisTemplate.opsForHash().putAll(hashKey, Common.objectToMap(user));
            }else{
                System.out.println("已经存在该用户：" + user.getUserId());
            }
//            RLock lock = redisson.getLock(user.getLoginName().toLowerCase());
//            if (lock.tryLock(1,1, TimeUnit.SECONDS)){
//
//            }
        }
    }

    /**
     * 用户发布状态信息
     * @param inform
     * @throws IllegalAccessException
     */
    public void createInform(Inform inform) {
        if (inform != null){
            inform.setId(redisTemplate.opsForValue().increment(STATUS_ID));
            redisTemplate.opsForHash().put(STATUS, String.valueOf(inform.getId()),  inform);
            zSetOperations.add(PROFILE + inform.getUId(), String.valueOf(inform.getId()), System.currentTimeMillis() / 1000);
        }
    }

    /**
     * 获取用户自己发布的信息
     * @param user
     */
    public List<Inform> getUserStatus(User user, String timeLine, int page, int pageCount){
        List<Inform> informs = null;
        if (user != null){
            int start = (page - 1) * pageCount;
            int end = page*pageCount-1;
            Set<String> statusId = zSetOperations.reverseRange(timeLine + user.getUserId(), start, end);
            informs = redisTemplate.opsForHash().multiGet(STATUS, statusId);
        }

        return informs;
    }

    /**
     * 用户user 关注了另一个用户 otherUser
     *      1、在该用户的关注列表中添加otherUser
     *      2、在otherUser的被关注列表中添加user
     *      3、在user的时间主线中添加otheruser个人时间线
     * @param user
     * @param otherUser
     */
    public void followUser(User user, User otherUser){
        if(user != null && otherUser != null){
            Integer userId = user.getUserId();
            Integer otherUserId = otherUser.getUserId();
            String followingKey = FOLLOWING + userId;
            String followersKey = FOLLOWERS + otherUserId;

            // 如果user已经关注了otherUser，则返回
            Double score = redisTemplate.opsForZSet().score(followingKey, otherUserId);
            if (score !=null && score > 0){
                System.out.println("该用户已经关注了该用户。。。");
                return;
            }

            redisTemplate.executePipelined(new RedisCallback<Object>() {
                @Override
                public Object doInRedis(RedisConnection connection) throws DataAccessException {
                    connection.openPipeline();
                    /**
                     * 1、在该用户的关注列表中添加otherUser
                     * 2、在otherUser的被关注列表中添加user
                     */
                    connection.zAdd(followingKey.getBytes(), Common.getCurrentSeconds(),otherUserId.toString().getBytes());
                    connection.zAdd(followersKey.getBytes(), Common.getCurrentSeconds(),userId.toString().getBytes());

                    /**
                     * 将各自用户的正在关注数量以及关注者数量+1
                     */
                    redisTemplate.opsForHash().increment(USER + userId, "following", 1);
                    redisTemplate.opsForHash().increment(USER + otherUserId, "followers", 1);

                    /**
                     * 从被关注者的个人时间线中获取指定数量的最新状态信息，
                     */
                    List<Inform> userStatus = getUserStatus(otherUser,PROFILE, 1, 30);
                    if(CollectionUtils.isEmpty(userStatus)){
                        return null;
                    }
                    /**
                     * 将被关注者的最新状态信息添加到该用户的时间线中
                     */
                    Set<ZSetOperations.TypedTuple> typedTuples = new HashSet<>();
                    userStatus.stream().forEach(inform -> {
                        ZSetOperations.TypedTuple typedTuple = new DefaultTypedTuple(inform.getId(), Double.valueOf(System.currentTimeMillis() / 1000));
                        typedTuples.add(typedTuple);
                    });
                    redisTemplate.opsForZSet().add(HOME + userId, typedTuples);
                    redisTemplate.opsForZSet().removeRange(HOME + userId, 0, -11);

                    return null;
                }
            });
        }
    }

    /**
     * 用户user 取消关注了另一个用户 otherUser
     *      1、在该用户的关注列表中删除otherUser
     *      2、在otherUser的被关注列表中删除user
     *      3、在user的时间主线中删除otheruser个人时间线
     * @param user
     * @param otherUser
     */
    public void unFollowUser(User user, User otherUser){
        if(user != null && otherUser != null){
            Integer userId = user.getUserId();
            Integer otherUserId = otherUser.getUserId();
            String followingKey = FOLLOWING + userId;
            String followersKey = FOLLOWERS + otherUserId;

            // 如果user已经关注了otherUser，则返回
            Double score = redisTemplate.opsForZSet().score(followingKey, otherUserId);
            if (score == null){
                System.out.println("该用户并没有关注该用户。。。");
                return;
            }

            redisTemplate.executePipelined(new RedisCallback<Object>() {
                @Override
                public Object doInRedis(RedisConnection connection) throws DataAccessException {
                    connection.openPipeline();
                    /**
                     * 1、在该用户的关注列表中删除otherUser
                     * 2、在otherUser的被关注列表中删除user
                     */
                    connection.zRem(followingKey.getBytes(), otherUserId.toString().getBytes());
                    connection.zRem(followersKey.getBytes(), userId.toString().getBytes());

                    /**
                     * 将各自用户的正在关注数量以及关注者数量-1
                     */
                    redisTemplate.opsForHash().increment(USER + userId, "following", -1);
                    redisTemplate.opsForHash().increment(USER + otherUserId, "followers", -1);

                    /**
                     * 从被关注者的个人时间线中获取指定数量的最新状态信息，
                     */
                    List<Inform> userStatus = getUserStatus(otherUser,PROFILE, 1, 30);
                    if(CollectionUtils.isEmpty(userStatus)){
                        return null;
                    }
                    /**
                     * 将被关注者的最新状态信息删除到该用户的时间线中
                     */
                    List<Long> infoIds = userStatus.stream()
                                                    .map(Inform::getId)
                                                    .collect(Collectors.toList());
                    redisTemplate.opsForZSet().remove(HOME + userId, infoIds);
                    redisTemplate.opsForZSet().removeRange(HOME + userId, 0, -11);

                    return null;
                }
            });
        }
    }


    public String getUserInfo(String userId, String key){
        if (StringUtils.isNotBlank(userId)){
            String hashKey = USER + userId;
            return redisTemplate.opsForHash().get(hashKey, key).toString();
        }
        return null;
    }

}
