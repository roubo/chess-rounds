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
					:class="{ active: activeTab === 'circle' }"
					@click="switchTab('circle')"
				>
					<text class="tab-text">圈子排行榜</text>
				</view>
				<view 
					class="tab-item" 
					:class="{ active: activeTab === 'history' }"
					@click="switchTab('history')"
				>
					<text class="tab-text">历史记录</text>
				</view>
			</view>
			
			<!-- Tab内容区域 - 简化scroll-view配置 -->
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
					
					<!-- 圈子排行榜页面 -->
					<view class="circle-content" v-if="activeTab === 'circle'">
						<view class="circle-main" v-if="isLoggedIn">
							<!-- 有圈子时显示标题、圈子选择器和排行榜 -->
							<view v-if="userCircles.length > 0">
						
								<!-- 圈子选择器和排行榜整合区域 -->
								<view class="circle-leaderboard-container">
									<!-- 圈子选择器 -->
									<view class="circle-selector">
										<view class="dropdown-selector" @click="toggleDropdown">
											<view class="selected-circle">
								<text class="circle-name">{{ selectedCircle ? getCircleDisplayName(selectedCircle) : '选择圈子' }}</text>
								<text class="member-count" v-if="selectedCircle">{{ selectedCircle.member_count }}人</text>
							</view>
											<view class="dropdown-icons">
												<text class="dropdown-arrow" :class="{ 'rotated': isDropdownOpen }">▼</text>
												<text class="add-icon" @click.stop="showCircleActions">+</text>
											</view>
										</view>
										<view class="dropdown-list" v-if="isDropdownOpen">
											<view 
												class="dropdown-item" 
												v-for="circle in userCircles" 
												:key="circle.id"
												@click="selectCircle(circle.id)"
											>
												<text class="circle-name">{{ getCircleDisplayName(circle) }}</text>
												<text class="member-count">{{ circle.member_count }}人</text>
											</view>
										</view>
									</view>
									
									<!-- 排行榜内容 -->
									<view class="leaderboard-content" v-if="selectedCircleId">
										<view class="leaderboard-header">
											<view class="sort-controls">
												<button 
													class="sort-btn" 
													:class="{ active: sortBy === 'amount' && sortOrder === 'desc' }"
													@click="setSortBy('amount', 'desc')"
													title="按金额正向排序"
												>
													<text class="sort-label">金额↑</text>
												</button>
												<button 
													class="sort-btn" 
													:class="{ active: sortBy === 'amount' && sortOrder === 'asc' }"
													@click="setSortBy('amount', 'asc')"
													title="按金额负向排序"
												>
													<text class="sort-label">金额↓</text>
												</button>
												<button 
													class="sort-btn" 
													:class="{ active: sortBy === 'winRate' && sortOrder === 'desc' }"
													@click="setSortBy('winRate', 'desc')"
													title="按胜率排序"
												>
													<text class="sort-label">胜率</text>
												</button>
											</view>
											<view class="sort-indicator">
												<text class="sort-text">
													{{ sortBy === 'amount' ? (sortOrder === 'desc' ? '按金额正向排序' : '按金额负向排序') : '按胜率排序' }}
												</text>
											</view>
										</view>
										
										<view class="leaderboard-list" v-if="leaderboardData.length > 0">
											<view 
												class="leaderboard-item" 
												v-for="(item, index) in leaderboardData" 
												:key="item.user_id"
												:class="{ 'is-self': item.user_id === currentUserId }"
											>
												<!-- 头像放在原排名位置 -->
												<view class="avatar-section">
													<view class="user-avatar">
														<image 
															class="avatar-img" 
															:src="item.avatar_url || '/static/images/default-avatar.png'" 
															mode="aspectFit"
															@error="handleAvatarError"
														/>
													</view>
												</view>
												
												<!-- 用户信息 -->
												<view class="user-info">
													<view class="user-details">
														<text class="user-name">{{ item.nickname || item.username }}</text>
														<text class="user-stats">{{ item.total_games }}回合 · 胜率{{ (item.win_rate * 100).toFixed(1) }}%</text>
													</view>
												</view>
												
												<!-- 排名序号移到右上角 -->
												<view class="rank-number-corner" :class="{ 'top3': index < 3 }">
													{{ getRankNumber(index) }}
												</view>
												
												<!-- 金额/胜率信息 -->
												<view class="amount-info">
													<text class="amount" :class="{ positive: item.total_amount >= 0, negative: item.total_amount < 0 }" v-if="sortBy === 'amount'">
														{{ item.total_amount >= 0 ? '+' : '' }}{{ item.total_amount }}
													</text>
													<text class="win-rate" v-else>
														{{ (item.win_rate * 100).toFixed(2) }}%
													</text>
												</view>
											</view>
										</view>
										
										<view class="loading-state" v-else-if="leaderboardLoading">
											<text class="loading-text">加载排行榜中...</text>
										</view>
										
										<view class="empty-leaderboard" v-else>
											<text class="empty-text">暂无排行榜数据</text>
										</view>
									</view>
								</view>
							</view>
							
							<!-- 没有圈子时显示空状态 -->
							<view class="empty-circles-state" v-else>
								<view class="empty-icon">🏆</view>
								<text class="empty-title">暂无圈子</text>
								<text class="empty-desc">创建或加入一个圈子，与朋友一起比拼排行榜吧</text>
								<button class="circle-action-btn" @click="showCircleActions">
									<text class="btn-text">创建/加入圈子</text>
								</button>
							</view>
						</view>
						
						<view class="empty-state" v-else>
							<text class="empty-text">登录后查看圈子排行榜</text>
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
		
		<!-- 圈子操作弹窗 -->
		<CircleActionModal 
			:visible="showActionModal" 
			@close="showActionModal = false"
			@create="handleActionCreate"
			@join="handleActionJoin"
		/>
		
		<!-- 圈子弹窗 -->
		<CircleModal 
			:visible="showCreateModal || showJoinModal" 
			:mode="showCreateModal ? 'create' : 'join'"
			@close="hideModal"
			@submit="handleModalConfirm"
		/>
	</view>
