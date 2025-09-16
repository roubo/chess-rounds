<template>
	<view class="detail-page">
		<!-- 回合信息卡片 -->
		<view class="round-card">
			<view class="round-header">
				<text class="round-name">{{ roundInfo.name }}</text>
				<view class="status-badge" :class="statusClass">
					<text class="status-text">{{ getStatusText(roundInfo.status) }}</text>
				</view>
			</view>
			
			<view class="round-info">
				<view class="info-item">
					<text class="info-label">游戏类型：</text>
					<text class="info-value">{{ roundInfo.gameType }}</text>
				</view>
				<view class="info-item">
					<text class="info-label">创建时间：</text>
					<text class="info-value">{{ formatDateTime(roundInfo.createTime) }}</text>
				</view>
				<view class="info-item">
					<text class="info-label">开始时间：</text>
					<text class="info-value">{{ formatDateTime(roundInfo.startTime) }}</text>
				</view>
				<view class="info-item">
					<text class="info-label">参与人数：</text>
					<text class="info-value">{{ roundInfo.currentPlayers }}/{{ roundInfo.maxPlayers }}</text>
				</view>
				<view class="info-item" v-if="roundInfo.description">
					<text class="info-label">描述：</text>
					<text class="info-value">{{ roundInfo.description }}</text>
				</view>
			</view>
		</view>
		
		<!-- 参与者列表 -->
		<view class="participants-section">
			<view class="section-title">
				<text class="title-text">参与者</text>
				<text class="title-count">({{ participants.length }})</text>
			</view>
			
			<view class="participants-list">
				<view class="participant-item" v-for="participant in participants" :key="participant.id">
					<image class="participant-avatar" :src="getAvatarUrl(participant.avatar)" mode="aspectFill"></image>
					<view class="participant-info">
						<text class="participant-name">{{ participant.nickname }}</text>
						<text class="participant-role">{{ participant.isCreator ? '创建者' : '参与者' }}</text>
					</view>
					<view class="participant-rating">
						<text class="rating-text">{{ participant.rating }}</text>
					</view>
				</view>
				
				<!-- 空位显示 -->
				<view class="empty-slot" v-for="(item, index) in emptySlots" :key="index">
					<view class="empty-avatar">+</view>
					<text class="empty-text">等待加入</text>
				</view>
			</view>
		</view>
		
		<!-- 操作按钮 -->
		<view class="action-section">
			<button class="action-btn join-btn" @click="joinRound" v-if="canJoin">加入回合</button>
			<button class="action-btn leave-btn" @click="leaveRound" v-if="canLeave">退出回合</button>
			<button class="action-btn start-btn" @click="startRound" v-if="canStart">开始回合</button>
			<button class="action-btn spectate-btn" @click="spectateRound" v-if="canSpectate">观战</button>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			roundId: '',
			roundInfo: {
				id: 1,
				name: '象棋友谊赛',
				description: '周末象棋比赛，欢迎各位棋友参加',
				gameType: '象棋',
				status: 'active',
				createTime: new Date(),
				startTime: new Date(Date.now() + 3600000),
				currentPlayers: 2,
				maxPlayers: 4,
				creatorId: 1
			},
			participants: [
				{
					id: 1,
					nickname: '象棋大师',
					avatar: '/static/default-avatar.png',
					rating: 1500,
					isCreator: true
				},
				{
					id: 2,
					nickname: '棋友小王',
					avatar: '/static/default-avatar.png',
					rating: 1200,
					isCreator: false
				}
			],
			currentUserId: 3 // 当前用户ID，用于判断操作权限
		}
	},
	onLoad(options) {
		if (options.id) {
			this.roundId = options.id
			this.loadRoundDetail()
		}
	},
	computed: {
		emptySlots() {
			const emptyCount = this.roundInfo.maxPlayers - this.roundInfo.participants.length
			return Array(emptyCount).fill(null)
		},
		isParticipant() {
			return this.roundInfo.participants.some(p => p.id === this.currentUserId)
		},
		isCreator() {
			return this.roundInfo.creatorId === this.currentUserId
		},
		canJoin() {
			return !this.isParticipant && 
				   this.roundInfo.status === 'waiting' && 
				   this.roundInfo.participants.length < this.roundInfo.maxPlayers
		},
		canLeave() {
			return this.isParticipant && 
				   !this.isCreator && 
				   this.roundInfo.status === 'waiting'
		},
		canStart() {
			return this.isCreator && 
				   this.roundInfo.status === 'waiting'
		},
		canSpectate() {
			return !this.isParticipant && this.roundInfo.status === 'playing'
		},
		statusClass() {
			return this.getStatusClass(this.roundInfo.status)
		}
	},
	methods: {
		getAvatarUrl(avatarUrl) {
			return this.$auth.getAvatarUrl(avatarUrl)
		},
		loadRoundDetail() {
			// 这里应该调用API加载回合详情
			// 目前使用示例数据
			console.log('加载回合详情:', this.roundId)
		},
		formatDateTime(dateTime) {
			if (!dateTime) return ''
			const date = new Date(dateTime)
			return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
		},
		getStatusText(status) {
			const statusMap = {
				'active': '招募中',
				'playing': '进行中',
				'finished': '已结束',
				'cancelled': '已取消'
			}
			return statusMap[status] || '未知'
		},
		getStatusClass(status) {
			return `status-${status}`
		},
		joinRound() {
			uni.showModal({
				title: '确认加入',
				content: '确定要加入这个回合吗？',
				success: (res) => {
					if (res.confirm) {
						// uni.showToast() - 已屏蔽
						// 这里应该调用API加入回合
					}
				}
			})
		},
		leaveRound() {
			uni.showModal({
				title: '确认退出',
				content: '确定要退出这个回合吗？',
				success: (res) => {
					if (res.confirm) {
						// uni.showToast() - 已屏蔽
						// 这里应该调用API退出回合
					}
				}
			})
		},
		startRound() {
			uni.showModal({
				title: '开始回合',
				content: '确定要开始这个回合吗？',
				success: (res) => {
					if (res.confirm) {
						// uni.showToast() - 已屏蔽
						// 这里应该调用API开始回合
					}
				}
			})
		},
		spectateRound() {
			// uni.showToast() - 已屏蔽
		}
	}
}
</script>

