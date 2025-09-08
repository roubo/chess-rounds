<template>
	<view class="menu-list">
		<view class="menu-section" v-for="(section, index) in menuSections" :key="index">
			<view class="section-title" v-if="section.title">
				<text class="title-text">{{ section.title }}</text>
			</view>
			
			<view class="menu-items">
				<view 
					class="menu-item" 
					v-for="item in section.items" 
					:key="item.key"
					@click="handleMenuClick(item)"
				>
					<view class="item-left">
						<view class="item-icon" :style="{ backgroundColor: item.iconBg || '#F8F9FA' }">
							<text class="icon-text">{{ item.icon }}</text>
						</view>
						<text class="item-title">{{ item.title }}</text>
					</view>
					
					<view class="item-right">
						<text class="item-badge" v-if="item.badge">{{ item.badge }}</text>
						<text class="item-arrow">›</text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	name: 'MenuList',
	props: {
		isLoggedIn: {
			type: Boolean,
			default: false
		}
	},
	computed: {
		menuSections() {
			const sections = []
			
			// 我的回合相关
			if (this.isLoggedIn) {
				sections.push({
					title: '我的回合',
					items: [
						{
							key: 'my-rounds',
							icon: '🎯',
							title: '我的回合',
							iconBg: '#E3F2FD'
						},
						{
							key: 'created-rounds',
							icon: '⭐',
							title: '我创建的',
							iconBg: '#FFF3E0'
						},
						{
							key: 'participated-rounds',
							icon: '🤝',
							title: '我参与的',
							iconBg: '#E8F5E8'
						},
						{
							key: 'spectated-rounds',
							icon: '👁️',
							title: '我观战的',
							iconBg: '#F3E5F5'
						}
					]
				})
			}
			
			// 应用功能
			sections.push({
				title: '应用功能',
				items: [
					{
						key: 'rankings',
						icon: '🏆',
						title: '排行榜',
						iconBg: '#FFF8E1'
					},
					{
						key: 'history',
						icon: '📊',
						title: '历史记录',
						iconBg: '#E1F5FE'
					},
					{
						key: 'friends',
						icon: '👥',
						title: '好友列表',
						iconBg: '#F1F8E9',
						badge: this.isLoggedIn ? '5' : null
					}
				]
			})
			
			// 设置和帮助
			sections.push({
				title: '设置和帮助',
				items: [
					{
						key: 'settings',
						icon: '⚙️',
						title: '设置',
						iconBg: '#F5F5F5'
					},
					{
						key: 'help',
						icon: '❓',
						title: '帮助与反馈',
						iconBg: '#E8EAF6'
					},
					{
						key: 'about',
						icon: 'ℹ️',
						title: '关于我们',
						iconBg: '#FCE4EC'
					}
				]
			})
			
			// 账户操作
			if (this.isLoggedIn) {
				sections.push({
					items: [
						{
							key: 'logout',
							icon: '🚪',
							title: '退出登录',
							iconBg: '#FFEBEE'
						}
					]
				})
			} else {
				sections.push({
					items: [
						{
							key: 'login',
							icon: '🔑',
							title: '登录/注册',
							iconBg: '#E8F5E8'
						}
					]
				})
			}
			
			return sections
		}
	},
	methods: {
		handleMenuClick(item) {
			this.$emit('menu-click', item.key)
		}
	}
}
</script>

<style scoped>
.menu-list {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.menu-section {
	background-color: #FFFFFF;
	border-radius: 16rpx;
	overflow: hidden;
	box-shadow: 0 2rpx 8rpx rgba(93, 104, 138, 0.1);
}

.section-title {
	padding: 16rpx 24rpx 8rpx;
	background-color: #FAFBFC;
	border-bottom: 1rpx solid #F0F0F0;
}

.title-text {
	font-size: 24rpx;
	color: #7F8C8D;
	font-weight: 500;
}

.menu-items {
	display: flex;
	flex-direction: column;
}

.menu-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 20rpx 24rpx;
	border-bottom: 1rpx solid #F8F9FA;
	position: relative;
}

.menu-item:last-child {
	border-bottom: none;
}

.menu-item:active {
	background-color: #F8F9FA;
}

.item-left {
	display: flex;
	align-items: center;
	gap: 16rpx;
}

.item-icon {
	width: 64rpx;
	height: 64rpx;
	border-radius: 12rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.icon-text {
	font-size: 28rpx;
}

.item-title {
	font-size: 28rpx;
	color: #2C3E50;
	font-weight: 500;
}

.item-right {
	display: flex;
	align-items: center;
	gap: 12rpx;
}

.item-badge {
	background-color: #E74C3C;
	color: #FFFFFF;
	font-size: 20rpx;
	padding: 4rpx 8rpx;
	border-radius: 10rpx;
	min-width: 32rpx;
	height: 32rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.item-arrow {
	font-size: 32rpx;
	color: #BDC3C7;
	font-weight: bold;
}
</style>