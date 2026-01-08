import axios from 'axios'

// 本地测试接口（不需要Token）
const API_BASE_URL = 'http://localhost:28089/api/test/face'

// 创建 axios 实例
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 60000, // 60秒超时（人脸识别可能需要较长时间）
  headers: {
    'Content-Type': 'application/json'
  }
})

/**
 * 人脸检测
 */
export function detectFace(imageBase64, drawBox = false, confidenceThreshold = null) {
  return apiClient.post('/detect', {
    imageBase64,
    drawBox,
    confidenceThreshold
  })
}

/**
 * 人脸特征提取
 */
export function extractFeature(imageBase64) {
  return apiClient.post('/extract', {
    imageBase64
  })
}

/**
 * 人脸比对（1:1）
 */
export function compareFace(image1Base64, image2Base64, threshold = 0.62) {
  return apiClient.post('/compare', {
    image1Base64,
    image2Base64,
    threshold
  })
}

/**
 * 人脸注册
 */
export function registerFace(imageBase64, userId, userName, faceId = null, metadata = null) {
  return apiClient.post('/register', {
    imageBase64,
    userId,
    userName,
    faceId,
    metadata
  })
}

/**
 * 人脸搜索（1:N）
 */
export function searchFace(imageBase64, topK = 1, threshold = null) {
  return apiClient.post('/search', {
    imageBase64,
    topK,
    threshold
  })
}

/**
 * 删除已注册的人脸
 */
export function deleteFace(faceId) {
  return apiClient.post('/delete', {
    faceId
  })
}

/**
 * 获取已注册人脸列表
 */
export function listFaces(page = 1, size = 10) {
  return apiClient.post('/list', {
    page,
    size
  })
}

