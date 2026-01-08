package com.example.basestation.dto;

import lombok.Data;

/**
 * Token申请请求DTO
 */
@Data
public class TokenApplyRequest {
    /**
     * 申请方名称（必填）
     */
    private String clientName;
    
    /**
     * 联系方式（必填，邮箱或手机号）
     */
    private String contact;
    
    /**
     * 申请原因/用途（必填）
     */
    private String purpose;
    
    /**
     * 有效期（天数，可选，默认30天）
     */
    private Integer validDays;
}

