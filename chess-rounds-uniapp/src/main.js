import App from './App'
import store from './store'
import api from './utils/api.js'
import WechatAPI from './utils/wechat.js'
import AuthManager from './utils/auth.js'
import config from './config/api.js'
import toastMixin from './mixins/toast.js'

// #ifndef VUE3
import Vue from 'vue'
Vue.config.productionTip = false
Vue.prototype.$store = store
Vue.prototype.$adpid = "1111111111"
Vue.prototype.$backgroundAudioData = {
	playing: false,
	playTime: 0,
	formatedPlayTime: '00:00:00'
}
// 注册全局工具类
Vue.prototype.$api = api
Vue.prototype.$wechat = WechatAPI
Vue.prototype.$auth = AuthManager
Vue.prototype.$config = config
// 注册Toast混入
Vue.mixin(toastMixin)
App.mpType = 'app'
const app = new Vue({
	store,
	...App
})
app.$mount()
// #endif

// #ifdef VUE3
import {
	createSSRApp
} from 'vue'
import * as Pinia from 'pinia';
import Vuex from "vuex";
export function createApp() {
	const app = createSSRApp(App)
	app.use(store)
	app.use(Pinia.createPinia());
	app.config.globalProperties.$adpid = "1111111111"
	app.config.globalProperties.$backgroundAudioData = {
		playing: false,
		playTime: 0,
		formatedPlayTime: '00:00:00'
	}
	// 注册全局工具类
	app.config.globalProperties.$api = api
	app.config.globalProperties.$wechat = WechatAPI
	app.config.globalProperties.$auth = AuthManager
	app.config.globalProperties.$config = config
	// 注册Toast混入
	app.mixin(toastMixin)
	return {
		app,
		Vuex, // 如果 nvue 使用 vuex 的各种map工具方法时，必须 return Vuex
		Pinia // 此处必须将 Pinia 返回
	}
}
// #endif
