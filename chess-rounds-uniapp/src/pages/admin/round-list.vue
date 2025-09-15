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
								<!-- 回合状态和时间 -->
								<view class="round-header">
									<view class="status-badge" :class="[
										round.status === 'WAITING' ? 'status-waiting' : '',
										(round.status === 'IN_PROGRESS' || round.status === 'PLAYING') ? 'status-playing' : '',
										round.status === 'FINISHED' ? 'status-finished' : '',
										round.status === 'CANCELLED' ? 'status-cancelled' : ''
									]">
										<text class="status-text">{{ getStatusLabel(round.status) }}</text>
									</view>
									<text class="round-time">{{ formatTime(round.created_at) }}</text>
								</view>

								<!-- 回合信息 -->
								<view class="round-info">
									<view class="info-row">
										<text class="info-label">回合ID:</text>
										<text class="info-value">#{{ round.id }}</text>
									</view>
									<view class="info-row" v-if="round.multiplier">
										<text class="info-label">倍率:</text>
										<text class="info-value">{{ round.multiplier }}x</text>
									</view>
									<view class="info-row" v-if="round.participants_count !== undefined">
										<text class="info-label">参与人数:</text>
										<text class="info-value">{{ round.participants_count }}人</text>
									</view>
									<view class="info-row" v-if="round.spectators_count !== undefined">
										<text class="info-label">旁观人数:</text>
										<text class="info-value">{{ round.spectators_count }}人</text>
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
					
					response = await uni.request({
						url: `${this.$config.baseURL}/rounds/batch`,
						method: 'POST',
						header: headers,
						data: {
							ids: currentPageIds
						}
					})
				} else {
					// 使用原有的搜索接口
					response = await uni.request({
						url: `${this.$config.baseURL}/rounds/search`,
						method: 'GET',
						header: headers,
						data: {
							title: '', // 空标题获取所有回合
							page: this.page - 1, // Spring Boot分页从0开始
							size: this.pageSize,
							status: this.status !== 'ALL' ? this.status : undefined // 传递状态参数
						}
					})
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

.status-badge {
	padding: 8rpx 16rpx;
	border-radius: 20rpx;
	font-size: 24rpx;
}

.status-text {
	font-weight: 500;
}

.round-time {
	font-size: 24rpx;
	color: #999;
}

.round-info {
	display: flex;
	flex-direction: column;
	gap: 12rpx;
}

.info-row {
	display: flex;
	align-items: center;
	gap: 16rpx;
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