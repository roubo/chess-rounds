<template>
	<view class="container">
		<!-- 顶部标题区域 -->
		<view class="header-section">
			<view class="admin-header">
				<text class="admin-title">管理员统计</text>
				<text class="admin-subtitle">系统数据总览</text>
			</view>
			<view class="refresh-btn" @click="refreshData">
				<text class="refresh-text">刷新</text>
			</view>
		</view>

		<!-- Tab切换区域 -->
		<view class="tab-container">
			<view class="tab-header">
				<view 
					class="tab-item" 
					:class="{ active: activeTab === 'overview' }"
					@click="switchTab('overview')"
				>
					<text class="tab-text">总览</text>
				</view>
				<view 
					class="tab-item" 
					:class="{ active: activeTab === 'users' }"
					@click="switchTab('users')"
				>
					<text class="tab-text">用户</text>
				</view>
			</view>

			<!-- Tab内容区域 -->
			<scroll-view 
				class="tab-content-scroll"
				scroll-y="true"
				refresher-enabled="true"
				:refresher-threshold="120"
				refresher-default-style="black"
				:refresher-triggered="refresherTriggered"
				@refresherrefresh="onRefresh"
				@refresherrestore="onRestore"
				enable-back-to-top="true"
				scroll-with-animation="true"
			>
				<view class="tab-content">
					<!-- 总览页面 -->
					<view class="overview-content" v-if="activeTab === 'overview'">
						<!-- 回合统计卡片 -->
					<view class="stats-card">
						<view class="card-header">
							<text class="card-title">回合统计</text>
							<text class="card-subtitle">点击查看详细列表</text>
						</view>
						<view class="round-stats-container">
							<view 
								class="round-stat-card" 
								v-for="(stat, key) in sortedRoundStats" 
								:key="key"
								:class="[
									key === 'WAITING' ? 'status-waiting' : '',
									(key === 'IN_PROGRESS' || key === 'PLAYING') ? 'status-playing' : '',
									key === 'FINISHED' ? 'status-finished' : '',
									key === 'CANCELLED' ? 'status-cancelled' : ''
								]"
								@click="navigateToRoundList(key)"
							>
								<view class="stat-content">
									<text class="stat-number">{{ getStatCount(stat) }}</text>
									<text class="stat-label">{{ getRoundStatusLabel(key) }}</text>
								</view>
								<view class="stat-arrow">
									<text class="arrow-icon">›</text>
								</view>
							</view>
						</view>
					</view>

						<!-- 用户统计卡片 -->
						<view class="stats-card">
							<view class="card-header">
								<text class="card-title">用户统计</text>
							</view>
							<view class="stats-grid">
								<view class="stat-item">
									<text class="stat-label">总用户数</text>
									<text class="stat-value">{{ userStats.totalUsers }}</text>
								</view>
								<view class="stat-item">
									<text class="stat-label">活跃用户</text>
									<text class="stat-value">{{ userStats.activeUsers }}</text>
								</view>
								<view class="stat-item">
									<text class="stat-label">今日新增</text>
									<text class="stat-value">{{ userStats.newUsersToday }}</text>
								</view>
								<view class="stat-item">
									<text class="stat-label">本周新增</text>
									<text class="stat-value">{{ userStats.newUsersThisWeek }}</text>
								</view>
							</view>
						</view>

					</view>

					<!-- 用户详情页面 -->
					<view class="users-content" v-if="activeTab === 'users'">
						<!-- 用户列表 -->
						<view class="user-list">
							<view class="list-header">
								<text class="list-title">用户列表 ({{ userDetails.totalUsers }})</text>
								<view class="sort-controls">
									<text class="sort-label">排序:</text>
									<picker @change="onSortChange" :value="sortIndex" :range="sortOptions">
										<view class="sort-picker">
											<text>{{ sortOptions[sortIndex] }}</text>
										</view>
									</picker>
								</view>
							</view>
							<view class="user-item" v-for="user in userDetails.users" :key="user.userId">
								<image class="user-avatar" :src="getAvatarUrl(user.avatarUrl)" mode="aspectFill"></image>
								<view class="user-info">
									<view class="user-name-row">
										<text class="user-name">{{ user.nickname || '未设置昵称' }}</text>
										<view class="user-status" :class="{ active: user.isActive }">
											<text class="status-text">{{ user.isActive ? '活跃' : '非活跃' }}</text>
										</view>
									</view>
									<text class="user-detail">参与回合: {{ user.roundsParticipated }}</text>
									<text class="user-detail">总流水: ¥{{ formatAmount(user.totalAmount) }}</text>
									<text class="user-detail">注册时间: {{ formatDate(user.createdAt) }}</text>
								</view>
							</view>
						</view>

						<!-- 分页控件 -->
						<view class="pagination" v-if="userDetails.totalPages > 1">
							<button class="page-btn" :disabled="userDetails.currentPage <= 1" @click="changePage(userDetails.currentPage - 1)">
								<text>上一页</text>
							</button>
							<text class="page-info">{{ userDetails.currentPage }} / {{ userDetails.totalPages }}</text>
							<button class="page-btn" :disabled="userDetails.currentPage >= userDetails.totalPages" @click="changePage(userDetails.currentPage + 1)">
								<text>下一页</text>
							</button>
						</view>
					</view>
				</view>
			</scroll-view>
		</view>

		<!-- 加载状态 -->
		<view class="loading-overlay" v-if="loading">
			<view class="loading-content">
				<text class="loading-text">加载中...</text>
			</view>
			</view>
	</view>
