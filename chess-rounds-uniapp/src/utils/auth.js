/**
 * 用户认证和状态管理工具
 */
import { userApi } from './api.js'
import config from '@/config/api.js'

export class AuthManager {
	/**
	 * 检查用户是否已登录
	 */
	static isLoggedIn() {
		const token = uni.getStorageSync('token')
		const userInfo = uni.getStorageSync('userInfo')
		return !!(token && userInfo)
	}
	
	/**
	 * 获取当前用户信息
	 */
	static getCurrentUser() {
		return uni.getStorageSync('userInfo')
	}
	
	/**
	 * 获取访问令牌
	 */
	static getToken() {
		return uni.getStorageSync('token')
	}
	
	/**
	 * 保存登录信息
	 * @param {Object} loginResult 登录结果
	 */
	static saveLoginInfo(loginResult) {
		console.log('保存登录信息:', loginResult)
		
		// 兼容不同的响应格式
		const data = loginResult.data || loginResult
		
		if (data) {
			// 保存token - 支持多种字段名
			const token = data.accessToken || data.access_token || data.token
			if (token) {
				uni.setStorageSync('token', token)
				console.log('Token已保存:', token.substring(0, 20) + '...')
			} else {
				console.error('未找到token字段:', Object.keys(data))
			}
			
			// 保存刷新token
			const refreshToken = data.refreshToken || data.refresh_token
			if (refreshToken) {
				uni.setStorageSync('refreshToken', refreshToken)
				console.log('RefreshToken已保存')
			}
			
			// 保存用户信息 - 支持多种字段名
			const userInfo = data.user || data.userInfo || data.user_info
			if (userInfo) {
				uni.setStorageSync('userInfo', userInfo)
				console.log('用户信息已保存:', userInfo)
				
				// 单独保存userId以便其他页面使用
				const userId = userInfo.userId || userInfo.user_id || userInfo.id
				if (userId) {
					uni.setStorageSync('userId', userId)
					console.log('用户ID已保存:', userId)
				}
			} else {
				console.error('未找到用户信息字段:', Object.keys(data))
			}
		} else {
			console.error('登录结果数据为空:', loginResult)
		}
	}
	
	/**
	 * 清除登录信息
	 */
	static clearLoginInfo() {
		uni.removeStorageSync('token')
		uni.removeStorageSync('refreshToken')
		uni.removeStorageSync('userInfo')
		uni.removeStorageSync('userId')
	}
	
	/**
	 * 退出登录
	 */
	static async logout() {
		try {
			// 调用后端退出接口（如果需要）
			// await userApi.logout()
			
			// 清除本地存储
			this.clearLoginInfo()
			
			// 跳转到登录页
			uni.reLaunch({
				url: '/pages/login/login'
			})
			
			// // uni.showToast() - 已屏蔽
		} catch (error) {
			console.error('退出登录失败:', error)
			// 即使后端调用失败，也要清除本地信息
			this.clearLoginInfo()
			uni.reLaunch({
				url: '/pages/login/login'
			})
		}
	}
	
	/**
	 * 检查登录状态并跳转
	 * @param {Boolean} showToast 是否显示提示
	 */
	static checkLoginAndRedirect(showToast = true) {
		if (!this.isLoggedIn()) {
			if (showToast) {
				// // uni.showToast() - 已屏蔽
			}
			
			setTimeout(() => {
				uni.reLaunch({
					url: '/pages/login/login'
				})
			}, showToast ? 1500 : 0)
			
			return false
		}
		return true
	}
	
	/**
	 * 获取用户信息（兼容旧版本调用）
	 */
	static getUserInfo() {
		return this.getCurrentUser()
	}
	
	/**
	 * 刷新用户信息
	 */
	static async refreshUserInfo() {
		try {
			const result = await userApi.getProfile()
			if (result.data) {
				uni.setStorageSync('userInfo', result.data)
				return result.data
			}
		} catch (error) {
			console.error('刷新用户信息失败:', error)
			// 如果是401错误，说明token过期
			if (error.message.includes('401') || error.message.includes('登录已过期')) {
				this.clearLoginInfo()
				uni.reLaunch({
					url: '/pages/login/login'
				})
			}
			throw error
		}
	}
	
	/**
	 * 更新用户信息
	 * @param {Object} userInfo 用户信息
	 */
	static async updateUserInfo(userInfo) {
		try {
			const result = await userApi.updateProfile(userInfo)
			if (result.data) {
				// 更新本地存储
				const currentUser = this.getCurrentUser()
				const updatedUser = { ...currentUser, ...result.data }
				uni.setStorageSync('userInfo', updatedUser)
				return updatedUser
			}
		} catch (error) {
			console.error('更新用户信息失败:', error)
			throw error
		}
	}
	
	/**
	 * 获取用户头像URL
	 * @param {String} avatarUrl 头像URL
	 */
	static getAvatarUrl(avatarUrl) {
		if (!avatarUrl) {
			return '/static/images/default-avatar.png'
		}
		
		// 如果是完整URL，直接返回
		if (avatarUrl.startsWith('http')) {
			return avatarUrl
		}
		
		// 如果是相对路径，拼接基础URL
		if (avatarUrl.startsWith('/')) {
			// 使用统一的静态资源基础URL配置
			return config.staticBaseURL + avatarUrl
		}
		
		// 默认头像
		return '/static/images/default-avatar.png'
	}
}

export default AuthManager