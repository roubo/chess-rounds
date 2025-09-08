/**
 * 微信小程序API工具类
 */
export class WechatAPI {
	/**
	 * 微信登录获取code
	 */
	static login() {
		return new Promise((resolve, reject) => {
			uni.login({
				provider: 'weixin',
				success: (res) => {
					if (res.code) {
						resolve(res.code)
					} else {
						reject(new Error('获取微信登录code失败'))
					}
				},
				fail: (err) => {
					console.error('微信登录失败:', err)
					reject(new Error('微信登录失败'))
				}
			})
		})
	}
	
	/**
	 * 获取用户信息（需要用户授权）
	 */
	static getUserProfile() {
		return new Promise((resolve, reject) => {
			uni.getUserProfile({
				desc: '用于完善用户资料',
				success: (res) => {
					resolve(res.userInfo)
				},
				fail: (err) => {
					console.error('获取用户信息失败:', err)
					reject(new Error('获取用户信息失败'))
				}
			})
		})
	}
	
	/**
	 * 获取用户信息（旧版本兼容）
	 */
	static getUserInfo() {
		return new Promise((resolve, reject) => {
			uni.getUserInfo({
				success: (res) => {
					resolve(res.userInfo)
				},
				fail: (err) => {
					console.error('获取用户信息失败:', err)
					reject(new Error('获取用户信息失败'))
				}
			})
		})
	}
	
	/**
	 * 扫码
	 */
	static scanCode() {
		return new Promise((resolve, reject) => {
			uni.scanCode({
				success: (res) => {
					resolve(res.result)
				},
				fail: (err) => {
					console.error('扫码失败:', err)
					reject(new Error('扫码失败'))
				}
			})
		})
	}
	
	/**
	 * 分享给好友
	 * @param {Object} options 分享配置
	 */
	static shareAppMessage(options = {}) {
		return uni.shareAppMessage({
			title: options.title || '回合记录',
			path: options.path || '/pages/rounds/rounds',
			imageUrl: options.imageUrl
		})
	}
	
	/**
	 * 分享到朋友圈
	 * @param {Object} options 分享配置
	 */
	static shareTimeline(options = {}) {
		return uni.shareTimeline({
			title: options.title || '回合记录',
			path: options.path || '/pages/rounds/rounds',
			imageUrl: options.imageUrl
		})
	}
	
	/**
	 * 显示分享菜单
	 */
	static showShareMenu() {
		uni.showShareMenu({
			withShareTicket: true,
			menus: ['shareAppMessage', 'shareTimeline']
		})
	}
	
	/**
	 * 预览图片
	 * @param {Array} urls 图片链接数组
	 * @param {Number} current 当前显示图片的索引
	 */
	static previewImage(urls, current = 0) {
		return uni.previewImage({
			urls,
			current
		})
	}
	
	/**
	 * 选择图片
	 * @param {Object} options 选择配置
	 */
	static chooseImage(options = {}) {
		return new Promise((resolve, reject) => {
			uni.chooseImage({
				count: options.count || 1,
				sizeType: options.sizeType || ['original', 'compressed'],
				sourceType: options.sourceType || ['album', 'camera'],
				success: (res) => {
					resolve(res.tempFilePaths)
				},
				fail: (err) => {
					console.error('选择图片失败:', err)
					reject(new Error('选择图片失败'))
				}
			})
		})
	}
	
	/**
	 * 获取系统信息
	 */
	static getSystemInfo() {
		return new Promise((resolve, reject) => {
			uni.getSystemInfo({
				success: (res) => {
					resolve(res)
				},
				fail: (err) => {
					console.error('获取系统信息失败:', err)
					reject(new Error('获取系统信息失败'))
				}
			})
		})
	}
}

export default WechatAPI