</template>

<script>
import UserStatsCard from '@/components/profile/UserStatsCard.vue'
import RoundCard from '@/components/rounds/RoundCard.vue'
import CircleModal from '@/components/circle/CircleModal.vue'
import CircleActionModal from '@/components/circle/CircleActionModal.vue'
import { roundsApi, userApi, handleApiError } from '@/api/rounds.js'
import { circleApi } from '@/api/circle.js'

export default {
	components: {
		UserStatsCard,
		RoundCard,
		CircleModal,
		CircleActionModal
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
			historyLoading: false,
			// 圈子相关数据
			userCircles: [], // 用户加入的圈子列表
			selectedCircleId: null, // 当前选中的圈子ID
			leaderboardData: [], // 排行榜数据
			leaderboardLoading: false, // 排行榜加载状态
			sortBy: 'amount', // 排序字段：amount金额，winRate胜率
			isDropdownOpen: false, // 下拉选择器是否打开
			sortOrder: 'desc', // 排序方式：desc正向，asc负向
			currentUserId: null, // 当前用户ID
			// 弹窗状态
			showCreateModal: false,
			showJoinModal: false,
			showActionModal: false
		}
	},
	computed: {
		userAvatarUrl() {
		return this.$auth.getAvatarUrl(this.userInfo.avatarUrl || this.userInfo.avatar_url || this.userInfo.avatar)
	},
		selectedCircle() {
			return this.userCircles.find(circle => circle.id === this.selectedCircleId) || null
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
		// 切换下拉选择器状态
		toggleDropdown() {
			this.isDropdownOpen = !this.isDropdownOpen
		},
		checkLoginStatus() {
			// 使用新的认证系统检查登录状态
			this.isLoggedIn = this.$auth.isLoggedIn()
			if (this.isLoggedIn) {
				// 获取当前用户ID
				const userInfo = uni.getStorageSync('userInfo')
				this.currentUserId = userInfo && (userInfo.userId || userInfo.user_id || userInfo.id)
				
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
				} else if (this.activeTab === 'circle') {
					this.loadUserCircles()
				}
			} else {
				// 重置数据
				this.currentUserId = null
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
				// 重置圈子相关数据
				this.userCircles = []
				this.selectedCircleId = null
				this.leaderboardData = []
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
				
				if (response && response.content && Array.isArray(response.content)) {
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
					// 确保在没有数据或数据格式不正确时设置为空数组
					this.finishedRounds = []
				}
			} catch (error) {
				console.error('加载历史记录失败:', error)
				handleApiError(error)
				// 确保错误时也设置为空数组，触发空状态显示
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
				} else if (tab === 'circle') {
					this.loadUserCircles()
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
				} else if (this.activeTab === 'circle') {
					await this.loadUserCircles()
					if (this.selectedCircleId) {
						await this.loadLeaderboard()
					}
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
		},
		// 圈子相关方法
		showCircleActions() {
			this.showActionModal = true
		},
		showCreateCircle() {
			this.showCreateModal = true
		},
		showJoinCircle() {
			this.showJoinModal = true
		},
		hideModal() {
			this.showCreateModal = false
			this.showJoinModal = false
			this.showActionModal = false
		},
		handleActionCreate() {
			this.showActionModal = false
			this.showCreateModal = true
		},
		handleActionJoin() {
			this.showActionModal = false
			this.showJoinModal = true
		},
		async handleModalConfirm(data) {
			if (this.showCreateModal) {
				await this.createCircle(data)
			} else if (this.showJoinModal) {
				await this.joinCircle(data.joinCode)
			}
		},
		setSortBy(field, order) {
			if (this.sortBy !== field || this.sortOrder !== order) {
				this.sortBy = field
				this.sortOrder = order
				this.loadLeaderboard()
			}
		},
		setSortOrder(order) {
			if (this.sortOrder !== order) {
				this.sortOrder = order
				this.loadLeaderboard()
			}
		},
		getRankNumber(index) {
			// 金额负向排序时，显示正向排序的序号值
			if (this.sortBy === 'amount' && this.sortOrder === 'asc') {
				// 负向排序时，计算正向排序的序号
				const totalCount = this.leaderboardData.length
				return totalCount - index
			}
			// 其他情况正常显示序号
			return index + 1
		},
		getMedalIcon(index, sortBy = 'amount', sortOrder = 'desc') {
			if (sortBy === 'winRate') {
				// 按胜率排序时的奖牌
				const medals = ['👑', '🎖️', '🏅'] // 皇冠、军功章、奖牌
				return medals[index] || ''
			} else {
				// 按金额排序时的奖牌
				if (sortOrder === 'desc') {
					const medals = ['🏆', '🥈', '🥉'] // 金杯、银牌、铜牌
					return medals[index] || ''
				} else {
					const medals = ['🛡️', '⚡', '💎'] // 盾牌、闪电、钻石（表示损失最少的前三名）
					return medals[index] || ''
				}
			}
		},
		async loadUserCircles() {
			try {
				// 调用API获取用户圈子列表
				const response = await circleApi.getUserCircles()
				
				// 处理后端返回的分页数据结构
				let circles = []
				
				if (response && response.content && Array.isArray(response.content)) {
					// 后端返回的是分页数据结构 Page<CircleInfoResponse>
					circles = response.content
				} else if (response && Array.isArray(response)) {
					// 后端返回的是数组
					circles = response
				} else if (response) {
					// 其他情况，尝试直接使用response
					circles = Array.isArray(response) ? response : [response]
				} else {
					console.error('response为空或未定义')
					circles = []
				}
				
				// 如果没有圈子数据，直接设置为空数组并返回
				if (!circles || circles.length === 0) {
					this.userCircles = []
					this.selectedCircleId = null
					this.leaderboardData = []
					return
				}
				
				// 检查第一个元素的结构
				if (circles.length > 0) {
					// 数据结构检查（仅在开发环境）
				}
				
				// 映射后端字段到前端期望的字段
				const mappedCircles = circles.map((circle, index) => {
					const mappedCircle = {
						id: circle.circle_id,  // 使用正确的后端字段名
						name: circle.name,
						member_count: circle.member_count,  // 使用正确的后端字段名
						isOwner: circle.user_role === 'CREATOR',  // 使用正确的后端字段名
						...circle // 保留其他字段
					}
					
					// 检查关键字段是否存在
					if (!mappedCircle.id) {
						console.error('圈子ID映射失败:', circle)
					}
					if (!mappedCircle.name) {
						console.error('圈子名称映射失败:', circle)
					}
					
					return mappedCircle
				})
				
				// 过滤掉无效的圈子数据
				const filteredCircles = mappedCircles.filter((circle, index) => {
					const hasId = !!circle.id
					const hasName = !!circle.name
					const isValid = hasId && hasName
					
					if (!isValid) {
						console.warn('过滤掉无效圈子:', circle)
					}
					
					return isValid
				})
				
				this.userCircles = filteredCircles
				
				// 如果有圈子且没有选中的圈子，默认选中第一个
				if (this.userCircles.length > 0 && !this.selectedCircleId) {
					this.selectedCircleId = this.userCircles[0].id
					await this.loadLeaderboard()
				} else if (this.userCircles.length === 0) {
					// 确保没有圈子时清空相关数据
					this.selectedCircleId = null
					this.leaderboardData = []
				}
			} catch (error) {
				console.error('加载圈子列表失败:', error)
				handleApiError(error)
				// 确保错误时也设置为空数组，触发空状态显示
				this.userCircles = []
				this.selectedCircleId = null
				this.leaderboardData = []
			}
		},
		async selectCircle(circleId) {
			this.selectedCircleId = circleId
			this.isDropdownOpen = false // 关闭下拉选择器
			await this.loadLeaderboard()
		},
		async loadLeaderboard() {
			if (!this.selectedCircleId) return
			
			try {
				this.leaderboardLoading = true
				
				// 构建排序参数
				let sortParam = 'score,desc' // 默认按积分降序
				if (this.sortBy === 'winRate') {
					sortParam = 'winRate,desc'
				} else if (this.sortBy === 'amount') {
					// 注意：后端可能没有amount字段，这里使用score作为替代
					sortParam = this.sortOrder === 'desc' ? 'score,desc' : 'score,asc'
				}
				
				// 调用API获取排行榜数据
				const response = await circleApi.getLeaderboard(this.selectedCircleId, {
					page: 0,
					size: 50, // 获取前50名
					sort: sortParam
				})
				
				console.log('排行榜API响应:', response)
				
				// 处理API响应数据
				let leaderboardData = []
				if (response && response.content && Array.isArray(response.content)) {
					// 后端返回分页数据结构
					leaderboardData = response.content
				} else if (response && Array.isArray(response)) {
					// 后端直接返回数组
					leaderboardData = response
				} else if (response && response.data && Array.isArray(response.data)) {
					// 其他可能的数据结构
					leaderboardData = response.data
				}
				
				// 转换后端数据格式为前端期望的格式
				const convertedData = leaderboardData.map((item, index) => ({
					rank: item.rank || (index + 1), // 使用后端排名或索引+1
					user_id: item.user_id || item.userId,
					nickname: item.nickname || item.circle_nickname || '未知用户',
					avatar_url: this.$auth.getAvatarUrl(item.avatar_url || item.circle_avatar_url || ''),
					score: item.score || 0,
					wins: item.wins || 0,
					losses: item.losses || 0,
					draws: item.draws || 0,
					total_games: item.total_games || item.totalGames || 0,
					win_rate: item.win_rate || item.winRate || 0,
					total_amount: item.score || 0, // 使用积分作为金额显示
					is_current_user: item.is_current_user || false
				}))
				
				this.leaderboardData = convertedData
				console.log('转换后的排行榜数据:', this.leaderboardData)
				
			} catch (error) {
				console.error('加载排行榜失败:', error)
				handleApiError(error)
				this.leaderboardData = []
			} finally {
				this.leaderboardLoading = false
			}
		},
		toggleSortOrder() {
			this.sortOrder = this.sortOrder === 'desc' ? 'asc' : 'desc'
			this.loadLeaderboard()
		},
		setSortOrder(order) {
			this.sortOrder = order
			this.loadLeaderboard()
		},
		showCreateCircleModal() {
			this.showCreateModal = true
		},
		showJoinCircleModal() {
			this.showJoinModal = true
		},
		hideCreateModal() {
			this.showCreateModal = false
		},
		hideJoinModal() {
			this.showJoinModal = false
		},
		async createCircle(circleData) {
			try {
				// 调用API创建圈子
				const response = await circleApi.createCircle(circleData)
				uni.showToast({ title: '创建成功', icon: 'success' })
				this.hideModal()
				await this.loadUserCircles()
			} catch (error) {
				console.error('创建圈子失败:', error)
				handleApiError(error)
			}
		},
		async joinCircle(joinCode) {
			try {
				// 调用API加入圈子
				const response = await circleApi.joinCircle(joinCode)
				uni.showToast({ title: '加入成功', icon: 'success' })
				this.hideModal()
				await this.loadUserCircles()
			} catch (error) {
				console.error('加入圈子失败:', error)
				handleApiError(error)
			}
		},
		// 头像加载错误处理
		handleAvatarError(e) {
			console.log('头像加载失败，使用默认头像')
			// 可以在这里设置默认头像或其他处理逻辑
		},
		// 获取圈子显示名称（包含邀请码）
		getCircleDisplayName(circle) {
			if (!circle) return ''
			// 如果圈子有邀请码，显示格式为：圈子名称（邀请码）
			if (circle.join_code || circle.join_code || circle.joinCode) {
				const inviteCode = circle.join_code || circle.join_code  || circle.joinCode
				return `${circle.name}（${inviteCode}）`
			}
			// 如果没有邀请码，只显示圈子名称
			return circle.name
		}
	}
}
</script>

