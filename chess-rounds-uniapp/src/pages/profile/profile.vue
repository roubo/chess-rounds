<template>
	<view class="container">
		<!-- 顶部用户信息区域 -->
		<view class="header-section">
			<view class="user-header" @click="handleUserClick">
				<image class="user-avatar" :src="userAvatarUrl" mode="aspectFill"></image>
				<view class="user-info">
					<text class="username">{{ userInfo.nickname || '点击登录' }}</text>
					<text class="user-desc">{{ userInfo.description || '登录后查看完整功能' }}</text>
				</view>
				<view class="login-arrow" v-if="!isLoggedIn">›</view>
				<button class="logout-btn" v-if="isLoggedIn" @click.stop="handleLogout">
					<text class="logout-text">退出</text>
				</button>
			</view>
		</view>
		
		<!-- Tab切换和内容区域 -->
		<view class="tab-container">
			<view class="tab-header">
				<view 
					class="tab-item" 
					:class="{ active: activeTab === 'summary' }"
					@click="switchTab('summary')"
				>
					<text class="tab-text">汇总</text>
				</view>
				<view 
					class="tab-item" 
					:class="{ active: activeTab === 'history' }"
					@click="switchTab('history')"
				>
					<text class="tab-text">历史记录</text>
				</view>
			</view>
			
			<!-- Tab内容区域 - 使用scroll-view实现下拉刷新 -->
			<scroll-view 
				class="tab-content-scroll"
				scroll-y="true"
				refresher-enabled="true"
				:refresher-triggered="refresherTriggered"
				@refresherrefresh="onRefresh"
				@refresherrestore="onRestore"
			>
				<view class="tab-content">
					<!-- 汇总页面 -->
					<view class="summary-content" v-if="activeTab === 'summary'">
						<UserStatsCard v-if="isLoggedIn" :stats="userStats" />
						<view class="empty-state" v-else>
							<text class="empty-text">登录后查看数据汇总</text>
							<button class="btn-primary" @click="handleLogin">立即登录</button>
						</view>
					</view>
					
					<!-- 历史记录页面 -->
					<view class="history-content" v-if="activeTab === 'history'">
						<view class="history-list" v-if="isLoggedIn && finishedRounds.length > 0">
							<RoundCard 
								v-for="round in finishedRounds" 
								:key="round.id" 
								:round="round"
								:is-history="true"
								@click="viewRoundDetail"
							/>
						</view>
						<view class="loading-state" v-else-if="isLoggedIn && historyLoading">
							<text class="loading-text">加载中...</text>
						</view>
						<view class="empty-state" v-else>
							<text class="empty-text">{{ isLoggedIn ? '暂无历史记录' : '登录后查看历史记录' }}</text>
							<button class="btn-primary" @click="handleLogin" v-if="!isLoggedIn">立即登录</button>
						</view>
					</view>
				</view>
			</scroll-view>
		</view>
	</view>
</template>

<script>
import UserStatsCard from '@/components/profile/UserStatsCard.vue'
import RoundCard from '@/components/rounds/RoundCard.vue'
import { roundsApi, userApi, handleApiError } from '@/api/rounds.js'

