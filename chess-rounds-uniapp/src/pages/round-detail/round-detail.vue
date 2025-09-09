<template>
  <view class="round-detail">
    <!-- 加载状态 -->
    <view v-if="loading" class="loading-container">
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 错误状态 -->
    <view v-else-if="!roundDetail" class="error-container">
      <text class="error-text">加载失败，请重试</text>
      <button class="retry-btn" @click="loadRoundDetail">重新加载</button>
    </view>

    <!-- 正常内容 -->
    <scroll-view 
      v-else
      scroll-y="true" 
      class="content-container"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <!-- 固定的回合累计区域 -->
      <view class="fixed-header">
        <view class="amounts-section">
          <view class="section-header">
            <view class="section-left">
              <text class="section-title">回合累计</text>
              <text v-if="roundDetail.multiplier" class="multiplier-hint">倍率x{{ roundDetail.multiplier }}</text>
            </view>
            <text class="section-subtitle">共{{ Array.isArray(gameRecords) ? gameRecords.length : 0 }}局</text>
          </view>
          
          <view class="amounts-list">
            <!-- 台板累计 -->
            <view v-if="hasTableBoardParticipant" class="amount-item-new table-board">
              <image class="participant-avatar" src="/static/images/table-board.png" mode="aspectFill" />
              <view class="participant-info">
                <text class="participant-name">台板</text>
                <text class="participant-amount" :class="{ 'positive': tableBoardAmount > 0, 'negative': tableBoardAmount < 0 }">
                  {{ formatAmount(tableBoardAmount) }}
                </text>
              </view>
            </view>
            
            <!-- 参与者累计 -->
            <view 
              v-for="participant in participantsWithAmounts" 
              :key="participant.id"
              class="amount-item-new"
              :class="{ 'current-user': participant.id === currentUserId }"
            >
              <image class="participant-avatar" :src="getParticipantAvatarUrl(participant)" mode="aspectFill" />
              <view class="participant-info">
                <text class="participant-name">{{ (participant.user_info && participant.user_info.nickname) || participant.name || '未知用户' }}</text>
                <text class="participant-amount" :class="{ 'positive': participant.totalAmount > 0, 'negative': participant.totalAmount < 0 }">
                  {{ formatAmount(participant.totalAmount) }}
                </text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 每局记录区域 -->
      <view class="records-section">
          <view class="section-header">
            <text class="section-title">每局记录</text>
            <view v-if="canAddRecord" class="add-button" @click="showAddRecordModal">
              <text class="add-icon">+</text>
            </view>
          </view>
          
          <view v-if="!Array.isArray(gameRecords) || gameRecords.length === 0" class="empty-records">
            <text class="empty-text">暂无记录</text>
          </view>
          
          <view v-else class="records-list">
            <view 
              v-for="(record, index) in (Array.isArray(gameRecords) ? gameRecords.slice().reverse() : [])" 
              :key="record.id"
              class="record-item"
            >
              <view class="record-header">
                <text class="record-title">第{{ Array.isArray(gameRecords) ? gameRecords.length - index : 0 }}局</text>
                <text class="record-time">{{ formatTime(record.createdAt) }}</text>
              </view>
              
              <view class="record-amounts-horizontal">
                <!-- 台板记录 -->
                <view v-if="hasTableBoardParticipant" class="record-amount-horizontal table-board">
                  <image class="amount-avatar" src="/static/images/table-board.png" mode="aspectFill" />
                  <view class="amount-info">
                    <text class="amount-name">台板</text>
                    <text class="amount-value" :class="{ 'positive': getTableBoardAmount(record) > 0, 'negative': getTableBoardAmount(record) < 0 }">
                      {{ formatAmount(Math.abs(getTableBoardAmount(record))) }}
                    </text>
                  </view>
                </view>
                
                <!-- 参与者记录 -->
                <view 
                  v-for="participantId in Object.keys(record.participantAmounts || {}).filter(id => id !== 'table-board')"
                  :key="participantId"
                  class="record-amount-horizontal"
                >
                  <image class="amount-avatar" :src="getParticipantAvatar(participantId)" mode="aspectFill" />
                  <text class="amount-value" :class="{ 'positive': record.participantAmounts[participantId] > 0, 'negative': record.participantAmounts[participantId] < 0 }">
                    {{ formatAmount(record.participantAmounts[participantId]) }}
                  </text>
                </view>
              </view>
            </view>
          </view>
        </view>
    </scroll-view>

    <!-- 添加记录弹窗 -->
    <uni-popup ref="addRecordPopup" type="bottom" background-color="#fff">
      <view class="add-record-modal">
        <view class="modal-header">
          <text class="modal-title">添加第{{ Array.isArray(gameRecords) ? gameRecords.length + 1 : 1 }}局记录</text>
        </view>
        
        <view class="modal-content">
          <view class="input-section">
            <!-- 台板输入 -->
            <view v-if="hasTableBoardParticipant" class="input-item">
              <text class="input-label">台板</text>
              <input 
                v-model.number="newRecord.tableBoardAmount"
                class="input-field"
                type="digit"
                placeholder="输入台板金额"
                @input="onAmountInput"
              />
            </view>
            
            <!-- 参与者输入 -->
            <view 
              v-for="(participant, index) in participantsWithAmounts" 
              :key="participant.id"
              class="participant-input-item"
            >
              <view class="participant-info">
                <image class="participant-avatar" :src="getParticipantAvatarUrl(participant)" mode="aspectFill" />
                <text class="participant-name">{{ (participant.user_info && participant.user_info.nickname) || participant.name || '未知用户' }}</text>
              </view>
              
              <view class="amount-input-group">
                <view class="sign-selector">
                  <button 
                    class="sign-btn" 
                    :class="{ active: getParticipantSign(participant.id) === '+' }"
                    @click="setParticipantSign(participant.id, '+')"
                  >
                    +
                  </button>
                  <button 
                    class="sign-btn" 
                    :class="{ active: getParticipantSign(participant.id) === '-' }"
                    @click="setParticipantSign(participant.id, '-')"
                  >
                    -
                  </button>
                </view>
                <input 
                   v-model.number="newRecord.participantAbsAmounts[participant.id]"
                   class="amount-input"
                   type="digit"
                   placeholder="筹码数"
                   :data-participant-id="participant.id"
                   @input="onAmountInput"
                 />
              </view>
            </view>
          </view>
          
          <view class="balance-info">
            <text class="balance-label">平衡检查：</text>
            <text class="balance-value" :class="{ 'balanced': isBalanced, 'unbalanced': !isBalanced }">
              {{ isBalanced ? '已平衡' : `差额 ${formatAmount(Math.abs(totalDifference))}` }}
            </text>
          </view>
        </view>
        
        <view class="modal-actions">
          <button class="action-btn cancel-btn" @click="hideAddRecordModal">
            取消
          </button>
          <button 
            class="action-btn confirm-btn"
            :disabled="!isBalanced"
            @click="addGameRecord"
          >
            确认添加
          </button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script>
