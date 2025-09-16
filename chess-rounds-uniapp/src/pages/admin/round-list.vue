<template>
	<view class="round-list-page">
		<!-- 导航栏 -->
		<view class="nav-bar">
			<view class="nav-left" @click="goBack">
				<text class="nav-icon">‹</text>
			</view>
			<view class="nav-center">
				<text class="nav-title">{{ pageTitle }}</text>
			</view>
			<view class="nav-right"></view>
		</view>

		<!-- 内容区域 -->
		<view class="content">
			<!-- 加载状态 -->
			<view v-if="loading" class="loading-container">
				<text class="loading-text">加载中...</text>
			</view>

			<!-- 回合列表 -->
			<scroll-view 
				class="scroll-container"
				scroll-y="true"
				:refresher-enabled="true"
				:refresher-triggered="refresherTriggered"
				@refresherrefresh="onRefresh"
				@refresherrestore="onRestore"
				@scrolltolower="loadMore"
			>
				<view class="rounds-container">
					<view v-if="rounds.length === 0 && !loading" class="empty-state">
						<text class="empty-icon">📋</text>
						<text class="empty-text">暂无{{ statusLabel }}的回合</text>
					</view>
					
					<view v-else>
						<view 
							class="round-item" 
							v-for="round in rounds" 
							:key="round.id"
						>
							<view class="round-card">
								<!-- 回合头部 -->
								<view class="round-header">
									<view class="round-info-horizontal">
										<view class="status-badge" :class="[
											round.status === 'WAITING' ? 'status-waiting' : '',
											(round.status === 'IN_PROGRESS' || round.status === 'PLAYING') ? 'status-playing' : '',
											round.status === 'FINISHED' ? 'status-finished' : '',
											round.status === 'CANCELLED' ? 'status-cancelled' : ''
										]">
											<text class="status-text">{{ getStatusLabel(round.status) }}</text>
										</view>
										<view v-if="round.multiplier && round.multiplier > 1" class="multiplier-hint">
											{{ round.multiplier }}倍
										</view>
									</view>
									<view class="create-time-left">
										<text class="create-time-text">{{ formatTime(round.created_at) }}</text>
									</view>
								</view>
								
								<!-- 参与者列表 -->
								<view v-if="round.participants && round.participants.length > 0" class="amounts-list">
									<view 
										v-for="participant in round.participants" 
										:key="participant.participant_id"
										class="amount-item-new"
										:class="{ 'current-user': participant.isCurrentUser }"
									>
										<image 
				:src="getAvatarUrl(participant.user_info && participant.user_info.avatar_url)" 
				class="participant-avatar"
				mode="aspectFill"
			/>
										<view class="participant-info">
											<text class="participant-name">{{ (participant.user_info && participant.user_info.nickname) || '未知用户' }}</text>
											<text 
												class="participant-amount"
												:class="{
													'positive': (participant.total_amount || 0) > 0,
													'negative': (participant.total_amount || 0) < 0
												}"
											>
												{{ (participant.total_amount || 0) > 0 ? '+' : '' }}{{ participant.total_amount || 0 }}
											</text>
										</view>
									</view>
								</view>
								
								<!-- 旁观者列表 -->
								<view v-if="round.spectators && round.spectators.length > 0" class="spectators-section">
									<view class="spectators-header">
										<text class="spectators-title">旁观者 ({{ round.spectators.length }})</text>
									</view>
									<view class="spectators-list">
										<view v-for="spectator in round.spectators" :key="spectator.participant_id" class="spectator-item">
											<image 
										:src="getAvatarUrl(spectator.user_info && spectator.user_info.avatar_url)" 
										class="spectator-avatar" 
										mode="aspectFill"
									></image>
											<text class="spectator-name">{{ (spectator.user_info && spectator.user_info.nickname) || '未知用户' }}</text>
										</view>
									</view>
								</view>
							</view>
						</view>
					</view>
				</view>

				<!-- 加载更多 -->
				<view v-if="hasMore && !loading" class="load-more">
					<text class="load-more-text">上拉加载更多</text>
				</view>
				<view v-if="loadingMore" class="load-more">
					<text class="load-more-text">加载中...</text>
				</view>
			</scroll-view>
		</view>
	</view>
