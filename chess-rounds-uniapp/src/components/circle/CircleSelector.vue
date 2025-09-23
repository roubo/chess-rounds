<template>
	<view class="circle-selector">
		<view class="selector-header">
			<text class="selector-title">我的圈子</text>
			<view class="action-buttons">
				<button class="btn-create" @click="$emit('create')">创建</button>
				<button class="btn-join" @click="$emit('join')">加入</button>
			</view>
		</view>
		
		<!-- 圈子列表 -->
		<view v-if="circles.length > 0" class="circle-list">
			<view 
				v-for="circle in circles" 
				:key="circle.id"
				class="circle-item"
				:class="{ active: selectedCircleId === circle.id }"
				@click="selectCircle(circle.id)"
			>
				<view class="circle-name">{{ circle.name }}</view>
				<view class="circle-info">
					{{ circle.memberCount }}人 
					<text v-if="circle.isOwner" class="owner-tag">·群主</text>
				</view>
			</view>
		</view>
		
		<!-- 空状态 -->
		<view v-else class="circle-empty-state">
			<text class="empty-icon">🎯</text>
			<view class="empty-title">还没有加入任何圈子</view>
			<view class="empty-desc">创建或加入圈子，与朋友一起比拼排行榜</view>
		</view>
	</view>
</template>

<script>
export default {
	name: 'CircleSelector',
	props: {
		circles: {
			type: Array,
			default: () => []
		},
		selectedCircleId: {
			type: [Number, String],
			default: null
		}
	},
	emits: ['select', 'create', 'join'],
	methods: {
		selectCircle(circleId) {
			if (circleId !== this.selectedCircleId) {
				this.$emit('select', circleId)
			}
		}
	}
}
</script>

<style scoped lang="scss">
.circle-selector {
	background: $chess-bg-card;
	border-radius: $uni-border-radius-lg;
	padding: 20rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 2rpx 8rpx rgba(212, 175, 55, 0.1);
	border: 2rpx solid $chess-border-light;
}

.selector-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
}

.selector-title {
	font-size: 32rpx;
	font-weight: 600;
	color: $chess-color-dark;
}

.action-buttons {
	display: flex;
	gap: 16rpx;
}

.btn-create, .btn-join {
	padding: 12rpx 20rpx;
	border-radius: 20rpx;
	font-size: 24rpx;
	border: none;
	transition: all 0.3s ease;
}

.btn-create {
	background: linear-gradient(135deg, $chess-color-gold 0%, darken($chess-color-gold, 8%) 100%);
	color: $chess-bg-primary;
	box-shadow: 0 2rpx 8rpx rgba(212, 175, 55, 0.3);
	
	&:hover {
		transform: translateY(-2rpx);
		box-shadow: 0 4rpx 12rpx rgba(212, 175, 55, 0.4);
	}
}

.btn-join {
	background: linear-gradient(135deg, $chess-bg-secondary 0%, darken($chess-bg-secondary, 5%) 100%);
	color: $chess-color-dark;
	border: 2rpx solid $chess-border-light;
	
	&:hover {
		background: linear-gradient(135deg, darken($chess-bg-secondary, 3%) 0%, darken($chess-bg-secondary, 8%) 100%);
		border-color: $chess-color-gold;
		transform: translateY(-2rpx);
	}
}
	background: #f0f0f0;
	color: #666;
}

.circle-list {
	display: flex;
	flex-wrap: wrap;
	gap: 16rpx;
}

.circle-item {
	flex: 1;
	min-width: 200rpx;
	padding: 16rpx;
	background: $chess-bg-secondary;
	border-radius: $uni-border-radius-base;
	border: 2rpx solid transparent;
	cursor: pointer;
	transition: all 0.2s ease;
	
	&:hover {
		transform: translateY(-2rpx);
		box-shadow: 0 4rpx 12rpx rgba(212, 175, 55, 0.1);
	}
}

.circle-item.active {
	background: lighten($chess-color-gold, 35%);
	border-color: $chess-color-gold;
	box-shadow: 0 4rpx 12rpx rgba(212, 175, 55, 0.2);
}

.circle-name {
	font-size: 28rpx;
	font-weight: 500;
	color: $chess-color-dark;
	margin-bottom: 8rpx;
}

.circle-info {
	font-size: 24rpx;
	color: $chess-color-muted;
}

.owner-tag {
	color: $chess-color-gold;
	font-weight: 500;
}

.circle-empty-state {
	text-align: center;
	padding: 60rpx 20rpx;
}

.empty-icon {
	font-size: 80rpx;
	margin-bottom: 20rpx;
}

.empty-title {
	font-size: 32rpx;
	font-weight: 500;
	color: $chess-color-dark;
	margin-bottom: 12rpx;
}

.empty-desc {
	font-size: 26rpx;
	color: $chess-color-muted;
	line-height: 1.5;
}
</style>