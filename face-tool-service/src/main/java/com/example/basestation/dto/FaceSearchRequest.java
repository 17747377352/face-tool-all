package com.example.basestation.dto;

import lombok.Data;

/**
 * 人脸搜索请求DTO
 */
@Data
public class FaceSearchRequest {
    /**
     * 图片Base64编码
     */
    private String imageBase64;
    
    /**
     * 图片URL（可选）
     */
    private String imageUrl;
    
    /**
     * 返回TopK个结果（默认1）
     */
    private Integer topK = 1;
    
    /**
     * 相似度阈值（可选）
     */
    private Float threshold;
}

