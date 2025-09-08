<script>
	import {
		mapMutations
	} from 'vuex'
	import {
		version
	} from './package.json'
	// #ifdef APP
	import checkUpdate from '@/uni_modules/uni-upgrade-center-app/utils/check-update';
	// #endif

	export default {
		onLaunch: function(options) {
		// #ifdef H5
		console.log(
			`%c hello uniapp %c v${version} `,
			'background:#35495e ; padding: 1px; border-radius: 3px 0 0 3px;  color: #fff',
			'background:#007aff ;padding: 1px; border-radius: 0 3px 3px 0;  color: #fff; font-weight: bold;'
		)
		// #endif
		// 线上示例使用
		// console.log('%c uni-app官方团队诚邀优秀前 端工程师加盟，一起打造更卓越的uni-app & uniCloud，欢迎投递简历到 hr2013@dcloud.io', 'color: red');
		console.log('App Launch', options);
		
		// 处理小程序启动参数
		this.handleLaunchOptions(options);
		
		// 检查登录状态
		this.checkLoginStatus();
			// #ifdef APP-PLUS
			// App平台检测升级，服务端代码是通过uniCloud的云函数实现的，详情可参考：https://ext.dcloud.net.cn/plugin?id=4542
			if (plus.runtime.appid !== 'HBuilder') { // 真机运行不需要检查更新，真机运行时appid固定为'HBuilder'，这是调试基座的appid
				checkUpdate()
			}

			// 一键登录预登陆，可以显著提高登录速度
			uni.preLogin({
				provider: 'univerify',
				success: (res) => {
					// 成功
					this.setUniverifyErrorMsg();
					console.log("preLogin success: ", res);
				},
				fail: (res) => {
					this.setUniverifyLogin(false);
					this.setUniverifyErrorMsg(res.errMsg);
					// 失败
					console.log("preLogin fail res: ", res);
				}
			})
			// #endif
		},
		onShow: function() {
			console.log('App Show')
			// 每次显示时检查登录状态
			this.checkLoginStatus();
		},
		onHide: function() {
			console.log('App Hide')
		},
		globalData: {
			test: '',
			apiBaseUrl: 'https://api.airoubo.com',
			baseUrl: 'https://mp.airoubo.com'
		},
		methods: {
			...mapMutations(['setUniverifyErrorMsg', 'setUniverifyLogin']),
			
			/**
			 * 检查登录状态
			 */
			checkLoginStatus() {
				// 获取当前页面路径
				const pages = getCurrentPages()
				if (pages.length === 0) return
				
				const currentPage = pages[pages.length - 1]
				const currentRoute = currentPage.route
				
				// 不需要登录的页面
				const noAuthPages = [
					'pages/login/login',
					'pages/index/index'
				]
				
				// 如果当前在不需要登录的页面，直接返回
				if (noAuthPages.includes(currentRoute)) {
					return
			}
			
			// 检查是否已登录
			if (!this.$auth.isLoggedIn()) {
				console.log('用户未登录，跳转到登录页面')
				// 延迟跳转，避免在应用启动时立即跳转
				setTimeout(() => {
					uni.reLaunch({
						url: '/pages/login/login'
					})
				}, 100)
			} else {
				console.log('用户已登录，当前用户:', this.$auth.getCurrentUser())
			}
		},
		
		/**
		 * 处理小程序启动参数
		 */
		handleLaunchOptions(options) {
			console.log('启动参数:', options)
			
			// 处理小程序码扫码进入的场景
			if (options && options.scene) {
				const scene = decodeURIComponent(options.scene)
				console.log('解析scene参数:', scene)
				
				// 解析roundId参数
				const roundIdMatch = scene.match(/roundId=(\d+)/)
				if (roundIdMatch) {
					const roundId = roundIdMatch[1]
					console.log('提取到回合ID:', roundId)
					
					// 保存到全局数据，供后续页面使用
					this.globalData.pendingRoundId = roundId
					
					// 延迟跳转到回合详情页，确保应用完全启动
					setTimeout(() => {
						this.navigateToRoundDetail(roundId)
					}, 1000)
				}
			}
		},
		
		/**
		 * 跳转到回合详情页
		 */
		navigateToRoundDetail(roundId) {
			console.log('准备跳转到回合详情页，roundId:', roundId)
			
			// 检查用户是否已登录
			const userInfo = uni.getStorageSync('userInfo')
			const isLoggedIn = userInfo && userInfo.token
			
			console.log('用户登录状态:', isLoggedIn)
			
			if (!isLoggedIn) {
				// 未登录，跳转到登录页并传递重定向参数
				console.log('用户未登录，跳转到登录页')
				uni.showToast({
					title: '请先登录',
					icon: 'none',
					duration: 1500
				})
				
				setTimeout(() => {
					uni.navigateTo({
						url: `/pages/login/login?redirect=${encodeURIComponent('/pages/round-detail/round-detail?id=' + roundId + '&autoJoin=true')}`
					})
				}, 1500)
				return
			}
			
			// 检查用户信息是否完善
			if (!userInfo.nickname || !userInfo.avatar) {
				console.log('用户信息不完善，跳转到完善信息页')
				uni.showToast({
					title: '请先完善个人信息',
					icon: 'none',
					duration: 1500
				})
				
				setTimeout(() => {
					uni.navigateTo({
						url: `/pages/profile/edit?redirectAfterProfile=${encodeURIComponent('/pages/round-detail/round-detail?id=' + roundId + '&autoJoin=true')}`
					})
				}, 1500)
				return
			}
			
			// 已登录且信息完善，直接跳转到回合详情页
			console.log('用户已登录且信息完善，直接跳转到回合详情页')
			uni.navigateTo({
				url: `/pages/round-detail/round-detail?id=${roundId}&autoJoin=true`
			})
		}
		}
	}
