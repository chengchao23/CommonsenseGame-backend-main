package com.chengchao.CommonsenseGame.Entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Getter
@Setter
@Document(indexName = "#{@IndexPrefixProvider.indexPrefix()}video")
public class Video {
    @Id
    private String id;

    private Integer number;
    private String category;
    private String captions;
    private String frequentWords;
    private String url;
}
