package com.chengchao.CommonsenseGame.Controller;

import com.chengchao.CommonsenseGame.Entity.*;
import com.chengchao.CommonsenseGame.Repository.*;
import com.chengchao.CommonsenseGame.util.Response;
import com.chengchao.CommonsenseGame.util.ResponseUtil;
import com.chengchao.CommonsenseGame.util.UserDetailUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.*;
import java.util.*;


@RestController
@RequestMapping("/game")
public class GameController {

    @Resource
    private GameRepository gameRepository;

    @Resource
    private VideoRepository videoRepository;

    @Resource
    private ResponseUtil responseUtil;

    @Resource
    private UserDetailUtil userDetailUtil;

    @Resource
    private GroupRepository groupRepository;


    @PostMapping("/start")
    Response<Pair<Game,Integer>> start(@RequestParam String groupName) throws IOException {
        String userId = userDetailUtil.getUserDetail().getId();
        Optional<Group> group1 = groupRepository.findGroupByName(groupName);
        if(group1.isEmpty()){
            return new Response<>("group:"+groupName+" didn't exists! fail to start",null);
        }
        if(!group1.get().getStatus()){
            return new Response<>("group:"+groupName+" didn't create succeeded! fail to start",null);
        }
        if(group1.get().getChange()){
            return new Response<>("group:"+groupName+" is changing role! fail to start",null);
        }
        Group group = group1.get();
        if(userId.equals(group.getMember1Id())){
            //成员1创建游戏返回Game
            Game game = new Game();
            game.setGroupName(groupName);
            // 获取当前组做过的所有游戏的Id
            List<String> gameList = group.getFinishedGamesId();
            gameList.addAll(group.getUnfinishedGamesId());
            List<Integer> gamesNumber =new ArrayList<>();
            //根据游戏Id找到所有做过的video编号
            for(String gameId :gameList){
                gamesNumber.add(gameRepository.findById(gameId).get().getVideoNumber());
            }
            //挑选一个没有做过的video
            ClassPathResource classPathResource = new ClassPathResource("static/testResource/视频测试.txt");
            InputStream inputStream =classPathResource.getInputStream();
            InputStreamReader read = new InputStreamReader(inputStream);// 考虑到编码格式
            BufferedReader bufferedReader = new BufferedReader(read);
            String VideoNum = bufferedReader.readLine();
            bufferedReader.close();
            read.close();
            String[] numbers = VideoNum.split(",");
            Random random = new Random();
            int r = random.nextInt(numbers.length);
            while (gamesNumber.contains(Integer.parseInt(numbers[r]))){
                r = random.nextInt(numbers.length);
            }
            game.setVideoNumber(Integer.parseInt(numbers[r]));
            game.setStatus(0);
            String gameId = gameRepository.save(game).getId();
            return new Response<>(null,Pair.of(gameRepository.findById(gameId).get(),1));
        }else if(userId.equals(group.getMember2Id())){
            //成员2找到最近需要完成的游戏，返回Game
            if(group.getUnfinishedGamesId().isEmpty()){
                return new Response<>("您的角色是猜标题者，"+groupName+"目前没有您可以进行猜标题的活动",null);
            }else {
                return new Response<>(null,Pair.of(gameRepository.findById(group.getUnfinishedGamesId().get(0)).get(),2));
            }
        }else {
            return new Response<>("you didn't join Group: "+groupName,null);
        }
    }
    @PostMapping("/deleteGame")
    Response<Boolean> deleteGame(@RequestParam String gameId){
        Game game = gameRepository.findById(gameId).orElse(null);
        if(game==null){return  responseUtil.fail("游戏不存在");}
        gameRepository.deleteById(gameId);
        return responseUtil.success();
    }
    @PostMapping("/next")
    Response<Game> next(@RequestParam String groupName,
                        @RequestParam String Id) throws IOException {
        Group group = groupRepository.findGroupByName(groupName).orElse(null);
        if(group!=null){
            Integer number = gameRepository.findById(Id).get().getVideoNumber();
            gameRepository.deleteById(Id);
            Game game = new Game();
            game.setGroupName(groupName);
            // 获取当前组做过的所有游戏的Id
            List<String> gameList = group.getFinishedGamesId();
            gameList.addAll(group.getUnfinishedGamesId());
            List<Integer> gamesNumber =new ArrayList<>();
            gamesNumber.add(number);
            //根据游戏Id找到所有做过的video编号
            for(String gameId :gameList){
                gamesNumber.add(gameRepository.findById(gameId).get().getVideoNumber());
            }
            //挑选一个没有做过的video
            ClassPathResource classPathResource = new ClassPathResource("static/testResource/视频测试.txt");
            InputStream inputStream =classPathResource.getInputStream();
            InputStreamReader read = new InputStreamReader(inputStream);// 考虑到编码格式
            BufferedReader bufferedReader = new BufferedReader(read);
            String VideoNum = bufferedReader.readLine();
            bufferedReader.close();
            read.close();
            String[] numbers = VideoNum.split(",");
            Random random = new Random();
            int r = random.nextInt(numbers.length);
            while (gamesNumber.contains(Integer.parseInt(numbers[r]))){
                r = random.nextInt(numbers.length);
            }
            game.setVideoNumber(Integer.parseInt(numbers[r]));
            game.setStatus(0);
            String gameid = gameRepository.save(game).getId();
            return new Response<>(null,gameRepository.findById(gameid).get());
        }else {
            return new Response<>("组名为："+groupName+"的小组不存在",null);
        }
    }
    @PostMapping("/member1Submit")
    Response<Boolean> member1Submit(@RequestParam String groupName,
                                    @RequestParam String gameId,
                                    @RequestParam String character,
                                    @RequestParam String behavior,
                                    @RequestParam String intention,
                                    @RequestParam String result,
                                    @RequestParam String attribute){
        if(gameRepository.findById(gameId).isEmpty()){
            return responseUtil.fail("game didn't exists!");
        }
        Group group=groupRepository.findGroupByName(groupName).orElse(null);
        if(group==null){
            gameRepository.deleteById(gameId);
            return responseUtil.fail("group didn't exists! delete game:"+gameId);
        }
        if(gameRepository.findById(gameId).get().getStatus()==1){
            return responseUtil.fail("you have submitted");
        }
        //成员1填写游戏
        Game game = gameRepository.findById(gameId).get();
        game.setCharacter(character);
        game.setBehavior(behavior);
        game.setIntention(intention);
        game.setResult(result);
        game.setAttribute(attribute);
        game.setStatus(1);
        game.setMember1Name(userDetailUtil.getUserDetail().getUsername());
        gameRepository.save(game);
        //加入到组未完成游戏列表中
        group.getUnfinishedGamesId().add(gameId);
        groupRepository.save(group);
        return responseUtil.success();
    }

