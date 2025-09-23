<template>
	<view class="login-page">
		<view class="login-container">
			<!-- Logo区域 -->
			<view class="logo-section">
				<view class="logo">
					<text class="logo-text">♟️</text>
				</view>
				<text class="app-name">回合</text>
				<text class="app-desc">一起享受回合乐趣</text>
			</view>
			
			<!-- 微信登录区域 -->
			<view class="login-section">
				<view class="login-desc">
					<text class="desc-text">使用微信账号登录，享受完整功能</text>
				</view>
				
				<!-- 微信登录按钮 -->
				<!-- #ifdef MP-WEIXIN -->
				<button class="wechat-btn" @click="wechatLogin" open-type="getUserInfo" @getuserinfo="onGetUserInfo">
					<text class="wechat-icon">💬</text>
					<text class="wechat-text">微信登录</text>
				</button>
				<!-- #endif -->
				
				<!-- 非微信环境提示 -->
				<!-- #ifndef MP-WEIXIN -->
				<view class="not-wechat-tip">
					<text class="tip-text">请在微信小程序中使用</text>
				</view>
				<!-- #endif -->
			</view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			// 页面数据
			redirectAfterLogin: '' // 登录成功后的重定向页面
		}
	},
	onLoad(options) {
		// 获取重定向参数
		if (options.redirect) {
			this.redirectAfterLogin = decodeURIComponent(options.redirect)
			console.log('登录成功后将重定向到:', this.redirectAfterLogin)
		} else if (options.redirectAfterLogin) {
			// 兼容旧参数名
			this.redirectAfterLogin = decodeURIComponent(options.redirectAfterLogin)
			console.log('登录成功后将重定向到:', this.redirectAfterLogin)
		}
	},
	methods: {
		async wechatLogin() {
			// #ifdef MP-WEIXIN
			try {
				uni.showLoading({
					title: '登录中...'
				})
				
				// 1. 获取微信登录code
				const code = await this.$wechat.login()
				console.log('获取到微信code:', code)
				
				// 2. 调用后端登录接口，只传递code
				const loginData = {
					code: code
				}
				
				const result = await this.$api.userApi.wechatLogin(loginData)
				console.log('登录结果:', result)
				
				// 4. 保存登录信息
				this.$auth.saveLoginInfo(result)
				
				// 验证token是否保存成功
				const savedToken = uni.getStorageSync('token')
				const savedUserInfo = uni.getStorageSync('userInfo')
				console.log('Token保存验证:', !!savedToken)
				console.log('用户信息保存验证:', !!savedUserInfo)
				
				if (!savedToken) {
					uni.hideLoading()
					// uni.showToast() - 已屏蔽
					return
				}
				
				uni.hideLoading()
				
				// 5. 检查用户信息是否需要完善
				const loginUserInfo = savedUserInfo || result.user_info || result.userInfo || result.user
				console.log('用户信息:', loginUserInfo)
				
				const isDefaultUser = !loginUserInfo || 
									  loginUserInfo.nickname === '微信用户' || 
									  loginUserInfo.avatarUrl === '/static/default-avatar.png' ||
									  loginUserInfo.avatar_url === '/static/default-avatar.png' ||
									  !loginUserInfo.avatarUrl && !loginUserInfo.avatar_url ||
									  (loginUserInfo.avatarUrl && loginUserInfo.avatarUrl.includes('default-avatar')) ||
									  (loginUserInfo.avatar_url && loginUserInfo.avatar_url.includes('default-avatar'))
				
				console.log('是否为默认用户:', isDefaultUser)
				
				if (isDefaultUser) {
					// uni.showToast() - 已屏蔽
					
					// 跳转到用户信息编辑页面，传递重定向参数
					setTimeout(() => {
						let editUrl = '/pages/profile/edit'
						if (this.redirectAfterLogin) {
							editUrl += '?redirectAfterProfile=' + encodeURIComponent(this.redirectAfterLogin)
						}
						uni.navigateTo({
							url: editUrl
						})
					}, 1500)
				} else {
					// uni.showToast() - 已屏蔽
					
					// 根据重定向参数跳转
					setTimeout(() => {
						if (this.redirectAfterLogin) {
							uni.navigateTo({
								url: this.redirectAfterLogin
							})
						} else {
							uni.switchTab({
								url: '/pages/rounds/rounds'
							})
						}
					}, 1500)
				}
				
			} catch (error) {
				uni.hideLoading()
				console.error('微信登录失败:', error)
				// uni.showToast() - 已屏蔽
			}
			// #endif
			
			// #ifndef MP-WEIXIN
			// uni.showToast() - 已屏蔽
			// #endif
		},
		onGetUserInfo(e) {
			// #ifdef MP-WEIXIN
			console.log('微信用户信息:', e.detail)
			// 处理微信登录逻辑
			// #endif
		}
	}
}
</script>

<style scoped>
.login-page {
	min-height: 100vh;
	background: linear-gradient(135deg, $chess-bg-secondary 0%, rgba(212, 175, 55, 0.3) 100%);
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 40rpx;
}

.login-container {
	width: 100%;
	max-width: 600rpx;
	background-color: $chess-bg-card;
	border-radius: $uni-border-radius-lg;
	padding: 60rpx 40rpx;
	box-shadow: 0 8rpx 32rpx rgba(212, 175, 55, 0.15);
}

.logo-section {
	display: flex;
	flex-direction: column;
	align-items: center;
	margin-bottom: 60rpx;
}

.logo {
	width: 120rpx;
	height: 120rpx;
	background: linear-gradient(135deg, $chess-color-gold, rgba(212, 175, 55, 0.8));
	border-radius: 60rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	margin-bottom: 24rpx;
	box-shadow: 0 4rpx 16rpx rgba(212, 175, 55, 0.2);
}

.logo-text {
	font-size: 60rpx;
	color: #fff;
}

.app-name {
	font-size: 48rpx;
	font-weight: bold;
	color: $chess-color-dark;
	margin-bottom: 12rpx;
}

.app-desc {
	font-size: 26rpx;
	color: $chess-color-muted;
	text-align: center;
}

.login-section {
	display: flex;
	flex-direction: column;
	align-items: center;
}

.login-desc {
	margin-bottom: 40rpx;
	text-align: center;
}

.desc-text {
	font-size: 28rpx;
	color: $chess-color-muted;
	line-height: 1.5;
}

.not-wechat-tip {
	margin-top: 40rpx;
	padding: 24rpx;
	background: rgba(255, 247, 230, 0.8);
	border-radius: $uni-border-radius-base;
	border: 1rpx solid rgba(212, 175, 55, 0.3);
	text-align: center;
}

.tip-text {
	font-size: 26rpx;
	color: $chess-color-warning;
}

.wechat-btn {
	width: 100%;
	height: 88rpx;
	background-color: #07C160;
	color: #FFFFFF;
	font-size: 28rpx;
	border-radius: 12rpx;
	border: none;
	display: flex;
	align-items: center;
	justify-content: center;
}

.wechat-icon {
	font-size: 32rpx;
	margin-right: 12rpx;
}

.wechat-text {
	font-size: 28rpx;
}
</style>