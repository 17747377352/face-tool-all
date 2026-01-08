package com.example.basestation.service;

import com.example.basestation.entity.TokenInfo;
import com.example.basestation.dto.TokenApplyRequest;

import java.util.List;

/**
 * Token管理服务接口
 */
public interface TokenService {
    
    /**
     * 申请Token（直接签发，无需审核）
     * @param request 申请请求
     * @return Token值
     */
    String applyToken(TokenApplyRequest request);
    
    /**
     * 验证Token
     * @param token Token值
     * @return 是否有效
     */
    boolean validateToken(String token);
    
    /**
     * 获取Token信息
     * @param token Token值
     * @return Token信息
     */
    TokenInfo getTokenInfo(String token);
    
    /**
     * 撤销Token
     * @param token Token值
     * @return 是否成功
     */
    boolean revokeToken(String token);
    
    
    /**
     * 获取所有已签发的Token列表
     * @return Token列表
     */
    List<TokenInfo> getAllTokens();
    
    /**
     * 刷新Token最后使用时间
     * @param token Token值
     */
    void refreshTokenUsage(String token);
}

