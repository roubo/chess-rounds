<template>
	<view class="round-card" :class="{ 'spectate-round': isSpectateRound }" @click="goToDetail">
		<!-- 回合状态和时间 -->
		<view class="card-header">
			<view class="create-time-left">
				<text class="create-time-text">{{ formatCreateTime(round.createdAt) }}</text>
			</view>
			<view class="round-info-horizontal">
				<text class="multiplier-hint">倍率x{{ round.multiplier || round.baseAmount || 1 }}</text>
				<text class="round-status" :class="statusClass">{{ getStatusText(round.status) }}</text>
			</view>
		</view>
		
		<!-- 参与者累计列表 -->
		<view class="amounts-list">
			<!-- 参与者累计 -->
			<view 
				v-for="participant in displayParticipants" 
				:key="participant.id"
				class="amount-item-new"
				:class="{ 'current-user': participant.isCurrentUser }"
			>
				<image class="participant-avatar" :src="participant.avatarUrl || participant.avatar || '/static/images/default-avatar.png'" mode="aspectFill" />
				<view class="participant-info">
					<text class="participant-name">{{ participant.nickname || participant.name }}</text>
					<text class="participant-amount" :class="{ 'positive': participant.totalAmount > 0, 'negative': participant.totalAmount < 0 }">
						{{ formatAmount(participant.totalAmount || 0) }}
					</text>
				</view>
			</view>
			

		</view>
		
		<!-- 旁观者列表 -->
		<view v-if="displaySpectators && displaySpectators.length > 0" class="spectators-section">
			<view class="spectators-header">
				<text class="spectators-title">旁观者 ({{ displaySpectators.length }})</text>
			</view>
			<view class="spectators-list">
				<view 
					v-for="spectator in displaySpectators" 
					:key="spectator.id"
					class="spectator-item"
				>
					<image class="spectator-avatar" :src="getSpectatorAvatarUrl(spectator)" mode="aspectFill" />
					<text class="spectator-name">{{ getSpectatorName(spectator) }}</text>
				</view>
			</view>
		</view>
		
		<!-- 操作按钮 -->
		<view class="card-actions" v-if="!isSpectateRound && (canJoin || canSpectate)">
			<button class="btn-primary btn-sm" v-if="canJoin" @click.stop="joinRound">
				加入
			</button>
			<button class="btn-secondary btn-sm" v-if="canSpectate" @click.stop="spectateRound">
				观战
			</button>
		</view>
	</view>
</template>

<script>
import { roundsApi, handleApiError } from '@/api/rounds.js'
import config from '@/config/api.js'