export default {
	components: {
		UserStatsCard,
		RoundCard
	},
	data() {
		return {
			isLoggedIn: false, // 登录状态
			activeTab: 'summary', // 当前激活的tab
			refresherTriggered: false, // 下拉刷新状态
			userInfo: {
				nickname: '',
				avatar: '',
				description: ''
			},
			userStats: {
				totalRounds: 0,
				winRounds: 0,
				loseRounds: 0,
				drawRounds: 0,
				totalAmount: 0,
				winAmount: 0,
				winRate: 0
			},
			historyList: [], // 历史记录列表
			finishedRounds: [],
			historyLoading: false
		}
	},
	computed: {
		userAvatarUrl() {
		return this.$auth.getAvatarUrl(this.userInfo.avatarUrl || this.userInfo.avatar_url || this.userInfo.avatar)
	}
	},
	onLoad() {
		this.checkLoginStatus()
	},
	
	onShow() {
		// 页面显示时检查登录状态，确保从登录页面返回后能更新状态
		this.checkLoginStatus()
	},
	methods: {
		checkLoginStatus() {
			// 使用新的认证系统检查登录状态
			this.isLoggedIn = this.$auth.isLoggedIn()
			if (this.isLoggedIn) {
			// 先初始化默认数据，避免异步加载期间出现undefined
			this.userStats = {
				totalRounds: 0,
				winRounds: 0,
				loseRounds: 0,
				drawRounds: 0,
				totalAmount: 0,
				winAmount: 0,
				winRate: 0
			}
			this.loadUserInfo()
			this.loadUserStats()
			if (this.activeTab === 'history') {
				this.loadHistoryList()
			}
		} else {
				// 重置数据
				this.userInfo = {
					nickname: '点击登录',
					avatarUrl: '/static/images/default-avatar.svg',
					description: '登录后查看完整功能'
				}
				this.userStats = {
					totalRounds: 0,
					winRounds: 0,
					loseRounds: 0,
					drawRounds: 0,
					totalAmount: 0,
					winAmount: 0,
					winRate: 0
				}
				this.historyList = []
				this.finishedRounds = []
			}
		},
		async loadUserInfo() {
			try {
				// 从认证管理器获取用户信息
				const currentUser = this.$auth.getCurrentUser()
				if (currentUser) {
				this.userInfo = {
					nickname: currentUser.nickname || '用户',
					avatarUrl: currentUser.avatarUrl || currentUser.avatar_url || currentUser.avatar,
					description: '回合记忆中'
				}
			} else {
				// 尝试刷新用户信息
				const refreshedUser = await this.$auth.refreshUserInfo()
				if (refreshedUser) {
					this.userInfo = {
						nickname: refreshedUser.nickname || '用户',
						avatarUrl: refreshedUser.avatarUrl || refreshedUser.avatar_url || refreshedUser.avatar,
						description: '回合记忆中'
					}
				}
			}
			} catch (error) {
				console.error('加载用户信息失败:', error)
				// 如果加载失败，使用默认信息
				this.userInfo = {
					nickname: '用户',
					avatarUrl: '/static/images/default-avatar.svg',
					description: '回合记忆中'
				}
			}
		},
		async loadUserStats() {
			try {
				const response = await userApi.getUserStatistics();
				const stats = response.data || response;
				
				// 验证数据完整性
				if (!stats || typeof stats !== 'object') {
					throw new Error('返回数据格式错误');
				}
				
				// 直接使用后端返回的数据，包括winRate，并提供默认值
				this.userStats = {
					totalRounds: stats.totalRounds || 0,
					winRounds: stats.winRounds || 0,
					loseRounds: stats.loseRounds || 0,
					drawRounds: stats.drawRounds || 0,
					totalAmount: Math.round((stats.totalAmount || 0) / 100), // 转换为元
					winAmount: Math.round((stats.winAmount || 0) / 100), // 转换为元
					winRate: ((stats.winRate || 0) * 100).toFixed(1) // 后端返回小数，转换为百分比
				};
			} catch (error) {
				console.error('加载用户统计数据失败:', error);
				uni.showToast({
					title: '加载统计数据失败',
					icon: 'none'
				});
				// 出错时使用默认数据
				this.userStats = {
					totalRounds: 0,
					winRounds: 0,
					loseRounds: 0,
					drawRounds: 0,
					totalAmount: 0,
					winAmount: 0,
					winRate: 0
				};
			}
		},
		async loadHistoryList() {
			try {
				this.historyLoading = true
				const response = await roundsApi.getMyFinishedRounds()
				
				if (response && response.content) {
					// 映射后端字段为前端camelCase格式
					this.finishedRounds = response.content.map(round => ({
						id: round.round_id || round.id,
						title: round.title,
						mahjongType: round.mahjong_type || round.mahjongType,
						multiplier: round.base_amount || round.baseAmount || round.multiplier,
						status: round.status,
						createdAt: round.created_at || round.createdAt,
						updatedAt: round.updated_at || round.updatedAt,
						finishedAt: round.finished_at || round.finishedAt,
						creatorId: round.creator_id || round.creatorId,
						// 保持participants原始数据结构，包含total_amount字段
						participants: round.participants || [],
						recordCount: round.record_count || round.recordCount || 0
					}))
				} else {
					this.finishedRounds = []
				}
			} catch (error) {
				console.error('加载历史记录失败:', error)
				handleApiError(error)
				this.finishedRounds = []
			} finally {
				this.historyLoading = false
			}
		},
		handleLogin() {
			uni.navigateTo({
				url: '/pages/login/login'
			})
		},
		handleUserClick() {
			if (!this.isLoggedIn) {
				this.handleLogin()
			} else {
				// 已登录时跳转到信息编辑页面
				uni.navigateTo({
					url: '/pages/profile/edit'
				})
			}
		},
		switchTab(tab) {
			this.activeTab = tab
			// 切换tab时刷新数据
			if (this.isLoggedIn) {
				if (tab === 'summary') {
					this.loadUserStats()
				} else if (tab === 'history') {
					this.loadHistoryList()
				}
			}
		},
		viewRoundDetail(round) {
			uni.navigateTo({
				url: `/pages/round-detail/round-detail?id=${round.id}`
			})
		},
		// 下拉刷新触发
		onRefresh() {
			this.refresherTriggered = true
			this.refreshData()
		},
		// 下拉刷新恢复
		onRestore() {
			this.refresherTriggered = false
		},
		// 刷新数据
		async refreshData() {
			try {
				if (this.isLoggedIn) {
					// 刷新用户信息
					await this.loadUserInfo()
					
					// 根据当前tab刷新对应数据
					if (this.activeTab === 'summary') {
						await this.loadUserStats()
					} else if (this.activeTab === 'history') {
						await this.loadHistoryList()
					}
				}
			} catch (error) {
				console.error('刷新数据失败:', error)
				// uni.showToast() - 已屏蔽
			} finally {
				// 延迟一点时间再关闭刷新状态，提供更好的用户体验
				setTimeout(() => {
					this.refresherTriggered = false
				}, 500)
			}
		},
		formatTime(timeStr) {
			const date = new Date(timeStr)
			const now = new Date()
			const diff = now - date
			const days = Math.floor(diff / (1000 * 60 * 60 * 24))
			
			if (days === 0) {
				return '今天'
			} else if (days === 1) {
				return '昨天'
			} else if (days < 7) {
				return `${days}天前`
			} else {
				return date.toLocaleDateString()
			}
		},
		getResultText(result) {
			switch(result) {
				case 'win': return '胜利'
				case 'lose': return '失败'
				case 'draw': return '平局'
				default: return '未知'
			}
		},
		handleLogout() {
			uni.showModal({
				title: '确认退出',
				content: '确定要退出登录吗？',
				success: (res) => {
					if (res.confirm) {
						// 使用认证管理器退出登录
						this.$auth.logout()
						
						// 更新页面状态
						this.isLoggedIn = false
						this.userInfo = {
							nickname: '点击登录',
							avatar: '/static/images/default-avatar.svg',
							description: '登录后查看完整功能'
						}
						this.userStats = {
							totalRounds: 0,
							winRounds: 0,
							loseRounds: 0,
							drawRounds: 0,
							totalAmount: 0,
							winAmount: 0,
							winRate: 0
						}
						this.historyList = []
						
						// uni.showToast() - 已屏蔽
					}
				}
			})
		}
	}
}
</script>

