# UniApp前端设计文档

## 项目概述

基于UniApp框架开发的"回合"微信小程序，提供棋牌游戏回合记录和数据统计功能。

## 设计规范

### 配色方案

```css
/* 主色调 */
:root {
  --primary-color: #5D688A;      /* 主色 - 深蓝灰 */
  --secondary-color: #F7A5A5;    /* 辅助色 - 粉红 */
  --accent-color: #FFDBB6;       /* 强调色 - 浅橙 */
  --background-color: #FFF2EF;   /* 背景色 - 浅粉白 */
  
  /* 扩展色彩 */
  --primary-light: #7A8AA8;      /* 主色浅色 */
  --primary-dark: #4A5670;       /* 主色深色 */
  --secondary-light: #F9C0C0;    /* 辅助色浅色 */
  --secondary-dark: #F58A8A;     /* 辅助色深色 */
  
  /* 功能色彩 */
  --success-color: #67C23A;
  --warning-color: #E6A23C;
  --error-color: #F56C6C;
  --info-color: #909399;
  
  /* 文字颜色 */
  --text-primary: #303133;
  --text-regular: #606266;
  --text-secondary: #909399;
  --text-placeholder: #C0C4CC;
  
  /* 边框颜色 */
  --border-light: #EBEEF5;
  --border-base: #DCDFE6;
  --border-dark: #C0C4CC;
}
```

### 字体规范

```css
/* 字体大小 */
.text-xs { font-size: 20rpx; }    /* 10px */
.text-sm { font-size: 24rpx; }    /* 12px */
.text-base { font-size: 28rpx; }  /* 14px */
.text-lg { font-size: 32rpx; }    /* 16px */
.text-xl { font-size: 36rpx; }    /* 18px */
.text-2xl { font-size: 40rpx; }   /* 20px */
.text-3xl { font-size: 48rpx; }   /* 24px */

/* 字体粗细 */
.font-light { font-weight: 300; }
.font-normal { font-weight: 400; }
.font-medium { font-weight: 500; }
.font-semibold { font-weight: 600; }
.font-bold { font-weight: 700; }
```

### 间距规范

```css
/* 内边距和外边距 */
.p-1 { padding: 8rpx; }
.p-2 { padding: 16rpx; }
.p-3 { padding: 24rpx; }
.p-4 { padding: 32rpx; }
.p-5 { padding: 40rpx; }

.m-1 { margin: 8rpx; }
.m-2 { margin: 16rpx; }
.m-3 { margin: 24rpx; }
.m-4 { margin: 32rpx; }
.m-5 { margin: 40rpx; }
```

## 项目结构