</template>

<script>
import { adminStatisticsApi } from '@/api/admin.js'
import AuthManager from '@/utils/auth.js'

export default {
	data() {
		return {
			activeTab: 'overview',
			loading: false,
			refresherTriggered: false,
			
			// 统计数据
			roundStats: {},
			userStats: {
				totalUsers: 0,
				activeUsers: 0,
				inactiveUsers: 0,
				newUsersToday: 0,
				newUsersThisWeek: 0
			},
			
			// 用户详情数据
			userDetails: {
				users: [],
				totalUsers: 0,
				currentPage: 1,
				pageSize: 5,
				totalPages: 0
			},
			
			// 排序选项
			sortOptions: ['注册时间', '最后登录', '参与回合数', '总流水'],
			sortIndex: 0,
			sortBy: 'created_at',
			sortDir: 'desc'
		}
	},
	
	computed: {
		// 按指定顺序排序的回合统计数据
		sortedRoundStats() {
			const order = ['WAITING', 'IN_PROGRESS', 'PLAYING', 'FINISHED', 'CANCELLED']
			const sorted = {}
			
			// 按指定顺序添加数据
			order.forEach(status => {
				if (this.roundStats[status]) {
					sorted[status] = this.roundStats[status]
				}
			})
			
			// 添加其他未在顺序中的状态
			Object.keys(this.roundStats).forEach(status => {
				if (!order.includes(status)) {
					sorted[status] = this.roundStats[status]
				}
			})
			
			return sorted
		}
	},

	onLoad() {
		// 检查管理员权限
		this.checkAdminPermission()
	},
	
	methods: {
		// 检查管理员权限
		async checkAdminPermission() {
			try {
				const userInfo = uni.getStorageSync('userInfo')
				if (!userInfo) {
					console.log('未找到用户信息')
					uni.showToast({
						title: '请先登录',
						icon: 'none'
					})
					setTimeout(() => {
						uni.navigateBack()
					}, 1500)
					return
				}
				
				// 兼容不同的用户ID字段名
				const userId = userInfo.userId || userInfo.user_id || userInfo.id
				console.log('管理员权限检查 - 用户信息:', userInfo)
				console.log('管理员权限检查 - 提取的用户ID:', userId)
				
				// 检查用户ID是否为1（管理员）
				const isAdmin = userId === 1 || userId === '1'
				console.log('管理员权限检查 - 是否为管理员:', isAdmin)
				
				if (!isAdmin) {
					uni.showToast({
						title: '无权限访问',
						icon: 'none'
					})
					setTimeout(() => {
						uni.navigateBack()
					}, 1500)
					return
				}
				
				// 加载数据
				await this.loadAllData()
			} catch (error) {
				console.error('权限检查失败:', error)
				uni.showToast({
					title: '权限检查失败',
					icon: 'none'
				})
			}
		},
		
		// 切换Tab
		switchTab(tab) {
			this.activeTab = tab
			
			// 根据tab加载对应数据
			if (tab === 'users' && this.userDetails.users.length === 0) {
				this.loadUserDetails()
			}
		},
		
		// 加载所有数据
		async loadAllData() {
			this.loading = true
			try {
				await this.loadOverviewData()
			} catch (error) {
				console.error('加载数据失败:', error)
				uni.showToast({
					title: '加载数据失败',
					icon: 'none'
				})
			} finally {
				this.loading = false
			}
		},
		
		// 加载总览数据
		async loadOverviewData() {
			try {
				const response = await adminStatisticsApi.getStatisticsOverview()
			console.log('API原始响应:', response)
			// 检查响应格式，适配不同的返回结构
			if (response && (response.success !== false)) {
				// 处理后端返回的下划线命名数据
				const data = response.data || response
					
					// 处理回合统计数据，保存完整的数据结构（包含round_ids）
					const roundStatsData = data.round_statistics || data.roundStatistics || {}
					this.roundStats = {}
					for (const [key, value] of Object.entries(roundStatsData)) {
						if (typeof value === 'object' && value.count !== undefined) {
							// 保存完整的数据结构：{ count: number, round_ids: array }
							this.roundStats[key] = {
								count: value.count || 0,
								round_ids: value.round_ids || value.roundIds || []
							}
						} else {
							// 兼容旧格式，只有数字的情况
							this.roundStats[key] = {
								count: typeof value === 'number' ? value : 0,
								round_ids: []
							}
						}
					}
					
					// 用户统计数据映射
					const userStatsData = data.user_statistics || data.userStatistics || {}
					this.userStats = {
						totalUsers: userStatsData.total_users || userStatsData.totalUsers || 0,
						activeUsers: userStatsData.active_users || userStatsData.activeUsers || 0,
						inactiveUsers: userStatsData.inactive_users || userStatsData.inactiveUsers || 0,
						newUsersToday: userStatsData.new_users_today || userStatsData.newUsersToday || 0,
						newUsersThisWeek: userStatsData.new_users_this_week || userStatsData.newUsersThisWeek || 0
					}
					
					console.log('原始后端数据:', response.data)
					console.log('加载的统计数据:', {
						roundStats: this.roundStats,
						userStats: this.userStats
					})
				} else {
					console.error('API响应失败:', response)
				}
			} catch (error) {
				console.error('加载总览数据失败:', error)
				throw error
			}
		},
		
		// 加载用户详情
		async loadUserDetails() {
			this.loading = true
			try {
				const response = await adminStatisticsApi.getUserDetails({
				page: this.userDetails.currentPage,
				size: this.userDetails.pageSize,
				sortBy: this.sortBy,
				sortDir: this.sortDir
			})
			console.log('用户详情API原始响应:', response)
			if (response && (response.success !== false)) {
				// 处理后端返回的下划线命名数据
				const data = response.data || response
					
					// 映射用户列表数据
					const users = (data.users || []).map(user => ({
						userId: user.user_id || user.userId || user.id,
						nickname: user.nickname,
						avatarUrl: user.avatar_url || user.avatarUrl,
						isActive: user.is_active || user.isActive,
						roundsParticipated: user.rounds_participated || user.roundsParticipated || 0,
						totalAmount: user.total_amount || user.totalAmount || 0,
						createdAt: user.created_at || user.createdAt
					}))
					
					this.userDetails = {
						users: users,
						totalUsers: data.total_users || data.totalUsers || 0,
						currentPage: data.current_page || data.currentPage || 1,
						pageSize: data.page_size || data.pageSize || 10,
						totalPages: data.total_pages || data.totalPages || 0
					}
					console.log('用户详情数据:', this.userDetails)
				}
			} catch (error) {
				console.error('加载用户详情失败:', error)
				uni.showToast({
					title: '加载用户详情失败',
					icon: 'none'
				})
			} finally {
				this.loading = false
			}
		},
		
		// 刷新数据
		async refreshData() {
			try {
				// 刷新缓存
				await adminStatisticsApi.refreshStatisticsCache()
				
				// 重新加载数据
				await this.loadAllData()
				
				// 根据当前tab重新加载对应数据
				if (this.activeTab === 'users') {
					await this.loadUserDetails()
				}
				
				uni.showToast({
					title: '刷新成功',
					icon: 'success'
				})
			} catch (error) {
				console.error('刷新数据失败:', error)
				uni.showToast({
					title: '刷新失败',
					icon: 'none'
				})
			}
		},
		
		// 下拉刷新
		async onRefresh() {
			this.refresherTriggered = true
			try {
				await this.refreshData()
			} finally {
				this.refresherTriggered = false
			}
		},
		
		// 下拉刷新恢复
		onRestore() {
			this.refresherTriggered = false
		},
		
		// 排序变更
		onSortChange(e) {
			this.sortIndex = e.detail.value
			const sortMappings = {
				0: { sortBy: 'created_at', sortDir: 'desc' },
				1: { sortBy: 'last_login_at', sortDir: 'desc' },
				2: { sortBy: 'rounds_participated', sortDir: 'desc' },
				3: { sortBy: 'total_amount', sortDir: 'desc' }
			}
			
			const mapping = sortMappings[this.sortIndex]
			this.sortBy = mapping.sortBy
			this.sortDir = mapping.sortDir
			
			// 重新加载用户数据
			this.userDetails.currentPage = 1
			this.loadUserDetails()
		},
		
		// 分页变更
		changePage(page) {
			if (page < 1 || page > this.userDetails.totalPages) return
			
			this.userDetails.currentPage = page
			this.loadUserDetails()
		},
		
		// 获取回合状态标签
		getRoundStatusLabel(status) {
			const labels = {
				'WAITING': '等待中',
				'IN_PROGRESS': '进行中',
				'PLAYING': '进行中',
				'FINISHED': '已结束',
				'CANCELLED': '已取消'
			}
			return labels[status] || status
		},
		
		// 获取回合状态图标
		getRoundStatusIcon(status) {
			const icons = {
				'WAITING': '⏳',
				'IN_PROGRESS': '🎮',
				'FINISHED': '✅',
				'CANCELLED': '❌'
			}
			return icons[status] || '📊'
		},
		
		// 获取统计数量
		getStatCount(stat) {
			if (typeof stat === 'object') {
				return stat.count !== undefined ? stat.count : 0
			}
			return stat
		},

		// 跳转到回合列表页
		navigateToRoundList(status) {
			const roundData = this.roundStats[status] || {}
			const roundIds = roundData.round_ids || []
			
			// 将round_ids数组转换为字符串传递
			const roundIdsStr = roundIds.length > 0 ? roundIds.join(',') : ''
			
			uni.navigateTo({
				url: `/pages/admin/round-list?status=${status}&title=${this.getRoundStatusLabel(status)}&round_ids=${roundIdsStr}`
			})
		},
		
		// 格式化金额
		formatAmount(amount) {
			if (!amount) return '0.00'
			return parseFloat(amount).toFixed(2)
		},
		
		// 格式化日期
		formatDate(dateString) {
			if (!dateString) return '-'
			const date = new Date(dateString)
			return date.toLocaleDateString('zh-CN')
		},
		
		// 获取头像URL
		getAvatarUrl(avatarUrl) {
			return AuthManager.getAvatarUrl(avatarUrl)
		}
	}
}
</script>

