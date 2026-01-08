package com.example.basestation.dto;

import lombok.Data;

/**
 * 人脸注册请求DTO
 */
@Data
public class FaceRegisterRequest {
    /**
     * 图片Base64编码
     */
    private String imageBase64;
    
    /**
     * 图片URL（可选）
     */
    private String imageUrl;
    
    /**
     * 自定义ID（可选，不设置则自动生成）
     */
    private String faceId;
    
    /**
     * 用户ID（关联到系统用户）
     */
    private String userId;
    
    /**
     * 用户名称
     */
    private String userName;
    
    /**
     * 其他元数据（JSON格式）
     */
    private String metadata;
}

