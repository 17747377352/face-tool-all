package com.example.basestation.service.impl;

import com.example.basestation.config.BusinessException;
import com.example.basestation.dto.TokenApplyRequest;
import com.example.basestation.entity.TokenInfo;
import com.example.basestation.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token管理服务实现类
 * 使用内存存储，生产环境建议使用数据库
 */
@Slf4j
@Service
public class TokenServiceImpl implements TokenService {
    
    // Token存储（token -> TokenInfo）
    private final Map<String, TokenInfo> tokenStore = new ConcurrentHashMap<>();
    
    
    // Token生成密钥（生产环境应从配置文件读取）
    private static final String SECRET_KEY = "face-tool-secret-key-2024";
    
    @Override
    public String applyToken(TokenApplyRequest request) {
        // 验证参数
        if (!StringUtils.hasText(request.getClientName())) {
            throw new BusinessException("申请方名称不能为空");
        }
        if (!StringUtils.hasText(request.getContact())) {
            throw new BusinessException("联系方式不能为空");
        }
        if (!StringUtils.hasText(request.getPurpose())) {
            throw new BusinessException("申请原因不能为空");
        }
        
        // 直接生成并签发Token，不需要审核
        String token = generateToken();
        
        // 计算过期时间
        Integer validDays = request.getValidDays() != null ? request.getValidDays() : 30;
        LocalDateTime expireTime = LocalDateTime.now().plusDays(validDays);
        
        // 创建Token信息
        TokenInfo tokenInfo = new TokenInfo();
        tokenInfo.setToken(token);
        tokenInfo.setClientName(request.getClientName());
        tokenInfo.setContact(request.getContact());
        tokenInfo.setPurpose(request.getPurpose());
        tokenInfo.setStatus("ACTIVE");
        tokenInfo.setCreateTime(LocalDateTime.now());
        tokenInfo.setExpireTime(expireTime);
        tokenInfo.setLastUsedTime(LocalDateTime.now());
        tokenInfo.setUseCount(0L);
        
        // 保存Token
        tokenStore.put(token, tokenInfo);
        
        log.info("Token申请并自动签发成功: token={}, clientName={}, expireTime={}", 
            token, request.getClientName(), expireTime);
        
        return token;
    }
    
    @Override
    public boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }
        
        TokenInfo tokenInfo = tokenStore.get(token);
        if (tokenInfo == null) {
            log.warn("Token不存在: {}", token);
            return false;
        }
        
        // 检查状态
        if (!"ACTIVE".equals(tokenInfo.getStatus())) {
            log.warn("Token状态无效: token={}, status={}", token, tokenInfo.getStatus());
            return false;
        }
        
        // 检查是否过期
        if (tokenInfo.getExpireTime() != null && LocalDateTime.now().isAfter(tokenInfo.getExpireTime())) {
            log.warn("Token已过期: token={}, expireTime={}", token, tokenInfo.getExpireTime());
            tokenInfo.setStatus("EXPIRED");
            return false;
        }
        
        return true;
    }
    
    @Override
    public TokenInfo getTokenInfo(String token) {
        return tokenStore.get(token);
    }
    
    @Override
    public boolean revokeToken(String token) {
        TokenInfo tokenInfo = tokenStore.get(token);
        if (tokenInfo == null) {
            return false;
        }
        
        tokenInfo.setStatus("REVOKED");
        log.info("Token已撤销: token={}, clientName={}", token, tokenInfo.getClientName());
        return true;
    }
    
    @Override
    public List<TokenInfo> getAllTokens() {
        List<TokenInfo> tokens = new ArrayList<>(tokenStore.values());
        // 按创建时间倒序
        tokens.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
        return tokens;
    }
    
    @Override
    public void refreshTokenUsage(String token) {
        TokenInfo tokenInfo = tokenStore.get(token);
        if (tokenInfo != null) {
            tokenInfo.setLastUsedTime(LocalDateTime.now());
            tokenInfo.setUseCount(tokenInfo.getUseCount() + 1);
        }
    }
    
    /**
     * 生成Token
     * 使用UUID + 时间戳 + 简单哈希
     */
    private String generateToken() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String timestamp = String.valueOf(System.currentTimeMillis());
        String raw = uuid + timestamp + SECRET_KEY;
        
        // 简单哈希
        int hash = raw.hashCode();
        String hashStr = Integer.toHexString(Math.abs(hash));
        
        return "FT-" + uuid.substring(0, 8) + "-" + timestamp.substring(timestamp.length() - 8) + "-" + hashStr.substring(0, 8);
    }
}