<style scoped lang="scss">
.container {
	background: $chess-bg-primary;
	min-height: 100vh;
	display: flex;
	flex-direction: column;
}

.header-section {
	position: sticky;
	top: 0;
	background-color: $chess-bg-primary;
	padding: 30rpx 20rpx 25rpx;
	margin-bottom: 20rpx;
	z-index: 10;
	box-shadow: 0 2rpx 8rpx rgba(212, 175, 55, 0.1);
}

.user-header {
	display: flex;
	align-items: center;
	padding: 40rpx 30rpx;
	background: $chess-bg-card;
	border-radius: $uni-border-radius-lg;
	box-shadow: 0 8rpx 32rpx rgba(212, 175, 55, 0.08);
	position: relative;
	overflow: hidden;
}

.user-header::before {
	content: '';
	position: absolute;
	top: 0;
	left: 0;
	right: 0;
	height: 4rpx;
	background: linear-gradient(90deg, $chess-color-gold, rgba(212, 175, 55, 0.8));
}

.user-avatar {
	width: 110rpx;
	height: 110rpx;
	border-radius: 50%;
	margin-right: 25rpx;
	border: 4rpx solid $chess-bg-card;
	box-shadow: 0 4rpx 16rpx rgba(212, 175, 55, 0.1);
	background: linear-gradient(135deg, $chess-color-gold 0%, rgba(212, 175, 55, 0.8) 100%);
	display: flex;
	align-items: center;
	justify-content: center;
	color: #fff;
	font-size: 40rpx;
	font-weight: 600;
}

