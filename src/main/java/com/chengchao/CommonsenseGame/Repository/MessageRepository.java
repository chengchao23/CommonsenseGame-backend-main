package com.chengchao.CommonsenseGame.Repository;

import com.chengchao.CommonsenseGame.Entity.Message;
import com.chengchao.CommonsenseGame.Entity.Video;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends ElasticsearchRepository<Message, String>{

    void  deleteMessageById(String id);

    List<Message> findByInviteePhone(String inviterPhone);


}
