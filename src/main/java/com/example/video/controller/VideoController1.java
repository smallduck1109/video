package com.example.video.controller;

import com.alibaba.fastjson.JSON;
import com.example.video.base.RestResponse;
import com.example.video.entity.*;
import com.example.video.server.IVideoService;
import com.example.video.utils.FileUtil;
import com.example.video.utils.Result;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.mail.MessagingException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * 功能描述
 *
 * @author: jxx
 * @date: 2024年06月06日 11:53
 */
@RestController
@RequestMapping("/video")
@Api(tags = "视频接口")
public class VideoController1 {

    @Autowired
    private IVideoService videoService;
    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/getData/{count}")
    @ApiOperation("获取视频数据")
    @CrossOrigin
    public Result<VideoData> getData (@PathVariable int count) {
        VideoData videoData = videoService.getVideoData(Constants.UNIQUE_KEY);
        if (videoData != null) {
            videoData.setSources(videoService.getLatestSources(Constants.UNIQUE_KEY, count));
        }
        return Result.success(videoData);
    }

    @PostMapping("/addSource")
    @ApiOperation("添加视频源")
    @CrossOrigin
    public Result addSource (@RequestBody AddUrl addUrl) {
        VideoData videoData = videoService.getVideoData(Constants.UNIQUE_KEY);
        List<Video> sources = videoData.getSources();

        int nextId = sources.isEmpty() ? 1 : sources.stream().mapToInt(Video::getId).max().getAsInt() + 1;

        Video newVideo = new Video();
        newVideo.setId(nextId);
        newVideo.setUrl(addUrl.getUrl());

        sources.add(newVideo);

        videoService.saveVideoData(Constants.UNIQUE_KEY, videoData);
        try {
            FileUtil.saveVideoDataToFile(videoData);
        } catch (IOException e) {
            return Result.error("保存视频数据失败: " + e.getMessage());
        }

        return Result.success("添加成功");
    }

    @DeleteMapping("/deleteSource/{id}")
    @CrossOrigin
    @ApiOperation("删除视频源")
    public Result deleteSource (@PathVariable int id) {
        VideoData videoData = videoService.getVideoData(Constants.UNIQUE_KEY);
        List<Video> sources = videoData.getSources();

        boolean removed = sources.removeIf(video -> video.getId() == id);

        if (removed) {
            videoService.saveVideoData(Constants.UNIQUE_KEY, videoData);
            try {
                FileUtil.saveVideoDataToFile(videoData);
            } catch (IOException e) {
                return Result.error("删除视频源后保存数据失败: " + e.getMessage());
            }
            return Result.success("删除成功");
        }

        return Result.error("没有找到id为" + id + "的视频源, 删除失败");
    }

/*    @PostMapping("/data")
    @ApiOperation("保存视频数据")
    @CrossOrigin
    public ResponseEntity<String> saveData(@RequestBody VideoData videoData) {
        String key = Constants.UNIQUE_KEY;
        videoService.saveVideoData(key, videoData);
        try {
            FileUtil.saveVideoDataToFile(videoData);
        } catch (IOException e) {
            return new ResponseEntity<>("保存视频数据到文件失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("视频数据已成功保存到Redis", HttpStatus.OK);
    }*/

    @PostMapping("/switchSource")
    @ApiOperation("切换视频源")
    @CrossOrigin
    public Result<VideoData> switchSource (@RequestParam int id) {
        String key = Constants.UNIQUE_KEY;
        VideoData videoData = videoService.getVideoData(key);
        List<Video> sources = videoData.getSources();

        Optional<Video> videoOptional = sources.stream()
                .filter(video -> video.getId() == id)
                .findFirst();

        if (videoOptional.isPresent()) {
            videoData.setApply(videoOptional.get());
            videoService.saveVideoData(key, videoData);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("apply", videoOptional.get());
            responseData.put("sources", videoService.getLatestSources(key, 5));

            try {
                FileUtil.saveVideoDataToFile(videoData);
            } catch (IOException e) {
                return Result.error("切换视频源后保存数据失败: " + e.getMessage());
            }

            return Result.success(responseData);
        } else {
            return Result.error("没有找到id为" + id + "的视频源, 切换失败");
        }
    }