.user-info {
	display: flex;
	flex-direction: column;
	flex: 1;
}

.username {
	font-size: 36rpx;
	font-weight: 700;
	color: $chess-color-dark;
	margin-bottom: 12rpx;
	letter-spacing: 0.5rpx;
}

.user-desc {
	font-size: 26rpx;
	color: $chess-color-muted;
	line-height: 1.4;
}

.login-arrow {
	font-size: 40rpx;
	color: $chess-color-gold;
	font-weight: bold;
}

.logout-btn {
	padding: 16rpx 24rpx;
	background: $chess-bg-secondary;
	border: none;
	border-radius: 25rpx;
	font-size: 26rpx;
	color: $chess-color-muted;
	margin-left: 15rpx;
	transition: all 0.3s ease;
}

.logout-btn:active {
	background: $chess-color-muted;
	transform: scale(0.95);
}

.logout-text {
	font-size: 26rpx;
	color: $chess-color-muted;
	font-weight: 500;
}

.tab-container {
	background: $chess-bg-card;
	border-radius: $uni-border-radius-lg;
	margin: 0 20rpx 25rpx;
	overflow: hidden;
	box-shadow: 0 8rpx 32rpx rgba(212, 175, 55, 0.08);
	flex: 1;
	display: flex;
	flex-direction: column;
}

