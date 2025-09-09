<template>
	<view class="round-card" @click="goToDetail">
		<!-- 回合状态和时间 -->
		<view class="card-header">
			<view class="create-time-left">
				<text class="create-time-text">{{ formatCreateTime(round.createdAt) }}</text>
			</view>
			<view class="round-info">
				<text class="round-status" :class="statusClass">{{ getStatusText(round.status) }}</text>
				<text class="round-time">共{{ round.recordCount || 0 }}局</text>
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
		
		<!-- 操作按钮 -->
		<view class="card-actions" v-if="canJoin || canSpectate">
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
			return `status-${this.round.status?.toLowerCase()}`
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
		}
	},
	methods: {
		getStatusText(status) {
			console.log('Status value:', status, 'Type:', typeof status)
			const statusMap = {
				waiting: '组局中',
				playing: '进行中',
				finished: '已结束',
				cancelled: '已取消'
			}
			return statusMap[status] || `未知(${status})`
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
			const now = new Date()
			const diff = now.getTime() - date.getTime()
			
			// 小于1分钟
			if (diff < 60 * 1000) {
				return '刚刚'
			}
			
			// 小于1小时
			if (diff < 60 * 60 * 1000) {
				const minutes = Math.floor(diff / (60 * 1000))
				return `${minutes}分钟前`
			}
			
			// 小于24小时
			if (diff < 24 * 60 * 60 * 1000) {
				const hours = Math.floor(diff / (60 * 60 * 1000))
				return `${hours}小时前`
			}
			
			// 小于7天
			if (diff < 7 * 24 * 60 * 60 * 1000) {
				const days = Math.floor(diff / (24 * 60 * 60 * 1000))
				return `${days}天前`
			}
			
			// 超过7天显示具体日期
			const month = (date.getMonth() + 1).toString().padStart(2, '0')
			const day = date.getDate().toString().padStart(2, '0')
			return `${month}-${day}`
		},
		goToDetail() {
			console.log(this.round)
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
				
				uni.showToast({
					title: '加入成功',
					icon: 'success'
				})
				
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

.card-actions {
	display: flex;
	justify-content: flex-end;
	gap: 12rpx;
	padding-top: 16rpx;
	border-top: 1rpx solid #e9ecef;
}
</style>