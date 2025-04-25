package com.example.video.entity;

import lombok.Data;

/**
 * 功能描述
 *
 * @author: jxx
 * @date: 2024年07月15日 15:29
 */
@Data
public class NotificationInfo {
    private int time;
    private int sum;
    private String standbySource;
    // 缓冲是否通知
    private int flag;
    // 缓冲通知时间
    private int huanTime;
}