</script>

<style lang="scss">
	/*每个页面公共css */
	@import '@/uni_modules/uni-scss/index.scss';
	@import '@/common/styles/global.scss';
	/* #ifndef APP-PLUS-NVUE */
	/* uni.css - 通用组件、模板样式库，可以当作一套ui库应用 */
	@import './common/uni.css';
	@import '@/static/customicons.css';
	/* H5 兼容 pc 所需 */
	/* #ifdef H5 */
	@media screen and (min-width: 768px) {
		body {
			overflow-y: scroll;
		}
	}

	/* 顶栏通栏样式 */
	/* .uni-top-window {
	    left: 0;
	    right: 0;
	} */

	uni-page-body {
		background-color: #F5F5F5 !important;
		min-height: 100% !important;
		height: auto !important;
	}

	.uni-top-window uni-tabbar .uni-tabbar {
		background-color: #fff !important;
	}

	.uni-app--showleftwindow .hideOnPc {
		display: none !important;
	}

	/* #endif */

	/* 以下样式用于 hello uni-app 演示所需 */
	page {
		background-color: $chess-bg-primary;
		height: 100%;
		font-size: 28rpx;
		color: $uni-text-color;
		/* line-height: 1.8; */
	}

	/* 全局字体设置 */
	* {
		font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Helvetica Neue', Arial, sans-serif;
	}

	/* 全局隐藏滚动条 */
	::-webkit-scrollbar {
		display: none;
	}
	
	/* 隐藏scroll-view组件的滚动条 */
	scroll-view::-webkit-scrollbar {
		display: none;
	}
	
	/* Firefox */
	* {
		scrollbar-width: none;
	}
	
	/* IE 10+ */
	* {
		-ms-overflow-style: none;
	}

	.fix-pc-padding {
		padding: 0 50px;
	}

	.uni-header-logo {
		padding: 30rpx;
		flex-direction: column;
		justify-content: center;
		align-items: center;
		margin-top: 10rpx;
	}

	.uni-header-image {
		width: 100px;
		height: 100px;
	}

	.uni-hello-text {
		color: #7A7E83;
	}

	.uni-hello-addfile {
		text-align: center;
		line-height: 300rpx;
		background: #FFF;
		padding: 50rpx;
		margin-top: 10px;
		font-size: 38rpx;
		color: #808080;
	}

	/* #endif*/
</style>
