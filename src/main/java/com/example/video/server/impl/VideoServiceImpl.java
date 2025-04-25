package com.example.video.server.impl;

import com.example.video.entity.Constants;
import com.example.video.entity.Video;
import com.example.video.entity.VideoData;
import com.example.video.server.IVideoService;
import com.example.video.utils.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VideoServiceImpl implements IVideoService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void saveVideoDataToFile(String key) throws IOException {
        VideoData videoData = this.getVideoData(key);
        FileUtil.saveVideoDataToFile(videoData);
    }

    @Override
    public VideoData readVideoDataFromFile() {
        try {
            return FileUtil.readVideoDataFromFile();
        } catch (IOException | ClassNotFoundException e) {
            // handle exception by logging or something else
            System.out.println("读取视频数据文件失败: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void loadVideoDataFromFile(String key) throws IOException, ClassNotFoundException {
        VideoData videoData = FileUtil.readVideoDataFromFile();
        if (videoData != null) {
            this.saveVideoData(key, videoData);
        }
    }

    @Override
    public void saveVideoData(String key, VideoData videoData) {
        redisTemplate.opsForValue().set(key, videoData);
    }

    @Override
    public VideoData getVideoData(String key) {
        return (VideoData) redisTemplate.opsForValue().get(key);
    }

    @Override
    public List<Video> getLatestSources(String key, int count) {
        VideoData videoData = getVideoData(key);
        return videoData.getSources()
                .stream()
                .sorted((a, b) -> Integer.compare(b.getId(), a.getId()))
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public void sendNotificationToEmails(String filePath, int num) throws IOException, MessagingException {
        List<String> emails = readEmailsFromFile(filePath);
        if (num == 1) {
            for (String email : emails) {
                sendEmail(email, "视频源挂掉告警", "视频源已挂，请及时更新");
            }
        } else if (num == 2){
            for (String email : emails) {
                sendEmail(email, "视频源缓冲告警", "视频源进入缓冲");
            }
        } else {
            for (String email : emails) {
                sendEmail(email, "视频源告警", "可能挂掉也可能进入缓冲");
            }
        }

    }


    public List<String> readEmailsFromFile(String filePath) throws IOException {
        List<String> emailList = new ArrayList<>();
        Files.lines(Paths.get(filePath)).forEach(line -> {
            if (line.contains("Email:")) {
                String email = line.substring(line.indexOf("Email:") + 6).trim();
                emailList.add(email);
            }
        });
        return emailList;
    }

    public void sendEmail(String to, String subject, String text) throws MessagingException{
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom("1985907993@qq.com");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text);
        mailSender.send(message);
    }

    @Override
    public String getStandbySource () throws IOException {
        String filePath = "notification.txt";
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        for (String line : lines) {
            if (line.contains("StandbySource:")) {
                return line.substring(line.indexOf("StandbySource:") + 14).trim();
            }
        }
        return null;
    }

    @Override
    public void updateApplySource(String standbySource) {
        String key = Constants.UNIQUE_KEY;
        VideoData videoData = getVideoData(key);
        if (videoData != null) {
            Video applyVideo = videoData.getApply();
            if (applyVideo != null && !standbySource.equals(applyVideo.getUrl())) {
                applyVideo.setUrl(standbySource);
                applyVideo.setId(1);
                saveVideoData(key, videoData);
                try {
                    FileUtil.saveVideoDataToFile(videoData);
                } catch (IOException e) {
                    System.out.println("更新apply视频源后保存数据失败: " + e.getMessage());
                }
            }
        }
    }

}
