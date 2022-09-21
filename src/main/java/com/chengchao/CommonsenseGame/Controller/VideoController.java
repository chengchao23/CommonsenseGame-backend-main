package com.chengchao.CommonsenseGame.Controller;

import com.chengchao.CommonsenseGame.Entity.Video;
import com.chengchao.CommonsenseGame.Repository.VideoRepository;
import com.chengchao.CommonsenseGame.util.Response;
import com.chengchao.CommonsenseGame.util.ResponseUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/video")
public class VideoController {
    @Resource
    private VideoRepository videoRepository;
    @Resource
    private ResponseUtil responseUtil;

    @RequestMapping("/one")
    Response<Video> find(@RequestParam Integer number){
        Video video =   videoRepository.findByNumber(number).orElse(null);
        if(video!=null){
            return  new Response<>(null,video);
        }else {
            return new Response<>("video:"+number+" didn't exists!",null);
        }
    }

    @PostMapping("/add")
    Response<Boolean> add(@RequestParam Integer number,
                          @RequestParam String captions,
                          @RequestParam String frequentWords,
                          @RequestParam String category){
        if(videoRepository.findByNumber(number).isPresent()){
            return responseUtil.fail("video:"+number+"has exists!");
        }else {
            Video video = new Video();
            video.setCaptions(captions);
            video.setFrequentWords(frequentWords);
            video.setCategory(category);
            video.setUrl("url"+number);
            videoRepository.save(video);
            return  responseUtil.success();
        }
    }

    @GetMapping("/all")
    Response<List<Video>> all(){
        List<Video> videos = videoRepository.findAll();
        System.out.println(videos.size());
        return new Response<>(null,videos);
    }

    @DeleteMapping("/deleteAll")
    void deleteAll(){
        videoRepository.deleteAll();
    }

}
