package com.example.basestation.controller;

import com.example.basestation.config.Result;
import com.example.basestation.dto.TokenApplyRequest;
import com.example.basestation.entity.TokenInfo;
import com.example.basestation.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Token管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/token")
public class TokenController {
    
    @Autowired
    private TokenService tokenService;
    
    /**
     * 申请Token（直接签发，无需审核）
     * POST /api/token/apply
     */
    @PostMapping("/apply")
    public Result<TokenInfo> applyToken(@RequestBody TokenApplyRequest request) {
        try {
            log.info("收到Token申请: clientName={}, contact={}", request.getClientName(), request.getContact());
            String token = tokenService.applyToken(request);
            TokenInfo tokenInfo = tokenService.getTokenInfo(token);
            return Result.success(tokenInfo);
        } catch (Exception e) {
            log.error("Token申请失败", e);
            return Result.error("Token申请失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有Token列表（管理员）
     * POST /api/token/list
     */
    @PostMapping("/list")
    public Result<List<TokenInfo>> getAllTokens() {
        try {
            List<TokenInfo> tokens = tokenService.getAllTokens();
            return Result.success(tokens);
        } catch (Exception e) {
            log.error("获取Token列表失败", e);
            return Result.error("获取Token列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 撤销Token（管理员）
     * POST /api/token/revoke
     */
    @PostMapping("/revoke")
    public Result<Map<String, Object>> revokeToken(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            if (token == null || token.isEmpty()) {
                return Result.error("Token不能为空");
            }
            
            boolean success = tokenService.revokeToken(token);
            if (success) {
                return Result.success(Map.of("message", "Token已撤销"));
            } else {
                return Result.error("Token不存在");
            }
        } catch (Exception e) {
            log.error("撤销Token失败", e);
            return Result.error("撤销Token失败: " + e.getMessage());
        }
    }
}

