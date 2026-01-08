package com.example.basestation.service.impl;

import ai.djl.modality.cv.Image;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.entity.face.FaceSearchResult;
import cn.smartjavaai.common.utils.Base64ImageUtils;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.enums.SimilarityType;
import cn.smartjavaai.face.config.FaceDetConfig;
import cn.smartjavaai.face.config.FaceRecConfig;
import cn.smartjavaai.face.constant.FaceDetectConstant;
import cn.smartjavaai.face.entity.FaceRegisterInfo;
import cn.smartjavaai.face.entity.FaceSearchParams;
import cn.smartjavaai.face.enums.FaceDetModelEnum;
import cn.smartjavaai.face.enums.FaceRecModelEnum;
import cn.smartjavaai.face.enums.IdStrategy;
import cn.smartjavaai.face.factory.FaceDetModelFactory;
import cn.smartjavaai.face.factory.FaceRecModelFactory;
import cn.smartjavaai.face.model.facedect.FaceDetModel;
import cn.smartjavaai.face.model.facerec.FaceRecModel;
import cn.smartjavaai.face.vector.config.MilvusConfig;
import cn.smartjavaai.face.vector.config.SQLiteConfig;
import cn.smartjavaai.face.vector.entity.FaceVector;
import com.alibaba.fastjson.JSONObject;
import com.example.basestation.config.BusinessException;
import com.example.basestation.dto.FaceCompareRequest;
import com.example.basestation.dto.FaceDetectRequest;
import com.example.basestation.dto.FaceRegisterRequest;
import com.example.basestation.dto.FaceSearchRequest;
import com.example.basestation.service.FaceRecognitionService;
import io.milvus.param.MetricType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.Enumeration;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 人脸识别服务实现类
 */
@Slf4j
@Service
public class FaceRecognitionServiceImpl implements FaceRecognitionService {

    @Value("${smartjavaai.face.det-model-path:}")
    private String detModelPath;

    @Value("${smartjavaai.face.rec-model-path:}")
    private String recModelPath;

    @Value("${smartjavaai.face.det-model-enum:MTCNN}")
    private String detModelEnum;

    @Value("${smartjavaai.face.rec-model-enum:INSIGHT_FACE_IRSE50_MODEL}")
    private String recModelEnum;

    @Value("${smartjavaai.face.confidence-threshold:0.5}")
    private Float confidenceThreshold;

    @Value("${smartjavaai.face.device:CPU}")
    private String device;

    @Value("${smartjavaai.face.crop-face:true}")
    private Boolean cropFace;

    @Value("${smartjavaai.face.align:true}")
    private Boolean align;

    @Value("${smartjavaai.face.db-path:}")
    private String dbPath;

    @Value("${smartjavaai.face.vector-db-type:SQLITE}")
    private String vectorDbType;

    @Value("${smartjavaai.face.milvus-host:127.0.0.1}")
    private String milvusHost;

    @Value("${smartjavaai.face.milvus-port:19530}")
    private int milvusPort;

    @Value("${smartjavaai.face.milvus-collection-name:}")
    private String milvusCollectionName;

    @Value("${smartjavaai.face.milvus-username:}")
    private String milvusUsername;

    @Value("${smartjavaai.face.milvus-password:}")
    private String milvusPassword;

    private FaceDetModel faceDetModel;
    private FaceRecModel faceRecModel;

    @PostConstruct
    public void init() {
        try {
            log.info("初始化 SmartJavaAI 人脸识别服务...");
            
            // 设置图片处理引擎为 OpenCV
            SmartImageFactory.setEngine(SmartImageFactory.Engine.OPENCV);
            
            // 初始化人脸检测模型（如果配置了路径）
            if (StringUtils.hasText(detModelPath)) {
                initFaceDetModel();
            } else {
                log.warn("未配置人脸检测模型路径，人脸检测功能将不可用");
            }
            
            // 初始化人脸识别模型（如果配置了路径）
            if (StringUtils.hasText(recModelPath)) {
                initFaceRecModel();
            } else {
                log.warn("未配置人脸识别模型路径，人脸识别功能将不可用");
            }
            
            log.info("SmartJavaAI 人脸识别服务初始化完成");
        } catch (Exception e) {
            log.error("初始化 SmartJavaAI 人脸识别服务失败", e);
            // 不抛出异常，允许服务启动，但功能不可用
            log.warn("人脸识别服务初始化失败，相关功能将不可用，请检查配置");
        }
    }