```
chess-rounds-miniprogram/
├── pages/
│   ├── index/
│   │   ├── index.vue
│   │   └── index.scss
│   ├── rounds/
│   │   ├── rounds.vue
│   │   ├── rounds.scss
│   │   ├── create/
│   │   │   ├── create.vue
│   │   │   └── create.scss
│   │   ├── detail/
│   │   │   ├── detail.vue
│   │   │   └── detail.scss
│   │   ├── record/
│   │   │   ├── record.vue
│   │   │   └── record.scss
│   │   └── spectate/
│   │       ├── spectate.vue
│   │       └── spectate.scss
│   ├── profile/
│   │   ├── profile.vue
│   │   ├── profile.scss
│   │   ├── statistics/
│   │   │   ├── statistics.vue
│   │   │   └── statistics.scss
│   │   └── relationships/
│   │       ├── relationships.vue
│   │       └── relationships.scss
│   └── login/
│       ├── login.vue
│       └── login.scss
├── components/
│   ├── common/
│   │   ├── NavBar/
│   │   │   ├── NavBar.vue
│   │   │   └── NavBar.scss
│   │   ├── TabBar/
│   │   │   ├── TabBar.vue
│   │   │   └── TabBar.scss
│   │   ├── Loading/
│   │   │   ├── Loading.vue
│   │   │   └── Loading.scss
│   │   └── Empty/
│   │       ├── Empty.vue
│   │       └── Empty.scss
│   ├── rounds/
│   │   ├── RoundCard/
│   │   │   ├── RoundCard.vue
│   │   │   └── RoundCard.scss
│   │   ├── PlayerList/
│   │   │   ├── PlayerList.vue
│   │   │   └── PlayerList.scss
│   │   ├── RecordItem/
│   │   │   ├── RecordItem.vue
│   │   │   └── RecordItem.scss
│   │   └── RoundAnalysis/
│   │       ├── RoundAnalysis.vue
│   │       └── RoundAnalysis.scss
│   ├── profile/
│   │   ├── StatCard/
│   │   │   ├── StatCard.vue
│   │   │   └── StatCard.scss
│   │   ├── Chart/
│   │   │   ├── Chart.vue
│   │   │   └── Chart.scss
│   │   └── RelationshipCard/
│   │       ├── RelationshipCard.vue
│   │       └── RelationshipCard.scss
│   └── ui/
│       ├── Button/
│       │   ├── Button.vue
│       │   └── Button.scss
│       ├── Input/
│       │   ├── Input.vue
│       │   └── Input.scss
│       ├── Modal/
│       │   ├── Modal.vue
│       │   └── Modal.scss
│       └── Toast/
│           ├── Toast.vue
│           └── Toast.scss
├── static/
│   ├── images/
│   │   ├── icons/
│   │   ├── avatars/
│   │   └── backgrounds/
│   └── fonts/
├── utils/
│   ├── request.js
│   ├── auth.js
│   ├── storage.js
│   ├── wechat.js
│   ├── format.js
│   └── constants.js
├── store/
│   ├── index.js
│   ├── modules/
│   │   ├── user.js
│   │   ├── rounds.js
│   │   └── app.js
├── styles/
│   ├── common.scss
│   ├── variables.scss
│   ├── mixins.scss
│   └── reset.scss
├── manifest.json
├── pages.json
├── App.vue
├── main.js
├── uni.scss
└── package.json
```

## 页面设计

### 1. 主页面结构 (TabBar)

```json
{
  "tabBar": {
    "color": "#909399",
    "selectedColor": "#5D688A",
    "backgroundColor": "#FFF2EF",
    "borderStyle": "white",
    "list": [
      {
        "pagePath": "pages/rounds/rounds",
        "text": "回合",
        "iconPath": "static/images/icons/rounds.png",
        "selectedIconPath": "static/images/icons/rounds-active.png"
      },
      {
        "pagePath": "pages/profile/profile",
        "text": "我的",
        "iconPath": "static/images/icons/profile.png",
        "selectedIconPath": "static/images/icons/profile-active.png"
      }
    ]
  }
}
```

### 2. 回合页面 (pages/rounds/rounds.vue)

#### 页面布局

```vue
<template>
  <view class="rounds-page">
    <!-- 顶部导航 -->
    <nav-bar title="回合" :show-back="false">
      <template #right>
        <view class="nav-actions">
          <button class="scan-btn" @click="scanQRCode">
            <image src="/static/images/icons/scan.png" class="scan-icon" />
          </button>
          <button class="create-btn" @click="createRound">
            <image src="/static/images/icons/add.png" class="add-icon" />
          </button>
        </view>
      </template>
    </nav-bar>
    
    <!-- 当前回合 -->
    <view class="current-round" v-if="currentRound">
      <round-card 
        :round="currentRound" 
        :is-current="true"
        @click="enterRound"
      />
      
      <!-- 回合分析 -->
      <round-analysis 
        :analysis="currentRound.analysis"
        :participants="currentRound.participants"
      />
    </view>
    
    <!-- 历史回合 -->
    <view class="history-rounds">
      <view class="section-title">历史回合</view>
      <view class="round-list">
        <round-card 
          v-for="round in historyRounds" 
          :key="round.id"
          :round="round"
          @click="viewRound"
        />
      </view>
    </view>
    
    <!-- 空状态 -->
    <empty 
      v-if="!currentRound && historyRounds.length === 0"
      title="暂无回合记录"
      description="扫描二维码加入回合或创建新回合"
    >
      <button class="create-first-btn" @click="createRound">
        创建第一个回合
      </button>
    </empty>
  </view>
</template>
```

