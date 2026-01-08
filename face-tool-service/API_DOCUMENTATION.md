# 人脸识别服务 API 接口文档

## 概述

本文档描述人脸识别服务的所有API接口。所有接口均使用POST请求。

**基础URL**: `http://your-domain:28089`

**Content-Type**: `application/json`

---

## 目录

1. [Token管理接口](#token管理接口)
2. [人脸识别接口（API调用）](#人脸识别接口api调用)
   - [人脸检测](#1-人脸检测)
   - [人脸特征提取](#2-人脸特征提取)
   - [人脸比对](#3-人脸比对)
   - [人脸注册](#4-人脸注册)
   - [人脸搜索](#5-人脸搜索)
   - [人脸列表](#6-人脸列表)
   - [删除人脸](#7-删除人脸)
3. [测试接口（本地测试）](#测试接口本地测试)

---

## Token管理接口

### Token认证说明

**API接口**（`/api/face/*`）需要在请求头中携带Token：

```
Authorization: Bearer {your_token}
```

或者通过URL参数传递：

```
?token={your_token}
```

**测试接口**（`/api/test/face/*`）不需要Token验证，用于本地测试。

### 1. 申请Token

**接口地址**: `POST /api/token/apply`

**说明**: 第三方申请Token，申请后立即签发，无需审核

**请求参数**:

```json
{
  "clientName": "申请方名称",
  "contact": "联系方式（邮箱或手机号）",
  "purpose": "申请原因/用途",
  "validDays": 30  // 可选，有效期天数，默认30天
}
```

**请求示例**:

```bash
curl -X POST http://your-domain:28089/api/token/apply \
  -H "Content-Type: application/json" \
  -d '{
    "clientName": "测试公司",
    "contact": "test@example.com",
    "purpose": "用于人脸识别功能测试",
    "validDays": 30
  }'
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "FT-12345678-12345678-abcdef12",
    "clientName": "测试公司",
    "contact": "test@example.com",
    "purpose": "用于人脸识别功能测试",
    "status": "ACTIVE",
    "createTime": "2024-01-08T15:35:00",
    "expireTime": "2024-02-07T15:35:00",
    "lastUsedTime": "2024-01-08T15:35:00",
    "useCount": 0
  }
}
```

---

### 2. 获取所有Token列表（管理员）

**接口地址**: `POST /api/token/list`

**说明**: 查看所有已签发的Token

**请求参数**: 无

**请求示例**:

```bash
curl -X POST http://your-domain:28089/api/token/list \
  -H "Content-Type: application/json"
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "token": "FT-12345678-12345678-abcdef12",
      "clientName": "测试公司",
      "status": "ACTIVE",
      "createTime": "2024-01-08T15:35:00",
      "expireTime": "2024-02-07T15:35:00",
      "lastUsedTime": "2024-01-08T16:00:00",
      "useCount": 10
    }
  ]
}
```

---

### 5. 撤销Token（管理员）

**接口地址**: `POST /api/token/revoke`

**说明**: 撤销指定Token，撤销后该Token将无法使用

**请求参数**:

```json
{
  "token": "FT-12345678-12345678-abcdef12"
}
```

**请求示例**:

```bash
curl -X POST http://your-domain:28089/api/token/revoke \
  -H "Content-Type: application/json" \
  -d '{
    "token": "FT-12345678-12345678-abcdef12"
  }'
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "message": "Token已撤销"
  }
}
```

---

## 人脸识别接口（API调用）

所有人脸识别接口都需要Token认证。接口路径为 `/api/face/*`。

### 1. 人脸检测

**接口地址**: `POST /api/face/detect`

**说明**: 检测图片中的人脸位置和数量

**请求头**:

```
Authorization: Bearer {your_token}
Content-Type: application/json
```

**请求参数**:

```json
{
  "imageBase64": "base64编码的图片数据",
  "imageUrl": "图片URL（可选，与imageBase64二选一）",
  "drawBox": false,  // 是否绘制人脸框，默认false
  "confidenceThreshold": 0.5  // 置信度阈值（0-1），默认0.5
}
```

**请求示例**:

```bash
curl -X POST http://your-domain:28089/api/face/detect \
  -H "Authorization: Bearer FT-12345678-12345678-abcdef12" \
  -H "Content-Type: application/json" \
  -d '{
    "imageBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRg...",
    "drawBox": false,
    "confidenceThreshold": 0.5
  }'
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "success": true,
    "faceCount": 1,
    "data": {
      "detectionInfoList": [
        {
          "score": 0.98,
          "detectionRectangle": {
            "x": 100,
            "y": 150,
            "width": 200,
            "height": 250
          }
        }
      ]
    }
  }
}
```

---

### 2. 人脸特征提取

**接口地址**: `POST /api/face/extract`

**说明**: 提取人脸特征向量

**请求头**:

```
Authorization: Bearer {your_token}
Content-Type: application/json
```

**请求参数**:

```json
{
  "imageBase64": "base64编码的图片数据"
}
```

**请求示例**:

```bash
curl -X POST http://your-domain:28089/api/face/extract \
  -H "Authorization: Bearer FT-12345678-12345678-abcdef12" \
  -H "Content-Type: application/json" \
  -d '{
    "imageBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRg..."
  }'
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "success": true,
    "feature": [0.123, -0.456, 0.789, ...],
    "featureLength": 512
  }
}
```

---

### 3. 人脸比对

**接口地址**: `POST /api/face/compare`

**说明**: 比对两张图片是否为同一人（1:1比对）

**请求头**:

```
Authorization: Bearer {your_token}
Content-Type: application/json
```

**请求参数**:

```json
{
  "image1Base64": "第一张图片的base64编码",
  "image1Url": "第一张图片URL（可选）",
  "image2Base64": "第二张图片的base64编码",
  "image2Url": "第二张图片URL（可选）",
  "threshold": 0.62  // 相似度阈值，默认0.62
}
```

**请求示例**:

```bash
curl -X POST http://your-domain:28089/api/face/compare \
  -H "Authorization: Bearer FT-12345678-12345678-abcdef12" \
  -H "Content-Type: application/json" \
  -d '{
    "image1Base64": "data:image/jpeg;base64,/9j/4AAQSkZJRg...",
    "image2Base64": "data:image/jpeg;base64,/9j/4AAQSkZJRg...",
    "threshold": 0.62
  }'
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "success": true,
    "similarity": 0.85,
    "threshold": 0.62,
    "isSamePerson": true
  }
}
```

**字段说明**:
- `similarity`: 相似度分数（0-1），值越大越相似
- `isSamePerson`: 是否同一人（基于threshold判断）

---

### 4. 人脸注册

**接口地址**: `POST /api/face/register`

**说明**: 注册人脸到数据库，用于后续搜索

**请求头**:

```
Authorization: Bearer {your_token}
Content-Type: application/json
```

**请求参数**:

```json
{
  "imageBase64": "base64编码的图片数据",
  "imageUrl": "图片URL（可选）",
  "faceId": "自定义人脸ID（可选，不设置则自动生成）",
  "userId": "用户ID",
  "userName": "用户名称",
  "metadata": "{\"department\":\"技术部\",\"position\":\"工程师\"}"  // 可选，JSON格式的元数据
}
```

**请求示例**:

```bash
curl -X POST http://your-domain:28089/api/face/register \
  -H "Authorization: Bearer FT-12345678-12345678-abcdef12" \
  -H "Content-Type: application/json" \
  -d '{
    "imageBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRg...",
    "userId": "user001",
    "userName": "张三",
    "metadata": "{\"department\":\"技术部\"}"
  }'
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "success": true,
    "faceId": "face_1234567890"
  }
}
```

---

### 5. 人脸搜索

**接口地址**: `POST /api/face/search`

**说明**: 在已注册的人脸库中搜索匹配的人脸（1:N搜索）

**请求头**:

```
Authorization: Bearer {your_token}
Content-Type: application/json
```

**请求参数**:

```json
{
  "imageBase64": "base64编码的图片数据",
  "imageUrl": "图片URL（可选）",
  "topK": 5,  // 返回TopK个最相似的结果，默认1
  "threshold": 0.62  // 可选，相似度阈值，低于此值的结果不返回
}
```

**请求示例**:

```bash
curl -X POST http://your-domain:28089/api/face/search \
  -H "Authorization: Bearer FT-12345678-12345678-abcdef12" \
  -H "Content-Type: application/json" \
  -d '{
    "imageBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRg...",
    "topK": 5,
    "threshold": 0.62
  }'
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "success": true,
    "count": 2,
    "results": [...],
    "formattedResults": [
      {
        "faceId": "face_1234567890",
        "similarity": 0.92,
        "metadata": "{\"userId\":\"user001\",\"userName\":\"张三\"}"
      },
      {
        "faceId": "face_0987654321",
        "similarity": 0.75,
        "metadata": "{\"userId\":\"user002\",\"userName\":\"李四\"}"
      }
    ]
  }
}
```

---

### 6. 人脸列表

**接口地址**: `POST /api/face/list`

**说明**: 获取已注册的人脸列表（分页）

**请求头**:

```
Authorization: Bearer {your_token}
Content-Type: application/json
```

**请求参数**:

```json
{
  "page": 1,  // 页码，默认1
  "size": 10  // 每页大小，默认10
}
```

**请求示例**:

```bash
curl -X POST http://your-domain:28089/api/face/list \
  -H "Authorization: Bearer FT-12345678-12345678-abcdef12" \
  -H "Content-Type: application/json" \
  -d '{
    "page": 1,
    "size": 10
  }'
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "success": true,
    "data": [
      {
        "faceId": "face_1234567890",
        "metadata": "{\"userId\":\"user001\",\"userName\":\"张三\"}"
      },
      {
        "faceId": "face_0987654321",
        "metadata": "{\"userId\":\"user002\",\"userName\":\"李四\"}"
      }
    ],
    "total": 2,
    "page": 1,
    "size": 10
  }
}
```

---

### 7. 删除人脸

**接口地址**: `POST /api/face/delete`

**说明**: 删除已注册的人脸

**请求头**:

```
Authorization: Bearer {your_token}
Content-Type: application/json
```

**请求参数**:

```json
{
  "faceId": "face_1234567890"
}
```

**请求示例**:

```bash
curl -X POST http://your-domain:28089/api/face/delete \
  -H "Authorization: Bearer FT-12345678-12345678-abcdef12" \
  -H "Content-Type: application/json" \
  -d '{
    "faceId": "face_1234567890"
  }'
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "success": true,
    "message": "删除成功"
  }
}
```

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 400 | 参数错误 |
| 401 | Token无效或已过期 |
| 500 | 服务器内部错误 |

## 错误响应格式

```json
{
  "code": 401,
  "message": "Token无效或已过期，请重新申请",
  "data": null
}
```

## 注意事项

1. **Token认证**: 除Token管理接口外，所有接口都需要在请求头中携带Token
2. **图片格式**: 支持JPEG、PNG等常见图片格式
3. **Base64编码**: 图片Base64编码可以包含`data:image/jpeg;base64,`前缀，也可以不包含
4. **图片大小**: 建议图片不超过5MB
5. **Token有效期**: Token默认有效期为30天，过期后需要重新申请
6. **请求频率**: 建议控制请求频率，避免频繁调用

## 测试接口（本地测试）

测试接口路径为 `/api/test/face/*`，**不需要Token验证**，用于本地开发和测试。

所有测试接口的路径、请求参数和响应格式与API接口完全相同，只需将路径前缀从 `/api/face` 改为 `/api/test/face`。

### 测试接口列表

- `POST /api/test/face/detect` - 人脸检测
- `POST /api/test/face/extract` - 人脸特征提取
- `POST /api/test/face/compare` - 人脸比对
- `POST /api/test/face/register` - 人脸注册
- `POST /api/test/face/search` - 人脸搜索
- `POST /api/test/face/list` - 人脸列表
- `POST /api/test/face/delete` - 删除人脸

### 测试接口示例

```bash
# 人脸检测（测试接口，不需要Token）
curl -X POST http://localhost:28089/api/test/face/detect \
  -H "Content-Type: application/json" \
  -d '{
    "imageBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRg...",
    "drawBox": false,
    "confidenceThreshold": 0.5
  }'
```

---

## 使用流程

### API调用流程（第三方）

1. **申请Token**: 调用`/api/token/apply`接口，立即获得Token
2. **使用Token**: 在请求头中添加 `Authorization: Bearer {token}`
3. **调用接口**: 使用Token调用 `/api/face/*` 接口

### 本地测试流程

1. **直接调用**: 使用 `/api/test/face/*` 接口，无需Token
2. **开发调试**: 用于本地开发和功能测试

## 技术支持

如有问题，请联系技术支持团队。

