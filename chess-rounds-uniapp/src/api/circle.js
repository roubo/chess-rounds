/**
 * 圈子相关API接口
 */

import config from '@/config/api.js'

// API基础URL
const BASE_URL = config.baseURL

// 获取用户ID的辅助函数
const getUserId = () => {
	const userInfo = uni.getStorageSync('userInfo')
	// 兼容不同的字段名：userId 或 user_id
	const userId = userInfo && (userInfo.userId || userInfo.user_id || userInfo.id)
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

// 圈子相关API
export const circleApi = {
	/**
	 * 获取用户加入的圈子列表
	 */
	async getUserCircles() {
		return await request('/circles/my')
	},

	/**
	 * 创建圈子
	 * @param {Object} circleData - 圈子数据
	 * @param {string} circleData.name - 圈子名称
	 * @param {string} circleData.description - 圈子描述
	 * @param {boolean} circleData.isPrivate - 是否私密圈子
	 */
	async createCircle(circleData) {
		return await request('/circles', {
			method: 'POST',
			data: circleData
		})
	},

	/**
	 * 通过邀请码加入圈子
	 * @param {string} joinCode - 邀请码
	 */
	async joinCircle(joinCode) {
		return await request('/circles/join-by-code', {
			method: 'POST',
			data: { joinCode }
		})
	},

	/**
	 * 退出圈子
	 * @param {number} circleId - 圈子ID
	 */
	async leaveCircle(circleId) {
		return await request(`/circles/${circleId}/leave`, {
			method: 'POST'
		})
	},

	/**
	 * 获取圈子详情
	 * @param {number} circleId - 圈子ID
	 */
	async getCircleDetail(circleId) {
		return await request(`/circles/${circleId}`)
	},

	/**
	 * 获取圈子成员列表
	 * @param {number} circleId - 圈子ID
	 */
	async getCircleMembers(circleId) {
		return await request(`/circles/${circleId}/members`)
	},



	/**
	 * 更新圈子信息（仅圈主可操作）
	 * @param {number} circleId - 圈子ID
	 * @param {Object} updateData - 更新数据
	 */
	async updateCircle(circleId, updateData) {
		return await request(`/circles/${circleId}`, {
			method: 'PUT',
			data: updateData
		})
	},

	/**
	 * 删除圈子（仅圈主可操作）
	 * @param {number} circleId - 圈子ID
	 */
	async deleteCircle(circleId) {
		return await request(`/circles/${circleId}`, {
			method: 'DELETE'
		})
	},

	/**
	 * 移除圈子成员（仅圈主可操作）
	 * @param {number} circleId - 圈子ID
	 * @param {number} userId - 用户ID
	 */
	async removeMember(circleId, userId) {
		return await request(`/circles/${circleId}/members/${userId}`, {
			method: 'DELETE'
		})
	},

	/**
	 * 转让圈主权限（仅圈主可操作）
	 * @param {number} circleId - 圈子ID
	 * @param {number} newOwnerId - 新圈主用户ID
	 */
	async transferOwnership(circleId, newOwnerId) {
		return await request(`/circles/${circleId}/transfer`, {
			method: 'POST',
			data: { newOwnerId }
		})
	},

	/**
	 * 获取圈子排行榜
	 * @param {number} circleId - 圈子ID
	 * @param {Object} params - 查询参数
	 * @param {number} params.page - 页码（从0开始）
	 * @param {number} params.size - 每页大小
	 * @param {string} params.sort - 排序字段，如 'score,desc' 或 'winRate,desc'
	 */
	async getLeaderboard(circleId, params = {}) {
		// 手动构建查询参数（小程序不支持URLSearchParams）
		const queryParts = []
		if (params.page !== undefined) queryParts.push(`page=${params.page}`)
		if (params.size !== undefined) queryParts.push(`size=${params.size}`)
		if (params.sort) queryParts.push(`sort=${params.sort}`)
		
		const queryString = queryParts.join('&')
		const url = `/circles/${circleId}/leaderboard${queryString ? '?' + queryString : ''}`
		
		return await request(url)
	},

	/**
	 * 刷新圈子排行榜（仅圈主和管理员可操作）
	 * @param {number} circleId - 圈子ID
	 */
	async refreshLeaderboard(circleId) {
		return await request(`/circles/${circleId}/leaderboard/refresh`, {
			method: 'POST'
		})
	}
}

// 错误处理函数
export const handleApiError = (error) => {
	console.error('API请求错误:', error)
	
	// 根据错误类型显示不同的提示
	if (error.message && error.message.includes('网络')) {
		uni.showToast({
			title: '网络连接失败',
			icon: 'none'
		})
	} else if (error.message && error.message.includes('401')) {
		uni.showToast({
			title: '请先登录',
			icon: 'none'
		})
		// 可以在这里跳转到登录页面
		// uni.navigateTo({ url: '/pages/login/login' })
	} else {
		uni.showToast({
			title: '操作失败，请重试',
			icon: 'none'
		})
	}
}