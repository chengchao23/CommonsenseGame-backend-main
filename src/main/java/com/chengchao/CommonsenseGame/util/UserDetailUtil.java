package com.chengchao.CommonsenseGame.util;

import com.chengchao.CommonsenseGame.Configure.SecurityUserDetail;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserDetailUtil {

  public SecurityUserDetail getUserDetail() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//    System.out.println("x: "+authentication.getPrincipal().toString());
    return (SecurityUserDetail) authentication.getPrincipal();
  }
}
