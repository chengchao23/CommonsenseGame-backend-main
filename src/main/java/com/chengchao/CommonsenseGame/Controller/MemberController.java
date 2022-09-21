package com.chengchao.CommonsenseGame.Controller;

import com.chengchao.CommonsenseGame.Configure.SecurityConfiguration;
import com.chengchao.CommonsenseGame.Entity.Member;
import com.chengchao.CommonsenseGame.Repository.MemberRepository;
import com.chengchao.CommonsenseGame.util.Response;
import com.chengchao.CommonsenseGame.util.ResponseUtil;
import com.chengchao.CommonsenseGame.util.UserDetailUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/members")
public class MemberController {

    @Resource
    private MemberRepository memberRepository;

    @Resource
    private ResponseUtil responseUtil;

    @Resource
    private UserDetailUtil userDetailUtil;

    //获得所有用户
    @GetMapping("/all")
    List<Member> all(){
        return memberRepository.findAll();
    }
    //注册
    @PostMapping("/signup")
    Response<Boolean> save(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String phone){
        List<Member> members = memberRepository.findAllByPhone(phone);
        if(!members.isEmpty()) return responseUtil.fail("手机号已经注册");
        Member member = new Member();
        member.setUsername(username);
        member.setPassword(new BCryptPasswordEncoder().encode(password));
        member.setPhone(phone);
        member.setRoles(new ArrayList<>(){{
            add("ROLE_" + SecurityConfiguration.ROLE_USER);
        }});
        memberRepository.save(member);
        return responseUtil.success();
    }
    //修改用户信息
    @PostMapping("/update")
    Response<Boolean> update(@RequestParam String name){
        String memberId = userDetailUtil.getUserDetail().getId();
        Member member = memberRepository.findById(memberId).orElse(null);
        if(member == null) return  responseUtil.fail("用户不存在");
        member.setUsername(name);
//        if(password != null) member.setPassword(new BCryptPasswordEncoder().encode(password));
        memberRepository.save(member);
        return responseUtil.success();
    }
    //查询登录用户的个人信息
    @GetMapping("/me")
    Response<Member> me(){
        String memberId = userDetailUtil.getUserDetail().getId();
        Member member = memberRepository.findById(memberId).orElse(null);
        if  (member != null) member.setPassword(null);
        return new Response<>(null, member);
    }
    //删除用户
    @DeleteMapping("/delete")
    void deleteMember(@RequestParam String id){
        memberRepository.deleteById(id);
    }
    //删除所有用户
    @DeleteMapping("/deleteAll")
    void  deleteAll(){
        memberRepository.deleteAll();
    }
    //根据电话号码查询用户
    @GetMapping("/findByPhone")
    Response<Member> findByPhone(@RequestParam String phone){
        if(memberRepository.findMemberByPhone(phone).isPresent()){
            return new Response<>(null,memberRepository.findMemberByUsername(phone).get());
        }else {
            return new Response<>("fail",null);
        }
    }
}
