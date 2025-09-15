/**
 * 管理员相关API接口
 */

import config from '@/config/api.js'

// API基础URL
const BASE_URL = config.baseURL

// 获取用户ID的辅助函数
const getUserId = () => {
	const userInfo = uni.getStorageSync('userInfo')
	// 兼容不同的字段名：userId、user_id 或 id
	const userId = userInfo && (userInfo.userId || userInfo.user_id || userInfo.id)
	return userId ? userId.toString() : null
}

// 兼容性函数：将参数对象转换为查询字符串
const buildQueryString = (params) => {
	if (!params || Object.keys(params).length === 0) {
		return ''
	}
	return Object.keys(params)
		.filter(key => params[key] !== undefined && params[key] !== null)
		.map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
		.join('&')
}

// 通用请求方法
const request = (url, options = {}) => {
	return new Promise((resolve, reject) => {
		// 构建请求头
		const headers = {
			'Content-Type': 'application/json',
			'Authorization': uni.getStorageSync('token') ? `Bearer ${uni.getStorageSync('token')}` : '',
			...options.header
		}
		
		// 如果需要用户ID且用户已登录，自动添加user-id请求头
		if (options.requireUserId !== false) {
			const userId = getUserId()
			if (userId) {
				headers['user-id'] = userId
			}
		}
		
		uni.request({
			url: `${BASE_URL}${url}`,
			method: options.method || 'GET',
			data: options.data || {},
			header: headers,
			success: (res) => {
				if (res.statusCode === 200) {
					resolve(res.data)
				} else {
					reject(new Error(`请求失败: ${res.statusCode}`))
				}
			},
			fail: (err) => {
				reject(err)
			}
		})
	})
}

// 管理员统计API
export const adminStatisticsApi = {
	/**
	 * 获取统计总览
	 */
	async getStatisticsOverview() {
		try {
			return await request('/admin/statistics/overview')
		} catch (error) {
			console.error('获取统计总览失败:', error)
			throw error
		}
	},

	/**
	 * 获取回合状态统计
	 */
	async getRoundStatistics() {
		try {
			return await request('/admin/statistics/rounds')
		} catch (error) {
			console.error('获取回合统计失败:', error)
			throw error
		}
	},

	/**
	 * 获取用户详细信息
	 * @param {Object} params - 查询参数
	 * @param {number} params.page - 页码
	 * @param {number} params.size - 每页大小
	 * @param {string} params.sortBy - 排序字段
	 * @param {string} params.sortDir - 排序方向
	 */
	async getUserDetails(params = {}) {
		try {
				const queryString = buildQueryString(params)
			const url = `/admin/statistics/users${queryString ? '?' + queryString : ''}`
			return await request(url)
		} catch (error) {
			console.error('获取用户详情失败:', error)
			throw error
		}
	},

	/**
	 * 获取财务流水详细统计
	 */
	async getFinancialDetails() {
		try {
			return await request('/admin/statistics/financial')
		} catch (error) {
			console.error('获取财务详情失败:', error)
			throw error
		}
	},

	/**
	 * 刷新统计缓存
	 */
	async refreshStatisticsCache() {
			try {
				return await request('/admin/statistics/refresh-cache', {
					method: 'POST'
				})
			} catch (error) {
				console.error('刷新统计缓存失败:', error)
				throw error
			}
		},


}

// 错误处理辅助函数
export const handleAdminApiError = (error) => {
	console.error('管理员API错误:', error)
	
	if (error.message.includes('401')) {
		uni.showToast({
			title: '未授权访问',
			icon: 'none'
		})
		return
	}
	
	if (error.message.includes('403')) {
		uni.showToast({
			title: '权限不足',
			icon: 'none'
		})
		return
	}
	
	if (error.message.includes('404')) {
		uni.showToast({
			title: '接口不存在',
			icon: 'none'
		})
		return
	}
	
	if (error.message.includes('500')) {
		uni.showToast({
			title: '服务器错误',
			icon: 'none'
		})
		return
	}
	
	// 网络错误
	if (error.errMsg && error.errMsg.includes('request:fail')) {
		uni.showToast({
			title: '网络连接失败',
			icon: 'none'
		})
		return
	}
	
	// 默认错误提示
	uni.showToast({
		title: '操作失败，请重试',
		icon: 'none'
	})
}

export default {
	adminStatisticsApi,
	handleAdminApiError
}