#### 样式设计

```scss
.rounds-page {
  min-height: 100vh;
  background: var(--background-color);
  
  .nav-actions {
    display: flex;
    align-items: center;
    gap: 16rpx;
    
    .scan-btn, .create-btn {
      width: 60rpx;
      height: 60rpx;
      border-radius: 50%;
      background: var(--primary-color);
      display: flex;
      align-items: center;
      justify-content: center;
      
      .scan-icon, .add-icon {
        width: 32rpx;
        height: 32rpx;
      }
    }
  }
  
  .current-round {
    padding: 32rpx;
    
    .round-analysis {
      margin-top: 24rpx;
    }
  }
  
  .history-rounds {
    padding: 0 32rpx;
    
    .section-title {
      font-size: 32rpx;
      font-weight: 600;
      color: var(--text-primary);
      margin-bottom: 24rpx;
    }
    
    .round-list {
      display: flex;
      flex-direction: column;
      gap: 16rpx;
    }
  }
}
```

### 3. 我的页面 (pages/profile/profile.vue)

#### 页面布局

```vue
<template>
  <view class="profile-page">
    <!-- 用户信息 -->
    <view class="user-info">
      <image :src="userInfo.avatarUrl" class="avatar" />
      <view class="user-details">
        <text class="nickname">{{ userInfo.nickname }}</text>
        <text class="join-date">加入时间：{{ formatDate(userInfo.createdAt) }}</text>
      </view>
    </view>
    
    <!-- 快速统计 -->
    <view class="quick-stats">
      <stat-card 
        title="今日"
        :data="todayStats"
        color="primary"
      />
      <stat-card 
        title="本月"
        :data="monthStats"
        color="secondary"
      />
      <stat-card 
        title="本年"
        :data="yearStats"
        color="accent"
      />
    </view>
    
    <!-- 图表区域 -->
    <view class="chart-section">
      <view class="section-title">数据趋势</view>
      <chart :data="chartData" type="line" />
    </view>
    
    <!-- 关系统计 -->
    <view class="relationships">
      <view class="section-title">游戏伙伴</view>
      
      <!-- 最佳合伙人 -->
      <view class="relationship-group">
        <view class="group-title">最佳合伙人</view>
        <relationship-card 
          v-for="partner in bestPartners" 
          :key="partner.id"
          :relationship="partner"
          type="partner"
        />
      </view>
      
      <!-- 最强对手 -->
      <view class="relationship-group">
        <view class="group-title">最强对手</view>
        <relationship-card 
          v-for="opponent in strongestOpponents" 
          :key="opponent.id"
          :relationship="opponent"
          type="opponent"
        />
      </view>
    </view>
  </view>
</template>
```

### 4. 回合详情页面 (pages/rounds/detail/detail.vue)

#### 页面布局

```vue
<template>
  <view class="round-detail">
    <!-- 回合信息 -->
    <view class="round-header">
      <view class="round-info">
        <text class="round-title">{{ round.title || '回合 #' + round.roundCode }}</text>
        <text class="round-code">回合码：{{ round.roundCode }}</text>
        <view class="round-meta">
          <text class="multiplier">倍率：{{ round.multiplier }}x</text>
          <text class="status" :class="round.status">{{ getStatusText(round.status) }}</text>
        </view>
      </view>
      
      <!-- 分享按钮 -->
      <button class="share-btn" @click="shareRound" v-if="canShare">
        <image src="/static/images/icons/share.png" class="share-icon" />
      </button>
    </view>
    
    <!-- 参与者列表 -->
    <view class="participants">
      <view class="section-title">参与者</view>
      <player-list 
        :players="round.participants"
        :current-user-id="currentUserId"
        :can-rate="isSpectator"
        @rate="handleRate"
      />
    </view>
    
    <!-- 记录列表 -->
    <view class="records">
      <view class="section-header">
        <text class="section-title">游戏记录</text>
        <button 
          class="add-record-btn" 
          @click="addRecord"
          v-if="canAddRecord"
        >
          添加记录
        </button>
      </view>
      
      <view class="record-list">
        <record-item 
          v-for="record in round.records" 
          :key="record.id"
          :record="record"
        />
      </view>
    </view>
    
    <!-- AI分析 -->
    <view class="ai-analysis" v-if="round.aiAnalysis">
      <view class="section-title">AI分析</view>
      <view class="analysis-content">
        {{ round.aiAnalysis }}
      </view>
    </view>
  </view>
</template>
```

