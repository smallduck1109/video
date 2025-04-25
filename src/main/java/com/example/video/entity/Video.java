package com.example.video.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 功能描述
 *
 * @author: jxx
 * @date: 2024年06月06日 14:49
 */
@Data
public class Video implements Serializable {
    private int id;
    private String url;

    public Video () {
    }

    public Video (int i, String s) {
    }
}