    @Bean
    public ApplicationRunner initializer (IVideoService videoService) {
        return args -> {
            VideoData videoData = videoService.readVideoDataFromFile();
            if (videoData == null) {
                // 创建 apply 视频源
                Video applyVideo = new Video();
                applyVideo.setId(1);
                applyVideo.setUrl("http://116.162.6.192/4403-txt.otvstream.otvcloud.com/otv/skcc/live/channel39/2300.m3u8");

                // 创建其他视频源列表
                List<Video> sources = new ArrayList<>();
                Video sourceVideo1 = new Video();
                sourceVideo1.setId(1);
                sourceVideo1.setUrl("http://116.162.6.192/4403-txt.otvstream.otvcloud.com/otv/skcc/live/channel39/2300.m3u8");

                Video sourceVideo2 = new Video();
                sourceVideo2.setId(2);
                sourceVideo2.setUrl("https://node1.olelive.com:6443/live/CCTV14HD/hls.m3u8");

                sources.add(sourceVideo1);
                sources.add(sourceVideo2);

                // 创建 VideoData 对象并保存它
                videoData = new VideoData();
                videoData.setApply(applyVideo);
                videoData.setSources(sources);
            }
            videoService.saveVideoData(Constants.UNIQUE_KEY, videoData);
            try {
                FileUtil.saveVideoDataToFile(videoData);
            } catch (IOException e) {
                System.out.println("初始化视频数据文件保存失败: " + e.getMessage());
            }
            System.out.println("数据初始化完成!");
        };
    }

