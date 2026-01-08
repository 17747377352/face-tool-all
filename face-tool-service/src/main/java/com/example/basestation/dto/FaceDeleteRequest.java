package com.example.basestation.dto;

import lombok.Data;

/**
 * 删除人脸请求DTO
 */
@Data
public class FaceDeleteRequest {
    /**
     * 人脸ID
     */
    private String faceId;
}

