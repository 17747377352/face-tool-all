package com.example.basestation.dto;

import lombok.Data;

/**
 * Token签发请求DTO（管理员使用）
 */
@Data
public class TokenIssueRequest {
    /**
     * 申请ID（从申请列表中获取）
     */
    private String applyId;
    
    /**
     * 是否批准（true-批准，false-拒绝）
     */
    private Boolean approved;
    
    /**
     * 有效期（天数，可选，默认30天）
     */
    private Integer validDays;
    
    /**
     * 备注
     */
    private String remark;
}