.tab-header {
	display: flex;
	background: $chess-bg-secondary;
	border-bottom: 1rpx solid rgba(212, 175, 55, 0.2);
}

.tab-item {
	flex: 1;
	padding: 30rpx 20rpx;
	text-align: center;
	position: relative;
	transition: all 0.3s ease;
}

.tab-item.active {
	background: $chess-bg-card;
}

.tab-item.active::after {
	content: '';
	position: absolute;
	bottom: 0;
	left: 50%;
	transform: translateX(-50%);
	width: 60rpx;
	height: 4rpx;
	background: linear-gradient(90deg, $chess-color-gold, rgba(212, 175, 55, 0.8));
	border-radius: 2rpx;
}

.tab-text {
	font-size: 28rpx;
	font-weight: 500;
	color: $chess-color-muted;
}

.tab-item.active .tab-text {
	color: $chess-color-gold;
	font-weight: 600;
}

.tab-content-scroll {
	flex: 1;
	/* 移除可能导致iOS兼容性问题的height: 0 */
	/* height: 0; */
	min-height: 0; /* 使用min-height替代 */
	/* iOS兼容性优化 */
	-webkit-overflow-scrolling: touch;
	/* 隐藏滚动条 */
	::-webkit-scrollbar {
		display: none;
	}
	scrollbar-width: none; /* Firefox */
	-ms-overflow-style: none; /* IE 10+ */
}

.tab-content {
	padding: 10rpx;
	padding-bottom: 80rpx; /* 与底部tab保持距离 */
	/* 移除可能导致iOS兼容性问题的min-height */
	/* min-height: 90%; */
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
	color: $chess-color-muted;
	margin-bottom: 30rpx;
}

.btn-primary {
	background: $chess-color-gold;
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
	background: $chess-bg-secondary;
	border-radius: $uni-border-radius-lg;
	border-left: 4rpx solid $chess-color-gold;
}

.history-info {
	display: flex;
	flex-direction: column;
	gap: 8rpx;
}

.history-title {
	font-size: 28rpx;
	font-weight: 500;
	color: $chess-color-dark;
}

.history-time {
	font-size: 24rpx;
	color: $chess-color-muted;
}

.history-result {
	padding: 8rpx 16rpx;
	border-radius: 20rpx;
	font-size: 24rpx;
	font-weight: 500;
}

.history-result.win {
	background: rgba(244, 67, 54, 0.1);
	color: $chess-color-error;
}

.history-result.lose {
	background: rgba(76, 175, 80, 0.1);
	color: $chess-color-success;
}

.history-result.draw {
	background: rgba(255, 152, 0, 0.1);
	color: $chess-color-warning;
}

.result-text {
	font-size: 24rpx;
}

/* 圈子排行榜样式 */
.circle-leaderboard-container {
	padding: 20rpx;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	border-radius: 20rpx;
	box-shadow: 0 8rpx 32rpx rgba(102, 126, 234, 0.3);
	margin-bottom: 20rpx;
	position: relative;
	z-index: 1;
}

/* 圈子选择器样式 */
.circle-selector {
	background: rgba(255, 255, 255, 0.95);
	border-radius: 16rpx;
	padding: 20rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
	backdrop-filter: blur(10rpx);
	position: relative;
	z-index: 200;
}

/* 页面标题样式 */
.page-header {
	margin-bottom: 32rpx;
	padding-bottom: 16rpx;
}

