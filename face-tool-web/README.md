# Framework AI Web - H5 应用

基于 Vue 3 的 AI 应用 H5 前端，包含 AI 对话和人脸识别功能。

## 功能特性

### AI 对话功能
- ✅ 模型选择（DeepSeek / 阿里千问）
- ✅ WebSocket 实时对话
- ✅ 流式输出显示
- ✅ 上下文对话支持
- ✅ 移动端适配

### 人脸识别功能
- ✅ 人脸检测
- ✅ 人脸比对（1:1）
- ✅ 人脸注册
- ✅ 人脸搜索（1:N）
- ✅ 人脸列表管理
- ✅ 图片上传（拍照/相册）
- ✅ 移动端优化

## 技术栈

- Vue 3
- Vue Router 4
- Vite
- Axios
- SockJS + STOMP (WebSocket)
- 响应式设计

## 项目结构

```
framework-ai-web/
├── src/
│   ├── views/
│   │   ├── ChatPage.vue          # AI 对话页面
│   │   └── FaceRecognitionPage.vue  # 人脸识别页面
│   ├── api/
│   │   └── faceApi.js            # 人脸识别 API 封装
│   ├── utils/
│   │   └── imageUtils.js         # 图片处理工具
│   ├── router/
│   │   └── index.js               # 路由配置
│   ├── App.vue                    # 根组件
│   └── main.js                    # 入口文件
├── index.html
├── package.json
└── vite.config.js
```

## 开发

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览生产构建
npm run preview
```

## 配置

### API 地址配置

默认连接后端地址：`http://localhost:28089`

如需修改，请编辑 `vite.config.js` 中的 proxy 配置。

### 人脸识别配置

确保后端已正确配置 SmartJavaAI 模型路径（见 `FACE_RECOGNITION_GUIDE.md`）。

## 使用说明

### AI 对话

1. 选择模型（DeepSeek 或阿里千问）
2. 在输入框输入消息
3. 按 Enter 发送，Shift+Enter 换行
4. 点击"清空"按钮清空对话历史

### 人脸识别

#### 1. 人脸检测
- 选择或拍摄图片
- 点击"开始检测"
- 查看检测结果（人脸数量、位置、置信度）

#### 2. 人脸比对
- 选择两张图片
- 点击"开始比对"
- 查看相似度和比对结果

#### 3. 人脸注册
- 选择图片
- 填写用户ID和用户名称
- 点击"注册人脸"
- 系统会返回人脸ID

#### 4. 人脸搜索
- 选择图片
- 点击"开始搜索"
- 查看匹配结果（TopK个最相似的人脸）

#### 5. 人脸列表
- 查看所有已注册的人脸
- 可以删除已注册的人脸

## 图片上传

支持两种方式：
1. **选择图片**：从相册选择
2. **拍照**：使用相机拍摄（移动端）

图片会自动压缩以提升性能。

## 注意事项

1. **后端服务**：确保后端服务已启动（framework-ai-service）
2. **模型配置**：使用人脸识别功能前，需要配置模型路径
3. **网络连接**：确保前后端在同一网络或配置正确的代理
4. **移动端**：建议使用 HTTPS 连接（生产环境）
5. **图片大小**：建议图片不超过 5MB

## 移动端优化

- 响应式布局
- 触摸友好的按钮
- 优化的图片预览
- 适配不同屏幕尺寸

## 故障排查

### WebSocket 连接失败

1. 检查后端服务是否启动（端口 28089）
2. 检查前端代理配置（`vite.config.js`）
3. 查看浏览器控制台错误信息
4. 检查防火墙设置

### 人脸识别失败

1. 检查后端模型配置
2. 检查图片格式和大小
3. 查看浏览器控制台错误信息
4. 检查网络连接

## 相关文档

- [WebSocket 对话功能指南](../WEBSOCKET_CHAT_GUIDE.md)
- [人脸识别功能指南](../FACE_RECOGNITION_GUIDE.md)
- [认证安全指南](../AUTH_SECURITY_GUIDE.md)
