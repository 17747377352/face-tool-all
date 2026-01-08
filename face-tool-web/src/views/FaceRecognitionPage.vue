<template>
  <div class="face-page">
    <!-- 顶部导航栏 -->
    <div class="header">
      <div class="nav-tabs">
        <div class="nav-tab" @click="$router.push('/chat')">AI 对话</div>
        <div class="nav-tab active">人脸识别</div>
      </div>
    </div>

    <!-- 功能标签页 -->
    <div class="tabs">
      <div 
        v-for="tab in tabs" 
        :key="tab.key"
        class="tab-item"
        :class="{ active: activeTab === tab.key }"
        @click="activeTab = tab.key"
      >
        {{ tab.label }}
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="content">
      <!-- 图片上传区域 -->
      <div class="upload-section">
        <div class="image-preview" v-if="currentImage">
          <img :src="currentImage" alt="预览图片" />
          <button class="btn-remove" @click="clearImage">×</button>
        </div>
        <div v-else class="upload-placeholder">
          <div class="upload-icon">📷</div>
          <p>请选择或拍摄图片</p>
        </div>
        <div class="upload-buttons">
          <button class="btn-upload" @click="selectImage" :disabled="loading">
            <span>📁</span> 选择图片
          </button>
          <button class="btn-upload" @click="captureImage" :disabled="loading">
            <span>📸</span> 拍照
          </button>
        </div>
      </div>

      <!-- 操作按钮区域 -->
      <div class="action-section" v-if="currentImage">
        <!-- 人脸检测 -->
        <div v-if="activeTab === 'detect'" class="action-group">
          <button class="btn-action" @click="handleDetect" :disabled="loading">
            {{ loading ? '检测中...' : '开始检测' }}
          </button>
          <div v-if="detectResult" class="result-box">
            <h3>检测结果</h3>
            <p>检测到 <strong>{{ detectResult.faceCount }}</strong> 张人脸</p>
            <div v-if="detectResult.data && detectResult.data.detectionInfoList" class="face-list">
              <div 
                v-for="(face, index) in detectResult.data.detectionInfoList" 
                :key="index"
                class="face-item"
              >
                <p>人脸 {{ index + 1 }}</p>
                <p>置信度: {{ (face.score * 100).toFixed(2) }}%</p>
                <p>位置: ({{ face.detectionRectangle.x }}, {{ face.detectionRectangle.y }})</p>
                <p>大小: {{ face.detectionRectangle.width }} × {{ face.detectionRectangle.height }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- 人脸比对 -->
        <div v-if="activeTab === 'compare'" class="action-group">
          <div class="compare-images">
            <div class="compare-image">
              <img v-if="compareImage1" :src="compareImage1" alt="图片1" />
              <button v-else class="btn-select" @click="selectCompareImage1">选择图片1</button>
            </div>
            <div class="compare-divider">VS</div>
            <div class="compare-image">
              <img v-if="compareImage2" :src="compareImage2" alt="图片2" />
              <button v-else class="btn-select" @click="selectCompareImage2">选择图片2</button>
            </div>
          </div>
          <button 
            class="btn-action" 
            @click="handleCompare" 
            :disabled="loading || !compareImage1 || !compareImage2"
          >
            {{ loading ? '比对中...' : '开始比对' }}
          </button>
          <div v-if="compareResult" class="result-box">
            <h3>比对结果</h3>
            <div class="similarity-result">
              <div class="similarity-value">{{ (compareResult.similarity * 100).toFixed(2) }}%</div>
              <p class="similarity-label">相似度</p>
              <div class="similarity-status" :class="{ match: compareResult.isSamePerson }">
                {{ compareResult.isSamePerson ? '✓ 同一人' : '✗ 不同人' }}
              </div>
            </div>
          </div>
        </div>

        <!-- 人脸注册 -->
        <div v-if="activeTab === 'register'" class="action-group">
          <div class="form-group">
            <label>用户ID：</label>
            <input v-model="registerUserId" type="text" placeholder="请输入用户ID" />
          </div>
          <div class="form-group">
            <label>用户名称：</label>
            <input v-model="registerUserName" type="text" placeholder="请输入用户名称" />
          </div>
          <button class="btn-action" @click="handleRegister" :disabled="loading || !registerUserId || !registerUserName">
            {{ loading ? '注册中...' : '注册人脸' }}
          </button>
          <div v-if="registerResult" class="result-box success">
            <h3>注册成功</h3>
            <p>人脸ID: <strong>{{ registerResult.faceId }}</strong></p>
          </div>
        </div>

        <!-- 人脸搜索 -->
        <div v-if="activeTab === 'search'" class="action-group">
          <button class="btn-action" @click="handleSearch" :disabled="loading">
            {{ loading ? '搜索中...' : '开始搜索' }}
          </button>
          <div v-if="searchResult" class="result-box">
            <h3>搜索结果</h3>
            <p v-if="searchResult.count === 0" class="no-result">未找到匹配的人脸</p>
            <div v-else class="search-results">
              <div 
                v-for="(result, index) in searchResult.formattedResults" 
                :key="index"
                class="search-item"
              >
                <div class="search-header">
                  <span class="search-rank">#{{ index + 1 }}</span>
                  <span class="search-similarity">{{ (result.similarity * 100).toFixed(2) }}%</span>
                </div>
                <div class="search-info">
                  <p><strong>人脸ID:</strong> {{ result.faceId }}</p>
                  <p v-if="result.metadata" class="metadata">
                    <strong>信息:</strong> {{ formatMetadata(result.metadata) }}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 人脸列表 -->
        <div v-if="activeTab === 'list'" class="action-group">
          <button class="btn-action" @click="loadFaceList" :disabled="loading">
            {{ loading ? '加载中...' : '刷新列表' }}
          </button>
          <div v-if="faceList.length > 0" class="face-list-container">
            <div 
              v-for="face in faceList" 
              :key="face.faceId"
              class="face-list-item"
            >
              <div class="face-info">
                <p><strong>人脸ID:</strong> {{ face.faceId }}</p>
                <p v-if="face.metadata" class="metadata">
                  <strong>信息:</strong> {{ formatMetadata(face.metadata) }}
                </p>
              </div>
              <button class="btn-delete" @click="handleDelete(face.faceId)">删除</button>
            </div>
          </div>
          <div v-else-if="!loading" class="empty-list">
            <p>暂无已注册的人脸</p>
          </div>
        </div>
      </div>

      <!-- 加载提示 -->
      <div v-if="loading" class="loading-overlay">
        <div class="loading-spinner-large"></div>
        <p>{{ loadingText }}</p>
      </div>

      <!-- 错误提示 -->
      <div v-if="error" class="error-message">
        <p>{{ error }}</p>
        <button @click="error = ''">关闭</button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { detectFace, compareFace, registerFace, searchFace, deleteFace, listFaces } from '../api/faceApi'
import { selectFromGallery, captureFromCamera, compressImage } from '../utils/imageUtils'

export default {
  name: 'FaceRecognitionPage',
  setup() {
    const activeTab = ref('detect')
    const currentImage = ref(null)
    const compareImage1 = ref(null)
    const compareImage2 = ref(null)
    const loading = ref(false)
    const loadingText = ref('处理中...')
    const error = ref('')
    
    // 检测结果
    const detectResult = ref(null)
    
    // 比对结果
    const compareResult = ref(null)
    
    // 注册相关
    const registerUserId = ref('')
    const registerUserName = ref('')
    const registerResult = ref(null)
    
    // 搜索结果
    const searchResult = ref(null)
    
    // 人脸列表
    const faceList = ref([])

    const tabs = [
      { key: 'detect', label: '人脸检测' },
      { key: 'compare', label: '人脸比对' },
      { key: 'register', label: '人脸注册' },
      { key: 'search', label: '人脸搜索' },
      { key: 'list', label: '人脸列表' }
    ]

    // 选择图片
    const selectImage = async () => {
      try {
        const base64 = await selectFromGallery()
        currentImage.value = base64
        clearResults()
      } catch (err) {
        showError('选择图片失败: ' + err.message)
      }
    }

    // 拍照
    const captureImage = async () => {
      try {
        const base64 = await captureFromCamera()
        currentImage.value = base64
        clearResults()
      } catch (err) {
        showError('拍照失败: ' + err.message)
      }
    }

    // 清除图片
    const clearImage = () => {
      currentImage.value = null
      compareImage1.value = null
      compareImage2.value = null
      clearResults()
    }

    // 清除结果
    const clearResults = () => {
      detectResult.value = null
      compareResult.value = null
      registerResult.value = null
      searchResult.value = null
      error.value = ''
    }

    // 选择比对图片1
    const selectCompareImage1 = async () => {
      try {
        const base64 = await selectFromGallery()
        compareImage1.value = base64
        compareResult.value = null
      } catch (err) {
        showError('选择图片失败: ' + err.message)
      }
    }

    // 选择比对图片2
    const selectCompareImage2 = async () => {
      try {
        const base64 = await selectFromGallery()
        compareImage2.value = base64
        compareResult.value = null
      } catch (err) {
        showError('选择图片失败: ' + err.message)
      }
    }

    // 人脸检测
    const handleDetect = async () => {
      if (!currentImage.value) {
        showError('请先选择图片')
        return
      }

      loading.value = true
      loadingText.value = '正在检测人脸...'
      error.value = ''

      try {
        const response = await detectFace(currentImage.value, false, 0.5)
        if (response.data.code === 200 && response.data.data.success) {
          detectResult.value = response.data.data
        } else {
          showError(response.data.message || '检测失败')
        }
      } catch (err) {
        showError('检测失败: ' + (err.response?.data?.message || err.message))
      } finally {
        loading.value = false
      }
    }

    // 人脸比对
    const handleCompare = async () => {
      if (!compareImage1.value || !compareImage2.value) {
        showError('请选择两张图片')
        return
      }

      loading.value = true
      loadingText.value = '正在比对人脸...'
      error.value = ''

      try {
        const response = await compareFace(compareImage1.value, compareImage2.value, 0.62)
        if (response.data.code === 200 && response.data.data.success) {
          compareResult.value = response.data.data
        } else {
          showError(response.data.message || '比对失败')
        }
      } catch (err) {
        showError('比对失败: ' + (err.response?.data?.message || err.message))
      } finally {
        loading.value = false
      }
    }

    // 人脸注册
    const handleRegister = async () => {
      if (!currentImage.value) {
        showError('请先选择图片')
        return
      }

      if (!registerUserId.value || !registerUserName.value) {
        showError('请填写用户ID和用户名称')
        return
      }

      loading.value = true
      loadingText.value = '正在注册人脸...'
      error.value = ''

      try {
        const metadata = JSON.stringify({
          userId: registerUserId.value,
          userName: registerUserName.value
        })
        
        const response = await registerFace(
          currentImage.value,
          registerUserId.value,
          registerUserName.value,
          null,
          metadata
        )
        
        if (response.data.code === 200 && response.data.data.success) {
          registerResult.value = response.data.data
          registerUserId.value = ''
          registerUserName.value = ''
          // 刷新列表
          loadFaceList()
        } else {
          showError(response.data.message || '注册失败')
        }
      } catch (err) {
        showError('注册失败: ' + (err.response?.data?.message || err.message))
      } finally {
        loading.value = false
      }
    }

    // 人脸搜索
    const handleSearch = async () => {
      if (!currentImage.value) {
        showError('请先选择图片')
        return
      }

      loading.value = true
      loadingText.value = '正在搜索人脸...'
      error.value = ''

      try {
        const response = await searchFace(currentImage.value, 5, null)
        if (response.data.code === 200 && response.data.data.success) {
          searchResult.value = response.data.data
        } else {
          showError(response.data.message || '搜索失败')
        }
      } catch (err) {
        showError('搜索失败: ' + (err.response?.data?.message || err.message))
      } finally {
        loading.value = false
      }
    }

    // 加载人脸列表
    const loadFaceList = async () => {
      loading.value = true
      loadingText.value = '正在加载列表...'
      error.value = ''

      try {
        const response = await listFaces(1, 100)
        if (response.data.code === 200 && response.data.data.success) {
          faceList.value = response.data.data.data || []
        } else {
          showError(response.data.message || '加载失败')
        }
      } catch (err) {
        showError('加载失败: ' + (err.response?.data?.message || err.message))
      } finally {
        loading.value = false
      }
    }

    // 删除人脸
    const handleDelete = async (faceId) => {
      if (!confirm('确定要删除这个人脸吗？')) {
        return
      }

      loading.value = true
      loadingText.value = '正在删除...'
      error.value = ''

      try {
        const response = await deleteFace(faceId)
        if (response.data.code === 200 && response.data.data.success) {
          // 刷新列表
          loadFaceList()
        } else {
          showError(response.data.message || '删除失败')
        }
      } catch (err) {
        showError('删除失败: ' + (err.response?.data?.message || err.message))
      } finally {
        loading.value = false
      }
    }

    // 格式化元数据
    const formatMetadata = (metadataStr) => {
      try {
        const metadata = JSON.parse(metadataStr)
        return Object.entries(metadata)
          .map(([key, value]) => `${key}: ${value}`)
          .join(', ')
      } catch {
        return metadataStr
      }
    }

    // 显示错误
    const showError = (message) => {
      error.value = message
      setTimeout(() => {
        error.value = ''
      }, 5000)
    }

    // 初始化
    onMounted(() => {
      loadFaceList()
    })

    return {
      activeTab,
      tabs,
      currentImage,
      compareImage1,
      compareImage2,
      loading,
      loadingText,
      error,
      detectResult,
      compareResult,
      registerUserId,
      registerUserName,
      registerResult,
      searchResult,
      faceList,
      selectImage,
      captureImage,
      clearImage,
      selectCompareImage1,
      selectCompareImage2,
      handleDetect,
      handleCompare,
      handleRegister,
      handleSearch,
      loadFaceList,
      handleDelete,
      formatMetadata
    }
  }
}
</script>

<style scoped>
.face-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f5f5;
}

