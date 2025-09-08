<template>
	<view class="profile-edit">
		<view class="header">
			<text class="title">完善个人信息</text>
			<text class="subtitle">请设置您的头像和昵称</text>
		</view>
		
		<view class="form-container">
			<!-- 头像设置 -->
			<view class="form-item">
				<text class="label">头像</text>
				<view class="avatar-section">
					<!-- 微信头像选择区域 -->
					<button 
						class="wechat-avatar-container" 
						open-type="chooseAvatar" 
						@chooseavatar="onChooseAvatar"
					>
						<image class="current-avatar" :src="currentAvatar" mode="aspectFill"></image>
						<view class="avatar-overlay">
							<view class="camera-icon">📷</view>
							<text class="change-text">点击更换</text>
						</view>
					</button>
					
					<!-- 其他选择方式 -->
					<view class="avatar-options">
						<text class="avatar-tip">点击头像获取微信头像</text>
						<view class="other-options">
							<button class="option-btn" @click="chooseImage(['album'])">
								<text class="option-text">📱 相册选择</text>
							</button>
							<button class="option-btn" @click="chooseImage(['camera'])">
								<text class="option-text">📸 拍照</text>
							</button>
						</view>
					</view>
				</view>
			</view>
			
			<!-- 昵称设置 -->
			<view class="form-item">
				<text class="label">昵称</text>
				<view class="nickname-section">
					<input 
						class="nickname-input" 
						v-model="currentNickname" 
						placeholder="请输入昵称"
						maxlength="20"
						type="nickname"
					/>
					<!-- 微信昵称获取提示 -->
					<text class="nickname-tip">可直接输入或通过微信授权获取</text>
				</view>
			</view>
		</view>
		
		<view class="button-container">
			<button class="save-btn" @click="saveProfile" :disabled="!canSave">
				保存
			</button>
			<button class="skip-btn" @click="skipProfile">
				跳过
			</button>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			currentAvatar: '/static/images/default-avatar.png',
			currentNickname: '微信用户',
			userInfo: null,
			redirectAfterProfile: '' // 完善信息后的重定向页面
		}
	},
	computed: {
		canSave() {
			return this.currentNickname.trim() !== '' && 
				   this.currentNickname !== '微信用户' && 
				   this.currentAvatar !== '/static/images/default-avatar.png'
		}
	},
	onLoad(options) {
		// 获取重定向参数
		if (options.redirectAfterProfile) {
			this.redirectAfterProfile = decodeURIComponent(options.redirectAfterProfile)
			console.log('完善信息后将重定向到:', this.redirectAfterProfile)
		}
		
		// 获取当前用户信息
		this.loadUserInfo()
	},
	methods: {
		loadUserInfo() {
			const userInfo = this.$auth.getUserInfo()
			if (userInfo) {
				this.userInfo = userInfo
				// 使用 getAvatarUrl 方法处理头像URL
				const avatarUrl = userInfo.avatarUrl || userInfo.avatar_url || userInfo.avatar
				this.currentAvatar = this.$auth.getAvatarUrl(avatarUrl)
				this.currentNickname = userInfo.nickname || '微信用户'
			}
		},
		

		
		// 微信头像选择
		async onChooseAvatar(e) {
			console.log('选择微信头像:', e.detail)
			const { avatarUrl } = e.detail
			if (avatarUrl && typeof avatarUrl === 'string') {
				// 立即上传微信临时头像，避免临时文件路径失效
				uni.showLoading({
					title: '上传头像中...'
				})
				
				try {
					const uploadResult = await this.$api.fileApi.uploadAvatar(avatarUrl)
					console.log('微信头像上传成功:', uploadResult)
					this.currentAvatar = this.$auth.getAvatarUrl(uploadResult.url)
					uni.hideLoading()
					uni.showToast({
						title: '头像已更新',
						icon: 'success'
					})
				} catch (uploadError) {
					console.error('微信头像上传失败:', uploadError)
					uni.hideLoading()
					uni.showToast({
						title: '头像上传失败: ' + uploadError.message,
						icon: 'none'
					})
					// 上传失败时使用默认头像
					this.currentAvatar = '/static/images/default-avatar.png'
				}
			} else {
				console.error('获取的avatarUrl类型不正确:', typeof avatarUrl, avatarUrl)
				uni.showToast({
					title: '头像获取失败，请重试',
					icon: 'none'
				})
			}
		},
		
		// 选择图片
		async chooseImage(sourceType = ['album', 'camera']) {
			uni.chooseImage({
				count: 1,
				sizeType: ['compressed'],
				sourceType: sourceType,
				success: async (res) => {
					const tempFilePath = res.tempFilePaths[0]
					
					// 立即上传选择的图片
					uni.showLoading({
						title: '上传头像中...'
					})
					
					try {
						const uploadResult = await this.$api.fileApi.uploadAvatar(tempFilePath)
						console.log('图片上传成功:', uploadResult)
						this.currentAvatar = this.$auth.getAvatarUrl(uploadResult.url)
						uni.hideLoading()
						uni.showToast({
							title: '头像已更新',
							icon: 'success'
						})
					} catch (uploadError) {
						console.error('图片上传失败:', uploadError)
						uni.hideLoading()
						uni.showToast({
							title: '头像上传失败: ' + uploadError.message,
							icon: 'none'
						})
						// 上传失败时使用默认头像
						this.currentAvatar = '/static/images/default-avatar.png'
					}
				},
				fail: (err) => {
					console.error('选择图片失败:', err)
					uni.showToast({
						title: '选择图片失败',
						icon: 'none'
					})
				}
			})
		},
		
		// 保存用户信息
		async saveProfile() {
			if (!this.canSave) {
				uni.showToast({
					title: '请完善头像和昵称',
					icon: 'none'
				})
				return
			}
			
			// 检查登录状态
			const token = uni.getStorageSync('token')
			const userInfo = uni.getStorageSync('userInfo')
			console.log('当前登录状态:', { hasToken: !!token, hasUserInfo: !!userInfo })
			console.log('Token:', token ? token.substring(0, 20) + '...' : 'null')
			console.log('UserInfo:', userInfo)
			
			// 验证token是否存在且有效
			if (!token || token.trim() === '') {
				uni.showModal({
					title: '提示',
					content: '请先登录后再保存个人信息',
					showCancel: false,
					success: () => {
						// 跳转到登录页面
						uni.reLaunch({
							url: '/pages/login/login'
						})
					}
				})
				return
			}
			
			// 验证用户信息是否存在
			console.log('验证用户信息:', userInfo)
			console.log('用户信息字段:', userInfo ? Object.keys(userInfo) : 'null')
			
			// 检查用户信息的多种可能字段
			const userId = userInfo && (userInfo.user_id || userInfo.userId || userInfo.id)
			console.log('用户ID:', userId)
			
			if (!userInfo || !userId) {
				console.log("用户信息异常:", userInfo)
				console.log("用户ID:", userId)
				uni.showModal({
					title: '提示', 
					content: '用户信息异常，请重新登录',
					showCancel: false,
					success: () => {
						// 清除异常的登录信息
						uni.removeStorageSync('token')
						uni.removeStorageSync('userInfo')
						// 跳转到登录页面
						uni.reLaunch({
							url: '/pages/login/login'
						})
					}
				})
				return
			}
			
			uni.showLoading({
				title: '保存中...'
			})
			
			try {
				// 头像已在选择时立即上传，这里需要将完整URL转换为相对路径保存到后端
				let avatarUrl = this.currentAvatar
				
				// 确保avatarUrl是字符串类型
				if (typeof avatarUrl !== 'string') {
					console.error('头像URL类型错误:', typeof avatarUrl, avatarUrl)
					avatarUrl = '/static/images/default-avatar.png'
				} else {
					// 如果是完整URL，提取相对路径部分保存到后端
					const baseURL = 'https://api.airoubo.com'
					if (avatarUrl.startsWith(baseURL)) {
						avatarUrl = avatarUrl.substring(baseURL.length)
					}
				}
				
				const updateData = {
					nickname: this.currentNickname,
					avatarUrl: avatarUrl
				}
				
				const result = await this.$api.userApi.updateProfile(updateData)
				console.log('更新用户信息结果:', result)
				
				// 处理后端返回的下划线格式字段名，转换为前端期望的驼峰格式
				const backendData = result.data || result
				const updatedUserInfo = {
					...this.userInfo,
					userId: backendData.user_id || backendData.userId,
					nickname: backendData.nickname || this.currentNickname,
					avatarUrl: backendData.avatar_url || backendData.avatarUrl || avatarUrl,
					openid: backendData.openid,
					unionid: backendData.unionid,
					gender: backendData.gender,
					country: backendData.country,
					province: backendData.province,
					city: backendData.city,
					language: backendData.language,
					status: backendData.status,
					lastLoginTime: backendData.last_login_time || backendData.lastLoginTime,
					createdAt: backendData.created_at || backendData.createdAt,
					updatedAt: backendData.updated_at || backendData.updatedAt
				}
				
				// 更新本地存储
				uni.setStorageSync('userInfo', updatedUserInfo)
				console.log('本地用户信息已更新:', updatedUserInfo)
				
				uni.hideLoading()
				uni.showToast({
					title: '保存成功',
					icon: 'success'
				})
				
				// 根据重定向参数跳转
				setTimeout(() => {
					if (this.redirectAfterProfile) {
						uni.navigateTo({
							url: this.redirectAfterProfile
						})
					} else {
						uni.switchTab({
							url: '/pages/rounds/rounds'
						})
					}
				}, 1500)
				
			} catch (error) {
				uni.hideLoading()
				console.error('保存用户信息失败:', error)
				
				// 检查是否是登录相关错误
				if (error.message && (error.message.includes('登录已过期') || error.message.includes('401'))) {
					// 登录过期错误已经在API层处理了，这里不需要额外处理
					return
				}
				
				// 其他错误显示提示
				uni.showToast({
					title: error.message || '保存失败，请重试',
					icon: 'none',
					duration: 2000
				})
			}
		},
		
		// 跳过设置
		skipProfile() {
			uni.showModal({
				title: '提示',
				content: '跳过设置后可在个人中心修改信息',
				success: (res) => {
					if (res.confirm) {
						if (this.redirectAfterProfile) {
							uni.navigateTo({
								url: this.redirectAfterProfile
							})
						} else {
							uni.switchTab({
								url: '/pages/rounds/rounds'
							})
						}
					}
				}
			})
		}
	}
}
</script>

