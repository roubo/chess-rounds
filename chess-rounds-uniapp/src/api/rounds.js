/**
 * 回合相关API接口
 */

import config from '@/config/api.js'

// API基础URL
const BASE_URL = config.baseURL

// 获取用户ID的辅助函数
const getUserId = () => {
	const userInfo = uni.getStorageSync('userInfo')
	// 兼容不同的字段名：userId 或 user_id
	const userId = userInfo && (userInfo.userId || userInfo.user_id)
	return userId ? userId.toString() : null
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

// 回合API
export const roundsApi = {
	// 获取回合列表
	async getRounds(params = {}) {
		const query = Object.keys(params)
			.map(key => `${key}=${encodeURIComponent(params[key])}`)
			.join('&')
		return request(`/rounds${query ? '?' + query : ''}`, { requireUserId: false })
	},

	// 获取回合详情
	async getRoundDetail(roundId) {
		return request(`/rounds/${roundId}`, { requireUserId: false })
	},

	// 创建回合
	async createRound(roundData) {
		return request('/rounds', {
			method: 'POST',
			data: roundData
		})
	},

	// 加入回合
	joinRound(roundId, password = '') {
		return request(`/rounds/${roundId}/join`, {
			method: 'POST',
			data: { password }
		})
	},

	// 退出回合
	leaveRound(roundId) {
		return request(`/rounds/${roundId}/leave`, {
			method: 'POST'
		})
	},

	startRound(roundId, hasTable, tableUserId) {
    return request(`/rounds/${roundId}/start`, {
      method: 'POST',
      data: {
        hasTable: hasTable,
        tableUserId: tableUserId
      }
    })
  },

	// 结束回合
	endRound(roundId) {
		return request(`/rounds/${roundId}/end`, {
			method: 'POST'
		})
	},

	// 获取我的回合
	async getMyRounds() {
		return request('/rounds/my')
	},

	// 获取回合参与者
	async getRoundParticipants(roundId) {
		return request(`/rounds/${roundId}/participants`, { requireUserId: false })
	},

	// 获取回合游戏记录
	async getGameRecords(roundId) {
		return request(`/records/round/${roundId}`, { requireUserId: false })
	},

	// 添加游戏记录
	async addGameRecord(recordData) {
		return request('/records', {
			method: 'POST',
			data: recordData
		})
	},

	// 删除游戏记录（管理员功能）
	deleteGameRecord(recordId) {
		return request(`/api/rounds/records/${recordId}`, {
			method: 'DELETE'
		})
	},
	
	// 删除回合
	deleteRound(roundId) {
		return request(`/rounds/${roundId}`, {
			method: 'DELETE'
		})
	}
}

// 用户API
export const userApi = {
	// 用户登录
	login(loginData) {
		return request('/auth/login', {
			method: 'POST',
			data: loginData,
			requireUserId: false
		})
	},

	// 用户注册
	register(registerData) {
		return request('/auth/register', {
			method: 'POST',
			data: registerData,
			requireUserId: false
		})
	},

	// 获取用户信息
	getUserInfo() {
		return request('/user/profile', { requireUserId: false })
	},

	// 更新用户信息
	updateUserInfo(userData) {
		return request('/user/profile', {
			method: 'PUT',
			data: userData,
			requireUserId: false
		})
	}
}

// 错误处理工具
export const handleApiError = (error) => {
	console.error('API Error:', error)
	uni.showToast({
		title: error.message || '网络请求失败',
		icon: 'none',
		duration: 2000
	})
}