    @PostMapping("/saveEmail")
    @ApiOperation("添加邮箱")
    @CrossOrigin(origins = "*")
    public Result<String> saveEmail (@RequestBody EmailInfo emailInfo) {
        String filePath = "email.txt";  // 可以使用相对路径或者绝对路径
        File file = new File(filePath);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            if (!file.exists()) {
                file.createNewFile();
            }

            writer.write("Name: " + emailInfo.getName() + ", Email: " + emailInfo.getEmail());
            writer.newLine();
            writer.flush();

            return Result.success("Email saved successfully");
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("Failed to save email");
        }
    }

    @PostMapping("/saveNotification")
    @ApiOperation("通知设置")
    @CrossOrigin(origins = "*")
    public Result<String> saveNotification (@RequestBody NotificationInfo notificationInfo) {
        String filePath = "notification.txt";  // 可以使用相对路径或者绝对路径
        File file = new File(filePath);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            if (!file.exists()) {
                file.createNewFile();
            }

            // 将原有内容删除并写入新的内容
            writer.write("Time: " + notificationInfo.getTime() + ", Sum: " + notificationInfo.getSum() + ", StandbySource: " + notificationInfo.getStandbySource()
                    + ", Flag: " + notificationInfo.getFlag() + ", HuanTime: " + notificationInfo.getHuanTime());
            writer.newLine();
            writer.flush();

            return Result.success("Notification saved successfully");
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("Failed to save notification");
        }
    }

    private static final String filePath1 = "notification.txt";

    @GetMapping("/getNotification")
    @ApiOperation("获取通知设置")
    @CrossOrigin(origins = "*")
    public Result<NotificationInfo> getNotification () {
        try {
            // 读取文件第一行内容
            String content = new String(Files.readAllBytes(Paths.get(filePath1))).trim();

            // 假设只有一个通知设置，直接解析第一行
            String[] parts = content.split(", ");
            NotificationInfo notificationInfo = new NotificationInfo();

            for (String part : parts) {
                if (part.startsWith("Time: ")) {
                    notificationInfo.setTime(Integer.parseInt(part.substring(6)));
                } else if (part.startsWith("Sum: ")) {
                    notificationInfo.setSum(Integer.parseInt(part.substring(5)));
                } else if (part.startsWith("StandbySource: ")) {
                    notificationInfo.setStandbySource(part.substring(15));
                } else if (part.startsWith("Flag: ")) {
                    notificationInfo.setFlag(Integer.parseInt(part.substring(6)));
                } else if (part.startsWith("HuanTime: ")) {
                    notificationInfo.setHuanTime(Integer.parseInt(part.substring(10)));
                }

            }

            return Result.success(notificationInfo);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("Failed to read notification");
        }
    }

    @PostMapping("/sendNotification")
    @ApiOperation("发送邮箱")
    @CrossOrigin(origins = "*")
    public Result<String> sendNotification (int num) {
        String emailFilePath = "email.txt";
        String videoDataKey = Constants.UNIQUE_KEY;

        try {
/*            //先不需要切换备用源只发送邮箱
            // Step 1: Read StandbySource from notification.txt
            String standbySource = videoService.getStandbySource();
            if (standbySource == null) {
                return Result.error("Failed to read StandbySource from notification.txt");
            }

            // Step 2: Get the video data
            VideoData videoData = videoService.getVideoData(videoDataKey);
            if (videoData == null) {
                return Result.error("Failed to get video data");
            }

            // Step 3: Compare and update if necessary
            Video videoSource = videoData.getSources().stream()
                    .filter(video -> video.getId() == 1)
                    .findFirst()
                    .orElse(null);

            if (videoSource != null && !standbySource.equals(videoSource.getUrl())) {
                videoSource.setUrl(standbySource);
                videoService.saveVideoData(videoDataKey, videoData);
                FileUtil.saveVideoDataToFile(videoData);
            }

            // Step 4: 切换当前使用
            videoService.updateApplySource(standbySource);*/

            // Step 5: Send notifications
            videoService.sendNotificationToEmails(emailFilePath, num);
            return Result.success("Notification emails sent successfully");

        } catch (IOException e) {
            return Result.error("Failed to read email file: " + e.getMessage());
        } catch (MessagingException e) {
            return Result.error("Failed to send emails: " + e.getMessage());
        }
    }

    // 外部接口地址
    private static final String EXTERNAL_URL = "http://www.fjqx121.com:8886/metepro/webservice_data/ws-ServiceRequest/requestQueryEncryption";

    // 构建请求参数
    //private static final String requestData = "eyJzZXJ2aWNlQ29kZSI6IjllZjRiOTVkNTk2MTQwMzU5N2FkMWY0MTI2NGIwNzY3IiwidG9rZW4iOiJKYktVWi1mVmRmUnUxOFNQbHppS2dNOTVRQjl0ajlwNWs1c2lkMnpjdzlYYzhTTXNNem14am9OSmlId2o0Y1YtVTluUi1XUDlXWlRoZ19xQXY3VnlZZnpfd09CaU9hcV9qYk9nYkZidlViTSIsIm5vcm1hbENvbHVtbiI6e30sInRpbWVDb2x1bW4iOnsiYmVnaW4iOiIyMDI0LTEyLTE3IDAwOjAwOjAwIiwiZW5kIjoiMjAyNC0xMi0xNyAyMzo1OTo1OSJ9LCJudW0iOjk5OX0=";

    @PostMapping("/test1")
    @ApiOperation("预警数据获取")
    @CrossOrigin(origins = "*")
    public Result<Map<String, Object>> test1 (@RequestBody Test1 test1) {
        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 设置请求体
        String body = "requestData=" + test1.getRequestData();
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        try {
            // 发起 POST 请求
            ResponseEntity<Map> response = restTemplate.exchange(
                    EXTERNAL_URL,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            // 获取返回数据
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null) {
                // 封装返回结果
                //System.out.println(responseBody);
                return Result.success(responseBody);
            } else {
                return Result.error("外部接口返回空数据");
            }
        } catch (Exception e) {
            // 异常处理
            return Result.error("调用外部接口失败: " + e.getMessage());
        }
    }

    private static final String EXTERNAL_URL2 = "http://www.fjqxfw.cn:8096/ztq30_fj_jc/service.do";

    @PostMapping("/test2")
    @ApiOperation("天气数据获取")
    @CrossOrigin(origins = "*")
    public Result<Map<String, Object>> test2 (@RequestBody Map<String, Object> requestData) {
        try {
            // 1. 参数转换: Map -> JSON字符串
            ObjectMapper objectMapper = new ObjectMapper();
            String p = objectMapper.writeValueAsString(requestData); // 转换为 JSON 字符串

            // 2. 构建请求头和请求体
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setContentType(MediaType.valueOf("application/x-www-form-urlencoded;charset=UTF-8"));  // 设置编码为 UTF-8
            String body = "p=" + p;

            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            // 3. 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                    EXTERNAL_URL2,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // 4. 返回响应数据
            String responseBody = response.getBody();
            //System.out.println("外部接口响应: " + responseBody);
            // 处理编码问题，确保UTF-8
            String result = new String(responseBody.getBytes("ISO-8859-1"), "UTF-8");
            // 返回结果
            return Result.success(JSON.parse(result));

        } catch (Exception e) {
            return Result.error("调用失败: " + e.getMessage());
        }
    }

    private static final String EXTERNAL_URL3 = "http://www.fjqx121.com:8886/metepro/webservice_data/ws-ServiceRequest/requestToken";

    @PostMapping("/test3")
    @ApiOperation("token获取")
    @CrossOrigin(origins = "*")
    public Result<Map<String, Object>> test3 (@RequestBody Test1 test1) {
        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 设置请求体
        String body = "requestData=" + test1.getRequestData();
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        try {
            // 发起 POST 请求
            ResponseEntity<Map> response = restTemplate.exchange(
                    EXTERNAL_URL3,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            // 获取返回数据
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null) {
                // 封装返回结果
                //System.out.println(responseBody);
                return Result.success(responseBody);
            } else {
                return Result.error("外部接口返回空数据");
            }
        } catch (Exception e) {
            // 异常处理
            return Result.error("调用外部接口失败: " + e.getMessage());
        }
    }

    private static final String EXTERNAL_URL4 = "http://www.fjqx121.com:8886/metepro/webservice_file/ws-ServiceRequest/requestQueryFile";

    @PostMapping("/test4")
    @ApiOperation("解析预警")
    @CrossOrigin(origins = "*")
    public ResponseEntity<byte[]> test4(@RequestBody Test1 test1) {
        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 设置请求体
        String body = "requestData=" + test1.getRequestData();
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        try {
            // 发起 POST 请求，直接获取字节流数据
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    EXTERNAL_URL4,
                    HttpMethod.POST,
                    entity,
                    byte[].class // 返回值直接作为字节数组处理
            );

            // 获取返回的字节数据
            byte[] responseBody = response.getBody();
            if (responseBody != null && responseBody.length > 0) {
                // 设置响应头，指定返回类型为 application/octet-stream
                HttpHeaders responseHeaders = new HttpHeaders();
                responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                responseHeaders.setContentLength(responseBody.length);

                // 返回字节流
                return new ResponseEntity<>(responseBody, responseHeaders, HttpStatus.OK);
            } else {
                // 外部接口返回空数据
                String errorMessage = "外部接口返回空数据";
                byte[] errorBytes = errorMessage.getBytes(StandardCharsets.UTF_8);

                HttpHeaders responseHeaders = new HttpHeaders();
                responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                responseHeaders.setContentLength(errorBytes.length);

                return new ResponseEntity<>(errorBytes, responseHeaders, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            // 异常处理，返回错误信息
            String errorMessage = "调用外部接口失败: " + e.getMessage();
            byte[] errorBytes = errorMessage.getBytes(StandardCharsets.UTF_8);

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            responseHeaders.setContentLength(errorBytes.length);

            return new ResponseEntity<>(errorBytes, responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
