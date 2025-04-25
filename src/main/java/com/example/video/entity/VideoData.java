package com.example.video.entity;

/**
 * 功能描述
 *
 * @author: jxx
 * @date: 2024年06月06日 14:48
 */
import lombok.Data;

import java.io.Serializable;
import java.util.List;
@Data
public class VideoData implements Serializable {
    private Video apply;
    private List<Video> sources;

}
