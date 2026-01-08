package com.example.basestation.dto;

import lombok.Data;

/**
 * 人脸列表请求DTO
 */
@Data
public class FaceListRequest {
    /**
     * 页码（默认1）
     */
    private Integer page = 1;
    
    /**
     * 每页大小（默认10）
     */
    private Integer size = 10;
}