.page-title {
	font-size: 32rpx;
	font-weight: bold;
	color: $chess-color-dark;
	display: block;
	margin-bottom: 8rpx;
}

.page-subtitle {
	font-size: 24rpx;
	color: $chess-color-muted;
}

.selector-header {
	margin-bottom: 24rpx;
	padding-bottom: 16rpx;
	border-bottom: 1rpx solid rgba(212, 175, 55, 0.2);
}

.selector-title {
	font-size: 32rpx;
	font-weight: bold;
	color: $chess-color-dark;
	display: block;
	margin-bottom: 8rpx;
}

.selector-subtitle {
	font-size: 24rpx;
	color: $chess-color-muted;
}

/* 空状态样式 */
.empty-circles-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 120rpx 40rpx;
	text-align: center;
}

.empty-icon {
	font-size: 120rpx;
	margin-bottom: 40rpx;
	opacity: 0.8;
}

.empty-title {
	font-size: 36rpx;
	font-weight: 600;
	color: $chess-color-dark;
	margin-bottom: 20rpx;
	letter-spacing: 0.5rpx;
}

.empty-desc {
	font-size: 28rpx;
	color: $chess-color-muted;
	line-height: 1.6;
	margin-bottom: 60rpx;
	max-width: 500rpx;
}

.circle-action-btn {
	background: linear-gradient(135deg, $chess-color-gold, rgba(212, 175, 55, 0.8));
	color: #fff;
	border: none;
	border-radius: 50rpx;
	padding: 24rpx 48rpx;
	font-size: 30rpx;
	font-weight: 600;
	box-shadow: 0 8rpx 24rpx rgba(212, 175, 55, 0.3);
	transition: all 0.3s ease;
}

.circle-action-btn:active {
	transform: translateY(2rpx);
	box-shadow: 0 4rpx 12rpx rgba(212, 175, 55, 0.4);
}

.btn-text {
	color: #fff;
}

.circle-list {
	display: flex;
	flex-wrap: wrap;
	gap: 20rpx;
}

/* 下拉选择器样式 */
.dropdown-container {
	position: relative;
	margin-bottom: 24rpx;
}

.dropdown-selector {
	display: flex;
	justify-content: space-between;
	align-items: center;
	background: #fff;
	border: 2rpx solid rgba(212, 175, 55, 0.3);
	border-radius: 16rpx;
	padding: 24rpx 32rpx;
	box-shadow: 0 2rpx 8rpx rgba(212, 175, 55, 0.1);
	transition: all 0.3s ease;
	cursor: pointer;
}

.dropdown-selector:hover {
	border-color: rgba(212, 175, 55, 0.5);
	box-shadow: 0 4rpx 12rpx rgba(212, 175, 55, 0.15);
}

.selected-circle {
	display: flex;
	flex-direction: column;
	flex: 1;
}

.selected-circle .circle-name {
	font-size: 28rpx;
	font-weight: 600;
	color: $chess-color-dark;
	margin-bottom: 4rpx;
}

.selected-circle .member-count {
	font-size: 22rpx;
	color: $chess-color-muted;
}

.dropdown-icons {
	display: flex;
	align-items: center;
	gap: 16rpx;
}

.dropdown-arrow {
	font-size: 20rpx;
	color: $chess-color-muted;
	transition: transform 0.3s ease;
}

.dropdown-arrow.rotated {
	transform: rotate(180deg);
}

.add-icon {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 48rpx;
	height: 48rpx;
	background: linear-gradient(135deg, $chess-color-gold, rgba(212, 175, 55, 0.8));
	color: #fff;
	border-radius: 50%;
	font-size: 28rpx;
	font-weight: bold;
	box-shadow: 0 4rpx 12rpx rgba(212, 175, 55, 0.2);
	transition: all 0.3s ease;
}

.add-icon:active {
	transform: translateY(2rpx);
	box-shadow: 0 2rpx 8rpx rgba(212, 175, 55, 0.3);
}

.dropdown-list {
	position: absolute;
	top: 100%;
	left: 0;
	right: 0;
	background: #fff;
	border: 2rpx solid rgba(212, 175, 55, 0.3);
	border-top: none;
	border-radius: 0 0 16rpx 16rpx;
	box-shadow: 0 8rpx 24rpx rgba(212, 175, 55, 0.15);
	z-index: 300;
	max-height: 400rpx;
	overflow-y: auto;
}

.dropdown-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 20rpx 32rpx;
	border-bottom: 1rpx solid rgba(212, 175, 55, 0.1);
	transition: background-color 0.2s ease;
}

.dropdown-item:last-child {
	border-bottom: none;
}

.dropdown-item:hover {
	background-color: rgba(212, 175, 55, 0.05);
}

.dropdown-item .circle-name {
	font-size: 26rpx;
	font-weight: 500;
	color: $chess-color-dark;
}

.dropdown-item .member-count {
	font-size: 22rpx;
	color: $chess-color-muted;
}

