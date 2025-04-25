package com.example.video.utils;

/**
 * 功能描述
 *
 * @author: jxx
 * @date: 2024年06月10日 16:33
 */
import com.example.video.entity.VideoData;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtil {

    private static final String VIDEO_SOURCE_FILE = "videoData.ser";

    public static void saveVideoDataToFile(VideoData videoData) throws IOException {
        Path path = Paths.get(VIDEO_SOURCE_FILE);
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(path))) {
            out.writeObject(videoData);
        }
    }

    public static VideoData readVideoDataFromFile() throws IOException, ClassNotFoundException {
        Path path = Paths.get(VIDEO_SOURCE_FILE);
        if (Files.exists(path)) {
            try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(path))) {
                return (VideoData) in.readObject();
            }
        }
        return null;
    }
}
