/*
package com.example.video.controller;

import com.example.video.entity.*;
import com.example.video.server.IVideoService;
import com.example.video.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

*/
/**
 * 功能描述
 * 不用了
 * @author: jxx
 * @date: 2024年06月06日 11:53
 *//*

@RestController
@RequestMapping("/video0")
@Api(tags = "视频接口0")
public class VideoController {
    @Autowired
    private IVideoService videoService;

    @GetMapping("/getData/{count}")
    @ApiOperation("获取视频数据")
    @CrossOrigin
    public Result<VideoData> getData(@PathVariable int count) {
        VideoData videoData = videoService.getVideoData(Constants.UNIQUE_KEY);
        if(videoData != null) {
            videoData.setSources(videoService.getLatestSources(Constants.UNIQUE_KEY, count));
        }
        return Result.success(videoData);
    }

    @PostMapping("/data")
    @ApiOperation("保存视频数据")
    @CrossOrigin
    public ResponseEntity<String> saveData(@RequestBody VideoData videoData) {
        String key = Constants.UNIQUE_KEY; // 如果每个VideoData对象都有唯一的键，你可以使用那个作为键
        videoService.saveVideoData(key, videoData);
        return new ResponseEntity<>("VideoData has been successfully saved in Redis", HttpStatus.OK);
    }

    @PostMapping("/addSource")
    @ApiOperation("添加视频源")
    @CrossOrigin
    public Result addSource(@RequestBody AddUrl addUrl) {
        VideoData videoData = videoService.getVideoData(Constants.UNIQUE_KEY);
        List<Video> sources = videoData.getSources();

        int nextId = 1;
        if (!sources.isEmpty()) {
            nextId = sources.stream()
                    .mapToInt(Video::getId)
                    .max()
                    .getAsInt() + 1;
        }

        Video newVideo = new Video();
        newVideo.setId(nextId);
        newVideo.setUrl(addUrl.getUrl());

        sources.add(newVideo);

        videoService.saveVideoData(Constants.UNIQUE_KEY, videoData);

        return Result.success("添加成功");
    }

    @DeleteMapping("/deleteSource/{id}")
    @CrossOrigin
    public Result deleteSource(@PathVariable int id) {
        VideoData videoData = videoService.getVideoData(Constants.UNIQUE_KEY);
        List<Video> sources = videoData.getSources();

        boolean removed = sources.removeIf(video -> video.getId() == id);

        if (removed) {
            videoService.saveVideoData(Constants.UNIQUE_KEY, videoData);
            return Result.success("删除成功");
        }

        return Result.error("没有找到id为" + id + "的视频源, 删除失败");
    }

    @PostMapping("/switchSource")
    @ApiOperation("切换视频源")
    @CrossOrigin
    public Result<VideoData> switchSource(@RequestParam int id) {
        String key = Constants.UNIQUE_KEY;
        VideoData videoData = videoService.getVideoData(key);
        List<Video> sources = videoData.getSources();

        Optional<Video> videoOptional = sources.stream()
                .filter(video -> video.getId() == id)
                .findFirst();

        if(videoOptional.isPresent()) {
            videoData.setApply(videoOptional.get());
            videoService.saveVideoData(key, videoData);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("apply", videoOptional.get());
            responseData.put("sources", videoService.getLatestSources(key, 5));

            return Result.success(responseData);
        } else {
            return Result.error();
        }
    }

}
*/
