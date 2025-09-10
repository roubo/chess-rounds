<template>
	<view class="round-list">
		
		<!-- 回合列表 -->
		<view class="list-content">
			<!-- 加载状态 -->
			<view class="loading-state" v-if="loading">
				<view class="loading-spinner"></view>
				<text class="loading-text">加载中...</text>
			</view>
			
			<!-- 空状态 -->
			<view class="empty-state" v-else-if="rounds.length === 0">
				<view class="empty-icon">🀄</view>
				<text class="empty-title">暂无回合</text>
				<text class="empty-desc">创建一个新的回合开始游戏吧</text>
				<button class="empty-action" @click="handleEmptyAction" v-if="showEmptyAction">
					创建回合
				</button>
			</view>
			
			<!-- 我的回合 -->
			<view class="my-rounds-section" v-if="myRounds.length > 0">
				<view class="section-title">
				<text class="title-text">我的回合</text>
			</view>
				<view class="rounds-container">
					<RoundCard 
						v-for="round in myRounds" 
						:key="round.id" 
						:round="round"
						:is-my-round="true"
						@join="handleJoinRound"
						@spectate="handleSpectateRound"
					/>
				</view>
			</view>
			
			<!-- 旁观回合 -->
			<view class="spectate-rounds-section" v-if="spectateRounds.length > 0">
				<view class="section-title">
					<text class="title-text">我的旁观</text>
				</view>
				<view class="rounds-container">
					<RoundCard 
						v-for="round in spectateRounds" 
						:key="round.id" 
						:round="round"
						:is-spectate-round="true"
						@join="handleJoinRound"
						@spectate="handleSpectateRound"
					/>
				</view>
			</view>
			
			<!-- 底部创建回合按钮 -->
			<view class="bottom-action" v-if="rounds.length > 0">
				<button class="create-round-bottom-btn" @click="handleCreateRound">
					<text class="btn-icon">+</text>
					<text class="btn-text">创建新回合</text>
				</button>
			</view>
		</view>
		

	</view>
</template>

<script>
import RoundCard from './RoundCard.vue'
import { roundsApi, handleApiError } from '@/api/rounds.js'

export default {
	name: 'RoundList',
	components: {
		RoundCard
	},
	props: {
		showFilter: {
			type: Boolean,
			default: true
		},
		showEmptyAction: {
			type: Boolean,
			default: true
		}
	},
	data() {
		return {
			rounds: [],
			loading: false
		}
	},
	onLoad() {
		this.loadRounds()
	},
	onShow() {
		// 页面显示时重新加载数据，确保数据是最新的
		// 只有在已有数据的情况下才刷新，避免首次加载时的重复请求
		if (this.rounds.length > 0) {
			this.loadRounds()
		}
	},
	computed: {
		// 我的回合（参与者或创建者，且participants不为空）
		myRounds() {
			return this.rounds.filter(round => {
				// 过滤掉participants为空的项目
				const hasParticipants = round.participants && round.participants.length > 0
				return hasParticipants && (round.isParticipant || round.isCreator)
			})
		},
		// 旁观回合（仅旁观者，且participants不为空）
		spectateRounds() {
			return this.rounds.filter(round => {
				// 过滤掉participants为空的项目
				const hasParticipants = round.participants && round.participants.length > 0
				return hasParticipants && round.isSpectator && !round.isParticipant && !round.isCreator
			})
		}
	},
	methods: {

		// 加载回合数据
		async loadRounds() {
			if (this.loading) return
			
			this.loading = true
			
			try {
				// 获取我的所有回合（包括参与和旁观）
				const result = await roundsApi.getMyRounds()
				
				// 处理API响应数据
				if (result && result.content) {
					// 处理回合数据，将后端snake_case字段映射为前端camelCase字段
					this.rounds = result.content.map(round => {
						return {
							// 基础字段映射
							id: round.round_id,
							roundId: round.round_id,
							code: round.round_code,
							roundCode: round.round_code,
							gameType: round.game_type,
							status: round.status,
							maxParticipants: round.max_participants,
							currentParticipants: round.current_participants,
							baseAmount: round.base_amount,
							totalAmount: round.total_amount,
							hasTable: round.has_table,
							
							isPublic: round.is_public,
							hasPassword: round.has_password,
							allowSpectator: round.allow_spectator,
							spectatorCount: round.spectator_count,
							startTime: round.start_time,
							endTime: round.end_time,
							createdAt: round.created_at,
							updatedAt: round.updated_at,
							creator: round.creator,
							tableUser: round.table_user,
							participants: round.participants || [],
							canJoin: round.can_join,
							recordCount: 0, // 暂时设为0，后续可从其他接口获取
							// 用户角色标识
							currentUserRole: round.current_user_role,
							isCreator: round.current_user_role === 'creator',
							isParticipant: round.current_user_role === 'participant' || round.current_user_role === 'creator',
							isSpectator: round.current_user_role === 'spectator',
							// 兼容字段
							maxPlayers: round.max_participants,
							currentPlayers: round.current_participants
						}
					})
					
					uni.showToast({
						title: `加载了${this.rounds.length}个回合`,
						icon: 'success',
						duration: 1500
					})
				} else {
					this.rounds = []
				}
				
			} catch (error) {
				handleApiError(error)
			} finally {
				this.loading = false
			}
		},

		handleEmptyAction() {
			this.$emit('create-round')
		},
		handleCreateRound() {
			this.$emit('create-round')
		},
		handleJoinRound(roundId) {
			this.$emit('join-round', roundId)
		},
		handleSpectateRound(roundId) {
			this.$emit('spectate-round', roundId)
		},
		// 刷新数据
		refresh() {
			this.rounds = []
			return this.loadRounds()
		}
	}
}
</script>