.header {
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #e0e0e0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.nav-tabs {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.nav-tab {
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 15px;
  cursor: pointer;
  color: #666;
  transition: all 0.2s;
}

.nav-tab.active {
  background: #007aff;
  color: #fff;
}

.nav-tab:not(.active):hover {
  background: #f0f0f0;
}

.tabs {
  display: flex;
  background: #fff;
  border-bottom: 1px solid #e0e0e0;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

.tab-item {
  flex: 1;
  min-width: 80px;
  padding: 12px 8px;
  text-align: center;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
  white-space: nowrap;
}

.tab-item.active {
  color: #007aff;
  border-bottom-color: #007aff;
  font-weight: 500;
}

.content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.upload-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.image-preview {
  position: relative;
  width: 100%;
  max-width: 400px;
  margin: 0 auto 16px;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f5f5;
}

.image-preview img {
  width: 100%;
  height: auto;
  display: block;
}

.btn-remove {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  border: none;
  font-size: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

.upload-placeholder {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.upload-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.upload-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.btn-upload {
  padding: 12px 24px;
  border: 2px dashed #ddd;
  border-radius: 8px;
  background: #f9f9f9;
  color: #666;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-upload:hover:not(:disabled) {
  border-color: #007aff;
  background: #f0f7ff;
  color: #007aff;
}

.btn-upload:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.action-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.action-group {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.form-group input {
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
}

.form-group input:focus {
  border-color: #007aff;
}

.btn-action {
  padding: 12px 24px;
  background: #007aff;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-action:hover:not(:disabled) {
  background: #0056b3;
}

.btn-action:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.compare-images {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.compare-image {
  flex: 1;
  min-height: 200px;
  border: 2px dashed #ddd;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f9f9f9;
  overflow: hidden;
}

.compare-image img {
  width: 100%;
  height: auto;
  max-height: 300px;
  object-fit: contain;
}

.btn-select {
  padding: 12px 24px;
  background: #f0f0f0;
  border: none;
  border-radius: 6px;
  color: #666;
  cursor: pointer;
}

.compare-divider {
  font-size: 18px;
  font-weight: bold;
  color: #999;
}

.result-box {
  margin-top: 16px;
  padding: 16px;
  background: #f9f9f9;
  border-radius: 8px;
  border-left: 4px solid #007aff;
}

.result-box.success {
  border-left-color: #4caf50;
  background: #f1f8f4;
}

.result-box h3 {
  margin: 0 0 12px 0;
  font-size: 16px;
  color: #333;
}

.face-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 12px;
}

.face-item {
  padding: 12px;
  background: #fff;
  border-radius: 6px;
  font-size: 14px;
}

.face-item p {
  margin: 4px 0;
  color: #666;
}

.similarity-result {
  text-align: center;
  padding: 20px;
}

.similarity-value {
  font-size: 48px;
  font-weight: bold;
  color: #007aff;
  margin-bottom: 8px;
}

.similarity-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
}

.similarity-status {
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  background: #f44336;
  color: #fff;
  display: inline-block;
}

.similarity-status.match {
  background: #4caf50;
}

.search-results {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 12px;
}

.search-item {
  padding: 12px;
  background: #fff;
  border-radius: 6px;
  border-left: 4px solid #007aff;
}

.search-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.search-rank {
  font-weight: bold;
  color: #007aff;
}

.search-similarity {
  font-size: 18px;
  font-weight: bold;
  color: #007aff;
}

.search-info {
  font-size: 14px;
  color: #666;
}

.search-info p {
  margin: 4px 0;
}

.metadata {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

.face-list-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
}

.face-list-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f9f9f9;
  border-radius: 6px;
}

.face-info {
  flex: 1;
}

.face-info p {
  margin: 4px 0;
  font-size: 14px;
  color: #666;
}

.btn-delete {
  padding: 6px 12px;
  background: #f44336;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
}

.btn-delete:hover {
  background: #d32f2f;
}

.empty-list {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  color: #fff;
}

.loading-spinner-large {
  width: 48px;
  height: 48px;
  border: 4px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 16px;
}

.error-message {
  position: fixed;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  background: #f44336;
  color: #fff;
  padding: 12px 20px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  z-index: 1001;
  max-width: 90%;
  display: flex;
  align-items: center;
  gap: 12px;
}

.error-message button {
  padding: 4px 8px;
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 移动端优化 */
@media (max-width: 768px) {
  .tabs {
    padding: 0 8px;
  }
  
  .tab-item {
    font-size: 13px;
    padding: 10px 6px;
  }
  
  .content {
    padding: 12px;
  }
  
  .upload-buttons {
    flex-direction: column;
  }
  
  .btn-upload {
    width: 100%;
    justify-content: center;
  }
  
  .compare-images {
    flex-direction: column;
  }
  
  .compare-divider {
    transform: rotate(90deg);
  }
}
</style>

