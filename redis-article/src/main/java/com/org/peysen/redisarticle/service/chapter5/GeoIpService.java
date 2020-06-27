package com.org.peysen.redisarticle.service.chapter5;

import com.org.peysen.redisarticle.entity.CityInfo;
import com.org.peysen.redisarticle.entity.IpInfo;
import com.org.peysen.redisarticle.utils.CsvUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @Author : mengmeng.pei
 * @Date : 2020/6/21
 * @Desc : 通过Ip地址查找城市ID
 *         通过城市ID查找城市具体信息
 */

@Service
public class GeoIpService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Resource(name = "redisTemplate")
    private ZSetOperations<String, String> zSetOperations;
    @Resource(name = "redisTemplate")
    private HashOperations<String, String, CityInfo> hashOperations;

    private static final String CITY  = "CITY";
    private static final String IP2CITYID  = "IP2CITYID";

    /**
     * 读取GeoLite2-City-Blocks-IPv4.csv 文件
     * 将ip 及其对应的城市id添加到redis中
     */
    public void importIpsToRedis(String fileName){
        Optional<List<IpInfo>> ipInfoOptional = new CsvUtil<IpInfo>().readCsv(fileName, IpInfo.class);
        if(ipInfoOptional.isPresent()){
            List<IpInfo> ipInfos = ipInfoOptional.get();
            System.out.println("ipInfos.size()=" + ipInfos.size());

            ipInfos.stream().forEach(ipInfo -> {
                String cityId = ipInfo.getGeoname_id().toString() + "_" + System.currentTimeMillis()/1000;
                double score = ipToScore(ipInfo.getNetwork());
                zSetOperations.add(IP2CITYID, cityId, score);
            });
        }
    }

    public void importCityToRedis(String fileName){
        Optional<List<CityInfo>> ipInfoOptional = new CsvUtil<CityInfo>().readCsv(fileName, CityInfo.class);
        if(ipInfoOptional.isPresent()){
            List<CityInfo> cityInfos = ipInfoOptional.get();
            System.out.println("cityInfos.size()=" + cityInfos.size());

            cityInfos.stream().forEach(cityInfo -> {
                hashOperations.put(CITY, cityInfo.getGeoname_id(), cityInfo);
            });
        }
    }

    public CityInfo findCityByIp(String ipAddress){
        CityInfo cityInfo = null;
        double score = ipToScore(ipAddress);

        Set<String> cityIds = zSetOperations.reverseRangeByScore(IP2CITYID, score, score, 0, 1);
        if (cityIds != null && cityIds.size() > 0){
            List<String> list = new ArrayList(cityIds);
            String cityId = list.get(0).split("_")[0];
            cityInfo = hashOperations.get(CITY, cityId);
        }
        System.out.println(cityInfo);
        return cityInfo;
    }

    public static void main(String[] args) {
        System.out.println(new GeoIpService().ipToScore("1.0.196.0/22"));
    }

    public double ipToScore(String network){
        double sum = 0;
        String [] split = null;
        if (network.contains("/")){
            String ipAddress = network.split("/")[0];
            split = ipAddress.split("\\.");
        }else{
            split = network.split("\\.");
        }

        for(String num : split){
            sum += sum * 256 + Double.valueOf(num);
        }

        return sum;
    }

}
