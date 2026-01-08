package com.example.basestation.dto;

import lombok.Data;

/**
 * 人脸比对请求DTO
 */
@Data
public class FaceCompareRequest {
    /**
     * 第一张图片Base64编码
     */
    private String image1Base64;
    
    /**
     * 第一张图片URL（可选）
     */
    private String image1Url;
    
    /**
     * 第二张图片Base64编码
     */
    private String image2Base64;
    
    /**
     * 第二张图片URL（可选）
     */
    private String image2Url;
    
    /**
     * 相似度阈值（默认0.62）
     */
    private Float threshold = 0.62f;
}

