<template>
	<view class="stats-card">
		<view class="card-header">
			<text class="card-title">我的统计</text>
			<text class="card-subtitle">数据概览</text>
		</view>
		
		<view class="stats-grid">
			<view class="stat-item">
				<view class="stat-value">{{ safeStats.totalRounds }}</view>
				<text class="stat-label">总回合数</text>
			</view>
			
			<view class="stat-item">
				<view class="stat-value" :class="winRateClass">{{ formatWinRate(safeStats.winRate) }}%</view>
				<text class="stat-label">胜率</text>
			</view>
			
			<view class="stat-item">
				<view class="stat-value" :class="totalAmountClass">{{ formatAmount(safeStats.totalAmount) }}</view>
				<text class="stat-label">总盈亏</text>
			</view>
			
			<view class="stat-item win-item">
				<view class="stat-value">{{ safeStats.winRounds }}</view>
				<text class="stat-label">胜场</text>
			</view>
			
			<view class="stat-item lose-item">
				<view class="stat-value">{{ safeStats.loseRounds }}</view>
				<text class="stat-label">负场</text>
			</view>
			
			<view class="stat-item draw-item">
				<view class="stat-value">{{ safeStats.drawRounds }}</view>
				<text class="stat-label">平场</text>
			</view>
		</view>
		
		<!-- 管理员入口 -->
		<view class="admin-section" v-if="isAdmin">
			<view class="admin-divider"></view>
			<button class="admin-btn" @click="goToAdminPanel">
				<text class="admin-btn-icon">📊</text>
				<text class="admin-btn-text">管理员统计</text>
				<text class="admin-btn-arrow">›</text>
			</button>
		</view>
	</view>
</template>

<script>
export default {
	name: 'UserStatsCard',
	props: {
		stats: {
			type: Object,
			default: () => ({
				totalRounds: 0,
				winRate: 0,
				totalAmount: 0,
				winAmount: 0,
				winRounds: 0,
				loseRounds: 0,
				drawRounds: 0
			})
		}
	},
	computed: {
		// 确保统计数据有默认值
		safeStats() {
			return {
				totalRounds: this.stats?.totalRounds || 0,
				winRate: this.stats?.winRate || 0,
				totalAmount: this.stats?.totalAmount || 0,
				winAmount: this.stats?.winAmount || 0,
				winRounds: this.stats?.winRounds || 0,
				loseRounds: this.stats?.loseRounds || 0,
				drawRounds: this.stats?.drawRounds || 0
			}
		},
		winRateClass() {
			const rate = this.safeStats.winRate || 0
			if (rate >= 60) return 'win-rate-high'
			if (rate >= 40) return 'win-rate-medium'
			return 'win-rate-low'
		},
		totalAmountClass() {
			const amount = this.safeStats.totalAmount || 0
			if (amount > 0) return 'positive-value'
			if (amount < 0) return 'negative-value'
			return 'neutral-value'
		},
		// 检查是否为管理员
		isAdmin() {
			try {
				const userInfo = uni.getStorageSync('userInfo')
				if (!userInfo) {
					console.log('未找到用户信息')
					return false
				}
				
				// 兼容不同的用户ID字段名
				const userId = userInfo.userId || userInfo.user_id || userInfo.id
				console.log('当前用户信息:', userInfo)
				console.log('提取的用户ID:', userId)
				
				// 检查用户ID是否为1（管理员）
				const isAdminUser = userId === 1 || userId === '1'
				console.log('是否为管理员:', isAdminUser)
				
				return isAdminUser
			} catch (error) {
				console.error('检查管理员权限失败:', error)
				return false
			}
		}
	},
	methods: {
		formatAmount(amount) {
			if (!amount) return '¥0'
			const absAmount = Math.abs(amount)
			return amount >= 0 ? `¥${absAmount}` : `-¥${absAmount}`
		},
		formatWinRate(rate) {
			if (!rate) return '0.0'
			return parseFloat(rate).toFixed(1)
		},
		// 跳转到管理员统计页面
		goToAdminPanel() {
			uni.navigateTo({
				url: '/pages/admin/statistics'
			})
		}
	}
}
</script>

<style scoped lang="scss">
.stats-card {
	background-color: $chess-bg-card;
	border-radius: $uni-border-radius-lg;
	padding: 24rpx;
	margin-bottom: 16rpx;
	box-shadow: 0 2rpx 12rpx rgba(212, 175, 55, 0.1);
	border: 1rpx solid rgba(212, 175, 55, 0.2);
	transition: all 0.3s ease;
}