import { roundsApi, handleApiError } from '@/api/rounds'
import config from '@/config/api'

export default {
  name: 'RoundDetail',
  data() {
    return {
      roundId: null,
      roundDetail: null,
      participants: [],
      gameRecords: [],
      loading: true,
      refreshing: false,
      currentUserId: null, // 从用户状态获取
      refreshTimer: null,
      autoJoin: false, // 是否自动加入回合
      
      // 添加记录相关
      newRecord: {
        tableBoardAmount: 0,
        participantAmounts: {},
        participantAbsAmounts: {},
        participantSigns: {}
      }
    }
  },
  
  computed: {
    statusText() {
      const statusMap = {
        waiting: '等待中',
        playing: '进行中',
        finished: '已结束',
        cancelled: '已取消'
      }
      return statusMap[(this.roundDetail && this.roundDetail.status)] || '未知'
    },
    
    statusClass() {
      return `status-${(this.roundDetail && this.roundDetail.status) || 'unknown'}`
    },
    
    isCreator() {
      return (this.roundDetail && this.roundDetail.creator && this.roundDetail.creator.id) === this.currentUserId
    },
    
    isParticipant() {
      return this.participants.some(p => {
        const participantUserId = (p.user_info && p.user_info.user_id) || p.id
        return participantUserId == this.currentUserId
      })
    },
    
    canAddRecord() {
      return ((this.roundDetail && this.roundDetail.status) === 'playing' || (this.roundDetail && this.roundDetail.status) === 'in_progress') && this.isParticipant
    },
    
    // 检查是否有台板参与者
    hasTableBoardParticipant() {
      return Array.isArray(this.participants) && 
             this.participants.some(p => p.id === 'table-board' || p.role === 'table_board' || p.role === 'table')
    },
    
    // 计算台板累加金额（从参与者金额中获取台板数据）
    tableBoardAmount() {
      if (!Array.isArray(this.gameRecords)) {
        return 0
      }
      return this.gameRecords.reduce((total, record) => {
        // 台板金额是负值，取绝对值作为累计
        return total + Math.abs((record.participantAmounts && record.participantAmounts['table-board']) || 0)
      }, 0)
    },
    
    // 计算参与者累加金额
    participantsWithAmounts() {
      if (!Array.isArray(this.participants)) {
        return []
      }
      return this.participants.map(participant => {
        let totalAmount = 0
        if (Array.isArray(this.gameRecords)) {
          totalAmount = this.gameRecords.reduce((total, record) => {
            // 使用participant_id或id进行匹配
            const participantId = participant.participant_id || participant.id
            const amount = (record.participantAmounts && record.participantAmounts[participantId]) || 0
            return total + amount
          }, 0)
        }
        
        return {
          ...participant,
          id: participant.participant_id || participant.id, // 统一使用participant_id
          user_id: participant.user_id, // 确保包含user_id字段
          name: (participant.user_info && participant.user_info.nickname) || participant.name,
          avatar: (participant.user_info && participant.user_info.avatar_url) || participant.avatar,
          totalAmount
        }
      })
    },
    

    
    // 检查是否平衡
    isBalanced() {
      return Math.abs(this.totalDifference) < 0.01
    },
    
    // 计算总差额
    totalDifference() {
      let total = this.newRecord.tableBoardAmount || 0
      
      // 加上所有参与者金额
      this.participantsWithAmounts.forEach((participant) => {
        const amount = this.newRecord.participantAmounts[participant.id]
        total += amount === '' ? 0 : Number(amount || 0)
      })
      
      return total
    },
    
    // 检查当前用户是否已参与回合
    isCurrentUserParticipant() {
      if (!this.currentUserId || !this.participants) return false
      return this.participants.some(p => {
        const participantUserId = (p.user_info && p.user_info.user_id) || p.id
        return participantUserId == this.currentUserId
      })
    }
  },
  
  onLoad(options) {
    console.log('回合详情页面参数:', options)
    
    // 支持从回合列表跳转和扫码进入
    this.roundId = options.id || options.roundId || options.scene
    
    // 处理小程序码参数
    if (options.scene) {
      // 解码小程序码参数，格式可能是 roundId=123
      const sceneParams = decodeURIComponent(options.scene)
      const match = sceneParams.match(/roundId=(\d+)/)
      if (match) {
        this.roundId = match[1]
      }
      // 扫码进入时自动设置为需要加入回合
      this.autoJoin = true
    } else {
      // 检查是否需要自动加入回合
      this.autoJoin = options.autoJoin === 'true'
    }
    
    if (this.roundId) {
      this.loadRoundDetail()
    } else {
      uni.showToast({
        title: '回合ID不存在',
        icon: 'error'
      })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    }
  },
  
  onUnload() {
    // 页面卸载时的清理工作
  },
  
  methods: {
    // 获取台板金额
    getTableBoardAmount(record) {
      return record.participantAmounts && record.participantAmounts['table-board'] ? record.participantAmounts['table-board'] : 0
    },
    
    async loadRoundDetail() {
      try {
        this.loading = true
        
        // 并行请求回合详情、参与者和游戏记录
        const [roundRes, participantsRes, recordsRes] = await Promise.all([
          roundsApi.getRoundDetail(this.roundId),
          roundsApi.getRoundParticipants(this.roundId),
          roundsApi.getGameRecords(this.roundId)
        ])
        
        // 调试日志
        console.log('API响应数据:', {
          roundRes,
          participantsRes,
          recordsRes
        })
        
        // 适配后端响应格式：可能直接返回数据，也可能包装在 {code, data} 中
        if (roundRes) {
          this.roundDetail = roundRes.code === 200 ? roundRes.data : roundRes
          console.log('解析后的roundDetail:', this.roundDetail)
        }
        
        if (participantsRes) {
          this.participants = (participantsRes.code === 200 ? participantsRes.data : participantsRes) || []
          console.log('解析后的participants:', this.participants)
        }
        
        if (recordsRes) {
          this.gameRecords = (recordsRes.code === 200 ? recordsRes.data : recordsRes) || []
          console.log('解析后的gameRecords:', this.gameRecords)
        }
        
        // 如果没有回合详情但有参与者数据，创建一个基本的回合详情对象
        if (!this.roundDetail && this.participants.length > 0) {
          console.log('创建临时回合详情对象')
          // 检查参与者中是否有台板
          const hasTableBoard = this.participants.some(p => p.id === 'table-board' || p.role === 'table_board' || p.role === 'table')
          this.roundDetail = {
            id: this.roundId,
            name: `回合 ${this.roundId}`,
            status: 'ACTIVE',
            hasTableBoard: hasTableBoard,
            multiplier: 1,
            createdAt: new Date().toISOString()
          }
        }
        
        // 确保roundDetail有hasTableBoard字段
        if (this.roundDetail && this.roundDetail.hasTableBoard === undefined) {
          // 通过检查参与者或后端字段来设置hasTableBoard
          const hasTableBoard = this.roundDetail.has_table || 
                               this.roundDetail.hasTable || 
                               this.participants.some(p => p.id === 'table-board' || p.role === 'table_board' || p.role === 'table')
          this.roundDetail.hasTableBoard = hasTableBoard
        }
        
        // 设置当前用户ID（mock模式下使用默认值）
        this.currentUserId = uni.getStorageSync('userId') || '1'
        
        // 显示加载成功提示
        if (this.roundDetail && this.participants.length > 0) {
          uni.showToast({
            title: '数据加载成功',
            icon: 'success',
            duration: 1500
          })
        }
        
        // 如果需要自动加入回合，则尝试加入
        if (this.autoJoin && this.roundDetail) {
          this.handleAutoJoinRound()
        }
        
      } catch (error) {
        console.error('加载回合详情失败:', error)
        handleApiError(error, '加载回合详情失败')
      } finally {
        this.loading = false
        this.refreshing = false
      }
    },
    
    // 下拉刷新
    async onRefresh() {
      try {
        this.refreshing = true
        await this.refreshAmounts()
      } catch (error) {
        console.error('刷新失败:', error)
      } finally {
          this.refreshing = false
        }
    },
    
    /**
     * 处理自动加入回合
     */
    async handleAutoJoinRound() {
      try {
        // 检查用户是否已经参与了这个回合
        if (this.isCurrentUserParticipant) {
          console.log('用户已参与该回合，无需重复加入')
          uni.showToast({
            title: '您已在该回合中',
            icon: 'success',
            duration: 1500
          })
          return
        }
        
        console.log('尝试自动加入回合:', this.roundId)
        
        // 调用加入回合接口
        const joinResult = await roundsApi.joinRound(this.roundId)
        console.log('加入回合结果:', joinResult)
        
        // 后端返回空响应体，只要没有抛出异常就表示成功
        uni.showToast({
          title: '成功加入回合',
          icon: 'success',
          duration: 1500
        })
        
        // 重新加载参与者数据
        await this.refreshParticipants()
        
      } catch (error) {
        console.error('自动加入回合失败:', error)
        uni.showToast({
          title: error.message || '加入回合失败',
          icon: 'error',
          duration: 2000
        })
      }
    },
    
    /**
     * 刷新参与者数据
     */
    async refreshParticipants() {
      try {
        const participantsRes = await roundsApi.getRoundParticipants(this.roundId)
        console.log('刷新参与者数据:', participantsRes)
        
        if (participantsRes) {
          this.participants = (participantsRes.code === 200 ? participantsRes.data : participantsRes) || []
          console.log('更新后的participants:', this.participants)
        }
      } catch (error) {
        console.error('刷新参与者数据失败:', error)
        handleApiError(error, '刷新参与者数据失败')
      }
    },
    
    // 只刷新累计区域的数值
    async refreshAmounts() {
      try {
        // 只重新获取游戏记录来更新累计数值
        const recordsRes = await roundsApi.getGameRecords(this.roundId)
        
        if (recordsRes) {
          this.gameRecords = (recordsRes.code === 200 ? recordsRes.data : recordsRes) || []
          
          // 显示刷新成功提示
          uni.showToast({
            title: '数据已更新',
            icon: 'success',
            duration: 1000
          })
        }
      } catch (error) {
        console.error('刷新累计数据失败:', error)
        uni.showToast({
          title: '刷新失败',
          icon: 'error',
          duration: 1500
        })
      }
    },
    

    
    // 显示添加记录弹窗
    showAddRecordModal() {
      // 重置新记录数据
      this.newRecord = {
        tableBoardAmount: 0,
        participantAmounts: {},
        participantAbsAmounts: {},
        participantSigns: {}
      }
      
      // 初始化参与者金额和符号
      this.participantsWithAmounts.forEach(participant => {
        this.$set(this.newRecord.participantAmounts, participant.id, '')
        this.$set(this.newRecord.participantAbsAmounts, participant.id, '')
        this.$set(this.newRecord.participantSigns, participant.id, '+')
      })
      
      this.$refs.addRecordPopup.open()
    },
    
    // 隐藏添加记录弹窗
    hideAddRecordModal() {
      this.$refs.addRecordPopup.close()
    },
    
    // 获取参与者符号
    getParticipantSign(participantId) {
      return this.newRecord.participantSigns[participantId] || '+'
    },
    
    // 设置参与者符号
    setParticipantSign(participantId, sign) {
      this.$set(this.newRecord.participantSigns, participantId, sign)
      this.updateParticipantAmount(participantId)
    },
    
    // 更新参与者实际金额
    updateParticipantAmount(participantId) {
      const absAmount = this.newRecord.participantAbsAmounts[participantId]
      const sign = this.newRecord.participantSigns[participantId] || '+'
      
      // 如果没有输入值，保持为空
      if (absAmount === undefined || absAmount === null || absAmount === '') {
        this.$set(this.newRecord.participantAmounts, participantId, '')
        return
      }
      
      const numAmount = Number(absAmount)
      const actualAmount = sign === '+' ? numAmount : -numAmount
      this.$set(this.newRecord.participantAmounts, participantId, actualAmount)
    },
    
    // 判断是否应该自动计算
    shouldAutoCalculate(participantId) {
      // 统计已输入金额的参与者数量
      const enteredCount = this.participantsWithAmounts.filter(p => {
        const absAmount = this.newRecord.participantAbsAmounts[p.id]
        return absAmount !== undefined && absAmount !== null && absAmount !== '' && !isNaN(Number(absAmount))
      }).length
      
      // 当只剩一个参与者未输入时，该参与者自动计算
      const totalParticipants = this.participantsWithAmounts.length
      const shouldAuto = enteredCount === totalParticipants - 1
      
      if (shouldAuto) {
        // 找到最后一个未输入的参与者（最后一个为空字符的参与者）
        let lastUnenteredParticipant = null
        for (let i = this.participantsWithAmounts.length - 1; i >= 0; i--) {
          const p = this.participantsWithAmounts[i]
          const absAmount = this.newRecord.participantAbsAmounts[p.id]
          if (absAmount === undefined || absAmount === null || absAmount === '') {
            lastUnenteredParticipant = p
            break
          }
        }
        return lastUnenteredParticipant && lastUnenteredParticipant.id === participantId
      }
      
      return false
    },
    
    // 格式化筹码数显示
    formatChips(amount) {
      if (amount === 0) return '0'
      const chips = Math.abs(amount)
      return chips.toString()
    },
    
    // 金额输入处理
    onAmountInput() {
      // 更新所有参与者的实际金额
      this.participantsWithAmounts.forEach(participant => {
        this.updateParticipantAmount(participant.id)
      })
      
      // 检查是否需要自动计算最后一个参与者
      const enteredCount = this.participantsWithAmounts.filter(p => {
        const absAmount = this.newRecord.participantAbsAmounts[p.id]
        return absAmount !== undefined && absAmount !== null && absAmount !== '' && !isNaN(Number(absAmount))
      }).length
      
      if (enteredCount === this.participantsWithAmounts.length - 1) {
        // 找到最后一个未输入的参与者
        let lastUnenteredParticipant = null
        for (let i = this.participantsWithAmounts.length - 1; i >= 0; i--) {
          const p = this.participantsWithAmounts[i]
          const absAmount = this.newRecord.participantAbsAmounts[p.id]
          if (absAmount === undefined || absAmount === null || absAmount === '') {
            lastUnenteredParticipant = p
            break
          }
        }
        
        if (lastUnenteredParticipant) {
          // 计算最后一个参与者应该的金额
          const totalOthers = this.participantsWithAmounts
            .filter(p => p.id !== lastUnenteredParticipant.id)
            .reduce((sum, p) => {
              const amount = this.newRecord.participantAmounts[p.id]
              return sum + (amount === '' ? 0 : Number(amount || 0))
            }, 0)
          
          const tableBoardAmount = Number(this.newRecord.tableBoardAmount || 0)
          const requiredAmount = -(tableBoardAmount + totalOthers)
          
          // 自动填入计算结果，即使为0
          const absAmount = Math.abs(requiredAmount)
          const sign = requiredAmount >= 0 ? '+' : '-'
          
          this.$set(this.newRecord.participantAbsAmounts, lastUnenteredParticipant.id, absAmount)
          this.$set(this.newRecord.participantSigns, lastUnenteredParticipant.id, sign)
          this.$set(this.newRecord.participantAmounts, lastUnenteredParticipant.id, requiredAmount)
        }
      }
      
      // 触发计算更新
      this.$forceUpdate()
    },
    
    // 添加游戏记录
    async addGameRecord() {
      try {
        // 验证数据
        if (!this.isBalanced) {
          uni.showToast({
            title: '金额不平衡，请检查输入',
            icon: 'error'
          })
          return
        }
        
        // 构建记录数据 - 适配mock数据格式
        const participantAmounts = {}
        this.participantsWithAmounts.forEach((participant) => {
          const amount = this.newRecord.participantAmounts[participant.id]
          participantAmounts[participant.id] = amount === '' ? 0 : Number(amount || 0)
        })
        
        // 构造participant_records数据
        console.log('participantsWithAmounts:', this.participantsWithAmounts)
        const participantRecords = this.participantsWithAmounts.map((participant) => {
          const amount = this.newRecord.participantAmounts[participant.id]
          // 从原始participants数组中查找对应的user_id
          const originalParticipant = this.participants.find(p => 
            (p.participant_id || p.id) === participant.id
          )
          const userId = originalParticipant ? originalParticipant.user_id : null
          console.log('participant:', participant, 'originalParticipant:', originalParticipant, 'user_id:', userId)
          return {
            user_id: userId,
            amount_change: amount === '' ? 0 : Number(amount || 0)
          }
        })
        console.log('participantRecords:', participantRecords)
        
        const recordData = {
          round_id: this.roundId,
          record_type: 'WIN', // 默认设置为胜利类型，可根据实际需求调整
          description: '游戏记录',
          total_amount: 0, // 可根据实际需求计算总金额
          participant_records: participantRecords,
          remarks: '',
          // 保留原有字段用于兼容
          roundId: this.roundId,
          tableBoardAmount: this.newRecord.tableBoardAmount,
          participantAmounts
        }
        
        const response = await roundsApi.addGameRecord(recordData)
        
        // 适配后端响应格式
        const success = response && (response.code === 200 || response.success || !response.code)
        if (success) {
          uni.showToast({
            title: '记录添加成功',
            icon: 'success'
          })
          
          // 关闭弹窗并刷新累计数据
          this.hideAddRecordModal()
          await this.refreshAmounts()
        } else {
          throw new Error(response.message || '添加记录失败')
        }
      } catch (error) {
        console.error('添加游戏记录失败:', error)
        handleApiError(error, '添加游戏记录失败')
      }
    },
    
    // 获取参与者姓名
    getParticipantName(participantId) {
      const participant = this.participants.find(p => p.id === participantId || p.participant_id === participantId)
      if (participant) {
        // 适配新的API格式：用户信息在user_info中
        return (participant.user_info && participant.user_info.nickname) || participant.name || '未知用户'
      }
      return '未知用户'
    },
    
    // 获取参与者头像
    getParticipantAvatar(participantId) {
      const participant = this.participants.find(p => p.id === participantId || p.participant_id === participantId)
      if (participant) {
        // 适配新的API格式：用户信息在user_info中
        const avatarUrl = (participant.user_info && participant.user_info.avatar_url) || participant.avatar
        // 处理相对路径的头像URL
        if (avatarUrl && avatarUrl.startsWith('/static/')) {
          const baseURL = config.staticBaseURL || 'https://api.airoubo.com'
          console.log('头像URL拼接:', baseURL, avatarUrl)
          return baseURL + avatarUrl
        }
        return avatarUrl || '/static/images/default-avatar.png'
      }
      return '/static/images/default-avatar.png'
    },
    
    // 格式化金额显示
    formatAmount(amount) {
      if (amount === 0) return '0'
      return amount > 0 ? `+${amount}` : `${amount}`
    },
    
    formatTime(timestamp) {
      if (!timestamp) return ''
      const date = new Date(timestamp)
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
    },
    
    // 获取参与者头像URL
    getParticipantAvatarUrl(participant) {
      if (!participant) {
        return '/static/images/default-avatar.png'
      }
      
      // 适配新的API格式：用户信息在user_info中
      const avatarUrl = (participant.user_info && participant.user_info.avatar_url) || participant.avatar
      
      // 处理相对路径的头像URL
      if (avatarUrl && avatarUrl.startsWith('/static/')) {
        const baseURL = config.staticBaseURL || 'https://api.airoubo.com'
        console.log('参与者头像URL拼接:', baseURL, avatarUrl)
        return baseURL + avatarUrl
      }
      
      return avatarUrl || '/static/images/default-avatar.png'
    },
    

  }
}
</script>