</template>

<script>
import AuthManager from '@/utils/auth.js'

export default {
	data() {
		return {
			status: '', // 回合状态
			statusLabel: '', // 状态标签
			pageTitle: '回合列表',
			rounds: [], // 回合列表
			roundIds: [], // 从统计页面传递的回合ID列表
			loading: false,
			refresherTriggered: false,
			loadingMore: false,
			hasMore: true,
			page: 1,
			pageSize: 20
		}
	},
	
	onLoad(options) {
		if (options.status) {
			this.status = options.status
		}
		if (options.title) {
			this.statusLabel = options.title
			this.pageTitle = `${options.title}回合`
		}
		if (options.round_ids) {
			// 解析round_ids字符串为数组
			this.roundIds = options.round_ids.split(',').map(id => parseInt(id.trim())).filter(id => !isNaN(id))
		}
		
		// 检查管理员权限
		this.checkAdminPermission()
	},
	
	methods: {
		// 检查管理员权限
		async checkAdminPermission() {
			// 获取用户信息
			const userInfo = uni.getStorageSync('userInfo')
			if (!userInfo || !userInfo.user_id) {
				uni.showToast({
					title: '请先登录',
					icon: 'none'
				})
				uni.navigateBack()
				return
			}
			
			// 检查是否为管理员用户（ID为1）
			if (userInfo.user_id !== 1) {
				uni.showToast({
					title: '无权限访问',
					icon: 'none'
				})
				uni.navigateBack()
				return
			}
			
			// 权限验证通过，加载数据
			this.loadRounds()
		},
		
		// 加载回合列表
		async loadRounds(refresh = false) {
			if (refresh) {
				this.page = 1
				this.hasMore = true
				this.rounds = []
			}
			
			if (refresh) {
				this.loading = true
			} else {
				this.loadingMore = true
			}
			
			try {
				const token = uni.getStorageSync('token')
				const headers = {
					'Content-Type': 'application/json'
				}
				
				if (token) {
					headers['Authorization'] = `Bearer ${token}`
				}
				
				let response
				
				// 如果有roundIds，使用批量查询接口
				if (this.roundIds && this.roundIds.length > 0) {
					// 计算当前页的roundIds
					const startIndex = (this.page - 1) * this.pageSize
					const endIndex = startIndex + this.pageSize
					const currentPageIds = this.roundIds.slice(startIndex, endIndex)
					
					if (currentPageIds.length === 0) {
						// 没有更多数据
						this.hasMore = false
						this.loading = false
						this.loadingMore = false
						return
					}
					
					// 使用admin API方法
					const adminApi = require('@/api/admin.js').default
					const batchResponse = await adminApi.request('/rounds/batch', {
						method: 'POST',
						data: {
							ids: currentPageIds
						}
					})
					// 包装响应格式以保持一致性
					response = {
						statusCode: 200,
						data: batchResponse
					}
				} else {
					// 使用原有的搜索接口
					const adminApi = require('@/api/admin.js').default
					const searchResponse = await adminApi.request('/rounds/search', {
						method: 'GET',
						data: {
							title: '', // 空标题获取所有回合
							page: this.page - 1, // Spring Boot分页从0开始
							size: this.pageSize,
							status: this.status !== 'ALL' ? this.status : undefined // 传递状态参数
						}
					})
					// 包装响应格式以保持一致性
					response = {
						statusCode: 200,
						data: searchResponse
					}
				}
				
				if (response.statusCode === 200) {
					let allRounds = []
					
					if (this.roundIds && this.roundIds.length > 0) {
						// 批量查询接口，直接返回回合数组
						allRounds = response.data || []
						
						// 检查是否还有更多数据
						const currentEndIndex = this.page * this.pageSize
						this.hasMore = currentEndIndex < this.roundIds.length
						if (this.hasMore) {
							this.page++
						}
					} else {
						// 搜索接口，Spring Boot分页响应格式
						const pageData = response.data
						allRounds = pageData.content || []
						
						// 检查是否还有更多数据
						this.hasMore = !pageData.last
						if (!pageData.last) {
							this.page++
						}
					}
					
					// 数据字段映射和处理
					allRounds = allRounds.map(round => {
						return {
							...round,
							// 映射API字段到前端期望的字段
							id: round.round_id || round.id,
							multiplier: round.base_amount || round.baseAmount || round.multiplier,
							participants_count: round.current_participants || round.participants?.length || 0,
							spectators_count: round.spectator_count || round.spectators?.length || 0,
							// 状态字段统一处理
							status: (round.status || '').toUpperCase()
						}
					})
					
					// 更新回合列表
					if (refresh) {
						this.rounds = allRounds
					} else {
						this.rounds = [...this.rounds, ...allRounds]
					}
				} else {
					uni.showToast({
						title: response.data.message || '加载失败',
						icon: 'none'
					})
				}
			} catch (error) {
				console.error('加载回合列表失败:', error)
				uni.showToast({
					title: '网络错误',
					icon: 'none'
				})
			} finally {
				this.loading = false
				this.loadingMore = false
			}
		},
		
		// 下拉刷新
		async onRefresh() {
			this.refresherTriggered = true
			try {
				await this.loadRounds(true)
			} finally {
				this.refresherTriggered = false
			}
		},
		
		// 刷新完成
		onRestore() {
			this.refresherTriggered = false
		},
		
		// 加载更多
		loadMore() {
			if (this.hasMore && !this.loading && !this.loadingMore) {
				this.loadRounds(false)
			}
		},
		
		// 返回上一页
		goBack() {
			uni.navigateBack()
		},
		
		// 获取状态标签
		getStatusLabel(status) {
			const labels = {
				'WAITING': '等待中',
				'IN_PROGRESS': '进行中',
				'PLAYING': '进行中',
				'FINISHED': '已结束',
				'CANCELLED': '已取消'
			}
			return labels[status] || status
		},
		
		// 获取头像URL
		getAvatarUrl(avatarUrl) {
			return AuthManager.getAvatarUrl(avatarUrl)
		},
		
		// 格式化时间
		formatTime(timeStr) {
			if (!timeStr) return ''
			try {
				const date = new Date(timeStr)
				const now = new Date()
				const diff = now - date
				
				// 小于1分钟
				if (diff < 60000) {
					return '刚刚'
				}
				// 小于1小时
				else if (diff < 3600000) {
					return `${Math.floor(diff / 60000)}分钟前`
				}
				// 小于1天
				else if (diff < 86400000) {
					return `${Math.floor(diff / 3600000)}小时前`
				}
				// 大于1天
				else {
					return date.toLocaleDateString('zh-CN', {
						month: 'short',
						day: 'numeric',
						hour: '2-digit',
						minute: '2-digit'
					})
				}
			} catch (error) {
				return timeStr
			}
		}
	}
}
</script>