<style scoped>
.container {
	min-height: 100vh;
	background-color: $chess-bg-primary;
	display: flex;
	flex-direction: column;
}

/* 头部区域 */
.header-section {
	background: linear-gradient(135deg, $chess-color-gold 0%, $chess-color-dark 100%);
	padding: 40rpx 32rpx 60rpx;
	color: $chess-bg-primary;
	position: relative;
	border-bottom: 2rpx solid rgba(212, 175, 55, 0.3);
}

.admin-header {
	display: flex;
	flex-direction: column;
	align-items: center;
	margin-bottom: 30rpx;
}

.admin-title {
	font-size: 48rpx;
	font-weight: bold;
	margin-bottom: 12rpx;
	color: $chess-bg-primary;
}

.admin-subtitle {
	font-size: 28rpx;
	opacity: 0.9;
	color: $chess-bg-primary;
}

.refresh-btn {
	position: absolute;
	top: 40rpx;
	right: 32rpx;
	padding: 16rpx 24rpx;
	background-color: rgba(255, 255, 255, 0.2);
	border-radius: $uni-border-radius-lg;
	backdrop-filter: blur(10rpx);
	border: 1rpx solid rgba(212, 175, 55, 0.3);
	transition: all 0.3s ease;
}

.refresh-btn:hover {
	background-color: rgba(212, 175, 55, 0.3);
	transform: translateY(-2rpx);
}