<style scoped>
.detail-page {
	padding: 20rpx;
	background-color: #FFF2EF;
	min-height: 100vh;
}

.round-card {
	background-color: #FFFFFF;
	border-radius: 16rpx;
	padding: 40rpx;
	margin-bottom: 30rpx;
	box-shadow: 0 4rpx 12rpx rgba(93, 104, 138, 0.1);
}

.round-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 30rpx;
}

.round-name {
	font-size: 36rpx;
	font-weight: bold;
	color: #2C3E50;
	flex: 1;
}

.status-badge {
	padding: 8rpx 16rpx;
	border-radius: 20rpx;
}

.status-text {
	font-size: 24rpx;
	font-weight: bold;
}

.status-active {
	background-color: #E8F5E8;
	color: #27AE60;
}

.status-playing {
	background-color: #FFF3CD;
	color: #F39C12;
}

.status-finished {
	background-color: #F5F5F5;
	color: #95A5A6;
}

.status-cancelled {
	background-color: #FADBD8;
	color: #E74C3C;
}

.round-info {
	display: flex;
	flex-direction: column;
}

.info-item {
	display: flex;
	margin-bottom: 16rpx;
}

.info-item:last-child {
	margin-bottom: 0;
}

.info-label {
	font-size: 28rpx;
	color: #7F8C8D;
	width: 160rpx;
	flex-shrink: 0;
}

.info-value {
	font-size: 28rpx;
	color: #2C3E50;
	flex: 1;
}

.participants-section {
	background-color: #FFFFFF;
	border-radius: 16rpx;
	padding: 40rpx;
	margin-bottom: 30rpx;
	box-shadow: 0 4rpx 12rpx rgba(93, 104, 138, 0.1);
}

.section-title {
	display: flex;
	align-items: center;
	margin-bottom: 30rpx;
}

.title-text {
	font-size: 32rpx;
	font-weight: bold;
	color: #2C3E50;
}

.title-count {
	font-size: 28rpx;
	color: #7F8C8D;
	margin-left: 8rpx;
}

.participants-list {
	display: flex;
	flex-direction: column;
}

.participant-item {
	display: flex;
	align-items: center;
	padding: 20rpx 0;
	border-bottom: 1rpx solid #F5F5F5;
}

.participant-item:last-child {
	border-bottom: none;
}

.participant-avatar {
	width: 80rpx;
	height: 80rpx;
	border-radius: 40rpx;
	margin-right: 24rpx;
	background-color: #F5F5F5;
}

.participant-info {
	display: flex;
	flex-direction: column;
	flex: 1;
}

.participant-name {
	font-size: 28rpx;
	color: #2C3E50;
	font-weight: bold;
	margin-bottom: 4rpx;
}

.participant-role {
	font-size: 24rpx;
	color: #7F8C8D;
}

.participant-rating {
	margin-left: 20rpx;
}

.rating-text {
	font-size: 26rpx;
	color: #5D688A;
	font-weight: bold;
}

.empty-slot {
	display: flex;
	align-items: center;
	padding: 20rpx 0;
	border-bottom: 1rpx solid #F5F5F5;
}

.empty-slot:last-child {
	border-bottom: none;
}

.empty-avatar {
	width: 80rpx;
	height: 80rpx;
	border-radius: 40rpx;
	margin-right: 24rpx;
	background-color: #F5F5F5;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 32rpx;
	color: #BDC3C7;
	border: 2rpx dashed #BDC3C7;
}

.empty-text {
	font-size: 28rpx;
	color: #BDC3C7;
}

.action-section {
	padding: 0 20rpx;
}

.action-btn {
	width: 100%;
	height: 88rpx;
	font-size: 32rpx;
	font-weight: bold;
	border-radius: 12rpx;
	border: none;
	margin-bottom: 20rpx;
}

.join-btn {
	background-color: #27AE60;
	color: #FFFFFF;
}

.leave-btn {
	background-color: #E74C3C;
	color: #FFFFFF;
}

.start-btn {
	background-color: #5D688A;
	color: #FFFFFF;
}

.spectate-btn {
	background-color: #F39C12;
	color: #FFFFFF;
}
</style>