export default {
	name: 'RoundCard',
	props: {
		round: {
			type: Object,
			required: true
		},
		isSpectateRound: {
			type: Boolean,
			default: false
		}
	},
	
	data() {
		return {
			isJoining: false,
			passwordInput: ''
		}
	},
	
	computed: {
		canJoin() {
			return this.round.status === 'watting' && 
				   this.round.currentPlayers < this.round.maxPlayers &&
				   !this.round.isParticipant
		},
		canSpectate() {
			return this.round.status === 'playing' && !this.round.isParticipant
		},
		statusClass() {
			const status = this.round.status
			if (status === 'waiting' || status === 'WAITING') {
				return 'status-waiting'
			} else if (status === 'playing' || status === 'PLAYING' || status === 'ACTIVE') {
				return 'status-playing'
			} else if (status === 'finished' || status === 'FINISHED') {
				// 对于已结束的回合，根据胜负平状态返回相应的类名
				const resultText = this.getUserResultText()
				if (resultText === '胜') {
					return 'result-win'
				} else if (resultText === '负') {
					return 'result-lose'
				} else if (resultText === '平') {
					return 'result-draw'
				}
				return 'status-finished'
			} else if (status === 'cancelled' || status === 'CANCELLED') {
				return 'status-cancelled'
			}
			return ''
		},
		displayParticipants() {
				if (!this.round.participants) return []
				// 过滤掉台板参与者，避免重复显示
				return this.round.participants
					.filter(participant => participant.role !== 'table_board' && participant.role !== 'table')
					.map(participant => {
						const mapped = {
					...participant,
					isCurrentUser: participant.user_id === this.currentUserId,
					totalAmount: participant.total_amount || 0,
					// 映射用户信息字段
					avatar: participant.user_info?.avatar_url || participant.avatar,
					avatarUrl: participant.user_info?.avatar_url || participant.avatarUrl,
					nickname: participant.user_info?.nickname || participant.nickname,
					name: participant.user_info?.nickname || participant.name
				}
				
				// 处理头像URL拼接
				if (mapped.avatarUrl && !mapped.avatarUrl.startsWith('http')) {
					mapped.avatarUrl = config.staticBaseURL + mapped.avatarUrl
				}
				
				console.log('原始participant:', participant)
				console.log('映射后participant:', mapped)
				console.log('头像URL:', mapped.avatarUrl)
				console.log('avatar字段:', mapped.avatar)
				console.log('avatarUrl字段:', mapped.avatarUrl)
				return mapped
					})
		},
		displaySpectators() {
			if (!this.round.spectators) return []
			return this.round.spectators.map(spectator => {
				const mapped = {
					...spectator,
					// 映射用户信息字段
					avatar: spectator.user_info?.avatar_url || spectator.avatar,
					avatarUrl: spectator.user_info?.avatar_url || spectator.avatarUrl,
					nickname: spectator.user_info?.nickname || spectator.nickname,
					name: spectator.user_info?.nickname || spectator.name
				}
				
				// 处理头像URL拼接
				if (mapped.avatarUrl && !mapped.avatarUrl.startsWith('http')) {
					mapped.avatarUrl = config.staticBaseURL + mapped.avatarUrl
				}
				
				return mapped
			})
		}
	},
	methods: {
		getStatusText(status) {
			console.log('Status value:', status, 'Type:', typeof status)
			
			// 如果是已结束的回合，显示胜负平状态
			if (status === 'finished') {
				return this.getUserResultText()
			}
			
			const statusMap = {
				waiting: '等待中',
				WAITING: '等待中',
				playing: '进行中',
				PLAYING: '进行中',
				ACTIVE: '进行中',
				finished: '已结束',
				FINISHED: '已结束',
				cancelled: '已取消',
				CANCELLED: '已取消'
			}
			return statusMap[status] || '未知状态'
		},
		
		// 获取当前用户在已结束回合中的胜负平状态
		getUserResultText() {
			if (!this.round || this.round.status !== 'finished') {
				return '已结束'
			}
			
			// 获取当前用户信息
			const currentUser = this.$auth?.getCurrentUser()
			const currentUserId = currentUser?.userId || currentUser?.user_id
			
			if (!currentUserId) {
				return '已结束'
			}
			
			// 查找当前用户的参与者信息
			const currentUserParticipant = this.round.participants?.find(p => {
				const participantUserId = p.user_info?.user_id || p.user_id || p.id
				return participantUserId == currentUserId
			})
			
			if (!currentUserParticipant) {
				return '已结束'
			}
			
			// 根据total_amount判断胜负平
			const totalAmount = currentUserParticipant.total_amount || currentUserParticipant.totalAmount || 0
			
			if (totalAmount > 0) {
				return '胜'
			} else if (totalAmount < 0) {
				return '负'
			} else {
				return '平'
			}
		},
		getStatusClass(status) {
			return `status-${status?.toLowerCase()}`
		},
		formatTime(timestamp) {
			if (!timestamp) return ''
			
			const date = new Date(timestamp)
			const now = new Date()
			const diff = date.getTime() - now.getTime()
			
			if (diff < 0) {
				return '已开始'
			}
			
			const hours = Math.floor(diff / (1000 * 60 * 60))
			const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
			
			if (hours > 24) {
				const days = Math.floor(hours / 24)
				return `${days}天后`
			} else if (hours > 0) {
				return `${hours}小时后`
			} else {
				return `${minutes}分钟后`
			}
		},
		formatCreateTime(timestamp) {
			if (!timestamp) return ''
			
			const date = new Date(timestamp)
			const year = date.getFullYear()
			const month = (date.getMonth() + 1).toString().padStart(2, '0')
			const day = date.getDate().toString().padStart(2, '0')
			const hours = date.getHours().toString().padStart(2, '0')
			const minutes = date.getMinutes().toString().padStart(2, '0')
			
			// 显示具体的创建时间
			return `${year}-${month}-${day} ${hours}:${minutes}`
		},
		goToDetail() {
			console.log(this.round)
			// 如果是历史模式，发出事件让父组件处理
			if (this.isHistory) {
				this.$emit('click', this.round)
				return
			}
			
			// 如果是组局中状态且当前用户是创建者，跳转到创建页面
			if (this.round.status === 'waiting' && this.round.currentUserRole === 'creator') {
				uni.navigateTo({
					url: `/pages/create-round/create-round?id=${this.round.id}`
				})
			} else {
				// 其他情况跳转到详情页面
				uni.navigateTo({
					url: `/pages/round-detail/round-detail?id=${this.round.id}`
				})
			}
		},
		async joinRound() {
			if (this.isJoining) return
			
			// 如果是私密回合且需要密码
			if (!this.round.isPublic && !this.passwordInput) {
				this.showPasswordDialog()
				return
			}
			
			this.isJoining = true
			
			try {
				await roundsApi.joinRound(this.round.id, this.passwordInput)
				
				// // uni.showToast() - 已屏蔽
				
				// 跳转到回合详情页
				setTimeout(() => {
					uni.navigateTo({
						url: `/pages/round-detail/round-detail?id=${this.round.id}`
					})
				}, 1000)
				
				// 通知父组件刷新数据
				this.$emit('refresh')
				
			} catch (error) {
				handleApiError(error)
			} finally {
				this.isJoining = false
				this.passwordInput = ''
			}
		},
		
		// 显示密码输入对话框
		showPasswordDialog() {
			uni.showModal({
				title: '输入回合密码',
				editable: true,
				placeholderText: '请输入密码',
				success: (res) => {
					if (res.confirm && res.content) {
						this.passwordInput = res.content
						this.joinRound()
					}
				}
			})
		},
		
		spectateRound() {
			// 跳转到观战页面
			uni.navigateTo({
				url: `/pages/round-detail/round-detail?id=${this.round.id}&mode=spectate`
			})
		},
		
		getSpectatorAvatarUrl(spectator) {
			const avatarUrl = spectator.user_info?.avatar_url || spectator.avatarUrl || spectator.avatar
			if (!avatarUrl) {
				return '/static/images/default-avatar.png'
			}
			if (avatarUrl.startsWith('http')) {
				return avatarUrl
			}
			return config.staticBaseURL + avatarUrl
		},
		
		getSpectatorName(spectator) {
			return (spectator.user_info && spectator.user_info.nickname) || spectator.nickname || spectator.name || '旁观者'
		},
		
		formatAmount(amount) {
			if (amount === 0) return '0'
			if (amount > 0) return `+${amount}`
			return `${amount}`
		}
	}
}
</script>