    /**
     * 初始化人脸检测模型
     */
    private void initFaceDetModel() {
        try {
            // 验证模型路径
            validateModelPath(detModelPath, detModelEnum);
            
            // 处理 resources 路径
            String actualModelPath = resolveModelPath(detModelPath);
            
            FaceDetConfig config = new FaceDetConfig();
            config.setModelEnum(FaceDetModelEnum.valueOf(detModelEnum));
            config.setModelPath(actualModelPath);
            config.setConfidenceThreshold(confidenceThreshold);
            config.setNmsThresh(FaceDetectConstant.NMS_THRESHOLD);
            
            faceDetModel = FaceDetModelFactory.getInstance().getModel(config);
            log.info("人脸检测模型初始化成功: model={}, path={}", detModelEnum, detModelPath);
        } catch (Exception e) {
            log.error("初始化人脸检测模型失败: path={}, model={}", detModelPath, detModelEnum, e);
            throw new RuntimeException("初始化人脸检测模型失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 验证模型路径和文件
     */
    private void validateModelPath(String modelPath, String modelEnum) {
        if (!StringUtils.hasText(modelPath)) {
            throw new RuntimeException("模型路径不能为空");
        }
        
        // 检查是否是 resources 路径（以 classpath: 开头）
        if (modelPath.startsWith("classpath:")) {
            String resourcePath = modelPath.substring("classpath:".length());
            // 确保路径不以 / 开头
            if (resourcePath.startsWith("/")) {
                resourcePath = resourcePath.substring(1);
            }
            
            log.debug("检查 Resources 路径: {}", resourcePath);
            
            // 对于 MTCNN，直接检查目录中的文件（目录本身无法通过 getResourceAsStream 获取）
            if ("MTCNN".equals(modelEnum)) {
                String[] requiredFiles = {"pnet_script.pt", "rnet_script.pt", "onet_script.pt"};
                List<String> missingFiles = new ArrayList<>();
                for (String fileName : requiredFiles) {
                    String fileResourcePath = resourcePath.endsWith("/") 
                        ? resourcePath + fileName 
                        : resourcePath + "/" + fileName;
                    log.debug("检查文件: {}", fileResourcePath);
                    java.io.InputStream fileIs = getClass().getClassLoader().getResourceAsStream(fileResourcePath);
                    if (fileIs == null) {
                        missingFiles.add(fileName);
                        log.warn("文件不存在: {}", fileResourcePath);
                    } else {
                        try {
                            fileIs.close();
                        } catch (IOException e) {
                            // ignore
                        }
                        log.debug("文件存在: {}", fileResourcePath);
                    }
                }
                if (!missingFiles.isEmpty()) {
                    throw new RuntimeException(String.format(
                        "Resources 中的 MTCNN 模型文件缺失，路径: %s，缺失文件: %s。请确保文件在 src/main/resources/%s/ 目录下",
                        resourcePath, String.join(", ", missingFiles), resourcePath
                    ));
                }
                log.info("Resources MTCNN 模型路径验证通过: {}", resourcePath);
                return;
            }
            
            // 其他模型类型，尝试检查资源是否存在
            java.io.InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
            if (is == null) {
                throw new RuntimeException(String.format("Resources 中的模型路径不存在: %s", resourcePath));
            }
            try {
                is.close();
            } catch (IOException e) {
                // ignore
            }
            
            log.info("Resources 模型路径验证通过: {}", resourcePath);
            return;
        }
        
        // 文件系统路径验证
        Path path = Paths.get(modelPath);
        
        // 检查路径是否存在
        if (!Files.exists(path)) {
            throw new RuntimeException(String.format("模型路径不存在: %s", modelPath));
        }
        
        // 检查是否为目录
        if (!Files.isDirectory(path)) {
            throw new RuntimeException(String.format("模型路径必须是目录: %s", modelPath));
        }
        
        // 根据模型类型检查必需的文件
        if ("MTCNN".equals(modelEnum)) {
            // MTCNN 需要三个文件
            Path pnetPath = path.resolve("pnet_script.pt");
            Path rnetPath = path.resolve("rnet_script.pt");
            Path onetPath = path.resolve("onet_script.pt");
            
            List<String> missingFiles = new ArrayList<>();
            if (!Files.exists(pnetPath)) {
                missingFiles.add("pnet_script.pt");
            }
            if (!Files.exists(rnetPath)) {
                missingFiles.add("rnet_script.pt");
            }
            if (!Files.exists(onetPath)) {
                missingFiles.add("onet_script.pt");
            }
            
            if (!missingFiles.isEmpty()) {
                throw new RuntimeException(String.format(
                    "MTCNN 模型文件缺失，路径: %s，缺失文件: %s。请确保目录中包含: pnet_script.pt, rnet_script.pt, onet_script.pt",
                    modelPath, String.join(", ", missingFiles)
                ));
            }
            
            log.info("MTCNN 模型文件验证通过: {}", modelPath);
        } else if ("RETINA_FACE".equals(modelEnum)) {
            // RetinaFace 通常是一个 .pt 文件
            Path modelFile = path.resolve("retinaface.pt");
            if (!Files.exists(modelFile)) {
                // 检查是否是文件路径而不是目录
                if (Files.isRegularFile(path)) {
                    log.info("RetinaFace 模型文件路径: {}", modelPath);
                } else {
                    throw new RuntimeException(String.format(
                        "RetinaFace 模型文件不存在: %s/retinaface.pt 或模型路径应指向 .pt 文件", modelPath
                    ));
                }
            }
        } else if (modelEnum.startsWith("YOLOV5_FACE")) {
            // YOLOv5 Face 通常是 .onnx 文件
            Path modelFile = path.resolve("yolov5face-n-0.5-320x320.onnx");
            if (!Files.exists(modelFile)) {
                // 检查是否是文件路径而不是目录
                if (Files.isRegularFile(path)) {
                    log.info("YOLOv5 Face 模型文件路径: {}", modelPath);
                } else {
                    throw new RuntimeException(String.format(
                        "YOLOv5 Face 模型文件不存在: %s，请确保路径指向正确的 .onnx 文件", modelPath
                    ));
                }
            }
        }
    }
    
    /**
     * 验证人脸识别模型路径和文件
     */
    private void validateRecModelPath(String modelPath, String modelEnum) {
        if (!StringUtils.hasText(modelPath)) {
            throw new RuntimeException("人脸识别模型路径不能为空");
        }
        
        // 检查是否是 resources 路径（以 classpath: 开头）
        if (modelPath.startsWith("classpath:")) {
            String resourcePath = modelPath.substring("classpath:".length());
            // 确保路径不以 / 开头
            if (resourcePath.startsWith("/")) {
                resourcePath = resourcePath.substring(1);
            }
            
            // 检查资源是否存在
            java.io.InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
            if (is == null) {
                throw new RuntimeException(String.format("Resources 中的识别模型文件不存在: %s", resourcePath));
            }
            try {
                is.close();
            } catch (IOException e) {
                // ignore
            }
            
            // 检查文件扩展名
            String fileName = resourcePath.substring(resourcePath.lastIndexOf("/") + 1);
            if (!fileName.endsWith(".pt") && !fileName.endsWith(".onnx")) {
                throw new RuntimeException(String.format("识别模型文件必须是 .pt 或 .onnx 格式: %s", fileName));
            }
            
            log.info("Resources 识别模型文件验证通过: {}", resourcePath);
            return;
        }
        
        // 文件系统路径验证
        Path path = Paths.get(modelPath);
        
        // 检查路径是否存在
        if (!Files.exists(path)) {
            throw new RuntimeException(String.format("人脸识别模型路径不存在: %s", modelPath));
        }
        
        // InsightFace 模型应该是文件，不是目录
        if ("INSIGHT_FACE_IRSE50_MODEL".equals(modelEnum)) {
            if (!Files.isRegularFile(path)) {
                throw new RuntimeException(String.format(
                    "INSIGHT_FACE_IRSE50_MODEL 模型路径必须指向 .pt 文件，当前路径: %s。请确保路径指向 model_ir_se50.pt 文件",
                    modelPath
                ));
            }
            if (!modelPath.endsWith(".pt")) {
                throw new RuntimeException(String.format(
                    "INSIGHT_FACE_IRSE50_MODEL 模型文件必须是 .pt 格式，当前路径: %s",
                    modelPath
                ));
            }
            log.info("INSIGHT_FACE_IRSE50_MODEL 模型文件验证通过: {}", modelPath);
        } else if ("INSIGHT_FACE_MOBILE_FACENET_MODEL".equals(modelEnum)) {
            if (!Files.isRegularFile(path)) {
                throw new RuntimeException(String.format(
                    "INSIGHT_FACE_MOBILE_FACENET_MODEL 模型路径必须指向 .pt 文件，当前路径: %s。请确保路径指向 model_mobilefacenet.pt 文件",
                    modelPath
                ));
            }
            if (!modelPath.endsWith(".pt")) {
                throw new RuntimeException(String.format(
                    "INSIGHT_FACE_MOBILE_FACENET_MODEL 模型文件必须是 .pt 格式，当前路径: %s",
                    modelPath
                ));
            }
            log.info("INSIGHT_FACE_MOBILE_FACENET_MODEL 模型文件验证通过: {}", modelPath);
        }
    }

    /**
     * 初始化人脸识别模型
     */
    private void initFaceRecModel() {
        try {
            // 验证模型路径
            validateRecModelPath(recModelPath, recModelEnum);
            
            // 处理 resources 路径
            String actualModelPath = resolveModelPath(recModelPath);
            
            FaceRecConfig config = new FaceRecConfig();
            config.setModelEnum(FaceRecModelEnum.valueOf(recModelEnum));
            config.setModelPath(actualModelPath);
            config.setCropFace(cropFace);
            config.setAlign(align);
            config.setDevice(DeviceEnum.valueOf(device));
            
            // 设置人脸检测模型
            if (faceDetModel != null) {
                config.setDetectModel(faceDetModel);
            } else {
                log.warn("人脸检测模型未初始化，人脸识别功能可能受限");
            }
            
            // 配置向量数据库（SQLite 或 Milvus）
            if ("MILVUS".equalsIgnoreCase(vectorDbType)) {
                // 使用 Milvus 向量数据库（适合大规模应用）
                MilvusConfig milvusConfig = new MilvusConfig();
                milvusConfig.setHost(milvusHost);
                milvusConfig.setPort(milvusPort);
                if (StringUtils.hasText(milvusCollectionName)) {
                    milvusConfig.setCollectionName(milvusCollectionName);
                }
                if (StringUtils.hasText(milvusUsername) && StringUtils.hasText(milvusPassword)) {
                    milvusConfig.setUsername(milvusUsername);
                    milvusConfig.setPassword(milvusPassword);
                }
                milvusConfig.setIdStrategy(IdStrategy.AUTO);
                milvusConfig.setMetricType(MetricType.IP);
                config.setVectorDBConfig(milvusConfig);
                log.info("使用 Milvus 向量数据库: host={}, port={}, collection={}", 
                    milvusHost, milvusPort, milvusCollectionName);
            } else {
                // 使用 SQLite 向量数据库（轻量级，适合小规模应用）
                SQLiteConfig sqliteConfig = new SQLiteConfig();
                sqliteConfig.setSimilarityType(SimilarityType.IP);
                // 如果配置了自定义数据库路径，则使用配置的路径
                if (StringUtils.hasText(dbPath)) {
                    sqliteConfig.setDbPath(dbPath);
                    log.info("使用自定义SQLite数据库路径: {}", dbPath);
                } else {
                    // 默认路径：~/smartjavaai_cache/face.db
                    String defaultPath = cn.smartjavaai.common.config.Config.getCachePath() + 
                        java.io.File.separator + "face.db";
                    log.info("使用默认SQLite数据库路径: {}", defaultPath);
                }
                config.setVectorDBConfig(sqliteConfig);
            }
            
            faceRecModel = FaceRecModelFactory.getInstance().getModel(config);
            
            // 等待加载人脸库完成
            int waitCount = 0;
            while (!faceRecModel.isLoadFaceCompleted() && waitCount < 100) {
                Thread.sleep(100);
                waitCount++;
            }
            
            if (waitCount >= 100) {
                log.warn("等待人脸库加载超时");
            }
            
            log.info("人脸识别模型初始化成功: model={}, path={}", recModelEnum, recModelPath);
        } catch (Exception e) {
            log.error("初始化人脸识别模型失败: path={}", recModelPath, e);
            throw new RuntimeException("初始化人脸识别模型失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> detectFace(FaceDetectRequest request) {
        try {
            if (faceDetModel == null) {
                throw new BusinessException("人脸检测模型未初始化，请检查配置");
            }

            // 创建Image对象
            Image image = createImage(request.getImageBase64(), request.getImageUrl());
            
            // 设置置信度阈值
            if (request.getConfidenceThreshold() != null) {
                // 注意：这里需要重新创建模型配置，实际使用中可以考虑缓存多个模型实例
                log.debug("使用自定义置信度阈值: {}", request.getConfidenceThreshold());
            }

            // 执行人脸检测
            R<DetectionResponse> result;
            if (Boolean.TRUE.equals(request.getDrawBox())) {
                // 检测并绘制人脸框
                result = faceDetModel.detectAndDraw(image);
            } else {
                // 仅检测
                result = faceDetModel.detect(image);
            }

            Map<String, Object> response = new HashMap<>();
            if (result.isSuccess()) {
                response.put("success", true);
                response.put("data", result.getData());
                response.put("faceCount", result.getData().getDetectionInfoList().size());
                log.info("人脸检测成功，检测到 {} 张人脸", result.getData().getDetectionInfoList().size());
            } else {
                response.put("success", false);
                response.put("message", result.getMessage());
                log.warn("人脸检测失败: {}", result.getMessage());
            }

            return response;
        } catch (Exception e) {
            log.error("人脸检测异常", e);
            throw new BusinessException("人脸检测失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> extractFeature(String imageBase64) {
        try {
            if (faceRecModel == null) {
                throw new BusinessException("人脸识别模型未初始化，请检查配置");
            }

            Image image = createImage(imageBase64, null);
            
            // 提取人脸特征
            R<float[]> result = faceRecModel.extractTopFaceFeature(image);
            
            Map<String, Object> response = new HashMap<>();
            if (result.isSuccess()) {
                response.put("success", true);
                response.put("feature", result.getData());
                response.put("featureLength", result.getData().length);
                log.info("人脸特征提取成功，特征向量长度: {}", result.getData().length);
            } else {
                response.put("success", false);
                response.put("message", result.getMessage());
                log.warn("人脸特征提取失败: {}", result.getMessage());
            }

            return response;
        } catch (Exception e) {
            log.error("人脸特征提取异常", e);
            throw new BusinessException("人脸特征提取失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> compareFace(FaceCompareRequest request) {
        try {
            if (faceRecModel == null) {
                throw new BusinessException("人脸识别模型未初始化，请检查配置");
            }

            Image image1 = createImage(request.getImage1Base64(), request.getImage1Url());
            Image image2 = createImage(request.getImage2Base64(), request.getImage2Url());

            // 执行人脸比对
            R<Float> result = faceRecModel.featureComparison(image1, image2);
            
            Map<String, Object> response = new HashMap<>();
            if (result.isSuccess()) {
                float similarity = result.getData();
                float threshold = request.getThreshold() != null ? request.getThreshold() : 0.62f;
                boolean isSamePerson = similarity >= threshold;
                
                response.put("success", true);
                response.put("similarity", similarity);
                response.put("threshold", threshold);
                response.put("isSamePerson", isSamePerson);
                log.info("人脸比对完成，相似度: {}, 阈值: {}, 是否同一人: {}", similarity, threshold, isSamePerson);
            } else {
                response.put("success", false);
                response.put("message", result.getMessage());
                log.warn("人脸比对失败: {}", result.getMessage());
            }

            return response;
        } catch (Exception e) {
            log.error("人脸比对异常", e);
            throw new BusinessException("人脸比对失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> registerFace(FaceRegisterRequest request) {
        try {
            if (faceRecModel == null) {
                throw new BusinessException("人脸识别模型未初始化，请检查配置");
            }

            // 验证输入参数
            if (!StringUtils.hasText(request.getImageBase64()) && !StringUtils.hasText(request.getImageUrl())) {
                throw new BusinessException("图片Base64编码或URL不能为空");
            }

            Image image = createImage(request.getImageBase64(), request.getImageUrl());
            
            // 先进行人脸检测，提供更详细的错误信息
            if (faceDetModel != null) {
                R<DetectionResponse> detectResult = faceDetModel.detect(image);
                if (!detectResult.isSuccess()) {
                    log.error("人脸检测失败: {}", detectResult.getMessage());
                    throw new BusinessException("人脸检测失败: " + detectResult.getMessage());
                }
                if (detectResult.getData() == null || 
                    detectResult.getData().getDetectionInfoList() == null || 
                    detectResult.getData().getDetectionInfoList().isEmpty()) {
                    log.warn("图片中未检测到人脸");
                    throw new BusinessException("图片中未检测到人脸，请确保图片中包含清晰、正面的人脸");
                }
                log.info("检测到 {} 张人脸，将使用第一张人脸进行注册", 
                    detectResult.getData().getDetectionInfoList().size());
            }
            
            // 提取人脸特征
            R<float[]> featureResult = faceRecModel.extractTopFaceFeature(image);
            if (!featureResult.isSuccess()) {
                String errorMsg = featureResult.getMessage();
                Integer errorCode = featureResult.getCode();
                log.error("人脸特征提取失败: code={}, message={}", errorCode, errorMsg);
                
                // 根据错误码提供更友好的错误提示
                if (errorCode != null && errorCode == 1001) { // NO_FACE_DETECTED
                    throw new BusinessException("图片中未检测到人脸，请确保图片中包含清晰、正面的人脸");
                } else if (errorMsg != null) {
                    if (errorMsg.contains("未检测到人脸") || errorMsg.contains("NO_FACE_DETECTED") || 
                        errorMsg.contains("no face") || errorMsg.contains("No face")) {
                        throw new BusinessException("图片中未检测到人脸，请确保图片中包含清晰、正面的人脸");
                    } else if (errorMsg.contains("检测失败") || errorMsg.contains("detect failed")) {
                        throw new BusinessException("人脸检测失败，请检查图片质量和格式");
                    }
                }
                throw new BusinessException("人脸特征提取失败: " + (errorMsg != null ? errorMsg : "未知错误"));
            }

            // 构建注册信息
            FaceRegisterInfo registerInfo = new FaceRegisterInfo();
            
            // 设置自定义ID（如果提供）
            if (StringUtils.hasText(request.getFaceId())) {
                registerInfo.setId(request.getFaceId());
            }
            
            // 构建元数据
            JSONObject metadata = new JSONObject();
            if (StringUtils.hasText(request.getUserId())) {
                metadata.put("userId", request.getUserId());
            }
            if (StringUtils.hasText(request.getUserName())) {
                metadata.put("userName", request.getUserName());
            }
            if (StringUtils.hasText(request.getMetadata())) {
                // 合并自定义元数据
                try {
                    JSONObject customMetadata = JSONObject.parseObject(request.getMetadata());
                    metadata.putAll(customMetadata);
                } catch (Exception e) {
                    log.warn("解析自定义元数据失败，将作为字符串存储: {}", request.getMetadata());
                    metadata.put("custom", request.getMetadata());
                }
            }
            registerInfo.setMetadata(metadata.toJSONString());

            // 注册人脸
            R<String> registerResult = faceRecModel.register(registerInfo, featureResult.getData());
            
            Map<String, Object> response = new HashMap<>();
            if (registerResult.isSuccess()) {
                response.put("success", true);
                response.put("faceId", registerResult.getData());
                log.info("人脸注册成功，FaceID: {}", registerResult.getData());
            } else {
                response.put("success", false);
                response.put("message", registerResult.getMessage());
                log.warn("人脸注册失败: {}", registerResult.getMessage());
            }

            return response;
        } catch (BusinessException e) {
            // 业务异常直接抛出
            throw e;
        } catch (Exception e) {
            log.error("人脸注册异常", e);
            // 提供更详细的错误信息
            String errorMessage = e.getMessage();
            if (errorMessage != null) {
                if (errorMessage.contains("未检测到人脸") || errorMessage.contains("No face detected")) {
                    throw new BusinessException("图片中未检测到人脸，请确保图片中包含清晰的人脸");
                } else if (errorMessage.contains("图片解析失败") || errorMessage.contains("Image")) {
                    throw new BusinessException("图片格式不正确或已损坏，请检查图片文件");
                } else if (errorMessage.contains("Base64")) {
                    throw new BusinessException("Base64编码格式错误，请检查图片数据");
                }
            }
            throw new BusinessException("人脸注册失败: " + (errorMessage != null ? errorMessage : e.getClass().getSimpleName()));
        }
    }

    @Override
    public Map<String, Object> searchFace(FaceSearchRequest request) {
        try {
            if (faceRecModel == null) {
                throw new BusinessException("人脸识别模型未初始化，请检查配置");
            }

            Image image = createImage(request.getImageBase64(), request.getImageUrl());
            
            // 提取人脸特征
            R<float[]> featureResult = faceRecModel.extractTopFaceFeature(image);
            if (!featureResult.isSuccess()) {
                throw new BusinessException("人脸特征提取失败: " + featureResult.getMessage());
            }

            // 构建搜索参数
            FaceSearchParams searchParams = new FaceSearchParams();
            searchParams.setTopK(request.getTopK() != null ? request.getTopK() : 1);
            if (request.getThreshold() != null) {
                searchParams.setThreshold(request.getThreshold());
            }

            // 执行搜索
            List<FaceSearchResult> searchResults = faceRecModel.search(featureResult.getData(), searchParams);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("results", searchResults);
            response.put("count", searchResults.size());
            
            // 格式化搜索结果
            List<Map<String, Object>> formattedResults = new ArrayList<>();
            for (FaceSearchResult result : searchResults) {
                Map<String, Object> item = new HashMap<>();
                item.put("faceId", result.getId());
                item.put("similarity", result.getSimilarity());
                item.put("metadata", result.getMetadata());
                formattedResults.add(item);
            }
            response.put("formattedResults", formattedResults);
            
            log.info("人脸搜索完成，找到 {} 个结果", searchResults.size());
            return response;
        } catch (Exception e) {
            log.error("人脸搜索异常", e);
            throw new BusinessException("人脸搜索失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> deleteFace(String faceId) {
        try {
            if (faceRecModel == null) {
                throw new BusinessException("人脸识别模型未初始化，请检查配置");
            }

            if (!StringUtils.hasText(faceId)) {
                throw new BusinessException("人脸ID不能为空");
            }

            faceRecModel.removeRegister(faceId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "删除成功");
            log.info("人脸删除成功，FaceID: {}", faceId);
            
            return response;
        } catch (Exception e) {
            log.error("人脸删除异常", e);
            throw new BusinessException("人脸删除失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> listFaces(int page, int size) {
        try {
            if (faceRecModel == null) {
                throw new BusinessException("人脸识别模型未初始化，请检查配置");
            }

            // 计算分页参数
            int offset = (page - 1) * size;
            
            // 获取人脸列表（注意：SQLite配置下可能不支持分页，这里使用简单实现）
            R<List<FaceVector>> result = faceRecModel.listFaces(page, size);
            
            Map<String, Object> response = new HashMap<>();
            if (result.isSuccess()) {
                List<FaceVector> faces = result.getData();
                
                // 格式化结果
                List<Map<String, Object>> formattedFaces = new ArrayList<>();
                for (FaceVector face : faces) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("faceId", face.getId());
                    item.put("metadata", face.getMetadata());
                    formattedFaces.add(item);
                }
                
                response.put("success", true);
                response.put("data", formattedFaces);
                response.put("total", formattedFaces.size());
                response.put("page", page);
                response.put("size", size);
            } else {
                response.put("success", false);
                response.put("message", result.getMessage());
            }

            return response;
        } catch (Exception e) {
            log.error("获取人脸列表异常", e);
            throw new BusinessException("获取人脸列表失败: " + e.getMessage());
        }
    }

    /**
     * 解析模型路径（支持 resources 路径）
     * 如果路径以 classpath: 开头，则从 resources 目录加载并复制到临时目录
     */
    private String resolveModelPath(String modelPath) {
        if (!StringUtils.hasText(modelPath)) {
            return modelPath;
        }
        
        // 如果是 resources 路径
        if (modelPath.startsWith("classpath:")) {
            String resourcePath = modelPath.substring("classpath:".length());
            try {
                // 获取 resources 中的文件
                java.io.InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
                if (is == null) {
                    throw new RuntimeException(String.format("Resources 中的模型文件不存在: %s", resourcePath));
                }
                
                // 创建临时目录存储模型文件
                String tempDir = System.getProperty("java.io.tmpdir") + File.separator + "smartjavaai_models";
                File tempDirFile = new File(tempDir);
                if (!tempDirFile.exists()) {
                    tempDirFile.mkdirs();
                }
                
                // 提取文件名
                String fileName = resourcePath.substring(resourcePath.lastIndexOf("/") + 1);
                if (fileName.isEmpty()) {
                    fileName = resourcePath.replaceAll("/", "_");
                }
                
                // 如果是目录路径（MTCNN需要多个文件）
                if (resourcePath.endsWith("/") || !fileName.contains(".")) {
                    // 处理目录：复制整个目录
                    String targetDir = tempDir + File.separator + fileName;
                    File targetDirFile = new File(targetDir);
                    if (!targetDirFile.exists()) {
                        targetDirFile.mkdirs();
                    }
                    
                    // 复制目录中的所有文件
                    copyResourceDirectory(resourcePath, targetDir);
                    is.close();
                    log.info("从 Resources 复制模型目录到: {}", targetDir);
                    return targetDir;
                } else {
                    // 处理单个文件
                    File targetFile = new File(tempDir, fileName);
                    if (!targetFile.exists() || targetFile.length() == 0) {
                        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(targetFile)) {
                            byte[] buffer = new byte[8192];
                            int bytesRead;
                            while ((bytesRead = is.read(buffer)) != -1) {
                                fos.write(buffer, 0, bytesRead);
                            }
                        }
                    }
                    is.close();
                    log.info("从 Resources 复制模型文件到: {}", targetFile.getAbsolutePath());
                    return targetFile.getAbsolutePath();
                }
            } catch (Exception e) {
                throw new RuntimeException("从 Resources 加载模型文件失败: " + e.getMessage(), e);
            }
        }
        
        return modelPath;
    }
    
    /**
     * 复制 resources 目录中的所有文件
     */
    private void copyResourceDirectory(String resourceDir, String targetDir) {
        try {
            // 尝试列出目录中的文件
            java.net.URL resourceUrl = getClass().getClassLoader().getResource(resourceDir);
            if (resourceUrl != null && "jar".equals(resourceUrl.getProtocol())) {
                // JAR 包中的资源
                JarURLConnection jarConnection = (JarURLConnection) resourceUrl.openConnection();
                JarFile jarFile = jarConnection.getJarFile();
                Enumeration<JarEntry> entries = jarFile.entries();
                
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String entryName = entry.getName();
                    if (entryName.startsWith(resourceDir) && !entry.isDirectory()) {
                        String fileName = entryName.substring(entryName.lastIndexOf("/") + 1);
                        File targetFile = new File(targetDir, fileName);
                        try (java.io.InputStream is = jarFile.getInputStream(entry);
                             java.io.FileOutputStream fos = new java.io.FileOutputStream(targetFile)) {
                            byte[] buffer = new byte[8192];
                            int bytesRead;
                            while ((bytesRead = is.read(buffer)) != -1) {
                                fos.write(buffer, 0, bytesRead);
                            }
                        }
                    }
                }
            } else {
                // 文件系统中的资源
                File resourceDirFile = new File(resourceUrl.toURI());
                if (resourceDirFile.isDirectory()) {
                    File[] files = resourceDirFile.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            if (file.isFile()) {
                                File targetFile = new File(targetDir, file.getName());
                                try (java.io.InputStream is = new java.io.FileInputStream(file);
                                     java.io.FileOutputStream fos = new java.io.FileOutputStream(targetFile)) {
                                    byte[] buffer = new byte[8192];
                                    int bytesRead;
                                    while ((bytesRead = is.read(buffer)) != -1) {
                                        fos.write(buffer, 0, bytesRead);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("复制 Resources 目录失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建Image对象（支持Base64和URL）
     */
    private Image createImage(String imageBase64, String imageUrl) {
        try {
            if (StringUtils.hasText(imageBase64)) {
                // 处理Base64编码（可能包含data:image前缀）
                String base64Data = imageBase64.trim();
                
                // 移除 data:image 前缀（如果存在）
                if (base64Data.contains(",")) {
                    base64Data = base64Data.substring(base64Data.indexOf(",") + 1);
                }
                
                // 验证Base64格式
                if (base64Data.isEmpty()) {
                    throw new BusinessException("Base64编码数据为空");
                }
                
                // 移除可能的空白字符
                base64Data = base64Data.replaceAll("\\s+", "");
                
                log.debug("处理Base64图片，长度: {}", base64Data.length());
                
                try {
                    // 使用 Base64ImageUtils 将 Base64 转换为字节数组，然后创建图片
                    byte[] imageBytes = Base64ImageUtils.base64ToImage(base64Data);
                    return SmartImageFactory.getInstance().fromBytes(imageBytes);
                } catch (Exception e) {
                    log.error("Base64图片解析失败，Base64长度: {}", base64Data.length(), e);
                    throw new BusinessException("图片Base64编码格式错误或图片已损坏: " + e.getMessage());
                }
            } else if (StringUtils.hasText(imageUrl)) {
                log.debug("从URL加载图片: {}", imageUrl);
                try {
                    return SmartImageFactory.getInstance().fromUrl(imageUrl);
                } catch (Exception e) {
                    log.error("从URL加载图片失败: {}", imageUrl, e);
                    throw new BusinessException("无法从URL加载图片: " + e.getMessage());
                }
            } else {
                throw new BusinessException("图片Base64编码或URL不能同时为空");
            }
        } catch (BusinessException e) {
            // 业务异常直接抛出
            throw e;
        } catch (Exception e) {
            log.error("创建Image对象时发生未知错误", e);
            throw new BusinessException("图片处理失败: " + e.getMessage());
        }
    }
}

