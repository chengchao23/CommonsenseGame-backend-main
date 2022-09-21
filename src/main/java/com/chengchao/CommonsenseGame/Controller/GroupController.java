package com.chengchao.CommonsenseGame.Controller;

import com.chengchao.CommonsenseGame.Entity.Game;
import com.chengchao.CommonsenseGame.Entity.Group;
import com.chengchao.CommonsenseGame.Entity.GroupDetail;
import com.chengchao.CommonsenseGame.Entity.Message;
import com.chengchao.CommonsenseGame.Repository.GameRepository;
import com.chengchao.CommonsenseGame.Repository.GroupRepository;
import com.chengchao.CommonsenseGame.Repository.MemberRepository;
import com.chengchao.CommonsenseGame.Repository.MessageRepository;
import com.chengchao.CommonsenseGame.util.Response;
import com.chengchao.CommonsenseGame.util.ResponseUtil;
import com.chengchao.CommonsenseGame.util.UserDetailUtil;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

@RestController
@RequestMapping("/group")
public class GroupController {
    @Resource
    private GroupRepository groupRepository;
    @Resource
    private ResponseUtil responseUtil;
    @Resource
    private UserDetailUtil userDetailUtil;
    @Resource
    private MemberRepository memberRepository;
    @Resource
    private MessageRepository messageRepository;
    @Resource
    private GameRepository gameRepository;
    //创建组
    @PostMapping("/create")
    Response<Boolean> creat(@RequestParam String groupName, @RequestParam Integer role, @RequestParam String inviteePhone){
        if(groupRepository.findGroupByName(groupName).isPresent()){
            return responseUtil.fail("Group: "+ groupRepository.findGroupByName(groupName).get().toString() +" exists!");
        }else{
            //查询被邀请的成员是否存在，若存在则发送邀请信息
            if(memberRepository.findMemberByPhone(inviteePhone).isPresent()){
                String uerId = userDetailUtil.getUserDetail().getId();
                if(uerId.equals(memberRepository.findMemberByPhone(inviteePhone).get().getId())){
                    return  responseUtil.fail("you can't invite yourself");
                }
                //创建组
                Group group = new Group();
                group.setName(groupName);
                if(role==1){
                    group.setMember1Id(uerId);
                }else if(role==2){
                    group.setMember2Id(uerId);
                }
                Message message = new Message();
                message.setInviterPhone(memberRepository.findById(uerId).get().getPhone());
                message.setInviterName(memberRepository.findById(uerId).get().getUsername());
                message.setInviteePhone(inviteePhone);
                message.setInviteeName(memberRepository.findMemberByPhone(inviteePhone).get().getUsername());
                message.setGroupName(groupName);
                message.setType(1);
                if(role==1){
                    message.setRole(2);
                }else{
                    message.setRole(1);
                }
                messageRepository.save(message);
                groupRepository.save(group);
                return responseUtil.success();
            }else{
                return  responseUtil.fail("Invitee: "+ inviteePhone +" didn't exists!" );
            }
        }
    }
    @DeleteMapping("/delete")
    Response<Boolean> delete(@RequestParam String groupName) {
        Optional<Group> group = groupRepository.findGroupByName(groupName);
        if (group.isPresent()) {
            //删除改小组的所有未完成游戏
            List<String> gamesIdU = group.get().getUnfinishedGamesId();
            for(String gameId : gamesIdU) {
                gameRepository.deleteById(gameId);
            }
            //删除改小组做过的所有游戏
            List<String> gamesId = group.get().getFinishedGamesId();
            for(String gameId : gamesId) {
                gameRepository.deleteById(gameId);
            }
            groupRepository.deleteByName(groupName);
            return responseUtil.success();
        }else{
            return responseUtil.fail("Group " + groupName + " didn't exists!");
        }
    }
    //找到登录成员参加的所有小组信息
    @GetMapping("/findAll")
    Response<List<Pair<GroupDetail,Group>>> findAll(){
        String uerId = userDetailUtil.getUserDetail().getId();
        //成员1加入的所有小组（作为角色1和角色2），包括未创建成功的组
        List<Group> groupAs1 = groupRepository.findGroupsByMember1Id(uerId);
        List<Group> groupAs2 = groupRepository.findGroupsByMember2Id(uerId);
        List<Group> groupAs1AndTrue = new ArrayList<>();
        List<Group> groupAs2AndTrue = new ArrayList<>();
        //筛选出创建成功的组
        for(Group group:groupAs1){
            if(group.getStatus()){
                groupAs1AndTrue.add(group);
            }
        }
        for(Group group:groupAs2){
            if(group.getStatus()){
                groupAs2AndTrue.add(group);
            }
        }
        groupAs1AndTrue.addAll(groupAs2AndTrue);
        //创建返回结果信息
        if(!groupAs1AndTrue.isEmpty()){
            List<Pair<GroupDetail,Group>> res = new ArrayList<>();
            for(Group group:groupAs1AndTrue){
                GroupDetail groupDetail = new GroupDetail();
                groupDetail.setName(group.getName());
                groupDetail.setChange(group.getChange());
                if(uerId.equals(group.getMember1Id())){
                    groupDetail.setTeammate(memberRepository.findById(group.getMember2Id()).get().getUsername());
                    groupDetail.setRole("描述者");
                }else {
                    groupDetail.setTeammate(memberRepository.findById(group.getMember1Id()).get().getUsername());
                    groupDetail.setRole("猜测者");
                }
                groupDetail.setScore(group.getScore());
                groupDetail.setTotal(group.getTotal());
                groupDetail.setAverage(group.getAverage());
                res.add(Pair.of(groupDetail,group));
            }
            return new Response<>(null,res);
        }else {
            return  new Response<>("member:"+memberRepository.findById(uerId).get().getUsername()+" hasn't join any group",null);
        }
    }
    //查询数据库中的所有小组
    @GetMapping("/all")
    Response<List<Group>> all(){
        Iterable<Group> groups =  groupRepository.findAll();
        List<Group> groupList = new ArrayList<>();
        for(Group group : groups){
            groupList.add(group);
        }
        return new Response<>(null,groupList);
    }
    @GetMapping("/allValid")
    Response<List<Group>> allValid(){
        Iterable<Group> groups =  groupRepository.findAll();
        List<Group> groupList = new ArrayList<>();
        for(Group group : groups){
            if(group.getStatus()&&group.getTotal()!=0)
                groupList.add(group);
        }
        groupList.sort(new Comparator<Group>() {
            @Override
            public int compare(Group o1, Group o2) {
                if (o1.getAverage()<o2.getAverage()){
                    return 1;
                }else if(o1.getAverage()>o2.getAverage()){
                    return -1;
                }else {
                    return 0;
                }
            }
        });
        return new Response<>(null,groupList);
    }
    //更名
    @PostMapping("/rename")
    Response<Boolean> rename(@RequestParam String groupName,@RequestParam String newName){
        Optional<Group> group = groupRepository.findGroupByName(groupName);
        if(group.isPresent()){
            if(groupRepository.findGroupByName(newName).isPresent()){
                return responseUtil.fail("组名为："+groupName+"的小组已经存在");
            }else if(!group.get().getStatus()){
                return responseUtil.fail("组名为："+groupName+"的小组还未创建完成,不能更改队名");
            }else if(group.get().getChange()){
                return responseUtil.fail("组名为："+groupName+"的小组正在交换角色,不能更改队名");
            }else{
                Group newGroup = group.get();
                newGroup.setName(newName);
                groupRepository.save(newGroup);
                return responseUtil.success();
            }
        }else {
            return responseUtil.fail("组名为："+groupName+"的小组不存在");
        }
    }

