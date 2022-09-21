package com.chengchao.CommonsenseGame.Entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.ArrayList;
import java.util.List;


@Data
@Document(indexName = "#{@IndexPrefixProvider.indexPrefix()}group")
public class Group {
    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    //设置type为keyword，当name是中文的时候，原本会默认为text，查询时使用模糊匹配进行查询
    private String name;

    private String member1Id;

    private String member2Id;

    private Boolean status = false;

    private List<String> unfinishedGamesId = new ArrayList<>();

    private List<String> finishedGamesId = new ArrayList<>();

    private Integer total = 0;

    private float score = 0;

    private float average = 0;

    private float topone = 0;

    private Boolean change = false;
}