## 组件设计

### 1. RoundCard 组件

```vue
<template>
  <view class="round-card" :class="{ 'is-current': isCurrent }" @click="$emit('click')">
    <view class="card-header">
      <view class="round-info">
        <text class="round-title">{{ round.title || '回合 #' + round.roundCode }}</text>
        <text class="round-time">{{ formatTime(round.createdAt) }}</text>
      </view>
      <view class="round-status" :class="round.status">
        {{ getStatusText(round.status) }}
      </view>
    </view>
    
    <view class="card-body">
      <view class="participants-preview">
        <image 
          v-for="participant in round.participants.slice(0, 4)" 
          :key="participant.id"
          :src="participant.avatarUrl"
          class="participant-avatar"
        />
        <text class="participant-count" v-if="round.participants.length > 4">
          +{{ round.participants.length - 4 }}
        </text>
      </view>
      
      <view class="round-stats">
        <view class="stat-item">
          <text class="stat-label">局数</text>
          <text class="stat-value">{{ round.roundCount }}</text>
        </view>
        <view class="stat-item">
          <text class="stat-label">倍率</text>
          <text class="stat-value">{{ round.multiplier }}x</text>
        </view>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.round-card {
  background: white;
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 4rpx 12rpx rgba(93, 104, 138, 0.1);
  transition: all 0.3s ease;
  
  &.is-current {
    border: 2rpx solid var(--primary-color);
    box-shadow: 0 8rpx 24rpx rgba(93, 104, 138, 0.2);
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 16rpx;
    
    .round-info {
      flex: 1;
      
      .round-title {
        font-size: 32rpx;
        font-weight: 600;
        color: var(--text-primary);
        display: block;
        margin-bottom: 8rpx;
      }
      
      .round-time {
        font-size: 24rpx;
        color: var(--text-secondary);
      }
    }
    
    .round-status {
      padding: 8rpx 16rpx;
      border-radius: 20rpx;
      font-size: 24rpx;
      
      &.waiting {
        background: var(--accent-color);
        color: var(--primary-color);
      }
      
      &.playing {
        background: var(--secondary-color);
        color: white;
      }
      
      &.finished {
        background: var(--primary-color);
        color: white;
      }
    }
  }
  
  .card-body {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .participants-preview {
      display: flex;
      align-items: center;
      
      .participant-avatar {
        width: 48rpx;
        height: 48rpx;
        border-radius: 50%;
        margin-right: -8rpx;
        border: 2rpx solid white;
      }
      
      .participant-count {
        margin-left: 16rpx;
        font-size: 24rpx;
        color: var(--text-secondary);
      }
    }
    
    .round-stats {
      display: flex;
      gap: 24rpx;
      
      .stat-item {
        text-align: center;
        
        .stat-label {
          font-size: 20rpx;
          color: var(--text-secondary);
          display: block;
        }
        
        .stat-value {
          font-size: 28rpx;
          font-weight: 600;
          color: var(--text-primary);
        }
      }
    }
  }
}
</style>
```

### 2. StatCard 组件

