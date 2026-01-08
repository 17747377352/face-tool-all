package com.example.basestation.dto;

import lombok.Data;

/**
 * 人脸检测请求DTO
 */
@Data
public class FaceDetectRequest {
    /**
     * 图片Base64编码
     */
    private String imageBase64;
    
    /**
     * 图片URL（可选，与imageBase64二选一）
     */
    private String imageUrl;
    
    /**
     * 是否绘制人脸框（默认false）
     */
    private Boolean drawBox = false;
    
    /**
     * 置信度阈值（0-1，默认0.5）
     */
    private Float confidenceThreshold;
}