<style scoped lang="scss">
.round-list {
	padding: 0;
}

.section-title {
	padding: 16rpx 0;
	margin-bottom: 16rpx;
	border-bottom: 1rpx solid $chess-border-light;
}

.title-text {
	font-size: 32rpx;
	font-weight: 600;
	color: $chess-color-dark;
}

.my-rounds-section,
.spectate-rounds-section {
	margin-bottom: 32rpx;

	&:last-child {
		margin-bottom: 0;
	}
}

.list-content {
	flex: 1;
	padding: 16rpx 24rpx;
	/* 隐藏滚动条 */
	::-webkit-scrollbar {
		display: none;
	}
	scrollbar-width: none; /* Firefox */
	-ms-overflow-style: none; /* IE 10+ */
}

.loading-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 80rpx 0;
}

.loading-spinner {
	width: 60rpx;
	height: 60rpx;
	border: 4rpx solid #F0F0F0;
	border-top: 4rpx solid #5D688A;
	border-radius: 50%;
	animation: spin 1s linear infinite;
	margin-bottom: 16rpx;
}

@keyframes spin {
	0% { transform: rotate(0deg); }
	100% { transform: rotate(360deg); }
}

.loading-text {
	font-size: 26rpx;
	color: #7F8C8D;
}

.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 180rpx 40rpx 120rpx;
	text-align: center;
}

.empty-icon {
	font-size: 120rpx;
	margin-bottom: 24rpx;
	opacity: 0.6;
	color: $chess-color-gold;
}

.empty-title {
	font-size: 32rpx;
	font-weight: bold;
	color: $chess-color-dark;
	margin-bottom: 12rpx;
}

.empty-desc {
	font-size: 26rpx;
	color: #7F8C8D;
	line-height: 1.4;
	margin-bottom: 32rpx;
}

.empty-action {
	padding: 16rpx 32rpx;
	background-color: $chess-color-gold;
	color: #FFFFFF;
	font-size: 28rpx;
	border-radius: 24rpx;
	border: none;
	transition: all 0.3s ease;

	&:hover {
		background-color: darken($chess-color-gold, 10%);
	}
}

.rounds-container {
	padding-bottom: 20rpx;
}

.bottom-action {
	padding: 32rpx 24rpx 40rpx;
	display: flex;
	justify-content: center;
	align-items: center;
}

.create-round-bottom-btn {
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 16rpx 32rpx;
	background-color: $chess-color-gold;
	color: #FFFFFF;
	font-size: 28rpx;
	border-radius: 24rpx;
	border: none;
	transition: all 0.3s ease;
	box-shadow: 0 4rpx 12rpx rgba(93, 104, 138, 0.15);
	min-width: 240rpx;
	
	&:active {
		background-color: darken($chess-color-gold, 10%);
		transform: translateY(2rpx);
		box-shadow: 0 2rpx 8rpx rgba(93, 104, 138, 0.2);
	}
}

.btn-icon {
	font-size: 32rpx;
	font-weight: bold;
	margin-right: 8rpx;
	line-height: 1;
}

.btn-text {
	font-size: 28rpx;
	font-weight: 500;
	line-height: 1;
}
</style>