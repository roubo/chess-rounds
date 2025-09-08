<template>
	<view class="stats-card">
		<view class="card-header">
			<text class="card-title">我的统计</text>
			<text class="card-subtitle">数据概览</text>
		</view>
		
		<view class="stats-grid">
			<view class="stat-item">
				<view class="stat-value">{{ stats.totalRounds }}</view>
				<text class="stat-label">总回合数</text>
			</view>
			
			<view class="stat-item">
				<view class="stat-value">{{ stats.winRate }}%</view>
				<text class="stat-label">胜率</text>
			</view>
			
			<view class="stat-item">
				<view class="stat-value">¥{{ stats.totalAmount || 0 }}</view>
				<text class="stat-label">金额</text>
			</view>
			
			<view class="stat-item win-item">
				<view class="stat-value">{{ stats.wins }}</view>
				<text class="stat-label">胜场</text>
			</view>
			
			<view class="stat-item lose-item">
				<view class="stat-value">{{ stats.losses }}</view>
				<text class="stat-label">负场</text>
			</view>
			
			<view class="stat-item draw-item">
				<view class="stat-value">{{ stats.draws }}</view>
				<text class="stat-label">平局</text>
			</view>
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
				wins: 0,
				losses: 0,
				draws: 0
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
	background: linear-gradient(135deg, #E8F5E8 0%, #F0F8F0 100%);
	border: 1rpx solid #27AE60;
}

.stat-item.lose-item {
	background: linear-gradient(135deg, #FDF2F2 0%, #FEF5F5 100%);
	border: 1rpx solid #E74C3C;
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

.win-item .stat-value {
	color: #27AE60;
}

.lose-item .stat-value {
	color: #E74C3C;
}

.draw-item .stat-value {
	color: #F39C12;
}

.stat-label {
	font-size: 22rpx;
	color: #7F8C8D;
	font-weight: 500;
}


</style>