package com.org.peysen.redisarticle.entity;

import com.opencsv.bean.CsvBindByPosition;
import lombok.Data;

/**
 * @Author : mengmeng.pei
 * @Date : 2020/6/21
 * @Desc :
 */

@Data
public class CityInfo {
    @CsvBindByPosition(position = 0)
    private String geoname_id;
    @CsvBindByPosition(position = 1)
    private String locale_code;
    @CsvBindByPosition(position = 2)
    private String continent_code;
    @CsvBindByPosition(position = 3)
    private String continent_name;
    @CsvBindByPosition(position = 4)
    private String country_iso_code;
    @CsvBindByPosition(position = 5)
    private String country_name;
    @CsvBindByPosition(position = 6)
    private String subdivision_1_iso_code;
    @CsvBindByPosition(position = 7)
    private String subdivision_1_name;
    @CsvBindByPosition(position = 8)
    private String subdivision_2_iso_code;
    @CsvBindByPosition(position = 9)
    private String subdivision_2_name;
    @CsvBindByPosition(position = 10)
    private String city_name;
    @CsvBindByPosition(position = 11)
    private String metro_code;
    @CsvBindByPosition(position = 12)
    private String time_zone;
    @CsvBindByPosition(position = 13)
    private String is_in_european_union;
}