<style lang="scss" scoped>
@import '@/uni.scss';

.round-detail {
  height: 100vh;
  background-color: #f8f8f8;
  display: flex;
  flex-direction: column;
}

.content-container {
  height: 100vh;
}

.fixed-header {
  background-color: #f8f8f8;
  padding: 20rpx;
  margin-bottom: 20rpx;
}



.loading-container,
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
  
  .loading-text,
  .error-text {
    font-size: 32rpx;
    color: $uni-text-color;
    margin-bottom: 20rpx;
  }
  
  .retry-btn {
    background-color: $uni-color-primary;
    color: white;
    border: none;
    border-radius: 8rpx;
    padding: 20rpx 40rpx;
    font-size: 28rpx;
  }
}

.detail-container {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  padding: 20rpx;
}

.round-info-card {
  background-color: white;
  border-radius: 16rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 12rpx rgba(93, 104, 138, 0.1);
  
  .round-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
    
    .round-name {
      font-size: 36rpx;
      font-weight: bold;
      color: $uni-text-color;
      flex: 1;
    }
    
    .round-status {
      padding: 8rpx 16rpx;
      border-radius: 20rpx;
      font-size: 24rpx;
      
      &.status-waiting {
        background-color: lighten(#d4af37, 40%);
        color: darken(#d4af37, 20%);
      }
      
      &.status-playing {
        background-color: lighten($uni-color-primary, 40%);
        color: darken($uni-color-primary, 20%);
      }
      
      &.status-finished {
        background-color: lighten($uni-color-success, 40%);
        color: darken($uni-color-success, 20%);
      }
      
      &.status-cancelled {
        background-color: lighten($uni-color-error, 40%);
        color: darken($uni-color-error, 20%);
      }
    }
  }
  
  .round-meta {
    display: flex;
    flex-direction: column;
    gap: 12rpx;
    
    .meta-item {
      display: flex;
      align-items: center;
      
      .meta-label {
        font-size: 28rpx;
        color: $uni-text-color-grey;
        width: 140rpx;
      }
      
      .meta-value {
        font-size: 28rpx;
        color: $uni-text-color;
        flex: 1;
      }
    }
  }
}

// 累加金额区域
.amounts-section {
  background-color: white;
  border-radius: 16rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 12rpx rgba(93, 104, 138, 0.1);
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
    
    .section-title {
      font-size: 32rpx;
      font-weight: bold;
      color: $uni-text-color;
    }
    
    .section-left {
      display: flex;
      align-items: center;
      gap: 16rpx;
    }
    
    .section-subtitle {
      font-size: 24rpx;
      color: $uni-text-color-grey;
    }
    
    .multiplier-hint {
      font-size: 22rpx;
      color: $uni-color-primary;
      font-weight: 500;
      background-color: lighten($uni-color-primary, 45%);
      padding: 4rpx 8rpx;
      border-radius: 8rpx;
    }
  }
  
  .amounts-list {
    display: flex;
    gap: 12rpx;
    
    .amount-item-new {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 12rpx 8rpx;
      background-color: #f8f9fa;
      border-radius: 12rpx;
      border: 1rpx solid #e9ecef;
      gap: 8rpx;
      flex: 1;
      min-width: 0;
      
      &.table-board {
              background-color: #fff3cd;
              border-color: #ffeaa7;
            }
            
            &.current-user {
              border-color: $uni-color-primary;
              background-color: lighten($uni-color-primary, 45%);
            }
      
      &.current-user {
        border-color: $uni-color-primary;
        background-color: lighten($uni-color-primary, 45%);
      }
      
      .participant-avatar {
        width: 60rpx;
        height: 60rpx;
        border-radius: 50%;
        background-color: #ddd;
      }
      
      .participant-info {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 4rpx;
        width: 100%;
        
        .participant-name {
          font-size: 24rpx;
          font-weight: 500;
          color: $uni-text-color;
          text-align: center;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          max-width: 100%;
        }
        
        .participant-amount {
          font-size: 28rpx;
          font-weight: bold;
          color: $uni-text-color;
          text-align: center;
          
          &.positive {
            color: $uni-color-success;
          }
          
          &.negative {
            color: $uni-color-error;
          }
        }
      }
    }
  }
}