<style lang="scss" scoped>
.profile-edit {
	min-height: 100vh;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	padding: 40rpx 30rpx;
}

.header {
	text-align: center;
	margin-bottom: 60rpx;
	
	.title {
		font-size: 48rpx;
		font-weight: bold;
		color: #ffffff;
		display: block;
		margin-bottom: 20rpx;
	}
	
	.subtitle {
		font-size: 28rpx;
		color: rgba(255, 255, 255, 0.8);
		display: block;
	}
}

.form-container {
	background: #ffffff;
	border-radius: 20rpx;
	padding: 40rpx;
	margin-bottom: 40rpx;
	box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.1);
}

.form-item {
	margin-bottom: 40rpx;
	
	&:last-child {
		margin-bottom: 0;
	}
	
	.label {
		font-size: 32rpx;
		font-weight: 600;
		color: #333333;
		display: block;
		margin-bottom: 20rpx;
	}
}

.avatar-section {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 30rpx;
	
	.wechat-avatar-container {
		position: relative;
		width: 160rpx;
		height: 160rpx;
		border: none;
		background: transparent;
		padding: 0;
		border-radius: 80rpx;
		overflow: hidden;
		
		.current-avatar {
			width: 100%;
			height: 100%;
			border-radius: 80rpx;
			border: 4rpx solid #f0f0f0;
			transition: all 0.3s ease;
		}
		
		.avatar-overlay {
			position: absolute;
			top: 0;
			left: 0;
			width: 100%;
			height: 100%;
			border-radius: 80rpx;
			background: rgba(0, 0, 0, 0.5);
			display: flex;
			flex-direction: column;
			justify-content: center;
			align-items: center;
			opacity: 0;
			transition: opacity 0.3s ease;
			
			.camera-icon {
				font-size: 40rpx;
				margin-bottom: 8rpx;
			}
			
			.change-text {
				font-size: 20rpx;
				color: #ffffff;
				text-align: center;
			}
		}
		
		&:active {
			.current-avatar {
				transform: scale(0.95);
			}
			
			.avatar-overlay {
				opacity: 1;
			}
		}
	}
	
	.avatar-options {
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 20rpx;
		
		.avatar-tip {
			font-size: 24rpx;
			color: #6c757d;
			text-align: center;
			line-height: 1.4;
		}
		
		.other-options {
			display: flex;
			gap: 20rpx;
			
			.option-btn {
				padding: 15rpx 25rpx;
				border-radius: 25rpx;
				border: 2rpx solid #e9ecef;
				background: #ffffff;
				font-size: 24rpx;
				transition: all 0.3s ease;
				
				.option-text {
					color: #495057;
					font-size: 24rpx;
				}
				
				&:active {
					background: #f8f9fa;
					border-color: #dee2e6;
					transform: scale(0.95);
				}
			}
		}
	}
}

.nickname-section {
	.nickname-input {
		width: 100%;
		padding: 25rpx 20rpx;
		border: 2rpx solid #e9ecef;
		border-radius: 10rpx;
		font-size: 30rpx;
		color: #333333;
		background: #ffffff;
		
		&:focus {
			border-color: #667eea;
		}
	}
	
	.nickname-tip {
		font-size: 24rpx;
		color: #6c757d;
		display: block;
		margin-top: 10rpx;
	}
}

.button-container {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
	
	.save-btn,
	.skip-btn {
		width: 100%;
		padding: 30rpx;
		border-radius: 15rpx;
		font-size: 32rpx;
		font-weight: 600;
		border: none;
		
		&:active {
			transform: translateY(2rpx);
		}
	}
	
	.save-btn {
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
		color: #ffffff;
		
		&:disabled {
			background: #cccccc;
			color: #666666;
			transform: none;
		}
	}
	
	.skip-btn {
		background: transparent;
		color: #ffffff;
		border: 2rpx solid rgba(255, 255, 255, 0.5);
		
		&:active {
			background: rgba(255, 255, 255, 0.1);
		}
	}
}
</style>