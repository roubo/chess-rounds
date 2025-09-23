<template>
	<view class="leaderboard-content">
		<view class="leaderboard-header">
			<text class="leaderboard-title">排行榜</text>
			<button class="sort-button" @click="$emit('toggleSort')">
				{{ sortOrder === 'desc' ? '正向排序' : '负向排序' }}
			</button>
		</view>
		
		<!-- 加载状态 -->
		<view v-if="loading" class="loading-state">
			<text>加载中...</text>
		</view>
		
		<!-- 排行榜列表 -->
		<view v-else-if="leaderboardData.length > 0" class="leaderboard-list">
			<view 
				v-for="(item, index) in leaderboardData" 
				:key="item.userId"
				class="leaderboard-item"
				:class="{ 'current-user': item.isCurrentUser }"
			>
				<view class="rank-number" :class="{ 'top3': item.rank <= 3 }">
					{{ item.rank }}
				</view>
				
				<image 
					v-if="item.avatar || item.avatar_url" 
					:src="item.avatar || item.avatar_url" 
					class="user-avatar"
					mode="aspectFill"
				/>
				<view v-else class="user-avatar default-avatar">
					<text class="avatar-text">{{ item.nickname.charAt(0) }}</text>
				</view>
				
				<view class="user-info">
					<view class="user-nickname">{{ item.nickname }}</view>
					<view v-if="item.isCurrentUser" class="user-tag">我</view>
				</view>
				
				<view class="amount-info">
					<view 
						class="total-amount"
						:class="{ 
							'positive': item.totalAmount > 0, 
							'negative': item.totalAmount < 0 
						}"
					>
						{{ formatAmount(item.totalAmount) }}
					</view>
					<view class="amount-label">总金额</view>
				</view>
			</view>
		</view>
		
		<!-- 空状态 -->
		<view v-else class="empty-state">
			<view class="empty-title">暂无排行榜数据</view>
			<view class="empty-desc">圈子成员完成对局后将显示排行榜</view>
		</view>
	</view>
</template>

<script>
export default {
	name: 'LeaderboardList',
	props: {
		leaderboardData: {
			type: Array,
			default: () => []
		},
		loading: {
			type: Boolean,
			default: false
		},
		sortOrder: {
			type: String,
			default: 'desc'
		}
	},
	emits: ['toggleSort'],
	methods: {
		formatAmount(amount) {
			if (amount === 0) return '0'
			const sign = amount > 0 ? '+' : ''
			return `${sign}${amount}`
		}
	}
}
</script>

<style scoped lang="scss">
.leaderboard-content {
	background: $chess-bg-card;
	border-radius: $uni-border-radius-lg;
	padding: 20rpx;
	box-shadow: 0 2rpx 8rpx rgba(212, 175, 55, 0.1);
	border: 2rpx solid $chess-border-light;
}

.leaderboard-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
}

.leaderboard-title {
	font-size: 32rpx;
	font-weight: 600;
	color: $chess-color-dark;
}

.sort-button {
	padding: 12rpx 20rpx;
	background: linear-gradient(135deg, $chess-bg-secondary 0%, darken($chess-bg-secondary, 5%) 100%);
	border: 2rpx solid $chess-border-light;
	border-radius: 20rpx;
	font-size: 24rpx;
	color: $chess-color-muted;
	transition: all 0.3s ease;
	
	&:hover {
		background: linear-gradient(135deg, darken($chess-bg-secondary, 3%) 0%, darken($chess-bg-secondary, 8%) 100%);
		border-color: $chess-color-gold;
		color: $chess-color-dark;
		transform: translateY(-2rpx);
	}
}

.loading-state {
	display: flex;
	justify-content: center;
	align-items: center;
	padding: 40rpx;
	color: $chess-color-muted;
}

.leaderboard-list {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.leaderboard-item {
	display: flex;
	align-items: center;
	padding: 20rpx;
	background: $chess-bg-secondary;
	border-radius: $uni-border-radius-lg;
	border-left: 4rpx solid $chess-border-light;
	transition: all 0.2s ease;
	box-shadow: 0 2rpx 8rpx rgba(212, 175, 55, 0.05);
	
	&:hover {
		transform: translateY(-2rpx);
		box-shadow: 0 4rpx 12rpx rgba(212, 175, 55, 0.1);
	}
}

.leaderboard-item.current-user {
	background: linear-gradient(135deg, lighten($chess-color-gold, 35%) 0%, lighten($chess-color-gold, 30%) 100%);
	border-left-color: $chess-color-gold;
	box-shadow: 0 4rpx 12rpx rgba(212, 175, 55, 0.2);
}

.rank-number {
	width: 60rpx;
	text-align: center;
	font-size: 28rpx;
	font-weight: 600;
	color: $chess-color-muted;
}

.rank-number.top3 {
	color: $chess-color-gold;
	background: linear-gradient(135deg, $chess-color-gold, darken($chess-color-gold, 15%));
	-webkit-background-clip: text;
	-webkit-text-fill-color: transparent;
}

.user-avatar {
	width: 60rpx;
	height: 60rpx;
	border-radius: 50%;
	margin: 0 20rpx;
	box-shadow: 0 2rpx 8rpx rgba(212, 175, 55, 0.1);
}

.default-avatar {
	background: linear-gradient(135deg, $chess-bg-secondary 0%, darken($chess-bg-secondary, 10%) 100%);
	display: flex;
	align-items: center;
	justify-content: center;
	border: 2rpx solid $chess-border-light;
}

.avatar-text {
	font-size: 24rpx;
	color: $chess-color-dark;
	font-weight: 500;
}

.user-info {
	flex: 1;
}

.user-nickname {
	font-size: 28rpx;
	font-weight: 500;
	color: $chess-color-dark;
	margin-bottom: 4rpx;
}

.user-tag {
	font-size: 20rpx;
	color: $chess-color-gold;
	background: linear-gradient(135deg, lighten($chess-color-gold, 35%) 0%, lighten($chess-color-gold, 30%) 100%);
	padding: 2rpx 8rpx;
	border-radius: $uni-border-radius-sm;
	display: inline-block;
	border: 1rpx solid $chess-color-gold;
}

.amount-info {
	text-align: right;
}

.total-amount {
	font-size: 32rpx;
	font-weight: 600;
	color: $chess-color-dark;
}

.total-amount.positive {
	color: #f44336;
}

.total-amount.negative {
	color: #4caf50;
}

.amount-label {
	font-size: 20rpx;
	color: $chess-color-muted;
	margin-top: 4rpx;
}

.empty-state {
	text-align: center;
	padding: 60rpx 20rpx;
}

.empty-icon {
	font-size: 80rpx;
	color: $chess-color-muted;
	margin-bottom: 20rpx;
	display: block;
}

.empty-title {
	font-size: 28rpx;
	color: $chess-color-dark;
	margin-bottom: 16rpx;
}

.empty-desc {
	font-size: 24rpx;
	color: $chess-color-muted;
	line-height: 1.5;
}
</style>