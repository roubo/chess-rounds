<template>
	<view class="container">
		<!-- 未登录时的空状态 -->
		<view class="empty-state" v-if="!userInfo">
			<view class="empty-icon">🀄</view>
			<text class="empty-title">暂无回合</text>
			<text class="empty-desc">登录后查看和创建回合</text>
			<button class="empty-action" @click="handleCreateRound">
				创建回合
			</button>
		</view>
		
		<!-- 回合列表 -->
		<RoundList ref="roundList" v-if="userInfo" @create-round="handleCreateRound" />
	</view>
</template>

<script>
import RoundList from '@/components/rounds/RoundList.vue'

export default {
	components: {
		RoundList
	},
	
	data() {
		return {
			userInfo: null
		}
	},
	

	
	onLoad() {
		this.checkAndLoadUserInfo()
	},
	
	// #ifdef MP-WEIXIN
	// 分享给好友
	onShareAppMessage() {
		return {
			title: '象棋回合 - 一起来下棋吧！',
			desc: '发现精彩的象棋回合，与棋友一起切磋技艺',
			path: '/pages/rounds/rounds',
			imageUrl: '/static/icons/rounds.svg'
		}
	},
	// 分享到朋友圈
	onShareTimeline() {
		return {
			title: '象棋回合 - 发现精彩对局',
			desc: '与棋友一起切磋技艺，提升棋艺水平',
			path: '/pages/rounds/rounds',
			imageUrl: '/static/icons/rounds.svg'
		}
	},
	// #endif
	
	onShow() {
		// 页面显示时检查登录状态和刷新数据
		this.checkAndLoadUserInfo()
		// 使用nextTick确保组件已经渲染完成
		this.$nextTick(() => {
			if (this.userInfo && this.$refs.roundList) {
				this.$refs.roundList.refresh()
			}
		})
	},
	
	// 下拉刷新
	async onPullDownRefresh() {
		this.checkAndLoadUserInfo()
		try {
			if (this.userInfo && this.$refs.roundList) {
				await this.$refs.roundList.refresh()
			}
		} catch (error) {
			console.error('刷新失败:', error)
		} finally {
			// 停止下拉刷新动画
			uni.stopPullDownRefresh()
		}
	},
	
	methods: {
		/**
		 * 检查并加载用户信息
		 */
		checkAndLoadUserInfo() {
			if (this.$auth.isLoggedIn()) {
				this.userInfo = this.$auth.getCurrentUser()
			} else {
				this.userInfo = null
			}
		},
		
		/**
		 * 跳转到登录页面
		 */
		goToLogin() {
			uni.navigateTo({
				url: '/pages/login/login'
			})
		},
		
		/**
		 * 处理创建回合
		 */
		handleCreateRound() {
			// 检查登录状态
			if (!this.$auth.isLoggedIn()) {
				// 未登录，跳转到登录页面，登录成功后返回创建页面
				const redirectUrl = encodeURIComponent('/pages/create-round/create-round')
				uni.navigateTo({
					url: `/pages/login/login?redirectAfterLogin=${redirectUrl}`
				})
				return
			}
			
			// 已登录，检查个人信息是否完整
			const userInfo = this.$auth.getCurrentUser()
			// 检查昵称是否完整
			const hasValidNickname = userInfo && userInfo.nickname && userInfo.nickname !== '微信用户'
			// 检查头像是否完整（avatarUrl 或 avatar_url 任一有效即可）
			const hasValidAvatar = userInfo && (
				(userInfo.avatarUrl && !userInfo.avatarUrl.includes('default')) ||
				(userInfo.avatar_url && !userInfo.avatar_url.includes('default'))
			)
			
			if (!hasValidNickname || !hasValidAvatar) {
				// 个人信息不完整，跳转到编辑页面，完成后返回创建页面
				const redirectUrl = encodeURIComponent('/pages/create-round/create-round')
				uni.navigateTo({
					url: `/pages/profile/edit?redirectAfterProfile=${redirectUrl}`
				})
				return
			}
			
			// 直接跳转到创建回合页面
			uni.navigateTo({
				url: '/pages/create-round/create-round'
			})
		}
	}
}
</script>

<style scoped lang="scss">
.container {
	display: flex;
	flex-direction: column;
	height: 100vh;
	background-color: #FFF2EF;
	/* 隐藏滚动条 */
	::-webkit-scrollbar {
		display: none;
	}
	scrollbar-width: none; /* Firefox */
	-ms-overflow-style: none; /* IE 10+ */
}

// 空状态样式（与RoundList组件保持一致）
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
</style>