.circle-item {
	flex: 1;
	min-width: 200rpx;
	padding: 20rpx 18rpx;
	background: $chess-bg-secondary;
	border-radius: $uni-border-radius-lg;
	border: 2rpx solid transparent;
	cursor: pointer;
	transition: all 0.3s ease;
	box-shadow: 0 2rpx 8rpx rgba(212, 175, 55, 0.05);
}

.circle-item:active {
	transform: scale(0.98);
}

.circle-item.active {
	background: linear-gradient(135deg, rgba(255, 247, 230, 0.8), rgba(250, 246, 232, 0.8));
	border-color: $chess-color-gold;
	box-shadow: 0 4rpx 16rpx rgba(212, 175, 55, 0.2);
}

.circle-name {
	font-size: 30rpx;
	font-weight: 600;
	color: $chess-color-dark;
	margin-bottom: 10rpx;
	letter-spacing: 0.3rpx;
}

.circle-info {
	font-size: 24rpx;
	color: $chess-color-muted;
}

.leaderboard-content {
	background: rgba(255, 255, 255, 0.95);
	border-radius: 16rpx;
	padding: 20rpx;
	box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
	backdrop-filter: blur(10rpx);
	margin-top: 20rpx;
	position: relative;
	z-index: 10;
}

.leaderboard-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
}

.leaderboard-title {
	font-size: 34rpx;
	font-weight: 700;
	color: $chess-color-dark;
	letter-spacing: 0.5rpx;
}

.sort-controls {
	display: flex;
	gap: 8rpx;
}

.sort-btn {
	padding: 8rpx 13rpx;
	background: $chess-bg-secondary;
	border: 2rpx solid transparent;
	border-radius: 16rpx;
	font-size: 18rpx;
	color: $chess-color-muted;
	font-weight: 600;
	transition: all 0.3s ease;
	box-shadow: 0 4rpx 16rpx rgba(212, 175, 55, 0.1);
}

.sort-btn.active {
	background: linear-gradient(135deg, rgba(255, 247, 230, 0.8), rgba(250, 246, 232, 0.8));
	border-color: $chess-color-gold;
	color: $chess-color-gold;
	box-shadow: 0 4rpx 16rpx rgba(212, 175, 55, 0.2);
}

.sort-btn:active {
	transform: translateY(2rpx);
}

.sort-icon {
	font-size: 19rpx;
}

.sort-indicator {
	display: flex;
	align-items: center;
	padding: 10rpx 16rpx;
	background: linear-gradient(135deg, rgba(255, 247, 230, 0.6), rgba(250, 246, 232, 0.6));
	border-radius: 16rpx;
	border: 1rpx solid rgba(212, 175, 55, 0.3);
}

.sort-text {
	font-size: 19rpx;
	color: $chess-color-gold;
	font-weight: 600;
}

.leaderboard-list {
	display: flex;
	flex-direction: column;
	gap: 15rpx;
}

.leaderboard-item {
	display: flex;
	align-items: center;
	padding: 22rpx;
	margin-bottom: 18rpx;
	background: rgba(255, 255, 255, 0.8);
	border-radius: 13rpx;
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
	transition: all 0.3s ease;
	position: relative;
}

.leaderboard-item:hover {
	transform: translateY(-2rpx);
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
}

.leaderboard-item.is-self {
	background: linear-gradient(135deg, rgba(255, 215, 0, 0.15), rgba(255, 193, 7, 0.1));
	border: 2rpx solid rgba(255, 215, 0, 0.3);
}

.leaderboard-item.is-self:hover {
	background: linear-gradient(135deg, rgba(255, 215, 0, 0.2), rgba(255, 193, 7, 0.15));
	border-color: rgba(255, 215, 0, 0.5);
}

// 头像区域样式
.avatar-section {
	display: flex;
	align-items: center;
	margin-right: 22rpx;
}

.avatar-section .avatar {
	width: 66rpx;
	height: 66rpx;
	border-radius: 50%;
	border: 2rpx solid rgba(255, 255, 255, 0.8);
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

// 右上角排名序号样式
.rank-number-corner {
	position: absolute;
	top: 7rpx;
	right: 18rpx;
	width: 29rpx;
	height: 29rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	text-align: center;
	font-size: 18rpx;
	font-weight: 700;
	color: $chess-color-muted;
	line-height: 1;
	border-radius: 50%;
	background: rgba(255, 255, 255, 0.9);
	border: 1rpx solid rgba(0, 0, 0, 0.1);
	box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.1);
	z-index: 10;
	transition: all 0.3s ease;
}

.rank-number-corner.top3 {
	color: #fff;
	background: linear-gradient(135deg, $chess-color-gold 0%, rgba(212, 175, 55, 0.8) 100%);
	font-size: 20rpx;
	font-weight: 800;
	border: none;
	box-shadow: 0 4rpx 12rpx rgba(212, 175, 55, 0.3);
	transform: scale(1.1);
}

