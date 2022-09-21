package com.chengchao.CommonsenseGame.service;


import com.chengchao.CommonsenseGame.Configure.SecurityUserDetail;
import com.chengchao.CommonsenseGame.Entity.Member;
import com.chengchao.CommonsenseGame.Repository.MemberRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class SecurityUserDetailService implements UserDetailsService {

  @Resource
  private MemberRepository memberRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    List<Member> members = memberRepository.findAllByPhone(username);
    if (members.isEmpty()) throw new UsernameNotFoundException("Member not found! ");
    System.out.println(members);
    Member member = members.get(0);
    if (member == null) {
      throw new UsernameNotFoundException("Member not found! ");
    }
    Collection<GrantedAuthority> authorities = new ArrayList<>() {{
      for (String role : member.getRoles()) add(new SimpleGrantedAuthority(role));
    }};
    SecurityUserDetail securityUserDetail = new SecurityUserDetail(member.getUsername(), member.getPassword(), authorities);
    securityUserDetail.setPhone(member.getPhone());
    securityUserDetail.setId(member.getId());
    return securityUserDetail;
  }
}
