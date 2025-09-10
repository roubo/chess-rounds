<template>
  <view class="container">
    <view class="card">
      <view class="card-title">Toast样式测试</view>
      
      <view class="test-buttons">
        <button class="btn-primary test-btn" @click="testSuccess">成功提示</button>
        <button class="btn-primary test-btn" @click="testError">错误提示</button>
        <button class="btn-primary test-btn" @click="testWarning">警告提示</button>
        <button class="btn-primary test-btn" @click="testInfo">信息提示</button>
        <button class="btn-primary test-btn" @click="testText">纯文本提示</button>
        <button class="btn-secondary test-btn" @click="testSystemToast">系统Toast对比</button>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'TestToast',
  methods: {
    testSuccess() {
      this.showSuccess('操作成功！这是一个成功提示')
    },
    
    testError() {
      this.showError('操作失败！这是一个错误提示')
    },
    
    testWarning() {
      this.showWarning('注意！这是一个警告提示')
    },
    
    testInfo() {
      this.showInfo('提示信息：这是一个普通信息提示')
    },
    
    testText() {
      this.showText('这是一个无图标的纯文本提示')
    },
    
    testSystemToast() {
      uni.showToast({
        title: '这是系统原生Toast',
        icon: 'success',
        duration: 2000
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.test-buttons {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.test-btn {
  width: 100%;
  margin: 0;
  padding: 24rpx;
  font-size: 28rpx;
}

.btn-secondary.test-btn {
  background-color: #f5f5f5;
  color: #666;
  border: 2rpx solid #ddd;
}
</style>