// 每局记录区域
.records-section {
  background-color: white;
  border-radius: 16rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 12rpx rgba(93, 104, 138, 0.1);
  margin: 0 20rpx;
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
    
    .section-title {
      font-size: 32rpx;
      font-weight: bold;
      color: $uni-text-color;
    }
    
    .add-button {
      width: 60rpx;
      height: 60rpx;
      background-color: #007aff;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      
      .add-icon {
        color: white;
        font-size: 32rpx;
        font-weight: bold;
        line-height: 1;
      }
    }
  }
}
  
  .empty-records {
    text-align: center;
    padding: 60rpx 20rpx;
    
    .empty-text {
      font-size: 28rpx;
      color: $uni-text-color-grey;
      margin-bottom: 10rpx;
    }
    
    .empty-hint {
      font-size: 24rpx;
      color: $uni-text-color-grey;
    }
  }
  
  .records-list {
    display: flex;
    flex-direction: column;
    gap: 16rpx;
    width: 100%;
    overflow: hidden;
    
    .record-item {
        background-color: #f8f9fa;
        border-radius: 12rpx;
        padding: 16rpx;
        border: 1rpx solid #e9ecef;
        width: 100%;
        max-width: 100%;
        box-sizing: border-box;
      
      .record-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12rpx;
        
        .record-title {
          font-size: 28rpx;
          font-weight: bold;
          color: #333;
        }
        
        .record-time {
          font-size: 22rpx;
          color: #666;
        }
      }
      
      .record-amounts-horizontal {
        display: flex;
        gap: 12rpx;
        flex-wrap: wrap;
        width: 100%;
        
        .record-amount-horizontal {
          display: flex;
          flex-direction: column;
          align-items: center;
          padding: 12rpx 8rpx;
          background-color: #f8f9fa;
          border-radius: 12rpx;
          border: 1rpx solid #e9ecef;
          gap: 8rpx;
          flex: 1;
          min-width: 0;
          
          &.table-board {
            background-color: #fff3cd;
            border-color: #ffeaa7;
          }
          
          .amount-avatar {
            width: 60rpx;
            height: 60rpx;
            border-radius: 50%;
            background-color: #ddd;
          }
          
          .amount-value {
            font-size: 28rpx;
            font-weight: bold;
            color: $uni-text-color;
            text-align: center;
            
            &.positive {
              color: $uni-color-success;
            }
            
            &.negative {
              color: $uni-color-error;
            }
          }
        }
      }
    }
  }

