<template>
	<view class="container">
		<!-- 下拉刷新 -->
		<scroll-view 
			class="scroll-container"
			scroll-y
			refresher-enabled
			:refresher-triggered="isRefreshing"
			@refresherrefresh="onRefresh"
			refresher-background="#f8f8f8"
		>

			
			<!-- 倍率设置 -->
			<view class="card mt-20">
				<view class="multiplier-setting">
					<view class="flex-between">
						<text class="setting-label">游戏倍率</text>
						<input 
							v-model.number="formData.gameMultiplier" 
							type="digit" 
							placeholder="请输入倍率" 
							class="multiplier-input"
							@input="validateMultiplier"
						/>
					</view>
					<text class="setting-hint">设置游戏倍率，影响最终结算金额</text>
				</view>
			</view>
			
			<!-- 参与者头像位置 -->
			<view class="card mt-20">
				<view class="participants-section">
					<view class="section-header">
						<text class="section-title">参与者 ({{ roundParticipants.length }}/{{ maxPlayers }})</text>
						<view class="table-board-toggle">
							<text class="toggle-label">台板</text>
							<switch 
								:checked="formData.hasTableBoard" 
								@change="onTableBoardChange"
								color="#d4af37"
							/>
						</view>
					</view>
					<view class="participants-grid compact">
						<view 
							v-for="(slot, index) in participantSlots" 
							:key="index"
							class="participant-slot compact"
							:class="{ 
								'occupied': slot.participant, 
								'table-board': slot.isTableBoard,
								'hidden': slot.isTableBoard && !formData.hasTableBoard
							}"
						>
							<view class="slot-avatar compact">
								<image 
									v-if="slot.participant"
									class="avatar-img compact" 
									:src="getAvatarUrl(slot.participant)" 
									mode="aspectFill" 
								/>
								<view v-else class="empty-avatar compact">
									<text class="slot-number">{{ index + 1 }}</text>
								</view>
							</view>
							<view class="slot-info compact">
								<text v-if="slot.participant" class="user-name compact">{{ slot.participant.user_info ? slot.participant.user_info.nickname : slot.participant.nickname }}</text>
								<text v-else-if="slot.isTableBoard" class="slot-label compact">台板</text>
								<text v-else class="slot-label compact">空位</text>
							</view>
						</view>
					</view>
				</view>
			</view>
			
			<!-- 二维码邀请区域 -->
			<view class="card mt-20">
				<view class="qr-section">
					<view class="section-title">邀请加入</view>
					<view class="qr-container">
						<view v-if="createdRoundId" class="qr-code-wrapper">
							<canvas 
								canvas-id="qrCanvas" 
								class="qr-canvas"
								:style="{width: qrSize + 'px', height: qrSize + 'px'}"
							></canvas>
						</view>
						<view v-else class="qr-placeholder">
							<view class="placeholder-icon">📱</view>
							<text class="placeholder-text">正在生成邀请二维码...</text>
						</view>
						<view v-if="createdRoundId" class="qr-actions">
							<button class="action-btn" @click="shareQRCode">分享二维码</button>
							<button class="action-btn secondary" @click="copyInviteLink">复制邀请链接</button>
						</view>
					</view>
				</view>
			</view>
			

		</scroll-view>
		
		<!-- 操作按钮 -->
		<view class="action-buttons">
			<button 
				class="btn-secondary btn-block" 
				@click="handleCancel"
			>
				取消
			</button>
			<button 
				v-if="createdRoundId"
				class="btn-primary btn-block" 
				@click="handleStartRound"
				:disabled="!canStartRound || isStarting"
			>
				{{ isStarting ? '开始中...' : '开始回合' }}
			</button>
			<view v-else class="loading-hint">
				<text>正在创建回合...</text>
			</view>
		</view>
	</view>
</template>

<script>
import QRCode from 'qrcode'
import { roundsApi } from '@/api/rounds'
import { AuthManager } from '@/utils/auth'

