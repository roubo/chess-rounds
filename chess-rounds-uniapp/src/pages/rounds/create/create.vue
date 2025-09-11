<template>
	<view class="create-page">
		<view class="form-container">
			<!-- 回合名称 -->
			<view class="form-item">
				<text class="label">回合名称</text>
				<input class="input" v-model="formData.name" placeholder="请输入回合名称" maxlength="20" />
			</view>
			
			<!-- 回合描述 -->
			<view class="form-item">
				<text class="label">回合描述</text>
				<textarea class="textarea" v-model="formData.description" placeholder="请输入回合描述" maxlength="100"></textarea>
			</view>
			
			<!-- 游戏类型 -->
			<view class="form-item">
				<text class="label">游戏类型</text>
				<picker :value="gameTypeIndex" :range="gameTypes" @change="onGameTypeChange">
					<view class="picker">
						<text class="picker-text">{{ gameTypes[gameTypeIndex] || '请选择游戏类型' }}</text>
						<text class="picker-arrow">></text>
					</view>
				</picker>
			</view>
			
			<!-- 最大参与人数 -->
			<view class="form-item">
				<text class="label">最大参与人数</text>
				<view class="number-input">
					<view class="number-btn" @click="decreaseMaxPlayers">-</view>
					<input class="number-value" v-model="formData.maxPlayers" type="number" disabled />
					<view class="number-btn" @click="increaseMaxPlayers">+</view>
				</view>
			</view>
			
			<!-- 是否公开 -->
			<view class="form-item">
				<text class="label">是否公开</text>
				<switch :checked="formData.isPublic" @change="onPublicChange" color="#5D688A" />
			</view>
			
			<!-- 开始时间 -->
			<view class="form-item">
				<text class="label">开始时间</text>
				<picker mode="datetime" :value="formData.startTime" @change="onStartTimeChange">
					<view class="picker">
						<text class="picker-text">{{ formatDateTime(formData.startTime) || '请选择开始时间' }}</text>
						<text class="picker-arrow">></text>
					</view>
				</picker>
			</view>
		</view>
		
		<!-- 创建按钮 -->
		<view class="button-container">
			<button class="create-btn" @click="createRound" :disabled="!isFormValid">创建回合</button>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			formData: {
				name: '',
				description: '',
				gameType: '',
				maxPlayers: 4,
				isPublic: true,
				startTime: ''
			},
			gameTypes: ['象棋', '围棋', '五子棋', '国际象棋', '跳棋'],
			gameTypeIndex: -1
		}
	},
	computed: {
		isFormValid() {
			return this.formData.name.trim() && 
				   this.formData.gameType && 
				   this.formData.maxPlayers >= 2 && 
				   this.formData.startTime
		}
	},
	methods: {
		onGameTypeChange(e) {
			this.gameTypeIndex = e.detail.value
			this.formData.gameType = this.gameTypes[e.detail.value]
		},
		increaseMaxPlayers() {
			if (this.formData.maxPlayers < 10) {
				this.formData.maxPlayers++
			}
		},
		decreaseMaxPlayers() {
			if (this.formData.maxPlayers > 2) {
				this.formData.maxPlayers--
			}
		},
		onPublicChange(e) {
			this.formData.isPublic = e.detail.value
		},
		onStartTimeChange(e) {
			this.formData.startTime = e.detail.value
		},
		formatDateTime(dateTime) {
			if (!dateTime) return ''
			const date = new Date(dateTime)
			return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
		},
		createRound() {
			if (!this.isFormValid) {
				// uni.showToast() - 已屏蔽
				return
			}
			
			uni.showLoading({
				title: '创建中...'
			})
			
			// 模拟API调用
			setTimeout(() => {
				uni.hideLoading()
				// uni.showToast() - 已屏蔽
				
				// 返回上一页
				setTimeout(() => {
					uni.navigateBack()
				}, 1500)
			}, 1000)
		}
	}
}
</script>

<style scoped>
.create-page {
	padding: 20rpx;
	background-color: #FFF2EF;
	min-height: 100vh;
}

.form-container {
	background-color: #FFFFFF;
	border-radius: 16rpx;
	padding: 40rpx;
	margin-bottom: 40rpx;
	box-shadow: 0 4rpx 12rpx rgba(93, 104, 138, 0.1);
}

.form-item {
	margin-bottom: 40rpx;
}

.form-item:last-child {
	margin-bottom: 0;
}

.label {
	display: block;
	font-size: 28rpx;
	color: #2C3E50;
	font-weight: bold;
	margin-bottom: 16rpx;
}

.input {
	width: 100%;
	height: 80rpx;
	padding: 0 20rpx;
	border: 2rpx solid #E8E8E8;
	border-radius: 8rpx;
	font-size: 28rpx;
	color: #2C3E50;
	box-sizing: border-box;
}

.input:focus {
	border-color: #5D688A;
}

.textarea {
	width: 100%;
	height: 120rpx;
	padding: 20rpx;
	border: 2rpx solid #E8E8E8;
	border-radius: 8rpx;
	font-size: 28rpx;
	color: #2C3E50;
	box-sizing: border-box;
	resize: none;
}

.textarea:focus {
	border-color: #5D688A;
}

.picker {
	display: flex;
	justify-content: space-between;
	align-items: center;
	height: 80rpx;
	padding: 0 20rpx;
	border: 2rpx solid #E8E8E8;
	border-radius: 8rpx;
	background-color: #FFFFFF;
}

.picker-text {
	font-size: 28rpx;
	color: #2C3E50;
}

.picker-arrow {
	font-size: 24rpx;
	color: #BDC3C7;
}

.number-input {
	display: flex;
	align-items: center;
	border: 2rpx solid #E8E8E8;
	border-radius: 8rpx;
	overflow: hidden;
}

.number-btn {
	width: 80rpx;
	height: 80rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	background-color: #F5F5F5;
	font-size: 32rpx;
	color: #5D688A;
	font-weight: bold;
}

.number-value {
	flex: 1;
	height: 80rpx;
	text-align: center;
	font-size: 28rpx;
	color: #2C3E50;
	border: none;
	background-color: #FFFFFF;
}

.button-container {
	padding: 0 20rpx;
}

.create-btn {
	width: 100%;
	height: 88rpx;
	background-color: #5D688A;
	color: #FFFFFF;
	font-size: 32rpx;
	font-weight: bold;
	border-radius: 12rpx;
	border: none;
}

.create-btn:disabled {
	background-color: #BDC3C7;
	color: #FFFFFF;
}
</style>