    @GetMapping("/find")
    Response<Group> find(@RequestParam String groupName){
        Group group = groupRepository.findGroupByName(groupName).orElse(null);
        if(group!=null){
            return new Response<>(null,group);
        }else {
            return new Response<>("组名为"+groupName+"的组不存在",null);
        }
    }
    @DeleteMapping("/Delete")
    Response<Boolean> Delete(@RequestParam String groupName){
        groupRepository.deleteByName(groupName);
        return responseUtil.success();
    }
    @PostMapping("/wirteTopone")
    Response<Boolean> wirteTopone(){
        Iterable<Group> groupList = groupRepository.findAll();
        for(Group g : groupList){
            if(g.getStatus()){
                float topone = 0;
                List<String> finishedGamesId = groupRepository.findById(g.getId()).get().getFinishedGamesId();
                for(String id : finishedGamesId){
                    float socre = gameRepository.findById(id).get().getScore();
                    if(socre>topone){
                        topone = socre;
                    }
                }
                g.setTopone(topone);
                groupRepository.save(g);
            }
        }
        return  responseUtil.success();
    }
    @GetMapping("/valid")
    Response<List<Pair<String,Integer>>> valid(){
        List<Pair<String,Integer>> pairList = new ArrayList<>();
        Iterable<Group> groups = groupRepository.findAll();
        for(Group group : groups){
            if(group.getStatus()&&group.getTotal()>10){
                List<String> finishedGamesId= group.getFinishedGamesId();
                int count = 0;
                for( String id : finishedGamesId){
                    if(gameRepository.findById(id).get().getScore()>0.4)
                        count++;
                }
                pairList.add(Pair.of(group.getName(),count));
            }
        }
        return new Response<>("succeed",pairList);
    }
    @GetMapping("/result")
    Response<List<Game>> result(@RequestParam String groupName,@RequestParam Float threshold){
        Optional<Group> group = groupRepository.findGroupByName(groupName);
        if(group.isPresent()){
            List<String> gamesIdList = group.get().getFinishedGamesId();
            List<Game> games = new ArrayList<>();
            for( String gameId : gamesIdList){
                Game game = gameRepository.findById(gameId).get();
                if(game.getScore()>threshold)
                    games.add(game);
            }
            return  new Response<>("success",games);
        }else {
            return new Response<>("组名为："+groupName+"的小组不存在",null);
        }
    }
}