.refresh-text {
	font-size: 28rpx;
	color: $chess-bg-primary;
	font-weight: 500;
}

/* Tab区域 */
.tab-container {
	flex: 1;
	display: flex;
	flex-direction: column;
	background-color: $chess-bg-primary;
}

.tab-header {
	display: flex;
	background-color: $chess-bg-card;
	border-bottom: 1rpx solid rgba(212, 175, 55, 0.2);
	padding: 0 32rpx;
}

.tab-item {
	flex: 1;
	text-align: center;
	padding: 32rpx 0;
	border-bottom: 4rpx solid transparent;
	transition: all 0.3s ease;
	cursor: pointer;
}

.tab-item:hover {
	background-color: rgba(212, 175, 55, 0.1);
}

.tab-item.active {
	border-bottom-color: $chess-color-gold;
}

.tab-text {
	font-size: 28rpx;
	color: $chess-color-muted;
}

.tab-item.active .tab-text {
	color: $chess-color-gold;
	font-weight: bold;
}

.tab-content-scroll {
	flex: 1;
	height: calc(100vh - 240rpx);
	min-height: 400rpx;
}

.tab-content {
	padding: 20rpx;
	min-height: 100%;
}

/* 统计卡片 */
.stats-card {
	background-color: $chess-bg-card;
	border-radius: $uni-border-radius-lg;
	padding: 30rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 4rpx 12rpx rgba(212, 175, 55, 0.1);
	border: 1rpx solid rgba(212, 175, 55, 0.2);
	transition: all 0.3s ease;
}

