// API配置和请求工具
import config from '@/config/api.js'

/**
 * 发起网络请求
 * @param {Object} options 请求配置
 */
function request(options) {
	return new Promise((resolve, reject) => {
		// 获取token
		const token = uni.getStorageSync('token')
		console.log('API请求 - Token:', token ? token.substring(0, 20) + '...' : 'null')
		
		// 设置请求头
		const header = {
			'Content-Type': 'application/json'
		}
		
		if (token) {
			header['Authorization'] = `Bearer ${token}`
			console.log('API请求 - Authorization头:', header['Authorization'].substring(0, 30) + '...')
		} else {
			console.log('API请求 - 没有token')
		}
		
		// 完整的请求URL
		const url = options.url.startsWith('http') ? options.url : config.baseURL + options.url
		
		uni.request({
			url,
			method: options.method || 'GET',
			data: options.data,
			header: { ...header, ...options.header },
			timeout: options.timeout || config.timeout,
			success: (res) => {
				console.log('API响应:', res.statusCode, res.data)
				
				if (res.statusCode === 200) {
					// 对于登录接口，直接返回数据，不检查业务状态码
					if (options.url.includes('/users/login')) {
						resolve(res.data)
					} else if (options.url.includes('/users/profile')) {
						// 对于用户资料相关接口，后端直接返回UserInfoResponse，不包装在ApiResponse中
						resolve({ code: 200, data: res.data, message: options.method === 'PUT' ? '更新成功' : '获取成功' })
					} else {
						// 其他接口检查业务状态码
						if (res.data.code === 200) {
							resolve(res.data)
						} else {
							// 业务错误
							reject(new Error(res.data.message || '请求失败'))
						}
					}
				} else if (res.statusCode === 401) {
					// token过期或无效，清除登录状态
					console.log('Token过期，清除登录信息')
					uni.removeStorageSync('token')
					uni.removeStorageSync('refreshToken')
					uni.removeStorageSync('userInfo')
					
					// 显示提示并跳转到登录页
					uni.showModal({
						title: '提示',
						content: '登录已过期，请重新登录',
						showCancel: false,
						success: () => {
							uni.reLaunch({
								url: '/pages/login/login'
							})
						}
					})
					
					reject(new Error('登录已过期，请重新登录'))
				} else if (res.statusCode === 403) {
					// 权限不足
					reject(new Error('权限不足，请重新登录'))
				} else {
					reject(new Error(`请求失败: ${res.statusCode}`))
				}
			},
			fail: (err) => {
				console.error('网络请求失败:', err)
				reject(new Error('网络请求失败，请检查网络连接'))
			}
		})
	})
}

// 用户相关API
export const userApi = {
	// 微信登录
	wechatLogin: (data) => request({ url: '/users/login', method: 'POST', data }),
	
	// 获取用户信息
	getUserInfo: () => request({ url: '/users/profile', method: 'GET' }),
	
	// 更新用户信息
	updateUserInfo: (data) => request({ url: '/users/profile', method: 'PUT', data }),
	
	// 更新用户资料（头像和昵称）
	updateProfile: (data) => request({ url: '/users/profile', method: 'PUT', data })
}

// 文件上传API
export const fileApi = {
	// 上传头像
	uploadAvatar: (filePath) => {
		return new Promise((resolve, reject) => {
			const token = uni.getStorageSync('token')
			console.log('上传头像，token:', token ? token.substring(0, 20) + '...' : 'null')
			
			uni.uploadFile({
				url: config.baseURL + '/files/upload/avatar',
				filePath: filePath,
				name: 'file',
				header: {
					'Authorization': token ? `Bearer ${token}` : ''
				},
				success: (res) => {
					console.log('上传响应:', res)
					try {
						const data = JSON.parse(res.data)
						if (data.success) {
							resolve(data)
						} else {
							reject(new Error(data.message || '上传失败'))
						}
					} catch (e) {
						console.error('解析上传响应失败:', e)
						reject(new Error('上传响应解析失败'))
					}
				},
				fail: (err) => {
					console.error('上传失败:', err)
					reject(new Error('上传失败: ' + err.errMsg))
				}
			})
		})
	}
}

// 回合相关API
export const roundApi = {
	/**
	 * 获取回合列表
	 * @param {Object} params 查询参数
	 */
	getRounds(params) {
		return request({
			url: '/rounds',
			data: params
		})
	},
	
	/**
	 * 创建回合
	 * @param {Object} roundData 回合数据
	 */
	createRound(roundData) {
		return request({
			url: '/rounds',
			method: 'POST',
			data: roundData
		})
	},
	
	/**
	 * 获取回合详情
	 * @param {String} roundId 回合ID
	 */
	getRoundDetail(roundId) {
		return request({
			url: `/rounds/${roundId}`
		})
	},
	
	/**
	 * 加入回合
	 * @param {String} roundId 回合ID
	 */
	joinRound(roundId) {
		return request({
			url: `/rounds/${roundId}/join`,
			method: 'POST'
		})
	}
}

export default {
	request,
	userApi,
	fileApi,
	roundApi
}