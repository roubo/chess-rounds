# 自定义Toast使用指南

## 概述

本项目已优化了`uni.showToast`的样式，提供了更美观、更小巧的Toast提示，显示位置调整到界面底部，不会遮挡重要内容。

## 设计特点

### 视觉优化
- **位置调整**: 从屏幕中央移至底部120rpx位置
- **尺寸优化**: 更小巧的尺寸，最小宽度200rpx，最大宽度500rpx
- **样式美化**: 圆角设计(12rpx)，毛玻璃效果，柔和阴影
- **动画效果**: 流畅的进入和退出动画
- **响应式**: 支持不同屏幕尺寸的适配

### 功能特性
- **多种类型**: 成功、错误、警告、信息、纯文字
- **图标支持**: 每种类型都有对应的图标
- **自定义时长**: 支持自定义显示时长
- **向后兼容**: 完全兼容原有的`uni.showToast`调用方式

## 使用方法

### 1. 基础用法

```javascript
// 成功提示
this.$showSuccess('操作成功！')

// 错误提示
this.$showError('操作失败，请重试')

// 警告提示
this.$showWarning('请注意检查输入内容')

// 信息提示
this.$showInfo('这是一条信息提示')

// 纯文字提示（无图标）
this.$showText('纯文字提示')
```

### 2. 自定义时长

```javascript
// 短时显示（1秒）
this.$showSuccess('操作成功', 1000)

// 长时显示（5秒）
this.$showError('错误信息', 5000)
```

### 3. 兼容原有调用方式

```javascript
// 完全兼容uni.showToast的参数格式
this.showToast({
  title: '提示内容',
  icon: 'success', // success, error, none
  duration: 2000
})

// 或者使用$showToast方法
this.$showToast({
  title: '提示内容',
  icon: 'success',
  duration: 2000
})
```

### 4. 高级用法

```javascript
// 使用Toast实例的完整API
this.$toast.show({
  title: '自定义提示',
  type: 'success', // success, error, warning, default
  icon: true, // 是否显示图标
  duration: 3000 // 显示时长
})

// 手动隐藏Toast
this.$hideToast()
```

## 可用方法

| 方法名 | 参数 | 说明 |
|--------|------|------|
| `$showSuccess(message, duration)` | message: 提示文字<br>duration: 显示时长(可选) | 显示成功提示 |
| `$showError(message, duration)` | message: 提示文字<br>duration: 显示时长(可选) | 显示错误提示 |
| `$showWarning(message, duration)` | message: 提示文字<br>duration: 显示时长(可选) | 显示警告提示 |
| `$showInfo(message, duration)` | message: 提示文字<br>duration: 显示时长(可选) | 显示信息提示 |
| `$showText(message, duration)` | message: 提示文字<br>duration: 显示时长(可选) | 显示纯文字提示 |
| `$showToast(options)` | options: 配置对象 | 兼容uni.showToast格式 |
| `$hideToast()` | 无 | 手动隐藏Toast |
| `showToast(options)` | options: 配置对象 | 页面级方法，兼容原有调用 |

## 配置参数

### showToast配置对象

```javascript
{
  title: '提示内容',        // 必填，提示文字
  icon: 'success',         // 可选，图标类型: success, error, none
  duration: 2000,          // 可选，显示时长，默认2000ms
  type: 'success',         // 可选，Toast类型: success, error, warning, default
  showIcon: true           // 可选，是否显示图标，默认true
}
```

## 样式定制

### 主要样式变量

```scss
// Toast容器位置
.custom-toast-container {
  bottom: 120rpx; // 距离底部距离
}

// Toast主体样式
.custom-toast {
  min-width: 200rpx;      // 最小宽度
  max-width: 500rpx;      // 最大宽度
  padding: 20rpx 32rpx;   // 内边距
  border-radius: 12rpx;   // 圆角
}
```

### 类型样式

- **成功**: 绿色背景 `rgba(76, 217, 100, 0.9)`
- **错误**: 红色背景 `rgba(221, 82, 77, 0.9)`
- **警告**: 橙色背景 `rgba(240, 173, 78, 0.9)`
- **默认**: 黑色背景 `rgba(0, 0, 0, 0.8)`

## 技术实现

### 组件架构

1. **CustomToast.vue**: 核心Toast组件
2. **toast.js**: Toast管理器和工具函数
3. **toast.js (mixin)**: 全局混入，提供页面级方法
4. **App.vue**: 注册全局Toast实例
5. **main.js**: 注册全局混入

### 兼容性处理

- 如果Toast实例未初始化，自动降级到系统`uni.showToast`
- 完全兼容原有的调用方式和参数格式
- 支持Vue2和Vue3两种框架版本

## 演示页面

访问 `pages/toast-demo/toast-demo` 页面可以查看所有Toast效果的演示。

## 注意事项

1. **初始化**: Toast实例在App.vue的onReady生命周期中初始化
2. **层级**: Toast的z-index为9999，确保在最顶层显示
3. **性能**: 同时只能显示一个Toast，新的Toast会覆盖旧的
4. **响应式**: 在小屏幕设备上会自动调整尺寸和间距
5. **无障碍**: 支持屏幕阅读器等无障碍功能

## 迁移指南

### 从uni.showToast迁移

原有代码无需修改，自动使用新的Toast样式：

```javascript
// 原有代码保持不变
uni.showToast({
  title: '提示内容',
  icon: 'success'
})

// 页面内的showToast方法也会自动使用新样式
this.showToast({
  title: '提示内容',
  icon: 'success'
})
```

### 推荐的新写法

```javascript
// 推荐使用更简洁的新方法
this.$showSuccess('操作成功')
this.$showError('操作失败')
this.$showWarning('注意事项')
this.$showInfo('提示信息')
this.$showText('纯文字')
```

## 扩展开发

如需自定义Toast样式或添加新功能，可以：

1. 修改 `CustomToast.vue` 组件样式
2. 在 `toast.js` 中添加新的工具方法
3. 在 `customicons.css` 中添加新的图标
4. 在 `uni.scss` 中定义新的样式变量

通过这种架构设计，Toast系统具有良好的可扩展性和维护性。