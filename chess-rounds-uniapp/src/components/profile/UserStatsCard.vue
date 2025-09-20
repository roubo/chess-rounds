<template>
	<view class="stats-card">
		<view class="card-header">
			<text class="card-title">我的统计</text>
			<text class="card-subtitle">数据概览</text>
		</view>
		
		<view class="stats-grid">
			<view class="stat-item">
				<view class="stat-value">{{ stats.totalRounds || 0 }}</view>
				<text class="stat-label">总回合数</text>
			</view>
			
			<view class="stat-item">
				<view class="stat-value" :class="winRateClass">{{ formatWinRate(stats.winRate) }}%</view>
				<text class="stat-label">胜率</text>
			</view>
			
			<view class="stat-item">
				<view class="stat-value" :class="totalAmountClass">{{ formatAmount(stats.totalAmount) }}</view>
				<text class="stat-label">总盈亏</text>
			</view>
			
			<view class="stat-item win-item">
				<view class="stat-value">{{ stats.winRounds || 0 }}</view>
				<text class="stat-label">胜场</text>
			</view>
			
			<view class="stat-item lose-item">
				<view class="stat-value">{{ stats.loseRounds || 0 }}</view>
				<text class="stat-label">负场</text>
			</view>
			
			<view class="stat-item draw-item">
				<view class="stat-value">{{ stats.drawRounds || 0 }}</view>
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
		winRateClass() {
			const rate = this.stats.winRate || 0
			if (rate >= 60) return 'positive-value'
			if (rate >= 40) return 'neutral-value'
			return 'negative-value'
		},
		totalAmountClass() {
			const amount = this.stats.totalAmount || 0
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

<style scoped>
.stats-card {
	background-color: #FFFFFF;
	border-radius: 16rpx;
	padding: 24rpx;
	margin-bottom: 16rpx;
	box-shadow: 0 2rpx 12rpx rgba(93, 104, 138, 0.1);
}

.card-header {
	margin-bottom: 24rpx;
	padding-bottom: 16rpx;
	border-bottom: 1rpx solid #F0F0F0;
}

.card-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #2C3E50;
	display: block;
	margin-bottom: 8rpx;
}

.card-subtitle {
	font-size: 24rpx;
	color: #7F8C8D;
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
	background-color: #F8F9FA;
	border-radius: 12rpx;
	position: relative;
	transition: all 0.3s ease;
}

.stat-item.win-item {
	background: linear-gradient(135deg, #FDF2F2 0%, #FEF5F5 100%);
	border: 1rpx solid #E74C3C;
}

.stat-item.lose-item {
	background: linear-gradient(135deg, #E8F5E8 0%, #F0F8F0 100%);
	border: 1rpx solid #27AE60;
}

.stat-item.draw-item {
	background: linear-gradient(135deg, #FEF9E7 0%, #FFFBF0 100%);
	border: 1rpx solid #F39C12;
}

.stat-value {
	font-size: 32rpx;
	font-weight: bold;
	color: #2C3E50;
	margin-bottom: 8rpx;
}

.stat-value.positive-value {
	color: #E74C3C;
}

.stat-value.negative-value {
	color: #27AE60;
}

.stat-value.neutral-value {
	color: #F39C12;
}

.win-item .stat-value {
	color: #E74C3C;
}

.lose-item .stat-value {
	color: #27AE60;
}

.draw-item .stat-value {
	color: #F39C12;
}

.stat-label {
	font-size: 22rpx;
	color: #7F8C8D;
	font-weight: 500;
}

/* 管理员入口样式 */
.admin-section {
	margin-top: 24rpx;
}

.admin-divider {
	height: 1rpx;
	background-color: #E8E8E8;
	margin-bottom: 20rpx;
}

.admin-btn {
	display: flex;
	align-items: center;
	justify-content: space-between;
	width: 100%;
	padding: 20rpx 24rpx;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	border: none;
	border-radius: 12rpx;
	box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
	transition: all 0.3s ease;
}

.admin-btn:active {
	transform: translateY(2rpx);
	box-shadow: 0 2rpx 8rpx rgba(102, 126, 234, 0.4);
}

.admin-btn-icon {
	font-size: 32rpx;
	margin-right: 16rpx;
}

.admin-btn-text {
	flex: 1;
	font-size: 28rpx;
	font-weight: 600;
	color: #FFFFFF;
	text-align: left;
}

.admin-btn-arrow {
	font-size: 32rpx;
	color: #FFFFFF;
	font-weight: bold;
}

</style>