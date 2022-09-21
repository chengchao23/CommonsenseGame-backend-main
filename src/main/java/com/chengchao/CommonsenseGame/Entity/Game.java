package com.chengchao.CommonsenseGame.Entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.List;

@Data
@Document(indexName = "#{@IndexPrefixProvider.indexPrefix()}game")
public class Game {
    @Id
    private String id;

    private Integer videoNumber;

    private String groupName;
    private String member1Name;
    private String member2Name;

    //for member1
    private String character;
    private String behavior;
    private String intention;
    private String result;
    private String attribute;

    //for member2
    private String caption;

    private float score;

    private String simCaption;
    /**
     * 0 表示创建阶段
     * 1 表示成员1已经完成
     * 2 表示成员2已经完成
     */
    private Integer status;

}