<style scoped>
.round-list-page {
	height: 100vh;
	background-color: #f5f5f5;
	display: flex;
	flex-direction: column;
}

/* 导航栏 */
.nav-bar {
	height: 88rpx;
	background-color: #fff;
	display: flex;
	align-items: center;
	padding: 0 32rpx;
	border-bottom: 1rpx solid #eee;
	position: sticky;
	top: 0;
	z-index: 100;
}

.nav-left, .nav-right {
	width: 80rpx;
	height: 88rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.nav-center {
	flex: 1;
	display: flex;
	align-items: center;
	justify-content: center;
}

.nav-icon {
	font-size: 48rpx;
	font-weight: bold;
	color: #333;
}

.nav-title {
	font-size: 36rpx;
	font-weight: bold;
	color: #333;
}

/* 内容区域 */
.content {
	flex: 1;
	position: relative;
}

.scroll-container {
	height: 100%;
}

.rounds-container {
	padding: 32rpx;
}

/* 加载状态 */
.loading-container {
	position: absolute;
	top: 50%;
	left: 50%;
	transform: translate(-50%, -50%);
	display: flex;
	align-items: center;
	justify-content: center;
}

.loading-text {
	font-size: 28rpx;
	color: #999;
}

/* 空状态 */
.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 120rpx 0;
}

.empty-icon {
	font-size: 120rpx;
	margin-bottom: 32rpx;
}

.empty-text {
	font-size: 28rpx;
	color: #999;
}

