/**
 * 自定义Toast工具类
 * 用于替代uni.showToast，提供更美观的样式和底部显示位置
 */

class ToastManager {
  constructor() {
    this.currentToast = null
    this.timer = null
  }
  
  // 创建Toast元素
  createToastElement(options) {
    const { title, type = 'default', icon = true, duration = 2000 } = options
    
    // 创建容器
    const container = document.createElement('div')
    container.className = 'custom-toast-container'
    
    // 创建Toast主体
    const toast = document.createElement('div')
    toast.className = `custom-toast toast-${type}`
    
    // 添加图标
    if (icon) {
      const iconElement = document.createElement('div')
      iconElement.className = 'toast-icon'
      const iconText = document.createElement('text')
      iconText.className = `customicons ${this.getIconClass(type)}`
      iconElement.appendChild(iconText)
      toast.appendChild(iconElement)
    }
    
    // 添加文本
    const textElement = document.createElement('text')
    textElement.className = 'toast-text'
    textElement.textContent = title
    toast.appendChild(textElement)
    
    container.appendChild(toast)
    return container
  }
  
  // 获取图标类名
  getIconClass(type) {
    const iconMap = {
      success: 'icon-success',
      error: 'icon-error',
      warning: 'icon-warning',
      default: 'icon-info'
    }
    return iconMap[type] || iconMap.default
  }
  
  // 显示Toast
  show(options) {
    // 如果有正在显示的Toast，先隐藏
    if (this.currentToast) {
      this.hide()
    }
    
    const { duration = 2000 } = options
    
    // 在H5环境下使用DOM操作
    // #ifdef H5
    try {
      const toastElement = this.createToastElement(options)
      document.body.appendChild(toastElement)
      this.currentToast = toastElement
      
      // 添加显示动画
      setTimeout(() => {
        toastElement.classList.add('toast-show')
      }, 10)
      
      // 设置自动隐藏
      this.timer = setTimeout(() => {
        this.hide()
      }, duration)
      
      return Promise.resolve()
    } catch (error) {
      console.warn('自定义Toast创建失败，使用系统Toast:', error)
      return this.fallbackToSystemToast(options)
    }
    // #endif
    
    // 小程序环境暂时注释掉toast功能
    // #ifndef H5
    // return this.fallbackToSystemToast(options)
    return Promise.resolve()
    // #endif
  }
  
  // 降级到系统Toast
  fallbackToSystemToast(options) {
    const { title, type, icon, duration = 2000 } = options
    let systemIcon = 'none'
    
    if (icon && type === 'success') {
      systemIcon = 'success'
    } else if (icon && type === 'error') {
      systemIcon = 'error'
    }
    
    return uni.showToast({
      title: title || '',
      icon: systemIcon,
      duration
    })
  }
  
  // 成功提示
  success(message, duration = 2000) {
    return this.show({
      title: message,
      type: 'success',
      icon: true,
      duration
    })
  }
  
  // 错误提示
  error(message, duration = 2000) {
    return this.show({
      title: message,
      type: 'error',
      icon: true,
      duration
    })
  }
  
  // 警告提示
  warning(message, duration = 2000) {
    return this.show({
      title: message,
      type: 'warning',
      icon: true,
      duration
    })
  }
  
  // 普通提示
  info(message, duration = 2000) {
    return this.show({
      title: message,
      type: 'default',
      icon: true,
      duration
    })
  }
  
  // 无图标提示
  text(message, duration = 2000) {
    return this.show({
      title: message,
      type: 'default',
      icon: false,
      duration
    })
  }
  
  // 兼容uni.showToast的参数格式
  showToast(options) {
    const { title, icon = 'success', duration = 1500, ...rest } = options
    
    let type = 'default'
    let showIcon = true
    
    // 根据icon参数确定类型
    if (icon === 'success') {
      type = 'success'
    } else if (icon === 'error') {
      type = 'error'
    } else if (icon === 'none') {
      showIcon = false
    }
    
    return this.show({
      title,
      type,
      icon: showIcon,
      duration,
      ...rest
    })
  }
  
  // 隐藏Toast
  hide() {
    if (this.timer) {
      clearTimeout(this.timer)
      this.timer = null
    }
    
    if (this.currentToast) {
      // #ifdef H5
      try {
        // 添加隐藏动画
        this.currentToast.classList.add('toast-hide')
        
        // 动画完成后移除元素
        setTimeout(() => {
          if (this.currentToast && this.currentToast.parentNode) {
            this.currentToast.parentNode.removeChild(this.currentToast)
          }
          this.currentToast = null
        }, 300)
      } catch (error) {
        console.warn('隐藏Toast时出错:', error)
        this.currentToast = null
      }
      // #endif
      
      // #ifndef H5
      // uni.hideToast()
      this.currentToast = null
      // #endif
    }
  }
}

// 创建全局实例
const toast = new ToastManager()

// 导出实例和类
export default toast
export { ToastManager }

// 全局方法，可以直接使用
export const showToast = (options) => toast.showToast(options)
export const showSuccess = (message, duration) => toast.success(message, duration)
export const showError = (message, duration) => toast.error(message, duration)
export const showWarning = (message, duration) => toast.warning(message, duration)
export const showInfo = (message, duration) => toast.info(message, duration)
export const showText = (message, duration) => toast.text(message, duration)
export const hideToast = () => toast.hide()