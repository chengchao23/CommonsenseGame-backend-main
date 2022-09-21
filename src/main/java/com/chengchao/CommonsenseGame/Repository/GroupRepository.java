package com.chengchao.CommonsenseGame.Repository;

import com.chengchao.CommonsenseGame.Entity.Group;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends ElasticsearchRepository<Group, String> {

    
    void deleteByName(String name);

    List<Group> findGroupsByMember1Id(String id);

    List<Group> findGroupsByMember2Id(String id);

    Optional<Group> findGroupByName(String name);



}