/* 回合卡片 */
.round-item {
	margin-bottom: 24rpx;
}

.round-card {
	background-color: #fff;
	border-radius: 16rpx;
	padding: 32rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.round-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 24rpx;
}

.round-info-horizontal {
	display: flex;
	align-items: center;
	gap: 8rpx;
}

.status-badge {
	padding: 8rpx 16rpx;
	border-radius: 20rpx;
	font-size: 24rpx;
}

.status-text {
	font-weight: 500;
}

.multiplier-hint {
	font-size: 22rpx;
	color: #007AFF;
	font-weight: 500;
	background-color: rgba(0, 122, 255, 0.1);
	padding: 4rpx 8rpx;
	border-radius: 8rpx;
}

.create-time-left {
	display: flex;
	align-items: center;
}

.create-time-text {
	font-size: 22rpx;
	color: #999;
	background-color: #f8f9fa;
	padding: 6rpx 12rpx;
	border-radius: 12rpx;
	border: 1rpx solid #e9ecef;
	font-weight: 500;
}

/* 参与者列表样式 */
.amounts-list {
	display: flex;
	gap: 12rpx;
	margin-bottom: 16rpx;
	flex-wrap: wrap;
}

.amount-item-new {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 12rpx 8rpx;
	background-color: #f8f9fa;
	border-radius: 12rpx;
	border: 1rpx solid #e9ecef;
	gap: 8rpx;
	flex: 1;
	min-width: 0;
	max-width: 200rpx;
}

.amount-item-new.current-user {
	border-color: #007AFF;
	background-color: rgba(0, 122, 255, 0.1);
}

.participant-avatar {
	width: 60rpx;
	height: 60rpx;
	border-radius: 50%;
	background-color: #ddd;
}

.participant-info {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 4rpx;
	width: 100%;
}

.participant-name {
	font-size: 24rpx;
	font-weight: 500;
	color: #333;
	text-align: center;
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
	max-width: 100%;
}

.participant-amount {
	font-size: 28rpx;
	font-weight: bold;
	color: #333;
	text-align: center;
	min-width: 60rpx;
}

.participant-amount.positive {
	color: #52c41a;
}

.participant-amount.negative {
	color: #ff4d4f;
}

/* 旁观者列表样式 */
.spectators-section {
	margin-top: 24rpx;
	padding-top: 24rpx;
	border-top: 1rpx solid #f0f0f0;
}

.spectators-header {
	margin-bottom: 16rpx;
}

.spectators-title {
	font-size: 24rpx;
	font-weight: 600;
	color: #666;
}

.spectators-list {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
}

.spectator-item {
	display: flex;
	align-items: center;
	background: #f8f9fa;
	border-radius: 20rpx;
	padding: 8rpx 16rpx;
	min-width: 0;
}

.spectator-avatar {
	width: 32rpx;
	height: 32rpx;
	border-radius: 50%;
	margin-right: 8rpx;
	flex-shrink: 0;
}

.spectator-name {
	font-size: 22rpx;
	color: #666;
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
	max-width: 100rpx;
}

/* 基础信息 */
.round-basic-info {
	margin-top: 16rpx;
	padding-top: 16rpx;
	border-top: 1rpx solid #f0f0f0;
}

.info-row {
	display: flex;
	align-items: center;
	gap: 16rpx;
	margin-bottom: 8rpx;
}

.info-label {
	font-size: 28rpx;
	color: #666;
	min-width: 140rpx;
}

.info-value {
	font-size: 28rpx;
	color: #333;
	font-weight: 500;
}

/* 状态样式 */
.status-waiting {
	background-color: #FFF8E1;
	color: #F57C00;
	border: 1rpx solid #FFB74D;
}

.status-playing {
	background-color: #E8F5E8;
	color: #388E3C;
	border: 1rpx solid #66BB6A;
}

.status-finished {
	background-color: #E3F2FD;
	color: #1976D2;
	border: 1rpx solid #42A5F5;
}

.status-cancelled {
	background-color: #FFEBEE;
	color: #D32F2F;
	border: 1rpx solid #EF5350;
}

/* 加载更多 */
.load-more {
	padding: 32rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.load-more-text {
	font-size: 28rpx;
	color: #999;
}
</style>