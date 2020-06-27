package com.org.peysen.redisarticle.utils;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.org.peysen.redisarticle.entity.IpInfo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class CsvUtil<T> {

    public Optional<List<T>> readCsv(String filePath, Class<T> clazz) {
        try {
            InputStreamReader reader = new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8);
            CSVParser csvParser = new CSVParserBuilder()
                                        .withIgnoreQuotations(true)
                                        .withIgnoreLeadingWhiteSpace(true)
                                        .withFieldAsNull(CSVReaderNullFieldIndicator.BOTH)
                                        .build();
            CSVReader csvReader = new CSVReaderBuilder(reader)
                                        .withCSVParser(csvParser)
                                        .build();

            HeaderColumnNameMappingStrategy<T> mapper = new HeaderColumnNameMappingStrategy<>();
            mapper.setType(clazz);

            return Optional.of(new CsvToBeanBuilder<T>(csvReader)
                    .withMappingStrategy(mapper)
                    .withIgnoreQuotations(true)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withFieldAsNull(CSVReaderNullFieldIndicator.BOTH)
                    .build()
                    .parse()
            );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println(String.format("csv文件: {%s} 读取异常", filePath));
        }
        return null;
    }

    public static void main(String[] args) {
        String filePath = "/Users/peysen/AllThing/works/workSpaces/IdeaSpace/testSpace/spring-redis/redis-article/src/main/resources/geoIpLite/GeoLite2-City-Blocks-IPv4.csv";
        Optional<List<IpInfo>> ipInfos = new CsvUtil<IpInfo>().readCsv(filePath, IpInfo.class);

        System.out.println(ipInfos.isPresent() ? ipInfos.get().size() : 0);
    }
}