.stats-card:hover {
	transform: translateY(-2rpx);
	box-shadow: 0 8rpx 20rpx rgba(212, 175, 55, 0.15);
}

.card-header {
	margin-bottom: 30rpx;
}

.card-title {
	font-size: 32rpx;
	font-weight: bold;
	color: $chess-color-dark;
}

.stats-grid {
	display: grid;
	grid-template-columns: 1fr 1fr;
	gap: 30rpx;
}

.stat-item {
	display: flex;
	flex-direction: column;
	align-items: center;
	text-align: center;
	padding: 20rpx;
	background: linear-gradient(135deg, $chess-bg-secondary 0%, $chess-bg-card 100%);
	border-radius: $uni-border-radius-base;
	border: 1rpx solid rgba(212, 175, 55, 0.2);
	transition: all 0.3s ease;
}

.stat-item:hover {
	transform: translateY(-2rpx);
	box-shadow: 0 4rpx 12rpx rgba(212, 175, 55, 0.1);
}

.stat-label {
	font-size: 26rpx;
	color: $chess-color-muted;
	margin-bottom: 12rpx;
	font-weight: 500;
}

.stat-value {
	font-size: 40rpx;
	font-weight: bold;
	color: $chess-color-gold;
	line-height: 1.2;
}

/* 回合统计容器 */
.round-stats-container {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.round-stat-card {
	display: flex;
	align-items: center;
	padding: 24rpx;
	border-radius: 12rpx;
	border: 2rpx solid transparent;
	transition: all 0.3s ease;
	cursor: pointer;
}

.round-stat-card:hover {
	transform: translateY(-2rpx);
	box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.1);
}

.stat-icon {
	width: 80rpx;
	height: 80rpx;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	margin-right: 24rpx;
}

.icon-text {
	font-size: 36rpx;
}

.stat-content {
	flex: 1;
	display: flex;
	flex-direction: column;
	gap: 4rpx;
}

.stat-number {
	font-size: 48rpx;
	font-weight: bold;
	line-height: 1;
}

/* 移除重复的样式定义，使用统一的样式 */

.stat-arrow {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 40rpx;
	height: 40rpx;
}

.arrow-icon {
	font-size: 32rpx;
	font-weight: bold;
	opacity: 0.6;
}

/* 状态样式 */
.status-waiting {
	background: linear-gradient(135deg, rgba(212, 175, 55, 0.1) 0%, rgba(212, 175, 55, 0.05) 100%);
	border-color: $chess-color-gold;
}

.status-waiting .stat-icon {
	background-color: $chess-color-gold;
	color: $chess-bg-primary;
}

.status-waiting .stat-number {
	color: $chess-color-gold;
}

.status-playing {
	background: linear-gradient(135deg, rgba(82, 196, 26, 0.1) 0%, rgba(82, 196, 26, 0.05) 100%);
	border-color: $chess-color-success;
}

.status-playing .stat-icon {
	background-color: $chess-color-success;
	color: $chess-bg-primary;
}

.status-playing .stat-number {
	color: $chess-color-success;
}