export default {
	name: 'CreateRound',
	data() {
		return {
			formData: {
				hasTableBoard: false,
				gameMultiplier: 1
			},
			isCreating: false,
			isStarting: false,
			isRefreshing: false,
			createdRoundId: null,
			qrSize: 200,
			roundParticipants: [],
			refreshTimer: null,
			maxPlayers: 4,
			currentUser: null
		}
	},
	computed: {
		// 参与者位置槽位
		participantSlots() {
			const slots = []
			
			// 默认4个位置
			for (let i = 0; i < 4; i++) {
				slots.push({
					participant: this.roundParticipants[i] || null,
					isTableBoard: false
				})
			}
			
			// 台板位置（第5个位置）
			if (this.formData.hasTableBoard) {
				slots.push({
					participant: this.roundParticipants[4] || null,
					isTableBoard: true
				})
			}
			
			return slots
		},
		// 是否可以开始回合
		canStartRound() {
			const minPlayers = this.formData.hasTableBoard ? 5 : 4
			return this.roundParticipants.length >= minPlayers
		}
	},
	async onLoad() {
		// 页面加载时的初始化
		this.updateMaxPlayers()
		this.currentUser = AuthManager.getCurrentUser()
		
		// 检查用户登录状态
		if (!this.currentUser) {
			uni.showToast({
				title: '请先登录',
				icon: 'none'
			})
			setTimeout(() => {
				uni.navigateBack()
			}, 1500)
			return
		}
		
		// 立即创建回合
		await this.autoCreateRound()
	},
	onUnload() {
		// 清理定时器
		if (this.refreshTimer) {
			clearInterval(this.refreshTimer)
		}
	},
	methods: {
		// 获取头像URL
		getAvatarUrl(participant) {
			if (!participant) return ''
			
			// 处理嵌套的user_info结构
			const userInfo = participant.user_info || participant
			
			const avatarUrl = userInfo.avatar_url || userInfo.avatarUrl || userInfo.avatar
			if (!avatarUrl) return ''
			
			return AuthManager.getAvatarUrl(avatarUrl)
		},
		
		// 台板设置变化
		onTableBoardChange(e) {
			this.formData.hasTableBoard = e.detail.value
			this.updateMaxPlayers()
		},
		
		// 验证倍率输入
		validateMultiplier(e) {
			let value = parseFloat(e.detail.value)
			if (isNaN(value) || value <= 0) {
				value = 1
			}
			this.formData.gameMultiplier = value
		},
		
		// 更新最大人数
		updateMaxPlayers() {
			this.maxPlayers = this.formData.hasTableBoard ? 5 : 4
		},
		
		// 下拉刷新
		async onRefresh() {
			this.isRefreshing = true
			try {
				if (this.createdRoundId) {
					await this.loadParticipants()
				}
			} catch (error) {
				console.error('刷新失败:', error)
			} finally {
				setTimeout(() => {
					this.isRefreshing = false
				}, 500)
			}
		},
		
		// 自动创建回合（页面加载时调用）
		async autoCreateRound() {
			try {
				uni.showLoading({
					title: '创建回合中...'
				})
				
				// 创建回合数据，字段名需要与后端CreateRoundRequest匹配
				const roundData = {
					game_type: 'mahjong', // 麻将类型
					max_participants: this.maxPlayers,
					base_amount: this.formData.gameMultiplier || 1.0,
					has_table: this.formData.hasTableBoard || false,
					table_user_id: this.formData.hasTableBoard ? (this.currentUser.id || this.currentUser.user_id) : null,
					is_public: false, // 默认私有
					allow_spectator: true, // 允许旁观
					auto_start_minutes: null // 不设置自动开始时间
				}
				
				const response = await roundsApi.createRound(roundData)
			console.log('创建回合响应:', response)
			// 后端返回的字段名是 round_id
			// 检查响应数据结构
			if (response && response.data && response.data.round_id) {
				this.createdRoundId = response.data.round_id
			} else if (response && response.round_id) {
				// 如果数据直接在 response 中
				this.createdRoundId = response.round_id
			} else {
				console.error('响应数据格式异常:', response)
				throw new Error('创建回合成功但返回数据格式异常')
			}
				
				// 创建者自动加入回合
				try {
					await roundsApi.joinRound(this.createdRoundId)
					console.log('创建者自动加入回合成功')
					// 立即刷新参与者列表
					await this.loadParticipants()
				} catch (joinError) {
					console.error('创建者自动加入回合失败:', joinError)
					// 加入失败不影响创建成功的流程，只记录错误
				}
				
				// 生成二维码
				await this.generateQRCode()
				
				// 开始定时刷新参与者列表
				this.startRefreshTimer()
				
				uni.hideLoading()
				uni.showToast({
					title: '回合创建成功',
					icon: 'success'
				})
				
			} catch (error) {
				console.error('自动创建回合失败:', error)
				uni.hideLoading()
				uni.showModal({
					title: '创建失败',
					content: error.message || '创建回合失败，请重试',
					confirmText: '重试',
					cancelText: '返回',
					success: (res) => {
						if (res.confirm) {
							// 重试创建
							this.autoCreateRound()
						} else {
							// 返回上一页
							uni.navigateBack()
						}
					}
				})
			}
		},
		
		// 创建回合
		async handleCreateRound() {
			this.isCreating = true
			
			try {
				const currentUser = AuthManager.getCurrentUser()
				if (!currentUser) {
					uni.showToast({
						title: '请先登录',
						icon: 'none'
					})
					return
				}
				
				// 创建回合数据，字段名需要与后端CreateRoundRequest匹配
				const roundData = {
					game_type: 'mahjong', // 麻将类型
					max_participants: this.maxPlayers,
					base_amount: this.formData.gameMultiplier || 1.0,
					has_table: this.formData.hasTableBoard || false,
					table_user_id: this.formData.hasTableBoard ? (currentUser.id || currentUser.user_id) : null,
					is_public: false, // 默认私有
					allow_spectator: true, // 允许旁观
					auto_start_minutes: null // 不设置自动开始时间
				}
				
				const response = await roundsApi.createRound(roundData)
			console.log('创建回合响应:', response)
			// 后端返回的字段名是 round_id
			// 检查响应数据结构
			if (response && response.data && response.data.round_id) {
				this.createdRoundId = response.data.round_id
			} else if (response && response.round_id) {
				// 如果数据直接在 response 中
				this.createdRoundId = response.round_id
			} else {
				console.error('响应数据格式异常:', response)
				throw new Error('创建回合成功但返回数据格式异常')
			}
				
				// 创建者自动加入回合
				try {
					await roundsApi.joinRound(this.createdRoundId)
					console.log('创建者自动加入回合成功')
					// 立即刷新参与者列表
					await this.loadParticipants()
				} catch (joinError) {
					console.error('创建者自动加入回合失败:', joinError)
					// 加入失败不影响创建成功的流程，只记录错误
				}
				
				// 生成二维码
				await this.generateQRCode()
				
				// 开始定时刷新参与者列表
				this.startRefreshTimer()
				
				uni.showToast({
					title: '回合创建成功',
					icon: 'success'
				})
				
			} catch (error) {
				console.error('创建回合失败:', error)
				uni.showToast({
					title: error.message || '创建失败',
					icon: 'none'
				})
			} finally {
				this.isCreating = false
			}
		},
		
		// 开始回合
		async handleStartRound() {
			if (!this.canStartRound) {
				uni.showToast({
					title: `需要${this.maxPlayers}人才能开始`,
					icon: 'none'
				})
				return
			}
			
			this.isStarting = true
			
			try {
				await roundsApi.startRound(this.createdRoundId)
				
				uni.showToast({
					title: '回合已开始',
					icon: 'success'
				})
				
				// 跳转到回合详情页
				uni.redirectTo({
					url: `/pages/round-detail/round-detail?id=${this.createdRoundId}`
				})
				
			} catch (error) {
				console.error('开始回合失败:', error)
				uni.showToast({
					title: error.message || '开始失败',
					icon: 'none'
				})
			} finally {
				this.isStarting = false
			}
		},
		
		// 取消创建
		handleCancel() {
			uni.navigateBack()
		},
		
		// 生成小程序码
		async generateQRCode() {
			try {
				console.log('开始生成小程序码，回合ID:', this.createdRoundId)
				// 调用后端API生成小程序码
				const response = await uni.request({
					url: `${getApp().globalData.apiBaseUrl}/rounds/${this.createdRoundId}/miniprogram-code`,
					method: 'GET',
					responseType: 'arraybuffer',
					header: {
						'Authorization': `Bearer ${uni.getStorageSync('token')}`
					}
				})
				console.log('小程序码API响应:', response.statusCode, response.data ? '有数据' : '无数据')
				
				if (response.statusCode === 200) {
					// 将arraybuffer转换为base64
					const base64 = uni.arrayBufferToBase64(response.data)
					const imageUrl = `data:image/png;base64,${base64}`
					
					// 在canvas上绘制小程序码
					const canvas = uni.createCanvasContext('qrCanvas', this)
					const img = canvas.createImage()
					img.onload = () => {
						canvas.drawImage(img, 0, 0, this.qrSize, this.qrSize)
						canvas.draw()
					}
					img.src = imageUrl
				} else {
					throw new Error('生成小程序码失败')
				}
				
			} catch (error) {
				console.error('生成小程序码失败:', error)
				uni.showToast({
					title: '生成小程序码失败',
					icon: 'none'
				})
			}
		},
		
		// 分享小程序码
		shareQRCode() {
			uni.share({
				title: '邀请加入麻将回合',
				path: `/pages/round-detail/round-detail?id=${this.createdRoundId}`
			})
		},
		
		// 复制邀请链接
		copyInviteLink() {
			const inviteUrl = `${getApp().globalData.baseUrl}/join-round?id=${this.createdRoundId}`
			uni.setClipboardData({
				data: inviteUrl,
				success: () => {
					uni.showToast({
						title: '链接已复制',
						icon: 'success'
					})
				}
			})
		},
		
		// 加载参与者列表
		async loadParticipants() {
			try {
				const response = await roundsApi.getRoundParticipants(this.createdRoundId)
				console.log('API响应完整数据:', response)
				console.log('参与者数据:', response.data || response)
				// 处理不同的响应格式
				if (response && response.data) {
					this.roundParticipants = response.data
				} else if (Array.isArray(response)) {
					this.roundParticipants = response
				} else {
					this.roundParticipants = []
				}
				console.log('设置后的roundParticipants:', this.roundParticipants)
			} catch (error) {
				console.error('加载参与者失败:', error)
				this.roundParticipants = []
			}
		},
		
		// 开始定时刷新
		startRefreshTimer() {
			this.loadParticipants() // 立即加载一次
			
			this.refreshTimer = setInterval(() => {
				this.loadParticipants()
			}, 5000) // 每5秒刷新一次
		}
	}
}
</script>

