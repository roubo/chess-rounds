<template>
	<view v-if="visible" class="modal-overlay" @click="handleOverlayClick">
		<view class="modal-content" @click.stop>
			<view class="modal-header">
				<text class="modal-title">{{ modalTitle }}</text>
				<text class="close-btn" @click="$emit('close')">×</text>
			</view>
			
			<view class="modal-body">
				<!-- 创建圈子模式 -->
				<template v-if="mode === 'create'">
					<view class="form-group">
						<text class="form-label">圈子名称</text>
						<input 
							v-model="formData.name"
							class="form-input"
							placeholder="请输入圈子名称"
							maxlength="20"
						/>
						<text class="char-count">{{ formData.name.length }}/20</text>
					</view>
				</template>
				
				<!-- 加入圈子模式 -->
				<template v-else-if="mode === 'join'">
					<view class="form-group">
						<text class="form-label">邀请码</text>
						<input 
							v-model="formData.joinCode"
							class="form-input"
							placeholder="请输入5位邀请码"
							maxlength="5"
						/>
					</view>
					
					<view class="join-tips">
						<text class="tips-title">💡 如何获取邀请码？</text>
						<text class="tips-text">• 向圈子创建者或管理员索要</text>
						<text class="tips-text">• 邀请码为5位字母数字组合</text>
					</view>
				</template>
			</view>
			
			<view class="modal-footer">
				<button class="btn-cancel" @click="$emit('close')">取消</button>
				<button 
					class="btn-confirm" 
					:disabled="!canSubmit"
					@click="handleSubmit"
				>
					{{ mode === 'create' ? '创建' : '加入' }}
				</button>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	name: 'CircleModal',
	props: {
		visible: {
			type: Boolean,
			default: false
		},
		mode: {
			type: String,
			default: 'create', // 'create' | 'join'
			validator: (value) => ['create', 'join'].includes(value)
		}
	},
	emits: ['close', 'submit'],
	data() {
		return {
			formData: {
				name: '',
				description: '',
				isPrivate: false,
				joinCode: ''
			}
		}
	},
	computed: {
		modalTitle() {
			return this.mode === 'create' ? '创建圈子' : '加入圈子'
		},
		canSubmit() {
			if (this.mode === 'create') {
				return this.formData.name.trim().length > 0
			} else {
				return this.formData.joinCode.trim().length === 5
			}
		}
	},
	watch: {
		visible(newVal) {
			if (newVal) {
				this.resetForm()
			}
		}
	},
	methods: {
		resetForm() {
			this.formData = {
				name: '',
				description: '',
				isPrivate: false,
				joinCode: ''
			}
		},
		handleOverlayClick() {
			this.$emit('close')
		},
		handlePrivateChange(e) {
			this.formData.isPrivate = e.detail.value.length > 0
		},
		handleSubmit() {
			if (!this.canSubmit) return
			
			if (this.mode === 'create') {
				this.$emit('submit', {
					name: this.formData.name.trim(),
					description: this.formData.description.trim(),
					isPrivate: true // 所有圈子都是私密的，需要邀请码
				})
			} else {
				this.$emit('submit', {
					joinCode: this.formData.joinCode.trim().toUpperCase()
				})
			}
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
  max-height: 60vh;
  overflow-y: auto;
  background: $chess-bg-card;
}

.form-group {
	margin-bottom: 40rpx;
	position: relative;
	
	&:last-child {
		margin-bottom: 0;
	}
}

.form-label {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: $chess-color-dark;
  margin-bottom: 20rpx;
  position: relative;
  
  &::after {
    content: '';
    position: absolute;
    bottom: -8rpx;
    left: 0;
    width: 40rpx;
    height: 3rpx;
    background: linear-gradient(90deg, $chess-color-gold, lighten($chess-color-gold, 10%));
    border-radius: 2rpx;
  }
}

.form-input, .form-textarea {
  width: 100%;
  padding: 24rpx 20rpx;
  border: 2rpx solid $chess-border-light;
  border-radius: $uni-border-radius-lg;
  font-size: 30rpx;
  color: $chess-color-dark;
  background: $chess-bg-card;
  box-sizing: border-box;
  transition: all 0.3s ease;
  position: relative;
  min-height: 88rpx; /* 增加最小高度确保文字显示完整 */
  line-height: 1.4; /* 设置行高 */
  
  &:focus {
    border-color: $chess-color-gold;
    box-shadow: 0 0 0 6rpx rgba(212, 175, 55, 0.1);
    outline: none;
    transform: translateY(-2rpx);
  }
  
  &::placeholder {
    color: $chess-color-muted;
    font-size: 28rpx;
  }
}

.form-textarea {
	height: 140rpx;
	resize: none;
	line-height: 1.6;
}

.char-count {
  display: block;
  text-align: right;
  font-size: 24rpx;
  color: $chess-color-muted;
  margin-top: 12rpx;
  font-weight: 500;
}

.checkbox-group {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 20rpx;
  background: linear-gradient(135deg, $chess-bg-primary 0%, lighten($chess-bg-primary, 2%) 100%);
  border-radius: $uni-border-radius-lg;
  border: 2rpx solid $chess-border-light;
  transition: all 0.3s ease;
  
  &:hover {
    border-color: $chess-color-gold;
    box-shadow: 0 4rpx 12rpx rgba(212, 175, 55, 0.1);
  }
}

.checkbox-label {
  font-size: 28rpx;
  color: $chess-color-dark;
  font-weight: 500;
}

.join-tips {
  background: linear-gradient(135deg, lighten($chess-color-gold, 35%) 0%, lighten($chess-color-gold, 30%) 100%);
  border: 2rpx solid $chess-color-gold;
  border-radius: $uni-border-radius-lg;
  padding: 28rpx;
  margin-top: 28rpx;
  position: relative;
  
  &::before {
    content: '';
    position: absolute;
    top: -2rpx;
    left: -2rpx;
    right: -2rpx;
    bottom: -2rpx;
    background: linear-gradient(45deg, $chess-color-gold, lighten($chess-color-gold, 10%), $chess-color-gold);
    border-radius: $uni-border-radius-lg;
    z-index: -1;
  }
}

.tips-title {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: darken($chess-color-gold, 15%);
  margin-bottom: 20rpx;
}

.tips-text {
  display: block;
  font-size: 26rpx;
  color: darken($chess-color-gold, 20%);
  line-height: 1.6;
  margin-bottom: 12rpx;
  font-weight: 500;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.modal-footer {
  display: flex;
  gap: 32rpx;
  padding: 48rpx 0 0;
  border-top: 2rpx solid $chess-border-light;
  margin-top: 48rpx;
}

.btn {
  flex: 1;
  height: 96rpx;
  border-radius: 48rpx;
  font-size: 34rpx;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 200rpx;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
    transition: left 0.5s;
  }
  
  &:active::before {
    left: 100%;
  }
}

.btn-cancel {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  color: $chess-color-muted;
  border: 2rpx solid #dee2e6;
  
  &:hover {
    background: linear-gradient(135deg, #e9ecef 0%, #dee2e6 100%);
    color: $chess-color-dark;
    border-color: #adb5bd;
    transform: translateY(-4rpx);
    box-shadow: 0 8rpx 25rpx rgba(0, 0, 0, 0.1);
  }
  
  &:active {
    transform: translateY(-2rpx);
    box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.1);
  }
}

.btn-confirm {
  background: linear-gradient(135deg, $chess-color-gold 0%, darken($chess-color-gold, 10%) 100%);
  color: #ffffff;
  border: 2rpx solid $chess-color-gold;
  box-shadow: 0 4rpx 15rpx rgba(212, 175, 55, 0.3);
  
  &:hover {
    background: linear-gradient(135deg, darken($chess-color-gold, 5%) 0%, darken($chess-color-gold, 15%) 100%);
    border-color: darken($chess-color-gold, 5%);
    transform: translateY(-4rpx);
    box-shadow: 0 8rpx 25rpx rgba(212, 175, 55, 0.4);
  }
  
  &:active {
    transform: translateY(-2rpx);
    box-shadow: 0 4rpx 15rpx rgba(212, 175, 55, 0.3);
  }
  
  &:disabled {
    background: linear-gradient(135deg, $chess-color-muted 0%, darken($chess-color-muted, 10%) 100%);
    color: #ffffff;
    border-color: $chess-color-muted;
    cursor: not-allowed;
    transform: none;
    box-shadow: none;
    
    &::before {
      display: none;
    }
    
    &:hover {
      transform: none;
      box-shadow: none;
    }
  }
}
</style>