.status-finished {
	background: linear-gradient(135deg, rgba(93, 104, 138, 0.1) 0%, rgba(93, 104, 138, 0.05) 100%);
	border-color: $chess-color-dark;
}

.status-finished .stat-icon {
	background-color: $chess-color-dark;
	color: $chess-bg-primary;
}

.status-finished .stat-number {
	color: $chess-color-dark;
}

.status-cancelled {
	background: linear-gradient(135deg, rgba(255, 77, 79, 0.1) 0%, rgba(255, 77, 79, 0.05) 100%);
	border-color: $chess-color-danger;
}

.status-cancelled .stat-icon {
	background-color: $chess-color-danger;
	color: $chess-bg-primary;
}

.status-cancelled .stat-number {
	color: $chess-color-danger;
}

.card-subtitle {
	font-size: 24rpx;
	color: #999;
	margin-top: 8rpx;
}

.stat-label {
	font-size: 26rpx;
	color: #666;
	margin-bottom: 12rpx;
	font-weight: 500;
}

.stat-value {
	font-size: 40rpx;
	font-weight: bold;
	color: #5D688A;
	line-height: 1.2;
}

/* 用户列表 */
.user-list {
	background-color: $chess-bg-card;
	border-radius: $uni-border-radius-lg;
	padding: 30rpx;
	box-shadow: 0 4rpx 12rpx rgba(212, 175, 55, 0.1);
	border: 1rpx solid rgba(212, 175, 55, 0.2);
}

.list-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 30rpx;
	padding-bottom: 20rpx;
	border-bottom: 1rpx solid rgba(212, 175, 55, 0.2);
}

.list-title {
	font-size: 32rpx;
	font-weight: bold;
	color: $chess-color-dark;
}

.sort-controls {
	display: flex;
	align-items: center;
	gap: 10rpx;
}

.sort-label {
	font-size: 24rpx;
	color: $chess-color-muted;
}

.sort-picker {
	padding: 8rpx 16rpx;
	background-color: $chess-bg-secondary;
	border-radius: $uni-border-radius-sm;
	font-size: 24rpx;
	color: $chess-color-gold;
	border: 1rpx solid rgba(212, 175, 55, 0.3);
}

.user-item {
	display: flex;
	align-items: flex-start;
	padding: 20rpx 0;
	border-bottom: 1rpx solid rgba(212, 175, 55, 0.1);
	gap: 20rpx;
	transition: all 0.3s ease;
}

.user-item:last-child {
	border-bottom: none;
}

.user-item:hover {
	background-color: rgba(212, 175, 55, 0.05);
	border-radius: $uni-border-radius-sm;
}

.user-avatar {
	width: 80rpx;
	height: 80rpx;
	border-radius: 50%;
	background-color: $chess-bg-secondary;
	border: 2rpx solid rgba(212, 175, 55, 0.2);
}

.user-info {
	flex: 1;
}

.user-name-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 8rpx;
}

.user-name {
	font-size: 28rpx;
	font-weight: bold;
	color: #333;
}

.user-status {
	padding: 4rpx 12rpx;
	border-radius: 12rpx;
	background-color: #f0f0f0;
}

.user-status.active {
	background-color: #e6f7ff;
}

.status-text {
	font-size: 20rpx;
	color: #666;
}

.user-status.active .status-text {
	color: #1890ff;
}

.user-detail {
	font-size: 24rpx;
	color: #666;
	margin-bottom: 4rpx;
}

/* 分页 */
.pagination {
	display: flex;
	justify-content: center;
	align-items: center;
	gap: 20rpx;
	padding: 30rpx;
	margin-top: 20rpx;
}

.page-btn {
	padding: 16rpx 32rpx;
	background-color: #5D688A;
	color: white;
	border: none;
	border-radius: 8rpx;
	font-size: 24rpx;
}

.page-btn[disabled] {
	background-color: #ccc;
	color: #999;
}

.page-info {
	font-size: 24rpx;
	color: #666;
}

/* 加载状态 */
.loading-overlay {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background-color: rgba(0, 0, 0, 0.3);
	display: flex;
	justify-content: center;
	align-items: center;
	z-index: 1000;
}

.loading-content {
	background-color: white;
	padding: 40rpx;
	border-radius: 16rpx;
	text-align: center;
}

.loading-text {
	font-size: 28rpx;
	color: #666;
}
</style>