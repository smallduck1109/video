package com.example.video;

import com.example.video.entity.Constants;
import com.example.video.entity.Video;
import com.example.video.entity.VideoData;
import com.example.video.server.IVideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class VideoApplication {

    public static void main (String[] args) {
        SpringApplication.run(VideoApplication.class, args);
    }

/*    @Bean
    public ApplicationRunner initializer(IVideoService videoService) {
        System.out.println(Constants.UNIQUE_KEY);
        return args -> {

            // 创建 apply 视频源
            Video applyVideo = new Video();
            applyVideo.setId(1);
            applyVideo.setUrl("http://116.162.6.192/4403-txt.otvstream.otvcloud.com/otv/skcc/live/channel39/2300.m3u8");

            // 创建其他视频源列表
            List<Video> sources = new ArrayList<>();
            for (int i = 1; i <= 2; i++) {
                Video sourceVideo = new Video();
                sourceVideo.setId(i);
                if (i == 1) {
                    sourceVideo.setUrl("http://116.162.6.192/4403-txt.otvstream.otvcloud.com/otv/skcc/live/channel39/2300.m3u8");
                } else if (i == 2) {
                    sourceVideo.setUrl("https://node1.olelive.com:6443/live/CCTV14HD/hls.m3u8");
                }
                sources.add(sourceVideo);
            }

            // 创建 VideoData 对象并保存它
            VideoData videoData = new VideoData();
            videoData.setApply(applyVideo);
            videoData.setSources(sources);
            videoService.saveVideoData(Constants.UNIQUE_KEY, videoData);
            System.out.println("Data initialization complete!");
        };
    }*/





}
