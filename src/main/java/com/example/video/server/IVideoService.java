package com.example.video.server;

import com.example.video.entity.Video;
import com.example.video.entity.VideoData;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;

public interface IVideoService {
    void saveVideoDataToFile (String key) throws IOException;

    VideoData readVideoDataFromFile ();

    void loadVideoDataFromFile (String key) throws IOException, ClassNotFoundException;

    void saveVideoData (String key, VideoData videoData);

    VideoData getVideoData (String key);

    List<Video> getLatestSources(String key, int count);

    void sendNotificationToEmails(String filePath, int num) throws IOException, MessagingException;

    String getStandbySource () throws IOException;

    void updateApplySource (String standbySource);
}
