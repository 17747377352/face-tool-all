package com.example.basestation.controller;

import com.example.basestation.config.Result;
import com.example.basestation.dto.FaceCompareRequest;
import com.example.basestation.dto.FaceDetectRequest;
import com.example.basestation.dto.FaceRegisterRequest;
import com.example.basestation.dto.FaceSearchRequest;
import com.example.basestation.service.FaceRecognitionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 人脸识别控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/face")
// 注意：CORS 配置已在 SecurityConfig 中全局配置，这里不需要单独配置
// 如果需要在 Controller 级别配置，应使用 originPatterns 而不是 origins
// @CrossOrigin(originPatterns = "*", maxAge = 3600, allowCredentials = "true")
public class FaceRecognitionController {

    @Autowired
    private FaceRecognitionService faceRecognitionService;

    /**
     * 人脸检测
     * POST /api/face/detect
     */
    @PostMapping("/detect")
    public Result<Map<String, Object>> detectFace(@RequestBody FaceDetectRequest request) {
        try {
            log.info("收到人脸检测请求");
            Map<String, Object> result = faceRecognitionService.detectFace(request);
            return Result.success(result);
        } catch (Exception e) {
            log.error("人脸检测失败", e);
            return Result.error("人脸检测失败: " + e.getMessage());
        }
    }

    /**
     * 人脸特征提取
     * POST /api/face/extract
     */
    @PostMapping("/extract")
    public Result<Map<String, Object>> extractFeature(@RequestBody Map<String, String> request) {
        try {
            String imageBase64 = request.get("imageBase64");
            if (imageBase64 == null || imageBase64.isEmpty()) {
                return Result.error("图片Base64编码不能为空");
            }
            
            log.info("收到人脸特征提取请求");
            Map<String, Object> result = faceRecognitionService.extractFeature(imageBase64);
            return Result.success(result);
        } catch (Exception e) {
            log.error("人脸特征提取失败", e);
            return Result.error("人脸特征提取失败: " + e.getMessage());
        }
    }

    /**
     * 人脸比对（1:1）
     * POST /api/face/compare
     */
    @PostMapping("/compare")
    public Result<Map<String, Object>> compareFace(@RequestBody FaceCompareRequest request) {
        try {
            log.info("收到人脸比对请求");
            Map<String, Object> result = faceRecognitionService.compareFace(request);
            return Result.success(result);
        } catch (Exception e) {
            log.error("人脸比对失败", e);
            return Result.error("人脸比对失败: " + e.getMessage());
        }
    }

    /**
     * 人脸注册
     * POST /api/face/register
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> registerFace(@RequestBody FaceRegisterRequest request) {
        try {
            log.info("收到人脸注册请求: userId={}", request.getUserId());
            Map<String, Object> result = faceRecognitionService.registerFace(request);
            return Result.success(result);
        } catch (Exception e) {
            log.error("人脸注册失败", e);
            return Result.error("人脸注册失败: " + e.getMessage());
        }
    }

    /**
     * 人脸搜索（1:N）
     * POST /api/face/search
     */
    @PostMapping("/search")
    public Result<Map<String, Object>> searchFace(@RequestBody FaceSearchRequest request) {
        try {
            log.info("收到人脸搜索请求");
            Map<String, Object> result = faceRecognitionService.searchFace(request);
            return Result.success(result);
        } catch (Exception e) {
            log.error("人脸搜索失败", e);
            return Result.error("人脸搜索失败: " + e.getMessage());
        }
    }

    /**
     * 删除已注册的人脸
     * POST /api/face/delete
     */
    @PostMapping("/delete")
    public Result<Map<String, Object>> deleteFace(@RequestBody com.example.basestation.dto.FaceDeleteRequest request) {
        try {
            if (request.getFaceId() == null || request.getFaceId().isEmpty()) {
                return Result.error("人脸ID不能为空");
            }
            log.info("收到人脸删除请求: faceId={}", request.getFaceId());
            Map<String, Object> result = faceRecognitionService.deleteFace(request.getFaceId());
            return Result.success(result);
        } catch (Exception e) {
            log.error("人脸删除失败", e);
            return Result.error("人脸删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取已注册人脸列表
     * POST /api/face/list
     */
    @PostMapping("/list")
    public Result<Map<String, Object>> listFaces(@RequestBody com.example.basestation.dto.FaceListRequest request) {
        try {
            int page = request.getPage() != null ? request.getPage() : 1;
            int size = request.getSize() != null ? request.getSize() : 10;
            log.info("收到人脸列表请求: page={}, size={}", page, size);
            Map<String, Object> result = faceRecognitionService.listFaces(page, size);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取人脸列表失败", e);
            return Result.error("获取人脸列表失败: " + e.getMessage());
        }
    }
}

