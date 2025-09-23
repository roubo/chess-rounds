<template>
	<view v-if="visible" class="modal-overlay" @click="handleOverlayClick">
		<view class="modal-content" @click.stop>
			<view class="modal-header">
				<text class="modal-title">选择操作</text>
				<text class="close-btn" @click="$emit('close')">×</text>
			</view>
			
			<view class="modal-body">
				<view class="action-list">
					<button class="action-btn create-btn" @click="handleCreate">
						<view class="btn-icon">🎯</view>
						<view class="btn-content">
							<text class="btn-title">创建圈子</text>
							<text class="btn-desc">创建新圈子，邀请朋友加入</text>
						</view>
						<view class="btn-arrow">›</view>
					</button>
					
					<button class="action-btn join-btn" @click="handleJoin">
						<view class="btn-icon">🤝</view>
						<view class="btn-content">
							<text class="btn-title">加入圈子</text>
							<text class="btn-desc">输入邀请码，加入已有圈子</text>
						</view>
						<view class="btn-arrow">›</view>
					</button>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	name: 'CircleActionModal',
	props: {
		visible: {
			type: Boolean,
			default: false
		}
	},
	emits: ['close', 'create', 'join'],
	methods: {
		handleOverlayClick() {
			this.$emit('close')
		},
		handleCreate() {
			this.$emit('create')
			this.$emit('close')
		},
		handleJoin() {
			this.$emit('join')
			this.$emit('close')
		}
	}
}
</script>

<style scoped lang="scss">
.modal-overlay {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(0, 0, 0, 0.6);
	backdrop-filter: blur(8rpx);
	display: flex;
	align-items: center;
	justify-content: center;
	z-index: 1000;
	animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
	from {
		opacity: 0;
	}
	to {
		opacity: 1;
	}
}

@keyframes slideUp {
	from {
		transform: translateY(100rpx);
		opacity: 0;
	}
	to {
		transform: translateY(0);
		opacity: 1;
	}
}

.modal-content {
	background: linear-gradient(135deg, $chess-bg-card 0%, lighten($chess-bg-card, 2%) 100%);
	border-radius: 24rpx;
	width: 640rpx;
	max-height: 85vh;
	overflow: hidden;
	box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.15), 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
	animation: slideUp 0.4s ease-out;
	position: relative;
	border: 2rpx solid $chess-border-light;
	
	&::before {
		content: '';
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		height: 4rpx;
		background: linear-gradient(90deg, $chess-color-gold, lighten($chess-color-gold, 10%), $chess-color-gold);
	}
}

.modal-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 40rpx 40rpx 32rpx;
	background: linear-gradient(135deg, $chess-bg-card 0%, lighten($chess-bg-card, 1%) 100%);
	border-bottom: 1rpx solid $chess-border-light;
	position: relative;
}

.modal-title {
	font-size: 38rpx;
	font-weight: 700;
	color: $chess-color-dark;
	letter-spacing: 1rpx;
}

.close-btn {
	font-size: 44rpx;
	color: $chess-color-muted;
	width: 56rpx;
	height: 56rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	border-radius: 50%;
	background: rgba(153, 153, 153, 0.1);
	transition: all 0.3s ease;
	
	&:hover {
		background: rgba(220, 20, 60, 0.1);
		color: $chess-color-red;
		transform: rotate(90deg);
	}
}

.modal-body {
	padding: 40rpx;
	background: $chess-bg-card;
}

.action-list {
	display: flex;
	flex-direction: column;
	gap: 24rpx;
}

.action-btn {
	display: flex;
	align-items: center;
	padding: 32rpx 28rpx;
	background: linear-gradient(135deg, $chess-bg-secondary 0%, lighten($chess-bg-secondary, 2%) 100%);
	border: 2rpx solid $chess-border-light;
	border-radius: $uni-border-radius-lg;
	transition: all 0.3s ease;
	position: relative;
	overflow: hidden;
	width: 100%; /* 确保按钮占满容器宽度 */
	box-sizing: border-box; /* 包含padding和border在内的宽度计算 */
	
	&::before {
		content: '';
		position: absolute;
		top: 0;
		left: -100%;
		width: 100%;
		height: 100%;
		background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
		transition: left 0.5s;
	}
	
	&:active::before {
		left: 100%;
	}
	
	&:hover {
		transform: translateY(-4rpx);
		box-shadow: 0 8rpx 24rpx rgba(212, 175, 55, 0.15);
		border-color: $chess-color-gold;
	}
	
	&:active {
		transform: translateY(-2rpx);
	}
}

.create-btn {
	&:hover {
		background: linear-gradient(135deg, lighten($chess-color-gold, 35%) 0%, lighten($chess-color-gold, 30%) 100%);
		border-color: $chess-color-gold;
	}
}

.join-btn {
	&:hover {
		background: linear-gradient(135deg, lighten($chess-bg-secondary, 5%) 0%, lighten($chess-bg-secondary, 8%) 100%);
		border-color: $chess-color-gold;
	}
}

.btn-icon {
	font-size: 48rpx;
	margin-right: 24rpx;
	opacity: 0.8;
	flex-shrink: 0; /* 防止图标被压缩 */
	width: 48rpx; /* 固定图标宽度 */
	text-align: center;
}

.btn-content {
	flex: 1;
	text-align: left;
	min-width: 0;
	display: flex;
	flex-direction: column;
	justify-content: center;
	min-height: 80rpx; /* 设置最小高度确保一致性 */
}

.btn-title {
	display: block;
	font-size: 32rpx;
	font-weight: 600;
	color: $chess-color-dark;
	margin-bottom: 8rpx;
	letter-spacing: 0.5rpx;
	white-space: nowrap; /* 防止标题换行 */
	overflow: hidden;
	text-overflow: ellipsis;
}

.btn-desc {
	display: block;
	font-size: 26rpx;
	color: $chess-color-muted;
	line-height: 1.4;
	white-space: nowrap; /* 防止描述换行 */
	overflow: hidden;
	text-overflow: ellipsis;
}

.btn-arrow {
	font-size: 32rpx;
	color: $chess-color-muted;
	margin-left: 16rpx;
	transition: all 0.3s ease;
	flex-shrink: 0; /* 防止箭头被压缩 */
	width: 32rpx; /* 固定箭头宽度 */
	text-align: center;
}

.action-btn:hover .btn-arrow {
	color: $chess-color-gold;
	transform: translateX(4rpx);
}
</style>