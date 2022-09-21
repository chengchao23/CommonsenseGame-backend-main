package com.chengchao.CommonsenseGame.Controller;

import com.chengchao.CommonsenseGame.Entity.Group;
import com.chengchao.CommonsenseGame.Entity.Message;
import com.chengchao.CommonsenseGame.Repository.GroupRepository;
import com.chengchao.CommonsenseGame.Repository.MemberRepository;
import com.chengchao.CommonsenseGame.Repository.MessageRepository;
import com.chengchao.CommonsenseGame.util.Response;
import com.chengchao.CommonsenseGame.util.ResponseUtil;
import com.chengchao.CommonsenseGame.util.UserDetailUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Resource
    private MessageRepository messageRepository;
    @Resource
    private UserDetailUtil userDetailUtil;
    @Resource
    private MemberRepository memberRepository;
    @Resource
    private ResponseUtil responseUtil;
    @Resource
    private GroupRepository groupRepository;

    //查询作为接收方收到的所有消息
    @GetMapping("/find")
    Response<List<Message>> find(){
        String uerId = userDetailUtil.getUserDetail().getId();
        String phone = memberRepository.findById(uerId).get().getPhone();
        List<Message> messages = messageRepository.findByInviteePhone(phone);
        if(messages.isEmpty()){
            return new Response<>("no messages",null);
        }else {
            return new Response<>(null,messages);
        }
    }
    //发送申请交换角色的消息
    @PostMapping("/change")
    Response<Boolean> change(@RequestParam String groupName){
        String uerId = userDetailUtil.getUserDetail().getId();
        Optional<Group> group = groupRepository.findGroupByName(groupName);
        if(group.isEmpty()){
            return responseUtil.fail("group:"+groupName+ " didn't exists! fail to send change message");
        }
        if(!group.get().getStatus()){
            return responseUtil.fail("group didn't creat successful yet! fail to send change message");
        }
        if(group.get().getChange()){
            return responseUtil.fail("you have send the message for change role");
        }
        if(!group.get().getUnfinishedGamesId().isEmpty()){
            return responseUtil.fail("group has some game unfinished! fail to send change message");
        }
        Message message = new Message();
        message.setType(2);
        message.setInviterPhone(memberRepository.findById(uerId).get().getPhone());
        message.setInviterName(memberRepository.findById(uerId).get().getUsername());
        message.setGroupName(groupName);
        Group g = group.get();
        g.setChange(true);
        if(uerId.equals(g.getMember1Id())){
            message.setInviteePhone(memberRepository.findById(g.getMember2Id()).get().getPhone());
            message.setInviteeName(memberRepository.findById(g.getMember2Id()).get().getUsername());
            messageRepository.save(message);
            groupRepository.save(g);
            return responseUtil.success();
        }else if(uerId.equals(g.getMember2Id())){
            message.setInviteePhone(memberRepository.findById(g.getMember1Id()).get().getPhone());
            message.setInviteeName(memberRepository.findById(g.getMember1Id()).get().getUsername());
            messageRepository.save(message);
            groupRepository.save(g);
            return responseUtil.success();
        }else {
            return responseUtil.fail("fail to send change message");
        }
    }
    //接受消息
    @PostMapping("/accept")
    Response<Boolean> accept(@RequestParam String messageId){
        Optional<Message> message = messageRepository.findById(messageId);
        if(message.isEmpty()){
            return  responseUtil.fail("messageId: "+messageId+" didn't exists!");
        }
        if(message.get().getType()==1){
            //接受加入组
            String groupName = message.get().getGroupName();
            Integer role = message.get().getRole();
            Optional<Group> group = groupRepository.findGroupByName(groupName);
            if(group.isPresent()){
                Group newGroup = group.get();
                String uerId = userDetailUtil.getUserDetail().getId();
                if(role==1&&group.get().getMember1Id()==null){
                    newGroup.setMember1Id(uerId);
                    newGroup.setStatus(true);
                    groupRepository.save(newGroup);
                    messageRepository.deleteById(messageId);
                    return  responseUtil.success();
                }else if(role==2&&group.get().getMember2Id()==null){
                    newGroup.setMember2Id(uerId);
                    newGroup.setStatus(true);
                    groupRepository.save(newGroup);
                    messageRepository.deleteById(messageId);
                    return  responseUtil.success();
                }else {
                    messageRepository.deleteById(messageId);
                    return responseUtil.fail("role:"+role+" exists!");
                }
            }else {
                //若在受邀者加入之前，邀请者删除了组，查询不到相应的组,则删除消息，并且加入失败
                messageRepository.deleteById(messageId);
                return responseUtil.fail("Group: "+ groupName +" didn't exists!");
            }
        }
        else if(message.get().getType()==2){
            //接受交换角色
            String groupName = message.get().getGroupName();
            if(groupRepository.findGroupByName(groupName).isPresent()){
                Group group = groupRepository.findGroupByName(groupName).get();
                group.setChange(false);
                if(!group.getStatus()){
                    messageRepository.deleteById(messageId);
                    groupRepository.save(group);
                    return responseUtil.fail("group didn't creat successful yet! fail to change role");
                }
                if(!group.getUnfinishedGamesId().isEmpty()){
                    messageRepository.deleteById(messageId);
                    groupRepository.save(group);
                    return responseUtil.fail("group has some game unfinished! fail to change role");
                }
                String member1Id = group.getMember1Id();
                group.setMember1Id(group.getMember2Id());
                group.setMember2Id(member1Id);
                groupRepository.save(group);
                messageRepository.deleteById(messageId);
                return responseUtil.success();
            }else {
                //若在交换之前，已经删除了小组，则删除消息
                messageRepository.deleteById(messageId);
                return responseUtil.fail("group"+groupName+" didn't exists!fail to change role");
            }
        }else {
            return  responseUtil.fail("messageId: "+messageId+" has wrong type: "+message.get().getType());
        }
    }
    //拒绝消息
    @PostMapping("/refuse")
    Response<Boolean> refuse(@RequestParam String messageId){
        Optional<Message> message = messageRepository.findById(messageId);
        if(message.isEmpty()){
            return  responseUtil.fail("messageId: "+messageId+" didn't exists!");
        }
        Group group = groupRepository.findGroupByName(message.get().getGroupName()).orElse(null);
        if(group==null){
            messageRepository.deleteById(messageId);
            return  responseUtil.fail("Group: "+message.get().getGroupName()+" didn't exists!");
        }
        if(message.get().getType()==2){
            //拒绝申请交换角色
            group.setChange(false);
            groupRepository.save(group);
        }else {
            //拒绝申请加入组
            groupRepository.deleteByName(message.get().getGroupName());
        }
        messageRepository.deleteById(messageId);
        return responseUtil.success();
    }
}
