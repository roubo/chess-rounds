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
					<view class="section-title">扫码加入</view>
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
		qrSize: 135,
		roundParticipants: [],
		refreshTimer: null,
		maxPlayers: 4,
		currentUser: null,
		isEditMode: false
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
			// 支持两人及以上参与者开始回合
			const minPlayers = 2
			return this.roundParticipants.length >= minPlayers
		}
	},
	async onLoad(options) {
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
		
		// 如果有回合ID，则加载现有回合；否则创建新回合
		if (options.id) {
			this.createdRoundId = options.id
			this.isEditMode = true
			await this.loadExistingRound(options.id)
		} else {
			// 立即创建回合
			await this.autoCreateRound()
		}
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
					// table_user_id 已移除，台板用户由后端自动创建
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
				
				// 设置页面标题
				uni.setNavigationBarTitle({
					title: '等待参与者'
				})
				
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
					// table_user_id 已移除，台板用户由后端自动创建
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
				return
			}
			
			try {
				// 开始回合，传递台板状态参数
				await roundsApi.startRound(
					this.createdRoundId,
					this.formData.hasTableBoard,
					null // 台板用户现在由后端自动创建
				)
				
				uni.showToast({
					title: '回合已开始',
					icon: 'success'
				})
				
				// 跳转到回合详情页
				setTimeout(() => {
					uni.navigateTo({
						url: `/pages/round-detail/round-detail?id=${this.createdRoundId}`
					})
				}, 1500)
				
			} catch (error) {
				console.error('开始回合失败:', error)
				uni.showToast({
					title: '开始回合失败',
					icon: 'error'
				})
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
				
				// 直接使用uni.downloadFile下载图片，避免UTF8转换问题
				console.log('使用downloadFile下载小程序码图片')
				
				const downloadResult = await new Promise((resolve, reject) => {
					uni.downloadFile({
						url: `${getApp().globalData.apiBaseUrl}/rounds/${this.createdRoundId}/miniprogram-code`,
						header: {
							'Authorization': `Bearer ${uni.getStorageSync('token')}`
						},
						success: (res) => {
							console.log('下载成功:', res)
							resolve(res)
						},
						fail: (err) => {
							console.log('下载失败:', err)
							reject(err)
						}
					})
				})
				
				if (downloadResult.statusCode === 200 && downloadResult.tempFilePath) {
						console.log('下载的临时文件路径:', downloadResult.tempFilePath)
						
						// 在canvas上绘制小程序码
						const canvas = uni.createCanvasContext('qrCanvas', this)
						
						// 使用uni.getImageInfo获取图片信息后绘制
						uni.getImageInfo({
							src: downloadResult.tempFilePath,
							success: (imageInfo) => {
								console.log('图片信息获取成功:', imageInfo)
								// 直接使用drawImage绘制
								canvas.drawImage(downloadResult.tempFilePath, 0, 0, this.qrSize, this.qrSize)
								canvas.draw()
								console.log('小程序码绘制完成')
							},
							fail: (err) => {
								console.error('获取图片信息失败:', err)
								// 尝试直接绘制
								canvas.drawImage(downloadResult.tempFilePath, 0, 0, this.qrSize, this.qrSize)
								canvas.draw()
							}
						})
				} else {
					console.error('下载文件失败:', downloadResult)
					throw new Error('下载小程序码失败')
				}
				
			} catch (error) {
				console.error('生成小程序码失败:', error)
				uni.showToast({
					title: '生成小程序码失败',
					icon: 'none'
				})
			}
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
		
		// 加载现有回合数据
		async loadExistingRound(roundId) {
			try {
				uni.showLoading({
					title: '加载回合中...'
				})
				
				// 获取回合详情
				const response = await roundsApi.getRoundDetail(roundId)
				const roundData = response.data || response
				
				// 设置表单数据
				this.formData.hasTableBoard = roundData.has_table || false
				this.formData.gameMultiplier = roundData.base_amount || 1
				this.updateMaxPlayers()
				
				// 加载参与者列表
				await this.loadParticipants()
				
				// 生成二维码
				await this.generateQRCode()
				
				// 开始定时刷新参与者列表
				this.startRefreshTimer()
				
				// 设置页面标题
				uni.setNavigationBarTitle({
					title: '等待参与者'
				})
				
				uni.hideLoading()
				
			} catch (error) {
				console.error('加载现有回合失败:', error)
				uni.hideLoading()
				uni.showModal({
					title: '加载失败',
					content: '无法加载回合信息，请重试',
					confirmText: '返回',
					success: () => {
						uni.navigateBack()
					}
				})
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
}

.card {
	background: white;
	border-radius: 20rpx;
	padding: 28rpx;
	margin-left: 18rpx;
	margin-right: 18rpx;
	box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
	border: 1rpx solid rgba(0, 0, 0, 0.05);
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
	gap: 12rpx;
	margin-top: 12rpx;
	
	&.compact {
		grid-template-columns: repeat(3, 1fr);
		gap: 10rpx;
	}
}

.participant-slot {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 16rpx 12rpx;
	border-radius: 10rpx;
	border: 2rpx dashed #e5e5e5;
	background: #fafafa;
	transition: all 0.3s;
	min-height: 110rpx;
	justify-content: center;
	
	&.compact {
		padding: 12rpx 8rpx;
		border-radius: 8rpx;
		min-height: 90rpx;
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
	width: 55rpx;
	height: 55rpx;
	border-radius: 50%;
	margin-bottom: 6rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	
	&.compact {
		width: 45rpx;
		height: 45rpx;
		margin-bottom: 4rpx;
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
	gap: 20rpx;
}

.qr-container {
	padding: 15rpx;
	background: white;
	border-radius: 8rpx;
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
	width: 120rpx;
	height: 120rpx;
	border: 2rpx dashed #e0e0e0;
	border-radius: 6rpx;
	background-color: #f9f9f9;
	margin: 0 auto;
}

.placeholder-icon {
	font-size: 36rpx;
	margin-bottom: 8rpx;
	opacity: 0.6;
}

.placeholder-text {
	font-size: 24rpx;
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
	gap: 16rpx;
	padding: 24rpx 30rpx calc(24rpx + env(safe-area-inset-bottom)) 30rpx;
	background: white;
	border-top: 1rpx solid #f0f0f0;
	box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
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
	border-radius: 14rpx;
	height: 92rpx;
	font-size: 32rpx;
	font-weight: 600;
	transition: all 0.3s;
	display: flex;
	align-items: center;
	justify-content: center;
	text-align: center;
	box-shadow: 0 4rpx 12rpx rgba(212, 175, 55, 0.3);
	
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
	border-radius: 14rpx;
	height: 92rpx;
	font-size: 32rpx;
	font-weight: 500;
	transition: all 0.3s;
	display: flex;
	align-items: center;
	justify-content: center;
	text-align: center;
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
	
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