.stats-card:hover {
	box-shadow: 0 4rpx 20rpx rgba(212, 175, 55, 0.15);
	transform: translateY(-2rpx);
}

.card-header {
	margin-bottom: 24rpx;
	padding-bottom: 16rpx;
	border-bottom: 1rpx solid rgba(212, 175, 55, 0.2);
}

.card-title {
	font-size: 32rpx;
	font-weight: bold;
	color: $chess-color-dark;
	display: block;
	margin-bottom: 8rpx;
}

.card-subtitle {
	font-size: 24rpx;
	color: $chess-color-muted;
}

.stats-grid {
	display: grid;
	grid-template-columns: 1fr 1fr 1fr;
	gap: 16rpx;
	margin-bottom: 0;
}

.stat-item {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 24rpx 16rpx;
	background-color: $chess-bg-secondary;
	border-radius: $uni-border-radius-base;
	position: relative;
	transition: all 0.3s ease;
	border: 1rpx solid rgba(212, 175, 55, 0.1);
}

.stat-item:hover {
	transform: translateY(-2rpx);
	box-shadow: 0 4rpx 12rpx rgba(212, 175, 55, 0.15);
}

.stat-item.win-item {
	background: linear-gradient(135deg, rgba(231, 76, 60, 0.1) 0%, rgba(254, 245, 245, 0.8) 100%);
	border: 1rpx solid rgba(231, 76, 60, 0.3);
}

.stat-item.lose-item {
	background: linear-gradient(135deg, rgba(39, 174, 96, 0.1) 0%, rgba(240, 248, 240, 0.8) 100%);
	border: 1rpx solid rgba(39, 174, 96, 0.3);
}

.stat-item.draw-item {
	background: linear-gradient(135deg, rgba(243, 156, 18, 0.1) 0%, rgba(255, 251, 240, 0.8) 100%);
	border: 1rpx solid rgba(243, 156, 18, 0.3);
}

.stat-value {
	font-size: 32rpx;
	font-weight: bold;
	color: $chess-color-dark;
	margin-bottom: 8rpx;
}

.stat-value.positive-value {
	color: $chess-color-success;
}

.stat-value.negative-value {
	color: $chess-color-error;
}

.stat-value.neutral-value {
	color: $chess-color-warning;
}

/* 胜率专用蓝色系样式 */
.stat-value.win-rate-high {
	color: #1890ff; /* 高胜率：深蓝色 */
}

.stat-value.win-rate-medium {
	color: #40a9ff; /* 中等胜率：中蓝色 */
}

.stat-value.win-rate-low {
	color: #69c0ff; /* 低胜率：浅蓝色 */
}

.win-item .stat-value {
	color: $chess-color-success;
}

.lose-item .stat-value {
	color: $chess-color-error;
}

.draw-item .stat-value {
	color: $chess-color-warning;
}

.stat-label {
	font-size: 22rpx;
	color: $chess-color-muted;
	font-weight: 500;
}

/* 管理员入口样式 */
.admin-section {
	margin-top: 24rpx;
}

.admin-divider {
	height: 1rpx;
	background-color: rgba(212, 175, 55, 0.3);
	margin-bottom: 20rpx;
}

.admin-btn {
	display: flex;
	align-items: center;
	justify-content: space-between;
	width: 100%;
	padding: 20rpx 24rpx;
	background: linear-gradient(135deg, $chess-color-gold 0%, rgba(212, 175, 55, 0.8) 100%);
	border: none;
	border-radius: $uni-border-radius-base;
	box-shadow: 0 4rpx 12rpx rgba(212, 175, 55, 0.3);
	transition: all 0.3s ease;
	border: 1rpx solid rgba(212, 175, 55, 0.4);
}

.admin-btn:active {
	transform: translateY(2rpx);
	box-shadow: 0 2rpx 8rpx rgba(212, 175, 55, 0.4);
}

.admin-btn-icon {
	font-size: 32rpx;
	margin-right: 16rpx;
}

.admin-btn-text {
	flex: 1;
	font-size: 28rpx;
	font-weight: 600;
	color: $chess-bg-primary;
	text-align: left;
}

.admin-btn-arrow {
	font-size: 32rpx;
	color: $chess-bg-primary;
	font-weight: bold;
}

</style>