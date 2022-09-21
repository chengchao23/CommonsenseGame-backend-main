package com.chengchao.CommonsenseGame.Repository;

import com.chengchao.CommonsenseGame.Entity.Member;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends ElasticsearchRepository<Member, String> {

    List<Member> findAll();

    List<Member> findAllByPhone(String phone);

    Optional<Member> findById(String id);

    void deleteAll();

    Optional<Member> findMemberByPhone(String phone);

    Optional<Member> findMemberByUsername(String username);
}
