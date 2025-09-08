/**
 * 回合相关API接口
 */

import { mockRoundData, calculateParticipantTotals, calculateTotalTableBoard, mockApiDelay, generateRecordId, mockRoundsList, getUserRounds } from '@/mock/roundData'

// Mock模式开关 - 开发时可以设置为true来使用mock数据
const USE_MOCK_DATA = false

// API基础配置
const BASE_URL = 'https://api.airoubo.com/api'

// 获取用户ID的辅助函数
const getUserId = () => {
	const userInfo = uni.getStorageSync('userInfo')
	// 兼容不同的字段名：userId 或 user_id
	const userId = userInfo && (userInfo.userId || userInfo.user_id)
	return userId ? userId.toString() : (USE_MOCK_DATA ? 'user-001' : null)
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
		
		// 如果需要用户ID且用户已登录，自动添加User-Id请求头
		if (options.requireUserId !== false) {
			const userId = getUserId()
			if (userId) {
				headers['User-Id'] = userId
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
		if (USE_MOCK_DATA) {
			await mockApiDelay(300)
			return {
				code: 200,
				message: 'success',
				data: mockRoundsList
			}
		}
		const query = Object.keys(params)
			.map(key => `${key}=${encodeURIComponent(params[key])}`)
			.join('&')
		return request(`/rounds${query ? '?' + query : ''}`, { requireUserId: false })
	},

	// 获取回合详情
	async getRoundDetail(roundId) {
		if (USE_MOCK_DATA) {
			await mockApiDelay(300)
			return {
				code: 200,
				message: 'success',
				data: mockRoundData.roundInfo
			}
		}
		return request(`/rounds/${roundId}`, { requireUserId: false })
	},

	// 创建回合
	async createRound(roundData) {
		if (USE_MOCK_DATA) {
			await mockApiDelay(500)
			// 模拟创建回合成功，返回与后端相同的数据结构
			return {
				code: 200,
				message: 'success',
				data: {
					round_id: Math.floor(Math.random() * 1000) + 1,
					round_code: Math.random().toString(36).substr(2, 6).toUpperCase(),
					game_type: roundData.game_type || 'mahjong',
					creator: {
						user_id: 1,
						openid: 'mock-openid',
						nickname: '测试用户',
						avatar_url: '/static/avatar1.png'
					},
					status: 'waiting',
					max_participants: roundData.max_participants || 4,
					base_amount: roundData.base_amount || 1,
					has_table: roundData.has_table || false,
					is_public: roundData.is_public || false,
					allow_spectator: roundData.allow_spectator || true,
					created_at: new Date().toISOString(),
					participants: []
				}
			}
		}
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

	// 开始回合
	startRound(roundId) {
		return request(`/rounds/${roundId}/start`, {
			method: 'POST'
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
		if (USE_MOCK_DATA) {
			await mockApiDelay(250)
			const userId = getUserId()
			const userRounds = getUserRounds(userId)
			return {
				code: 200,
				message: 'success',
				data: userRounds
			}
		}
		const userId = getUserId()
		if (!userId) {
			return Promise.reject(new Error('用户未登录或用户信息不完整'))
		}
		return request('/rounds/my')
	},

	// 获取回合参与者
	async getRoundParticipants(roundId) {
		if (USE_MOCK_DATA) {
			await mockApiDelay(200)
			return {
				code: 200,
				message: 'success',
				data: calculateParticipantTotals(mockRoundData.participants, mockRoundData.gameRecords)
			}
		}
		return request(`/rounds/${roundId}/participants`, { requireUserId: false })
	},

	// 获取回合游戏记录
	async getGameRecords(roundId) {
		if (USE_MOCK_DATA) {
			await mockApiDelay(250)
			return {
				code: 200,
				message: 'success',
				data: mockRoundData.gameRecords
			}
		}
		return request(`/rounds/${roundId}/records`, { requireUserId: false })
	},

	// 添加游戏记录
	async addGameRecord(recordData) {
		if (USE_MOCK_DATA) {
			await mockApiDelay(400)
			// 模拟添加记录到mock数据
			const newRecord = {
				id: generateRecordId(),
				roundId: recordData.roundId,
				gameNumber: mockRoundData.gameRecords.length + 1,
				tableBoardAmount: recordData.tableBoardAmount,
				participantAmounts: recordData.participantAmounts,
				createdAt: new Date().toISOString(),
				createdBy: getUserId()
			}
			// 添加到mock数据中
			mockRoundData.gameRecords.push(newRecord)
			return {
				code: 200,
				message: 'success',
				data: newRecord
			}
		}
		return request('/game-records', {
			method: 'POST',
			data: recordData
		})
	},

	// 删除游戏记录（管理员功能）
	deleteGameRecord(recordId) {
		return request(`/game-records/${recordId}`, {
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