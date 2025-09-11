/**
 * Toast混入
 * 为所有页面提供自定义Toast方法，同时保持向后兼容
 */

import toast, { showToast, showSuccess, showError, showWarning, showInfo, showText, hideToast } from '@/utils/toast.js'

export default {
  methods: {
    // 自定义Toast方法
    $toast: showToast,
    $showToast: showToast,
    $showSuccess: showSuccess,
    $showError: showError,
    $showWarning: showWarning,
    $showInfo: showInfo,
    $showText: showText,
    $hideToast: hideToast,
    
    // 重写uni.showToast，使用自定义Toast
    showToast(options) {
      return showToast(options)
    },
    
    // 便捷方法
    showSuccess(message, duration) {
      return showSuccess(message, duration)
    },
    
    showError(message, duration) {
      return showError(message, duration)
    },
    
    showWarning(message, duration) {
      return showWarning(message, duration)
    },
    
    showInfo(message, duration) {
      return showInfo(message, duration)
    },
    
    showText(message, duration) {
      return showText(message, duration)
    },
    
    hideToast() {
      return hideToast()
    }
  }
}