// 添加记录弹窗
.add-record-modal {
  background: white;
  border-radius: 24rpx 24rpx 0 0;
  padding: 40rpx;
  max-height: 80vh;
  overflow-y: auto;
  
  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 40rpx;
    
    .modal-title {
      font-size: 36rpx;
      font-weight: bold;
      color: $uni-text-color;
    }
    
    .modal-close {
      width: 60rpx;
      height: 60rpx;
      border-radius: 30rpx;
      background-color: #f8f9fa;
      border: none;
      display: flex;
      align-items: center;
      justify-content: center;
      
      .close-icon {
        font-size: 40rpx;
        color: $uni-text-color-grey;
      }
      
      &:active {
        background-color: #e9ecef;
      }
    }
  }
  
  .modal-content {
    margin-bottom: 40rpx;
    
    .input-section {
      margin-bottom: 30rpx;
      
      .input-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 20rpx 0;
        border-bottom: 1rpx solid #f0f0f0;
        
        &:last-child {
          border-bottom: none;
        }
        

        
        .input-label {
          font-size: 28rpx;
          color: $uni-text-color;
          min-width: 120rpx;
        }
        
        .input-field {
          flex: 1;
          text-align: right;
          font-size: 28rpx;
          color: $uni-text-color;
          background: none;
          border: none;
          outline: none;
        }
        
        .auto-amount {
          display: flex;
          align-items: center;
          gap: 8rpx;
          
          .auto-text {
            font-size: 28rpx;
            font-weight: 500;
            color: $uni-text-color;
            
            &.positive {
              color: $uni-color-success;
            }
            
            &.negative {
              color: $uni-color-error;
            }
          }
          
          .auto-hint {
            font-size: 24rpx;
            color: $uni-text-color-grey;
          }
        }
      }
      
      .participant-input-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 24rpx 0;
        border-bottom: 1rpx solid #f0f0f0;
        
        &:last-child {
          border-bottom: none;
        }
        

        
        .participant-info {
          display: flex;
          align-items: center;
          gap: 16rpx;
          
          .participant-avatar {
            width: 60rpx;
            height: 60rpx;
            border-radius: 50%;
            background-color: #ddd;
          }
          
          .participant-name {
            font-size: 28rpx;
            color: $uni-text-color;
            font-weight: 500;
          }
        }
        
        .amount-input-group {
            display: flex;
            align-items: center;
            gap: 12rpx;
            

            
            .sign-selector {
              display: flex;
              border-radius: 8rpx;
              overflow: hidden;
              border: 1rpx solid #e9ecef;
              
              .sign-btn {
                width: 60rpx;
                height: 60rpx;
                background-color: #f8f9fa;
                border: none;
                font-size: 28rpx;
                font-weight: bold;
                color: $uni-text-color-grey;
                display: flex;
                align-items: center;
                justify-content: center;
                
                &.active {
                  color: white;
                  
                  &:first-child {
                    background-color: $uni-color-success;
                  }
                  
                  &:last-child {
                    background-color: $uni-color-error;
                  }
                }
                
                &:active:not(:disabled) {
                  opacity: 0.8;
                }
                
                &:disabled {
                  cursor: not-allowed;
                }
              }
            }
          
          .amount-input {
            width: 160rpx;
            height: 60rpx;
            text-align: center;
            font-size: 28rpx;
            color: $uni-text-color;
            background-color: #f8f9fa;
            border: 1rpx solid #e9ecef;
            border-radius: 8rpx;
            padding: 0 12rpx;
          }
          

        }
        
        .auto-amount {
          display: flex;
          align-items: center;
          gap: 8rpx;
          
          .auto-text {
            font-size: 28rpx;
            font-weight: 500;
            color: $uni-color-primary;
          }
          
          .auto-hint {
            font-size: 24rpx;
            color: $uni-text-color-grey;
          }
        }
      }
    }
    
    .balance-info {
      background-color: #f8f9fa;
      border-radius: 12rpx;
      padding: 20rpx;
      text-align: center;
      
      .balance-label {
        font-size: 26rpx;
        color: $uni-text-color-grey;
        margin-right: 8rpx;
      }
      
      .balance-value {
        font-size: 28rpx;
        font-weight: 500;
        
        &.balanced {
          color: $uni-color-success;
        }
        
        &.unbalanced {
          color: $uni-color-error;
        }
      }
    }
  }
  
  .modal-actions {
    display: flex;
    gap: 20rpx;
    
    .action-btn {
      flex: 1;
      height: 88rpx;
      border-radius: 44rpx;
      font-size: 32rpx;
      font-weight: 500;
      border: none;
      display: flex;
      align-items: center;
      justify-content: center;
      
      &.cancel-btn {
        background-color: #f8f9fa;
        color: $uni-text-color-grey;
        
        &:active {
          background-color: #e9ecef;
        }
      }
      
      &.confirm-btn {
        background-color: $uni-color-primary;
        color: white;
        
        &:active {
          background-color: darken($uni-color-primary, 10%);
        }
        
        &:disabled {
          background-color: #e9ecef;
          color: #6c757d;
          cursor: not-allowed;
        }
      }
    }
  }
}
</style>