```vue
<template>
  <view class="stat-card" :class="color">
    <view class="card-header">
      <text class="card-title">{{ title }}</text>
      <image :src="getIconPath()" class="card-icon" />
    </view>
    
    <view class="card-body">
      <view class="main-stat">
        <text class="stat-value">{{ data.totalAmount || 0 }}</text>
        <text class="stat-unit">元</text>
      </view>
      
      <view class="sub-stats">
        <view class="sub-stat">
          <text class="sub-label">回合数</text>
          <text class="sub-value">{{ data.totalRounds || 0 }}</text>
        </view>
        <view class="sub-stat">
          <text class="sub-label">胜率</text>
          <text class="sub-value">{{ getWinRate() }}%</text>
        </view>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.stat-card {
  background: white;
  border-radius: 16rpx;
  padding: 24rpx;
  flex: 1;
  
  &.primary {
    background: linear-gradient(135deg, var(--primary-color), var(--primary-light));
    color: white;
  }
  
  &.secondary {
    background: linear-gradient(135deg, var(--secondary-color), var(--secondary-light));
    color: white;
  }
  
  &.accent {
    background: linear-gradient(135deg, var(--accent-color), #FFE4B8);
    color: var(--primary-color);
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16rpx;
    
    .card-title {
      font-size: 28rpx;
      font-weight: 500;
    }
    
    .card-icon {
      width: 32rpx;
      height: 32rpx;
      opacity: 0.8;
    }
  }
  
  .card-body {
    .main-stat {
      display: flex;
      align-items: baseline;
      margin-bottom: 16rpx;
      
      .stat-value {
        font-size: 48rpx;
        font-weight: 700;
        line-height: 1;
      }
      
      .stat-unit {
        font-size: 24rpx;
        margin-left: 8rpx;
        opacity: 0.8;
      }
    }
    
    .sub-stats {
      display: flex;
      justify-content: space-between;
      
      .sub-stat {
        text-align: center;
        
        .sub-label {
          font-size: 20rpx;
          opacity: 0.8;
          display: block;
        }
        
        .sub-value {
          font-size: 24rpx;
          font-weight: 600;
        }
      }
    }
  }
}
</style>
```

## 工具函数

### 1. 请求封装 (utils/request.js)

```javascript
const BASE_URL = 'https://api.airoubo.com'

class Request {
  constructor() {
    this.baseURL = BASE_URL
    this.timeout = 10000
  }
  
  // 请求拦截器
  interceptors = {
    request: (config) => {
      // 添加token
      const token = uni.getStorageSync('token')
      if (token) {
        config.header = {
          ...config.header,
          'Authorization': `Bearer ${token}`
        }
      }
      
      // 添加公共header
      config.header = {
        'Content-Type': 'application/json',
        ...config.header
      }
      
      return config
    },
    
    response: (response) => {
      const { data, statusCode } = response
      
      // 处理HTTP状态码
      if (statusCode >= 200 && statusCode < 300) {
        // 处理业务状态码
        if (data.code === 200) {
          return data.data
        } else if (data.code === 401) {
          // token过期，跳转登录
          uni.removeStorageSync('token')
          uni.removeStorageSync('userInfo')
          uni.reLaunch({
            url: '/pages/login/login'
          })
          return Promise.reject(new Error('登录已过期'))
        } else {
          uni.showToast({
            title: data.message || '请求失败',
            icon: 'none'
          })
          return Promise.reject(new Error(data.message))
        }
      } else {
        uni.showToast({
          title: '网络错误',
          icon: 'none'
        })
        return Promise.reject(new Error('网络错误'))
      }
    }
  }
  
  request(options) {
    return new Promise((resolve, reject) => {
      // 应用请求拦截器
      const config = this.interceptors.request({
        url: this.baseURL + options.url,
        method: options.method || 'GET',
        data: options.data,
        header: options.header || {},
        timeout: this.timeout
      })
      
      uni.request({
        ...config,
        success: (response) => {
          try {
            const result = this.interceptors.response(response)
            resolve(result)
          } catch (error) {
            reject(error)
          }
        },
        fail: (error) => {
          uni.showToast({
            title: '网络连接失败',
            icon: 'none'
          })
          reject(error)
        }
      })
    })
  }
  
  get(url, params) {
    return this.request({
      url: params ? `${url}?${this.buildQuery(params)}` : url,
      method: 'GET'
    })
  }
  
  post(url, data) {
    return this.request({
      url,
      method: 'POST',
      data
    })
  }
  
  put(url, data) {
    return this.request({
      url,
      method: 'PUT',
      data
    })
  }
  
  delete(url) {
    return this.request({
      url,
      method: 'DELETE'
    })
  }
  
  buildQuery(params) {
    return Object.keys(params)
      .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
      .join('&')
  }
}

export default new Request()
```