<style lang="scss" scoped>
.container {
	padding: 0;
	min-height: 100vh;
	background-color: #f8f8f8;
	display: flex;
	flex-direction: column;
}

.scroll-container {
	flex: 1;
	padding: 20rpx;
}

.card {
	background: white;
	border-radius: 16rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.1);
}

.card-title {
	font-size: 36rpx;
	font-weight: 600;
	color: #333;
	margin-bottom: 20rpx;
}

// 参与者区域样式
.participants-section {
	.section-header {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-bottom: 30rpx;
		
		.section-title {
			font-size: 32rpx;
			color: #333;
			font-weight: 600;
			margin: 0;
		}
		
		.table-board-toggle {
			display: flex;
			align-items: center;
			gap: 12rpx;
			
			.toggle-label {
				font-size: 26rpx;
				color: #666;
			}
			
			switch {
				transform: scale(0.8);
			}
		}
	}
}

// 台板设置样式
.table-board-setting {
	.flex-between {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-bottom: 16rpx;
	}
	
	.setting-label {
		font-size: 32rpx;
		color: #333;
		font-weight: 500;
	}
	
	.setting-hint {
		font-size: 24rpx;
		color: #999;
		line-height: 1.4;
	}
}

// 参与者网格布局
.participants-grid {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 20rpx;
	margin-top: 20rpx;
	
	&.compact {
		grid-template-columns: repeat(3, 1fr);
		gap: 15rpx;
	}
}

