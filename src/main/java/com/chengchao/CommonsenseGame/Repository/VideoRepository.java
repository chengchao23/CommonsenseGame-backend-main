package com.chengchao.CommonsenseGame.Repository;


import com.chengchao.CommonsenseGame.Entity.Video;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;
import java.util.Optional;

public interface VideoRepository extends ElasticsearchRepository<Video,String> {
    Optional<Video> findByNumber(Integer number);

    List<Video> findAll();
}