<style scoped lang="scss">
.container {
	background: #f5f5f5;
	height: 100vh;
	display: flex;
	flex-direction: column;
}

.header-section {
	padding: 20rpx 15rpx 10rpx;
	margin-bottom: 20rpx;
}

.user-header {
	display: flex;
	align-items: center;
	padding: 15rpx;
	background: #fff;
	border-radius: 12rpx;
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
}

.user-avatar {
	width: 100rpx;
	height: 100rpx;
	border-radius: 50rpx;
	margin-right: 20rpx;
	border: 3rpx solid #d4af37;
}

.user-info {
	display: flex;
	flex-direction: column;
	flex: 1;
}

.username {
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
	margin-bottom: 8rpx;
}

.user-desc {
	font-size: 24rpx;
	color: #666;
}

.login-arrow {
	font-size: 40rpx;
	color: #d4af37;
	font-weight: bold;
}

.logout-btn {
	padding: 8rpx 16rpx;
	background: #f5f5f5;
	border: 1rpx solid #ddd;
	border-radius: 20rpx;
	font-size: 24rpx;
	color: #666;
	margin-left: 10rpx;
}

.logout-btn:active {
	background: #e8e8e8;
}

.logout-text {
	font-size: 24rpx;
	color: #666;
}

.tab-container {
	background: #fff;
	border-radius: 12rpx;
	margin: 0 15rpx 20rpx;
	overflow: hidden;
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
	flex: 1;
	display: flex;
	flex-direction: column;
}

.tab-header {
	display: flex;
	background: #f8f9fa;
}

.tab-item {
	flex: 1;
	padding: 30rpx 0;
	text-align: center;
	position: relative;
}

.tab-item.active {
	background: #fff;
}

.tab-item.active::after {
	content: '';
	position: absolute;
	bottom: 0;
	left: 50%;
	transform: translateX(-50%);
	width: 60rpx;
	height: 4rpx;
	background: #d4af37;
	border-radius: 2rpx;
}

.tab-text {
	font-size: 28rpx;
	font-weight: 500;
	color: #666;
}

.tab-item.active .tab-text {
	color: #d4af37;
	font-weight: 600;
}

.tab-content-scroll {
	flex: 1;
	height: 0;
	/* 隐藏滚动条 */
	::-webkit-scrollbar {
		display: none;
	}
	scrollbar-width: none; /* Firefox */
	-ms-overflow-style: none; /* IE 10+ */
}

.tab-content {
	padding: 20rpx;
	padding-bottom: 80rpx; /* 与底部tab保持距离 */
	min-height: 90%;
}

.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 80rpx 0;
}

.empty-text {
	font-size: 28rpx;
	color: #999;
	margin-bottom: 30rpx;
}

.btn-primary {
	background: #d4af37;
	color: #fff;
	border: none;
	border-radius: 25rpx;
	padding: 20rpx 40rpx;
	font-size: 28rpx;
}

.history-list {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.history-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 24rpx;
	background: #f8f9fa;
	border-radius: 12rpx;
	border-left: 4rpx solid #d4af37;
}

.history-info {
	display: flex;
	flex-direction: column;
	gap: 8rpx;
}

.history-title {
	font-size: 28rpx;
	font-weight: 500;
	color: #333;
}

.history-time {
	font-size: 24rpx;
	color: #999;
}

.history-result {
	padding: 8rpx 16rpx;
	border-radius: 20rpx;
	font-size: 24rpx;
	font-weight: 500;
}

.history-result.win {
	background: #ffebee;
	color: #f44336;
}

.history-result.lose {
	background: #e8f5e8;
	color: #4caf50;
}

.history-result.draw {
	background: #fff3e0;
	color: #ff9800;
}

.result-text {
	font-size: 24rpx;
}
</style>