.participant-slot {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 30rpx 20rpx;
	border-radius: 16rpx;
	border: 2rpx dashed #e5e5e5;
	background: #fafafa;
	transition: all 0.3s;
	min-height: 160rpx;
	justify-content: center;
	
	&.compact {
		padding: 20rpx 15rpx;
		border-radius: 12rpx;
		min-height: 120rpx;
	}
	
	&.occupied {
		border: 2rpx solid #d4af37;
		background: linear-gradient(135deg, #fff9e6 0%, #fef7d6 100%);
		box-shadow: 0 4rpx 12rpx rgba(212, 175, 55, 0.2);
	}
	
	&.table-board {
		border-color: #8b4513;
		background: linear-gradient(135deg, #f4f1e8 0%, #ede4d3 100%);
		
		&.occupied {
			border-color: #8b4513;
			background: linear-gradient(135deg, #f0e6d2 0%, #e8dcc6 100%);
			box-shadow: 0 4rpx 12rpx rgba(139, 69, 19, 0.2);
		}
	}
	
	&.hidden {
		display: none;
	}
}

.slot-avatar {
	width: 80rpx;
	height: 80rpx;
	border-radius: 50%;
	margin-bottom: 12rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	
	&.compact {
		width: 60rpx;
		height: 60rpx;
		margin-bottom: 8rpx;
	}
}

.avatar-img {
	width: 100%;
	height: 100%;
	border-radius: 50%;
	border: 3rpx solid #d4af37;
	
	&.compact {
		border: 2rpx solid #d4af37;
	}
}

.empty-avatar {
	width: 100%;
	height: 100%;
	border-radius: 50%;
	background: #e5e5e5;
	display: flex;
	align-items: center;
	justify-content: center;
	border: 2rpx solid #ccc;
	
	&.compact {
		border: 1rpx solid #ccc;
	}
}

.slot-number {
	font-size: 32rpx;
	color: #999;
	font-weight: bold;
}

.slot-info {
	display: flex;
	flex-direction: column;
	align-items: center;
	
	&.compact {
		margin-top: 5rpx;
	}
}

.user-name {
	font-size: 24rpx;
	color: #333;
	font-weight: 500;
	text-align: center;
	max-width: 120rpx;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
	
	&.compact {
		font-size: 20rpx;
		max-width: 80rpx;
	}
}

.slot-label {
	font-size: 24rpx;
	color: #999;
	text-align: center;
	
	&.compact {
		font-size: 20rpx;
	}
}

.empty-slot {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 8rpx;
	
	.slot-icon {
		font-size: 48rpx;
		color: #ccc;
		font-weight: 300;
	}
	
	.slot-text {
		font-size: 24rpx;
		color: #999;
	}
}

.participant-name {
	font-size: 24rpx;
	color: #333;
	font-weight: 500;
	text-align: center;
	max-width: 120rpx;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

// 倍率设置样式
.multiplier-setting {
	.flex-between {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-bottom: 12rpx;
	}
	
	.setting-label {
		font-size: 28rpx;
		color: #333;
		font-weight: 500;
	}
	
	.setting-hint {
		font-size: 22rpx;
		color: #999;
		line-height: 1.3;
	}
}

.multiplier-input {
	width: 160rpx;
	height: 60rpx;
	border: 2rpx solid #e5e5e5;
	border-radius: 6rpx;
	padding: 0 16rpx;
	font-size: 28rpx;
	text-align: center;
	background: #fff;
	transition: border-color 0.3s;
	
	&:focus {
		border-color: #d4af37;
	}
}

// 二维码区域样式
.qr-section {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 30rpx;
}

.qr-container {
	padding: 20rpx;
	background: white;
	border-radius: 12rpx;
	border: 2rpx solid #f0f0f0;
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.qr-canvas {
	border-radius: 8rpx;
}

.qr-placeholder {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	width: 200rpx;
	height: 200rpx;
	border: 2rpx dashed #e0e0e0;
	border-radius: 8rpx;
	background-color: #f9f9f9;
	margin: 0 auto;
}

.placeholder-icon {
	font-size: 48rpx;
	margin-bottom: 12rpx;
	opacity: 0.6;
}

.placeholder-text {
	font-size: 28rpx;
	color: #999;
	text-align: center;
}

.qr-actions {
	display: flex;
	gap: 20rpx;
}

.action-btn {
	padding: 20rpx 40rpx;
	border-radius: 25rpx;
	font-size: 28rpx;
	border: none;
	background: #d4af37;
	color: #fff;
	transition: all 0.3s ease;
	
	&.secondary {
		background: #f5f5f5;
		color: #333;
		border: 2rpx solid #e5e5e5;
	}
	
	&:active {
		transform: scale(0.95);
	}
}

// 创建提示样式
.create-tip {
	display: flex;
	justify-content: center;
	align-items: center;
	padding: 60rpx 20rpx;
	
	.tip-text {
		font-size: 28rpx;
		color: #999;
		text-align: center;
	}
}

// 操作按钮样式
.action-buttons {
	display: flex;
	gap: 20rpx;
	padding: 20rpx 20rpx calc(20rpx + env(safe-area-inset-bottom));
	background: white;
	border-top: 2rpx solid #f0f0f0;
}

.loading-hint {
	display: flex;
	align-items: center;
	justify-content: center;
	height: 88rpx;
	background: #f5f5f5;
	border-radius: 12rpx;
	flex: 1;
	
	text {
		font-size: 28rpx;
		color: #999;
	}
}

.btn-primary {
	background: linear-gradient(135deg, #d4af37 0%, #f4d03f 100%);
	color: white;
	border: none;
	border-radius: 12rpx;
	height: 88rpx;
	font-size: 32rpx;
	font-weight: 600;
	transition: all 0.3s;
	display: flex;
	align-items: center;
	justify-content: center;
	text-align: center;
	
	&:active {
		transform: translateY(2rpx);
		opacity: 0.9;
	}
	
	&:disabled {
		background: #ccc;
		transform: none;
		opacity: 0.6;
	}
}

.btn-secondary {
	background: white;
	color: #666;
	border: 2rpx solid #e5e5e5;
	border-radius: 12rpx;
	height: 88rpx;
	font-size: 32rpx;
	font-weight: 500;
	transition: all 0.3s;
	display: flex;
	align-items: center;
	justify-content: center;
	text-align: center;
	
	&:active {
		background: #f5f5f5;
		border-color: #d4af37;
	}
}

.btn-block {
	flex: 1;
}

.btn-sm {
	height: 64rpx;
	padding: 0 24rpx;
	font-size: 26rpx;
	border-radius: 8rpx;
}

// 工具类
.mt-20 {
	margin-top: 40rpx;
}

.mb-20 {
	margin-bottom: 40rpx;
}

.flex-between {
	display: flex;
	align-items: center;
	justify-content: space-between;
}
</style>