<style scoped lang="scss">
.round-card {
	background-color: white;
	border-radius: 16rpx;
	padding: 20rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 4rpx 12rpx rgba(93, 104, 138, 0.1);
	transition: all 0.3s ease;

	&:active {
		transform: scale(0.98);
	}
}

.card-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 16rpx;
	position: relative;
}



.round-info {
	display: flex;
	flex-direction: column;
	align-items: flex-end;
	gap: 4rpx;

	.round-status {
		font-size: 22rpx;
		font-weight: 500;
		padding: 4rpx 8rpx;
		border-radius: 8rpx;
	}

	.round-time {
		font-size: 22rpx;
		color: $uni-text-color-grey;
	}

	.multiplier-hint {
		font-size: 22rpx;
		color: $uni-color-primary;
		font-weight: 500;
		background-color: lighten($uni-color-primary, 45%);
		padding: 4rpx 8rpx;
		border-radius: 8rpx;
	}
}

.round-info-horizontal {
	display: flex;
	align-items: center;
	gap: 8rpx;

	.round-status {
		font-size: 22rpx;
		font-weight: 500;
		padding: 4rpx 8rpx;
		border-radius: 8rpx;
	}

	.multiplier-hint {
		font-size: 22rpx;
		color: $uni-color-primary;
		font-weight: 500;
		background-color: lighten($uni-color-primary, 45%);
		padding: 4rpx 8rpx;
		border-radius: 8rpx;
	}
}