### 2. 微信API封装 (utils/wechat.js)

```javascript
export class WechatAPI {
  // 微信登录
  static login() {
    return new Promise((resolve, reject) => {
      uni.login({
        provider: 'weixin',
        success: (loginRes) => {
          resolve(loginRes.code)
        },
        fail: reject
      })
    })
  }
  
  // 获取用户信息
  static getUserProfile() {
    return new Promise((resolve, reject) => {
      uni.getUserProfile({
        desc: '用于完善用户资料',
        success: resolve,
        fail: reject
      })
    })
  }
  
  // 扫码
  static scanCode() {
    return new Promise((resolve, reject) => {
      uni.scanCode({
        success: (res) => {
          resolve(res.result)
        },
        fail: reject
      })
    })
  }
  
  // 分享
  static share(options) {
    return uni.share({
      provider: 'weixin',
      scene: 'WXSceneSession',
      type: 0,
      href: options.path,
      title: options.title,
      summary: options.desc,
      imageUrl: options.imageUrl
    })
  }
  
  // 显示分享菜单
  static showShareMenu() {
    uni.showShareMenu({
      withShareTicket: true,
      menus: ['shareAppMessage', 'shareTimeline']
    })
  }
}
```

## 状态管理

### Vuex Store 结构

```javascript
// store/index.js
import { createStore } from 'vuex'
import user from './modules/user'
import rounds from './modules/rounds'
import app from './modules/app'

export default createStore({
  modules: {
    user,
    rounds,
    app
  }
})

// store/modules/user.js
export default {
  namespaced: true,
  
  state: {
    userInfo: null,
    token: null,
    isLoggedIn: false
  },
  
  mutations: {
    SET_USER_INFO(state, userInfo) {
      state.userInfo = userInfo
    },
    
    SET_TOKEN(state, token) {
      state.token = token
      state.isLoggedIn = !!token
    },
    
    CLEAR_USER_DATA(state) {
      state.userInfo = null
      state.token = null
      state.isLoggedIn = false
    }
  },
  
  actions: {
    async login({ commit }, { code, userInfo }) {
      try {
        const response = await request.post('/auth/login', {
          code,
          userInfo
        })
        
        commit('SET_TOKEN', response.token)
        commit('SET_USER_INFO', response.userInfo)
        
        // 持久化存储
        uni.setStorageSync('token', response.token)
        uni.setStorageSync('userInfo', response.userInfo)
        
        return response
      } catch (error) {
        throw error
      }
    },
    
    logout({ commit }) {
      commit('CLEAR_USER_DATA')
      uni.removeStorageSync('token')
      uni.removeStorageSync('userInfo')
    }
  }
}
```

## 性能优化

### 1. 图片优化

- 使用WebP格式
- 实现图片懒加载
- 压缩图片资源

### 2. 代码分割

- 按页面分包
- 组件按需加载

### 3. 缓存策略

- 接口数据缓存
- 图片缓存
- 静态资源缓存

## 测试策略

### 1. 单元测试

- 工具函数测试
- 组件测试

### 2. 集成测试

- 页面流程测试
- API接口测试

### 3. 真机测试

- 不同设备适配
- 性能测试
- 网络环境测试

这个前端设计文档提供了：
- 完整的UI设计规范
- 清晰的项目结构
- 详细的页面和组件设计
- 实用的工具函数
- 状态管理方案
- 性能优化建议

所有设计都严格遵循指定的配色方案，确保界面简洁美观，用户体验良好。