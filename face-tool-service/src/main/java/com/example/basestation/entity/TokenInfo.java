package com.example.basestation.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Token信息实体
 */
@Data
public class TokenInfo {
    /**
     * Token值
     */
    private String token;
    
    /**
     * 申请方名称
     */
    private String clientName;
    
    /**
     * 申请方联系方式
     */
    private String contact;
    
    /**
     * 申请原因/用途
     */
    private String purpose;
    
    /**
     * Token状态：ACTIVE-激活, REVOKED-已撤销, EXPIRED-已过期
     */
    private String status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 过期时间
     */
    private LocalDateTime expireTime;
    
    /**
     * 最后使用时间
     */
    private LocalDateTime lastUsedTime;
    
    /**
     * 使用次数
     */
    private Long useCount;
}