.create-time-left {
	display: flex;
	align-items: center;

	.create-time-text {
		font-size: 22rpx;
		color: $uni-text-color-grey;
		background-color: #f8f9fa;
		padding: 6rpx 12rpx;
		border-radius: 12rpx;
		border: 1rpx solid #e9ecef;
		font-weight: 500;
	}
}

.status-waiting {
	background-color: #fff3cd;
	color: #856404;
}

.status-playing {
	background-color: #d4edda;
	color: #155724;
}

.status-finished {
	background-color: #d1ecf1;
	color: #0c5460;
}

.status-cancelled {
	background-color: #f8d7da;
	color: #721c24;
}

// 胜负平状态颜色
.result-win {
	background: linear-gradient(135deg, #E8F5E8 0%, #F0F8F0 100%);
	border: 1rpx solid #27AE60;
	color: #27AE60;
}

.result-lose {
	background: linear-gradient(135deg, #FDF2F2 0%, #FEF5F5 100%);
	border: 1rpx solid #E74C3C;
	color: #E74C3C;
}

.result-draw {
	background: linear-gradient(135deg, #FEF9E7 0%, #FFFBF0 100%);
	border: 1rpx solid #F39C12;
	color: #F39C12;
}

.amounts-list {
	display: flex;
	gap: 12rpx;
	margin-bottom: 16rpx;

	.amount-item-new {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 12rpx 8rpx;
		background-color: #f8f9fa;
		border-radius: 12rpx;
		border: 1rpx solid #e9ecef;
		gap: 8rpx;
		flex: 1;
		min-width: 0;



		&.current-user {
			border-color: $uni-color-primary;
			background-color: lighten($uni-color-primary, 45%);
		}

		.participant-avatar {
			width: 60rpx;
			height: 60rpx;
			border-radius: 50%;
			background-color: #ddd;
		}

		.participant-info {
			display: flex;
			flex-direction: column;
			align-items: center;
			gap: 4rpx;
			width: 100%;

			.participant-name {
				font-size: 24rpx;
				font-weight: 500;
				color: $uni-text-color;
				text-align: center;
				white-space: nowrap;
				overflow: hidden;
				text-overflow: ellipsis;
				max-width: 100%;
			}

			.participant-amount {
				font-size: 28rpx;
				font-weight: bold;
				color: $uni-text-color;
				text-align: center;

				&.positive {
					color: $uni-color-success;
				}

				&.negative {
					color: $uni-color-error;
				}
			}
		}
	}
}

.spectators-section {
	margin-top: 24rpx;
	padding-top: 24rpx;
	border-top: 1rpx solid #f0f0f0;
}

.spectators-header {
	margin-bottom: 16rpx;
}

.spectators-title {
	font-size: 24rpx;
	font-weight: 600;
	color: #666;
}

.spectators-list {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
}

.spectator-item {
	display: flex;
	align-items: center;
	background: #f8f9fa;
	border-radius: 20rpx;
	padding: 8rpx 16rpx;
	min-width: 0;
}

.spectator-avatar {
	width: 32rpx;
	height: 32rpx;
	border-radius: 50%;
	margin-right: 8rpx;
	flex-shrink: 0;
}

.spectator-name {
	font-size: 22rpx;
	color: #666;
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
	max-width: 100rpx;
}

.card-actions {
	display: flex;
	justify-content: flex-end;
	gap: 12rpx;
	padding-top: 16rpx;
	border-top: 1rpx solid #e9ecef;
}

// 旁观回合不显示分割线和按钮
.round-card.spectate-round .card-actions {
	border-top: none;
	padding-top: 0;
	display: none;
}
</style>