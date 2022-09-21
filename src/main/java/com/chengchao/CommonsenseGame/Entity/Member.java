package com.chengchao.CommonsenseGame.Entity;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@Document(indexName = "#{@IndexPrefixProvider.indexPrefix()}member")
@Data
public class Member implements UserDetails {
    @Id
    private String id;
    //昵称
    private String username;
    //密码
    private String password;

    //可以看作是唯一的用户名
    @Field(type = FieldType.Keyword)
    private String phone;

    private List<String> roles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new ArrayList<SimpleGrantedAuthority>() {{
            for (String role : roles) add(new SimpleGrantedAuthority(role));
        }};
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
