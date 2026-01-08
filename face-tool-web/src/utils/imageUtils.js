/**
 * 图片工具类
 */

/**
 * 将 File 对象转换为 Base64
 */
export function fileToBase64(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => {
      resolve(reader.result)
    }
    reader.onerror = reject
    reader.readAsDataURL(file)
  })
}

/**
 * 将图片 URL 转换为 Base64
 */
export function urlToBase64(url) {
  return new Promise((resolve, reject) => {
    const img = new Image()
    img.crossOrigin = 'anonymous'
    img.onload = () => {
      const canvas = document.createElement('canvas')
      canvas.width = img.width
      canvas.height = img.height
      const ctx = canvas.getContext('2d')
      ctx.drawImage(img, 0, 0)
      resolve(canvas.toDataURL('image/jpeg', 0.8))
    }
    img.onerror = reject
    img.src = url
  })
}

/**
 * 压缩图片
 */
export function compressImage(base64, maxWidth = 1920, maxHeight = 1920, quality = 0.8) {
  return new Promise((resolve, reject) => {
    const img = new Image()
    img.onload = () => {
      let width = img.width
      let height = img.height

      // 计算缩放比例
      if (width > maxWidth || height > maxHeight) {
        const ratio = Math.min(maxWidth / width, maxHeight / height)
        width = width * ratio
        height = height * ratio
      }

      const canvas = document.createElement('canvas')
      canvas.width = width
      canvas.height = height
      const ctx = canvas.getContext('2d')
      ctx.drawImage(img, 0, 0, width, height)
      
      resolve(canvas.toDataURL('image/jpeg', quality))
    }
    img.onerror = reject
    img.src = base64
  })
}

/**
 * 从相机拍照
 */
export function captureFromCamera() {
  return new Promise((resolve, reject) => {
    const input = document.createElement('input')
    input.type = 'file'
    input.accept = 'image/*'
    input.capture = 'environment' // 使用后置摄像头
    
    input.onchange = async (e) => {
      const file = e.target.files[0]
      if (file) {
        try {
          const base64 = await fileToBase64(file)
          // 压缩图片
          const compressed = await compressImage(base64, 1920, 1920, 0.8)
          resolve(compressed)
        } catch (error) {
          reject(error)
        }
      } else {
        reject(new Error('未选择文件'))
      }
    }
    
    input.click()
  })
}

/**
 * 从相册选择图片
 */
export function selectFromGallery() {
  return new Promise((resolve, reject) => {
    const input = document.createElement('input')
    input.type = 'file'
    input.accept = 'image/*'
    
    input.onchange = async (e) => {
      const file = e.target.files[0]
      if (file) {
        try {
          const base64 = await fileToBase64(file)
          // 压缩图片
          const compressed = await compressImage(base64, 1920, 1920, 0.8)
          resolve(compressed)
        } catch (error) {
          reject(error)
        }
      } else {
        reject(new Error('未选择文件'))
      }
    }
    
    input.click()
  })
}