    @PostMapping("/member2Submit")
    Response<Float> member2Submit(@RequestParam String groupName,
                                    @RequestParam String gameId,
                                    @RequestParam String caption,
                                    @RequestParam Float score,
                                    @RequestParam String simCaption
                                    ){
        if(gameRepository.findById(gameId).isEmpty()){
            return new Response<>("game didn't exists!",null);
        }
        if(groupRepository.findGroupByName(groupName).isEmpty()){
            gameRepository.deleteById(gameId);
            return new Response<>("group didn't exists!",null);
        }
        if(gameRepository.findById(gameId).get().getStatus()==2){
            return new Response<>("you have submitted",null);
        }
        //成员2填写游戏
        Game game = gameRepository.findById(gameId).get();
        game.setMember2Name(userDetailUtil.getUserDetail().getUsername());
        game.setCaption(caption);
        game.setSimCaption(simCaption);
        game.setScore(score);
        game.setStatus(2);
        //添加游戏Id至小组的完成游戏列表中，从游戏未完成游戏列表中移除游戏Id
        Group group = groupRepository.findGroupByName(groupName).get();
        group.setTotal(group.getTotal()+1);
        group.setScore(group.getScore()+score);
        group.setAverage(group.getScore()/group.getTotal());
        if(score>group.getTopone()){
            group.setTopone(score);
        }
        group.getUnfinishedGamesId().remove(gameId);
        group.getFinishedGamesId().add(gameId);
        gameRepository.save(game);
        groupRepository.save(group);
        return new Response<>(null,score);
    }

    @GetMapping("/findByGameId")
    Response<Game> findByGameId(@RequestParam String gameId){
        if(gameRepository.findById(gameId).isPresent()){
            return new Response<>(null,gameRepository.findById(gameId).get());
        }else {
            return  new Response<>("fail to find game!",null);
        }
    }

    @GetMapping("/findAll")
    Response<List<Game>> findAll(){
        Iterable<Game> gameList = gameRepository.findAll();
        List<Game> games = new ArrayList<>();
        for(Game game : gameList){
            games.add(game);
        }
        return new Response<>(null,games);
    }

    @GetMapping("/findFinishedGamesByGroupName")
    Response<List<Game>> findFinishedGamesByGroupName(@RequestParam String groupName){
        if(groupRepository.findGroupByName(groupName).isPresent()){
            List<String> gamesId = groupRepository.findGroupByName(groupName).get().getFinishedGamesId();
            List<Game> games = new ArrayList<>();
            for(String id: gamesId){
                games.add(gameRepository.findById(id).get());
            }
            return new Response<>(null,games);
        }else {
            return new Response<>("group didn't exists!",null);
        }
    }

    @GetMapping("/findUnfinishedGamesByGroupId")
    Response<List<Game>> findUnfinishedGamesByGroupId(@RequestParam String groupId){
        if(groupRepository.findById(groupId).isPresent()){
            List<String> gamesId = groupRepository.findById(groupId).get().getUnfinishedGamesId();
            List<Game> games = new ArrayList<>();
            for(String id: gamesId){
                games.add(gameRepository.findById(id).get());
            }
            return new Response<>(null,games);
        }else {
            return new Response<>("group didn't exists!",null);
        }
    }

    @GetMapping("/findByAsMember2")
    //找到作为成员2的未完成游戏，返回组名和未完成游戏数量
    Response<List<Pair<String,Integer>>> findByAsMember2(){
        String userId = userDetailUtil.getUserDetail().getId();
        List<Group> groups = groupRepository.findGroupsByMember2Id(userId);
        List<Pair<String,Integer>> games = new ArrayList<>();
        for(Group group:groups){
            List<String> gamesId = group.getUnfinishedGamesId();
            if(!gamesId.isEmpty()){
                games.add(Pair.of(group.getName(),gamesId.size()));
            }
        }
        if(games.isEmpty()){
            return  new Response<>("There is no unfinished game for member 2 to do right now by memberId!",null);
        }else {
            return new Response<>(null,games);
        }
    }

    @DeleteMapping("/delete")
    //删除所有游戏
    void delete(){
        gameRepository.deleteAll();
    }

    @DeleteMapping("/deleteOne")
    //根据ID删除游戏
    void deleteOne(@RequestParam String Id){
        gameRepository.deleteById(Id);
    }

}
