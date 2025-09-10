<template>
  <view class="toast-demo">
    <view class="demo-header">
      <text class="demo-title">自定义Toast演示</text>
      <text class="demo-desc">优化后的Toast样式，显示在界面底部，更小更美观</text>
    </view>
    
    <view class="demo-content">
      <view class="demo-section">
        <text class="section-title">基础用法</text>
        <view class="button-group">
          <button class="demo-btn success" @click="showSuccessToast">成功提示</button>
          <button class="demo-btn error" @click="showErrorToast">错误提示</button>
          <button class="demo-btn warning" @click="showWarningToast">警告提示</button>
          <button class="demo-btn info" @click="showInfoToast">信息提示</button>
        </view>
      </view>
      
      <view class="demo-section">
        <text class="section-title">无图标提示</text>
        <view class="button-group">
          <button class="demo-btn default" @click="showTextToast">纯文字提示</button>
          <button class="demo-btn default" @click="showLongTextToast">长文字提示</button>
        </view>
      </view>
      
      <view class="demo-section">
        <text class="section-title">兼容性测试</text>
        <view class="button-group">
          <button class="demo-btn primary" @click="showCompatibleToast">兼容uni.showToast</button>
          <button class="demo-btn primary" @click="showSystemToast">系统原生Toast</button>
        </view>
      </view>
      
      <view class="demo-section">
        <text class="section-title">自定义时长</text>
        <view class="button-group">
          <button class="demo-btn secondary" @click="showShortToast">短时显示(1秒)</button>
          <button class="demo-btn secondary" @click="showLongToast">长时显示(5秒)</button>
        </view>
      </view>
    </view>
    
    <view class="demo-footer">
      <text class="footer-text">Toast显示在界面底部，不会遮挡重要内容</text>
    </view>
  </view>
</template>

<script>
export default {
  name: 'ToastDemo',
  methods: {
    showSuccessToast() {
      this.$showSuccess('操作成功！')
    },
    
    showErrorToast() {
      this.$showError('操作失败，请重试')
    },
    
    showWarningToast() {
      this.$showWarning('请注意检查输入内容')
    },
    
    showInfoToast() {
      this.$showInfo('这是一条信息提示')
    },
    
    showTextToast() {
      this.$showText('纯文字提示，无图标')
    },
    
    showLongTextToast() {
      this.$showText('这是一条比较长的文字提示，用于测试文字换行和显示效果')
    },
    
    showCompatibleToast() {
      // 使用兼容的showToast方法
      this.showToast({
        title: '兼容uni.showToast格式',
        icon: 'success',
        duration: 2000
      })
    },
    
    showSystemToast() {
      // 使用系统原生Toast作为对比
      uni.showToast({
        title: '系统原生Toast',
        icon: 'success',
        duration: 2000
      })
    },
    
    showShortToast() {
      this.$showSuccess('短时显示', 1000)
    },
    
    showLongToast() {
      this.$showInfo('长时显示，5秒后消失', 5000)
    }
  }
}
</script>

<style lang="scss" scoped>
.toast-demo {
  padding: 40rpx;
  background: $chess-bg-primary;
  min-height: 100vh;
}

.demo-header {
  text-align: center;
  margin-bottom: 60rpx;
  
  .demo-title {
    display: block;
    font-size: 48rpx;
    font-weight: bold;
    color: $chess-color-gold;
    margin-bottom: 20rpx;
  }
  
  .demo-desc {
    display: block;
    font-size: 28rpx;
    color: $uni-text-color-grey;
    line-height: 1.5;
  }
}

.demo-content {
  .demo-section {
    margin-bottom: 60rpx;
    
    .section-title {
      display: block;
      font-size: 32rpx;
      font-weight: 600;
      color: $uni-text-color;
      margin-bottom: 30rpx;
    }
    
    .button-group {
      display: flex;
      flex-wrap: wrap;
      gap: 20rpx;
    }
  }
}

.demo-btn {
  flex: 1;
  min-width: 200rpx;
  height: 80rpx;
  border-radius: 12rpx;
  font-size: 28rpx;
  border: none;
  color: white;
  
  &.success {
    background: $uni-color-success;
  }
  
  &.error {
    background: $uni-color-error;
  }
  
  &.warning {
    background: $uni-color-warning;
  }
  
  &.info {
    background: #17a2b8;
  }
  
  &.primary {
    background: $chess-color-gold;
  }
  
  &.secondary {
    background: #6c757d;
  }
  
  &.default {
    background: #495057;
  }
  
  &:active {
    opacity: 0.8;
    transform: scale(0.98);
  }
}

.demo-footer {
  margin-top: 80rpx;
  padding: 40rpx;
  background: $chess-bg-card;
  border-radius: 16rpx;
  text-align: center;
  
  .footer-text {
    font-size: 26rpx;
    color: $uni-text-color-grey;
    line-height: 1.6;
  }
}
</style>