.rank-medal {
	position: absolute;
	top: -8rpx;
	right: -8rpx;
	z-index: 2;
	background: rgba(255, 255, 255, 0.9);
	border-radius: 50%;
	padding: 4rpx;
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.medal-icon {
	font-size: 24rpx;
	filter: drop-shadow(0 1rpx 2rpx rgba(0, 0, 0, 0.2));
}

.user-info {
	display: flex;
	flex: 1;
	position: relative; // 为右上角排名序号提供定位基准
}

.user-avatar {
	width: 62rpx;
	height: 62rpx;
	border-radius: 50%;
	margin-right: 18rpx;
	overflow: hidden;
}

.avatar-img {
	width: 100%;
	height: 100%;
	object-fit: cover;
	display: block;
}

.user-details {
	flex: 1;
	display: flex;
	flex-direction: column;
	justify-content: center;
	min-width: 0; // 允许flex子项收缩
}

.user-name {
	font-size: 24rpx;
	font-weight: 600;
	color: $chess-color-dark;
	margin-bottom: 6rpx;
}

.user-stats {
	font-size: 20rpx;
	color: $chess-color-muted;
}

.amount-info {
	text-align: right;
	margin-left: 18rpx;
	display: flex;
	flex-direction: column;
	align-items: flex-end;
	min-width: 88rpx;
}

// 统一的数值容器样式
.value-container {
	font-size: 26rpx;
	font-weight: 600;
	padding: 7rpx 14rpx;
	border-radius: 9rpx;
	background: rgba(0, 0, 0, 0.02);
	transition: all 0.3s ease;
	text-align: center;
	display: inline-block;
	position: relative; // 为伪元素提供定位基准
}

.amount {
	@extend .value-container;
}

.amount.positive {
	color: #ff4d4f; // 正数用红色
	background: linear-gradient(135deg, rgba(255, 77, 79, 0.1) 0%, rgba(255, 77, 79, 0.05) 100%);
	border: 1rpx solid rgba(255, 77, 79, 0.2);
}

.amount.negative {
	color: #52c41a; // 负数用绿色
	background: linear-gradient(135deg, rgba(82, 196, 26, 0.1) 0%, rgba(82, 196, 26, 0.05) 100%);
	border: 1rpx solid rgba(82, 196, 26, 0.2);
}

.amount.positive::before {
	content: '↗';
	position: absolute;
	top: -3rpx;
	right: -3rpx;
	font-size: 14rpx;
	opacity: 0.6;
}

.amount.negative::before {
	content: '↘';
	position: absolute;
	top: -3rpx;
	right: -3rpx;
	font-size: 14rpx;
	opacity: 0.6;
}

.win-rate {
	@extend .value-container;
	background: linear-gradient(135deg, rgba(24, 144, 255, 0.1) 0%, rgba(24, 144, 255, 0.05) 100%);
	border: 1rpx solid rgba(24, 144, 255, 0.2);
	color: #1890ff;
}

.win-rate::before {
	content: '🎯';
	position: absolute;
	top: -3rpx;
	right: -3rpx;
	font-size: 14rpx;
	opacity: 0.6;
}

.total-amount.positive {
	color: #ff4d4f; // 使用红色表示正数
	font-weight: 600;
}

.total-amount.negative {
	color: #52c41a; // 使用绿色表示负数
	font-weight: 600;
}

.amount-label {
	font-size: 22rpx;
	color: $chess-color-muted;
	margin-top: 6rpx;
	font-weight: 500;
}

.loading-state {
	display: flex;
	justify-content: center;
	align-items: center;
	padding: 60rpx;
	color: $chess-color-muted;
	font-size: $uni-font-size-base;
}

.circle-empty-state {
	text-align: center;
	padding: 80rpx 30rpx;
	background: $chess-bg-card;
	border-radius: $uni-border-radius-lg;
	margin: 20rpx;
	box-shadow: 0 8rpx 32rpx rgba(212, 175, 55, 0.08);
}

.empty-icon {
	font-size: 100rpx;
	color: $chess-color-muted;
	margin-bottom: 30rpx;
}

.empty-title {
	font-size: 32rpx;
	color: $chess-color-dark;
	margin-bottom: 20rpx;
	font-weight: 600;
}

.empty-desc {
	font-size: $uni-font-size-base;
	color: $chess-color-muted;
	margin-bottom: 40rpx;
	line-height: 1.5;
}

/* 圈子主容器样式 */
.circle-main {
	background: $chess-bg-card;
	border-radius: $uni-border-radius-lg;
	padding: 15rpx;
	box-shadow: 0 2rpx 12rpx rgba(212, 175, 55, 0.1);
	border: 1rpx solid rgba(212, 175, 55, 0.2);
	position: relative;
	overflow: hidden;
}


</style>