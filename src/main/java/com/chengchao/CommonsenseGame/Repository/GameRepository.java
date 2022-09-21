package com.chengchao.CommonsenseGame.Repository;

import com.chengchao.CommonsenseGame.Entity.Game;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface GameRepository extends ElasticsearchRepository<Game,String> {

}
