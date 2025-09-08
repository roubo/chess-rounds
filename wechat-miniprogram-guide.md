# 微信小程序配置指南

## 概述

本指南详细说明"回合"微信小程序的注册、配置、开发和发布流程，确保小程序能够顺利上线并符合微信平台的各项要求。

## 第一步：小程序注册

### 1.1 注册微信小程序账号

1. 访问 [微信公众平台](https://mp.weixin.qq.com/)
2. 点击"立即注册" → 选择"小程序"
3. 填写账号信息：
   - 邮箱：使用未注册过微信公众平台的邮箱
   - 密码：设置强密码
   - 确认邮箱并激活账号

### 1.2 完善小程序信息

1. **基本信息设置**：
   - 小程序名称："回合"
   - 小程序头像：设计符合品牌的头像
   - 小程序介绍："简洁美观的棋牌计数记录工具"
   - 服务类别：选择"工具 > 效率"

2. **开发者信息**：
   - 主体类型：个人/企业
   - 主体信息：按实际情况填写
   - 管理员信息：设置管理员微信号

### 1.3 获取小程序信息

在"开发 > 开发管理 > 开发设置"中获取：
- **AppID**：小程序唯一标识
- **AppSecret**：小程序密钥（用于服务端API调用）

```
AppID: wx402b5a6e5f74462a
AppSecret: 1234567890abcdef1234567890abcdef
```

## 第二步：开发者工具配置

### 2.1 下载微信开发者工具

1. 访问 [微信开发者工具官网](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)
2. 下载对应操作系统版本
3. 安装并使用微信扫码登录

### 2.2 创建项目

1. 打开微信开发者工具
2. 新建项目：
   - 项目名称：回合
   - 目录：选择UniApp构建后的目录 `dist/build/mp-weixin`
   - AppID：填入获取的AppID
   - 开发模式：小程序

## 第三步：服务器域名配置

### 3.1 配置服务器域名

在"开发 > 开发管理 > 开发设置 > 服务器域名"中配置：

```
request合法域名：
https://api.airoubo.com
https://mp.airoubo.com

uploadFile合法域名：
https://api.airoubo.com

downloadFile合法域名：
https://api.airoubo.com

socket合法域名：
wss://api.airoubo.com
```

### 3.2 业务域名配置

如需在小程序中打开网页，需配置业务域名：

```
业务域名：
https://mp.airoubo.com
```

**注意**：需要在域名根目录放置校验文件 `MP_verify_xxx.txt`

## 第四步：接口权限配置

### 4.1 接口设置

在"开发 > 接口设置"中开启需要的接口权限：

- [x] getUserProfile（获取用户信息）
- [x] getLocation（获取地理位置）
- [x] chooseImage（选择图片）
- [x] previewImage（预览图片）
- [x] getNetworkType（获取网络状态）
- [x] onNetworkStatusChange（监听网络状态）
- [x] showShareMenu（转发功能）

### 4.2 用户隐私保护指引

配置用户隐私保护指引，说明收集的用户信息：

```json
{
  "requiredPrivateInfos": [
    {
      "type": "chooseAddress",
      "desc": "用于记录对局地点信息"
    },
    {
      "type": "getLocation",
      "desc": "用于显示附近的对局信息"
    }
  ],
  "optionalPrivateInfos": [
    {
      "type": "choosePoi",
      "desc": "用于选择对局地点"
    }
  ]
}
```

## 第五步：小程序代码配置

### 5.1 app.json 配置

```json
{
  "pages": [
    "pages/index/index",
    "pages/rounds/rounds",
    "pages/profile/profile",
    "pages/round-detail/round-detail",
    "pages/spectate/spectate"
  ],
  "tabBar": {
    "color": "#999999",
    "selectedColor": "#5D688A",
    "backgroundColor": "#FFF2EF",
    "borderStyle": "white",
    "list": [
      {
        "pagePath": "pages/rounds/rounds",
        "text": "回合",
        "iconPath": "static/icons/rounds.png",
        "selectedIconPath": "static/icons/rounds-active.png"
      },
      {
        "pagePath": "pages/profile/profile",
        "text": "我的",
        "iconPath": "static/icons/profile.png",
        "selectedIconPath": "static/icons/profile-active.png"
      }
    ]
  },
  "window": {
    "backgroundTextStyle": "light",
    "navigationBarBackgroundColor": "#5D688A",
    "navigationBarTitleText": "回合",
    "navigationBarTextStyle": "white",
    "backgroundColor": "#FFF2EF"
  },
  "permission": {
    "scope.userLocation": {
      "desc": "你的位置信息将用于显示附近的对局"
    }
  },
  "requiredPrivateInfos": [
    "getLocation",
    "chooseAddress"
  ],
  "lazyCodeLoading": "requiredComponents"
}
```

### 5.2 sitemap.json 配置

```json
{
  "desc": "关于本文件的更多信息，请参考文档 https://developers.weixin.qq.com/miniprogram/dev/framework/sitemap.html",
  "rules": [
    {
      "action": "allow",
      "page": "pages/index/index"
    },
    {
      "action": "allow",
      "page": "pages/rounds/rounds"
    },
    {
      "action": "disallow",
      "page": "pages/profile/profile"
    },
    {
      "action": "allow",
      "page": "pages/spectate/spectate"
    }
  ]
}
```

### 5.3 project.config.json 配置

```json
{
  "description": "回合计数记录小程序",
  "packOptions": {
    "ignore": [
      {
        "type": "file",
        "value": ".eslintrc.js"
      },
      {
        "type": "file",
        "value": ".gitignore"
      }
    ]
  },
  "setting": {
    "urlCheck": true,
    "es6": true,
    "enhance": true,
    "postcss": true,
    "preloadBackgroundData": false,
    "minified": true,
    "newFeature": false,
    "coverView": true,
    "nodeModules": false,
    "autoAudits": false,
    "showShadowRootInWxmlPanel": true,
    "scopeDataCheck": false,
    "uglifyFileName": false,
    "checkInvalidKey": true,
    "checkSiteMap": true,
    "uploadWithSourceMap": true,
    "compileHotReLoad": false,
    "lazyloadPlaceholderEnable": false,
    "useMultiFrameRuntime": true,
    "useApiHook": true,
    "useApiHostProcess": true,
    "babelSetting": {
      "ignore": [],
      "disablePlugins": [],
      "outputPath": ""
    },
    "enableEngineNative": false,
    "useIsolateContext": false,
    "userConfirmedBundleSwitch": false,
    "packNpmManually": false,
    "packNpmRelationList": [],
    "minifyWXSS": true,
    "disableUseStrict": false,
    "minifyWXML": true,
    "showES6CompileOption": false,
    "useCompilerPlugins": false
  },
  "compileType": "miniprogram",
  "libVersion": "2.19.4",
  "appid": "wx402b5a6e5f74462a",
  "projectname": "chess-rounds",
  "debugOptions": {
    "hidedInDevtools": []
  },
  "scripts": {},
  "staticServerOptions": {
    "baseURL": "",
    "servePath": ""
  },
  "isGameTourist": false,
  "condition": {
    "search": {
      "list": []
    },
    "conversation": {
      "list": []
    },
    "game": {
      "list": []
    },
    "plugin": {
      "list": []
    },
    "gamePlugin": {
      "list": []
    },
    "miniprogram": {
      "list": []
    }
  }
}
```

## 第六步：微信登录配置

### 6.1 登录流程配置

在小程序中实现微信登录：

```javascript
// utils/auth.js
class AuthService {
  async login() {
    try {
      // 1. 获取微信授权码
      const { code } = await wx.login()
      
      // 2. 发送到后端换取token
      const response = await this.request({
        url: '/api/v1/auth/wechat/login',
        method: 'POST',
        data: { code }
      })
      
      // 3. 保存token
      wx.setStorageSync('token', response.data.token)
      wx.setStorageSync('refreshToken', response.data.refreshToken)
      
      return response.data.user
    } catch (error) {
      console.error('登录失败:', error)
      throw error
    }
  }
  
  async getUserProfile() {
    try {
      const { userInfo } = await wx.getUserProfile({
        desc: '用于完善用户资料'
      })
      return userInfo
    } catch (error) {
      console.error('获取用户信息失败:', error)
      return null
    }
  }
}

export default new AuthService()
```

### 6.2 后端微信API配置

在Spring Boot后端配置微信API：

```yaml
# application.yml
app:
  wechat:
    app-id: wx402b5a6e5f74462a
    app-secret: 1234567890abcdef1234567890abcdef
    api-url: https://api.weixin.qq.com
```

## 第七步：版本管理和发布

### 7.1 版本号规范

采用语义化版本号：`主版本号.次版本号.修订号`

- **主版本号**：不兼容的API修改
- **次版本号**：向下兼容的功能性新增
- **修订号**：向下兼容的问题修正

示例：
- `1.0.0`：首次发布版本
- `1.1.0`：新增旁观功能
- `1.1.1`：修复登录问题

### 7.2 开发版本管理

1. **开发版**：开发者上传的版本，用于开发测试
2. **体验版**：选择某个开发版作为体验版，供内部测试
3. **正式版**：通过审核的版本，用户可以搜索到

### 7.3 上传代码流程

1. **本地构建**：
   ```bash
   # 构建UniApp项目
   npm run build:mp-weixin
   ```

2. **开发者工具上传**：
   - 打开微信开发者工具
   - 点击"上传"按钮
   - 填写版本号和项目备注
   - 确认上传

3. **后台管理**：
   - 登录微信公众平台
   - 进入"管理 > 版本管理"
   - 查看上传的版本

## 第八步：小程序审核

### 8.1 提交审核前检查

**功能完整性检查**：
- [x] 微信登录功能正常
- [x] 主要功能页面完整
- [x] 数据加载和错误处理
- [x] 页面跳转和返回
- [x] 分享功能正常

**合规性检查**：
- [x] 用户协议和隐私政策
- [x] 内容合规（无违规内容）
- [x] 功能描述准确
- [x] 服务类目匹配

**技术规范检查**：
- [x] 页面加载速度
- [x] 内存使用合理
- [x] 网络请求优化
- [x] 兼容性测试

### 8.2 提交审核

1. **选择版本**：在版本管理中选择要提审的版本
2. **填写审核信息**：
   - 版本描述：详细说明本版本的功能和改进
   - 测试账号：提供测试用的微信号（如需要）
   - 补充说明：特殊功能的使用说明

3. **审核时间**：通常1-7个工作日

### 8.3 审核结果处理

**审核通过**：
- 收到审核通过通知
- 可以选择立即发布或定时发布
- 发布后用户可以搜索到小程序

**审核被拒**：
- 查看拒绝原因
- 根据反馈修改代码
- 重新提交审核

## 第九步：运营配置

### 9.1 搜索优化

在"推广 > 自定义关键词"中设置：

```
关键词：
- 棋牌
- 计数
- 记录
- 回合
- 游戏统计
```

### 9.2 附近的小程序

开启"附近的小程序"功能：
1. 进入"推广 > 附近的小程序"
2. 添加地点信息
3. 等待审核通过

### 9.3 小程序码生成

```javascript
// 生成小程序码的API调用
const generateQRCode = async (scene, page) => {
  const response = await fetch('https://api.weixin.qq.com/wxa/getwxacodeunlimit', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      scene: scene,           // 自定义参数
      page: page,            // 页面路径
      width: 430,            // 二维码宽度
      auto_color: false,     // 自动配置线条颜色
      line_color: {          // 线条颜色
        r: 93,
        g: 104, 
        b: 138
      }
    })
  })
  
  return response.blob()
}
```

## 第十步：数据统计和分析

### 10.1 小程序数据助手

关注"小程序数据助手"公众号，获取：
- 用户访问数据
- 用户行为分析
- 性能监控数据
- 错误日志统计

### 10.2 自定义数据统计

```javascript
// 自定义事件统计
wx.reportAnalytics('round_created', {
  multiplier: 1.5,
  participant_count: 4,
  has_table_board: true
})

// 页面访问统计
wx.reportAnalytics('page_view', {
  page_name: 'round_detail',
  round_id: 'round-uuid'
})
```

## 第十一步：版本更新策略

### 11.1 热更新机制

小程序支持热更新，用户下次打开时自动更新：

```javascript
// app.js
App({
  onLaunch() {
    // 检查更新
    this.checkForUpdate()
  },
  
  checkForUpdate() {
    const updateManager = wx.getUpdateManager()
    
    updateManager.onCheckForUpdate((res) => {
      console.log('检查更新结果:', res.hasUpdate)
    })
    
    updateManager.onUpdateReady(() => {
      wx.showModal({
        title: '更新提示',
        content: '新版本已经准备好，是否重启应用？',
        success: (res) => {
          if (res.confirm) {
            updateManager.applyUpdate()
          }
        }
      })
    })
    
    updateManager.onUpdateFailed(() => {
      console.error('更新失败')
    })
  }
})
```

### 11.2 兼容性处理

```javascript
// 版本兼容性检查
const checkCompatibility = () => {
  const systemInfo = wx.getSystemInfoSync()
  const SDKVersion = systemInfo.SDKVersion
  
  // 检查基础库版本
  if (compareVersion(SDKVersion, '2.10.0') < 0) {
    wx.showModal({
      title: '提示',
      content: '当前微信版本过低，无法使用该功能，请升级到最新微信版本后重试。'
    })
    return false
  }
  
  return true
}

// 版本比较函数
const compareVersion = (v1, v2) => {
  const v1Arr = v1.split('.')
  const v2Arr = v2.split('.')
  const len = Math.max(v1Arr.length, v2Arr.length)
  
  while (v1Arr.length < len) {
    v1Arr.push('0')
  }
  while (v2Arr.length < len) {
    v2Arr.push('0')
  }
  
  for (let i = 0; i < len; i++) {
    const num1 = parseInt(v1Arr[i])
    const num2 = parseInt(v2Arr[i])
    
    if (num1 > num2) {
      return 1
    } else if (num1 < num2) {
      return -1
    }
  }
  
  return 0
}
```

## 第十二步：性能优化

### 12.1 代码包优化

```javascript
// 分包配置
// app.json
{
  "pages": [
    "pages/index/index",
    "pages/rounds/rounds",
    "pages/profile/profile"
  ],
  "subPackages": [
    {
      "root": "pages/statistics",
      "name": "statistics",
      "pages": [
        "monthly-chart/monthly-chart",
        "partners/partners",
        "opponents/opponents"
      ]
    },
    {
      "root": "pages/spectate",
      "name": "spectate", 
      "pages": [
        "spectate/spectate",
        "share/share"
      ]
    }
  ],
  "preloadRule": {
    "pages/rounds/rounds": {
      "network": "all",
      "packages": ["statistics"]
    }
  }
}
```

### 12.2 图片优化

```javascript
// 图片懒加载
Component({
  data: {
    imageSrc: '',
    loading: true
  },
  
  methods: {
    onImageLoad() {
      this.setData({ loading: false })
    },
    
    onImageError() {
      this.setData({ 
        imageSrc: '/static/images/default-avatar.png',
        loading: false 
      })
    }
  }
})
```

### 12.3 数据缓存策略

```javascript
// 数据缓存管理
class CacheManager {
  static set(key, data, expireTime = 30 * 60 * 1000) {
    const cacheData = {
      data: data,
      timestamp: Date.now(),
      expireTime: expireTime
    }
    wx.setStorageSync(key, cacheData)
  }
  
  static get(key) {
    try {
      const cacheData = wx.getStorageSync(key)
      if (!cacheData) return null
      
      const now = Date.now()
      if (now - cacheData.timestamp > cacheData.expireTime) {
        wx.removeStorageSync(key)
        return null
      }
      
      return cacheData.data
    } catch (error) {
      return null
    }
  }
  
  static clear(key) {
    wx.removeStorageSync(key)
  }
}
```

## 常见问题和解决方案

### Q1: 服务器域名配置失败

**问题**：提示"不在以下 request 合法域名列表中"

**解决方案**：
1. 检查域名是否已在小程序后台配置
2. 确认域名必须是HTTPS
3. 域名不能使用IP地址
4. 检查域名是否可以正常访问

### Q2: 微信登录失败

**问题**：调用wx.login()返回错误

**解决方案**：
1. 检查AppID是否正确
2. 确认小程序是否已发布或设为体验版
3. 检查后端接口是否正常
4. 验证AppSecret配置

### Q3: 审核被拒绝

**常见拒绝原因**：
1. 功能与描述不符
2. 存在违规内容
3. 用户体验问题
4. 缺少必要的用户协议

**解决方案**：
1. 仔细阅读拒绝原因
2. 根据反馈修改相应内容
3. 完善功能描述和截图
4. 添加用户协议和隐私政策

### Q4: 性能问题

**问题**：小程序运行缓慢或卡顿

**解决方案**：
1. 优化图片大小和格式
2. 减少不必要的网络请求
3. 使用分包加载
4. 优化数据结构和算法

这个微信小程序配置指南提供了从注册到发布的完整流程，包括：
- 详细的注册和配置步骤
- 代码配置示例
- 审核和发布流程
- 性能优化建议
- 常见问题解决方案

按照这个指南，可以顺利完成微信小程序的配置和发布。