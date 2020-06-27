package com.org.peysen.redisarticle.entity;

import com.opencsv.bean.CsvBindByPosition;
import lombok.Data;

/**
 * @Author : mengmeng.pei
 * @Date : 2020/6/21
 * @Desc :
 */

@Data
public class IpInfo {
    @CsvBindByPosition(position = 0)
    private String network;
    @CsvBindByPosition(position = 1)
    private Integer geoname_id;
    @CsvBindByPosition(position = 2)
    private Integer registered_country_geoname_id;
    @CsvBindByPosition(position = 3)
    private Integer represented_country_geoname_id;
    @CsvBindByPosition(position = 4)
    private Integer is_anonymous_proxy;
    @CsvBindByPosition(position = 5)
    private Integer is_satellite_provider;
    @CsvBindByPosition(position = 6)
    private String postal_code;
    @CsvBindByPosition(position = 7)
    private Double latitude;
    @CsvBindByPosition(position = 8)
    private Double longitude;
    @CsvBindByPosition(position = 9)
    private Integer accuracy_radius;
}
