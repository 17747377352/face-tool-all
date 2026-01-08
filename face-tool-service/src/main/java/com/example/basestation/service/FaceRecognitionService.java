package com.example.basestation.service;

import com.example.basestation.dto.FaceCompareRequest;
import com.example.basestation.dto.FaceDetectRequest;
import com.example.basestation.dto.FaceRegisterRequest;
import com.example.basestation.dto.FaceSearchRequest;

import java.util.Map;

/**
 * 人脸识别服务接口
 */
public interface FaceRecognitionService {
    
    /**
     * 人脸检测
     * @param request 检测请求（包含图片）
     * @return 检测结果
     */
    Map<String, Object> detectFace(FaceDetectRequest request);
    
    /**
     * 人脸特征提取
     * @param imageBase64 图片Base64编码
     * @return 特征向量
     */
    Map<String, Object> extractFeature(String imageBase64);
    
    /**
     * 人脸比对（1:1）
     * @param request 比对请求（包含两张图片）
     * @return 相似度结果
     */
    Map<String, Object> compareFace(FaceCompareRequest request);
    
    /**
     * 人脸注册
     * @param request 注册请求
     * @return 注册结果（包含人脸ID）
     */
    Map<String, Object> registerFace(FaceRegisterRequest request);
    
    /**
     * 人脸搜索（1:N）
     * @param request 搜索请求
     * @return 搜索结果
     */
    Map<String, Object> searchFace(FaceSearchRequest request);
    
    /**
     * 删除已注册的人脸
     * @param faceId 人脸ID
     * @return 删除结果
     */
    Map<String, Object> deleteFace(String faceId);
    
    /**
     * 获取已注册人脸列表
     * @param page 页码
     * @param size 每页大小
     * @return 人脸列表
     */
    Map<String, Object> listFaces(int page, int size);
}

