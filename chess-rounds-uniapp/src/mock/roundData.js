// Mock数据 - 回合详情页面测试数据
export const mockRoundData = {
	// 回合基本信息
	roundInfo: {
		id: 'mock-round-001',
		name: '测试回合 - 周末对局',
		status: 'in_progress', // pending, in_progress, completed
		type: 'private',
		creator: {
			id: '1',
			name: '张三',
			avatar: '/static/avatar1.png'
		},
		createdAt: '2025-01-08T10:00:00Z',
		description: '周末朋友聚会，轻松对局',
		multiplier: 2, // 游戏倍率
		participantCount: 4,
		maxParticipants: 4
	},

	// 参与者信息
	participants: [
		{
			id: '1',
			name: '张三',
			avatar: '/static/avatar1.png',
			role: 'creator', // creator, participant
			status: 'active', // active, inactive
			joinedAt: '2025-01-08T10:00:00Z'
		},
		{
			id: 'user-002',
			name: '李四',
			avatar: '/static/avatar2.png',
			role: 'participant',
			status: 'active',
			joinedAt: '2025-01-08T10:05:00Z'
		},
		{
			id: 'user-003',
			name: '王五',
			avatar: '/static/avatar3.png',
			role: 'participant',
			status: 'active',
			joinedAt: '2025-01-08T10:08:00Z'
		},
		{
			id: 'user-004',
			name: '赵六',
			avatar: '/static/avatar4.png',
			role: 'participant',
			status: 'active',
			joinedAt: '2025-01-08T10:12:00Z'
		},
		{
			id: 'table-board',
			name: '台板',
			avatar: '/static/table-board.png',
			role: 'table_board', // 特殊角色：台板
			status: 'active',
			joinedAt: '2025-01-08T10:00:00Z'
		}
	],

	// 游戏记录
	gameRecords: [
		{
			id: 'record-001',
			roundId: 'mock-round-001',
			gameNumber: 1,
			participantAmounts: {
				'1': 15,  // 张三赢了15
				'user-002': 10,  // 李四赢了10
				'user-003': -10, // 王五输了10
				'user-004': -25, // 赵六输了25
				'table-board': 10 // 台板赢了10（总和：15+10-10-25+10=0）
			},
			createdAt: '2025-01-08T10:30:00Z',
			createdBy: '1'
		},
		{
			id: 'record-002',
			roundId: 'mock-round-001',
			gameNumber: 2,
			participantAmounts: {
				'1': -30, // 张三输了30
				'user-002': 20,  // 李四赢了20
				'user-003': 35,  // 王五赢了35
				'user-004': -35, // 赵六输了35
				'table-board': 10 // 台板赢了10（总和：-30+20+35-35+10=0）
			},
			createdAt: '2025-01-08T11:15:00Z',
			createdBy: 'user-002'
		},
		{
			id: 'record-003',
			roundId: 'mock-round-001',
			gameNumber: 3,
			participantAmounts: {
				'1': 5,   // 张三赢了5
				'user-002': -25, // 李四输了25
				'user-003': 0,   // 王五平局
				'user-004': 5,   // 赵六赢了5
				'table-board': 15 // 台板赢了15（总和：5-25+0+5+15=0）
			},
			createdAt: '2025-01-08T12:00:00Z',
			createdBy: 'user-003'
		},
		{
			id: 'record-004',
			roundId: 'mock-round-001',
			gameNumber: 4,
			participantAmounts: {
				'1': -20,  // 张三输了20
				'user-002': 25,  // 李四赢了25
				'user-003': -25, // 王五输了25
				'user-004': 10,  // 赵六赢了10
				'table-board': 10 // 台板赢了10（总和：-20+25-25+10+10=0）
			},
			createdAt: '2025-01-08T12:45:00Z',
			createdBy: 'user-002'
		},
		{
			id: 'record-005',
			roundId: 'mock-round-001',
			gameNumber: 5,
			participantAmounts: {
				'1': 20,   // 张三赢了20
				'user-002': -15, // 李四输了15
				'user-003': 10,  // 王五赢了10
				'user-004': -30, // 赵六输了30
				'table-board': 15 // 台板赢了15（总和：20-15+10-30+15=0）
			},
			createdAt: '2025-01-08T13:30:00Z',
			createdBy: '1'
		},
		{
			id: 'record-006',
			roundId: 'mock-round-001',
			gameNumber: 6,
			participantAmounts: {
				'1': -20,  // 张三输了20
				'user-002': -15, // 李四输了15
				'user-003': 45,  // 王五赢了45
				'user-004': -10, // 赵六输了10
				'table-board': 0 // 台板为0（总和：-20-15+45-10+0=0）
			},
			createdAt: '2025-01-08T14:15:00Z',
			createdBy: 'user-003'
		},
		{
			id: 'record-007',
			roundId: 'mock-round-001',
			gameNumber: 7,
			participantAmounts: {
				'1': 35,   // 张三赢了35
				'user-002': 5,   // 李四赢了5
				'user-003': -15, // 王五输了15
				'user-004': -25, // 赵六输了25
				'table-board': 0 // 台板为0（总和：35+5-15-25+0=0）
			},
			createdAt: '2025-01-08T15:00:00Z',
			createdBy: '1'
		},
		{
			id: 'record-008',
			roundId: 'mock-round-001',
			gameNumber: 8,
			participantAmounts: {
				'1': -25,  // 张三输了25
				'user-002': 20,  // 李四赢了20
				'user-003': -5,  // 王五输了5
				'user-004': 10,  // 赵六赢了10
				'table-board': 0 // 台板为0（总和：-25+20-5+10+0=0）
			},
			createdAt: '2025-01-08T15:45:00Z',
			createdBy: 'user-004'
		},
		{
			id: 'record-009',
			roundId: 'mock-round-001',
			gameNumber: 9,
			participantAmounts: {
				'1': 15,   // 张三赢了15
				'user-002': -30, // 李四输了30
				'user-003': 25,  // 王五赢了25
				'user-004': -10, // 赵六输了10
				'table-board': 0 // 台板为0（总和：15-30+25-10+0=0）
			},
			createdAt: '2025-01-08T16:30:00Z',
			createdBy: 'user-003'
		},
		{
			id: 'record-010',
			roundId: 'mock-round-001',
			gameNumber: 10,
			participantAmounts: {
				'1': -10,  // 张三输了10
				'user-002': 40,  // 李四赢了40
				'user-003': -20, // 王五输了20
				'user-004': -10, // 赵六输了10
				'table-board': 0 // 台板为0（总和：-10+40-20-10+0=0）
			},
			createdAt: '2025-01-08T17:15:00Z',
			createdBy: 'user-002'
		},
		{
			id: 'record-011',
			roundId: 'mock-round-001',
			gameNumber: 11,
			participantAmounts: {
				'1': 30,   // 张三赢了30
				'user-002': -5,  // 李四输了5
				'user-003': -10, // 王五输了10
				'user-004': -15, // 赵六输了15
				'table-board': 0 // 台板为0（总和：30-5-10-15+0=0）
			},
			createdAt: '2025-01-08T18:00:00Z',
			createdBy: '1'
		},
		{
			id: 'record-012',
			roundId: 'mock-round-001',
			gameNumber: 12,
			participantAmounts: {
				'1': -35,  // 张三输了35
				'user-002': 15,  // 李四赢了15
				'user-003': 35,  // 王五赢了35
				'user-004': -15, // 赵六输了15
				'table-board': 0 // 台板为0（总和：-35+15+35-15+0=0）
			},
			createdAt: '2025-01-08T18:45:00Z',
			createdBy: 'user-003'
		}
	]
};

// 计算参与者累加金额的辅助函数
export function calculateParticipantTotals(participants, gameRecords) {
	return participants.map(participant => {
		const totalAmount = gameRecords.reduce((sum, record) => {
			return sum + (record.participantAmounts[participant.id] || 0);
		}, 0);
		
		return {
			...participant,
			totalAmount
		};
	});
}

// 计算台板累计金额（从参与者金额中获取台板数据）
export function calculateTotalTableBoard(gameRecords) {
	return gameRecords.reduce((total, record) => {
		// 台板金额是负值，取绝对值作为累计
		return total + Math.abs(record.participantAmounts?.['table-board'] || 0)
	}, 0)
}

// 模拟API延迟的辅助函数
export function mockApiDelay(ms = 500) {
	return new Promise(resolve => setTimeout(resolve, ms));
}

// 生成新的游戏记录ID
export function generateRecordId() {
	return 'record-' + Date.now().toString(36) + Math.random().toString(36).substr(2);
}

// Mock回合列表数据
export const mockRoundsList = [
  {
    id: 'round-001',
    name: '周末麻将局',
    creatorId: 'user-001',
    creatorName: '张三',
    status: 'active',
    participantCount: 4,
    maxParticipants: 4,
    gameCount: 3,
    totalTableBoard: 150,
    createdAt: '2024-01-15 14:30:00',
    updatedAt: '2024-01-15 18:45:00',
    description: '周末朋友聚会麻将'
  },
  {
    id: 'round-002',
    name: '公司团建',
    creatorId: 'user-002',
    creatorName: '李四',
    status: 'waiting',
    participantCount: 2,
    maxParticipants: 4,
    gameCount: 0,
    totalTableBoard: 0,
    createdAt: '2024-01-16 10:00:00',
    updatedAt: '2024-01-16 10:00:00',
    description: '公司同事娱乐活动'
  },
  {
    id: 'round-003',
    name: '春节家庭局',
    creatorId: 'user-003',
    creatorName: '王五',
    status: 'finished',
    participantCount: 4,
    maxParticipants: 4,
    gameCount: 8,
    totalTableBoard: 320,
    createdAt: '2024-01-10 19:00:00',
    updatedAt: '2024-01-10 23:30:00',
    description: '春节期间家人聚会'
  },
  {
    id: 'round-004',
    name: '老友聚会',
    creatorId: 'user-004',
    creatorName: '赵六',
    status: 'active',
    participantCount: 3,
    maxParticipants: 4,
    gameCount: 1,
    totalTableBoard: 25,
    createdAt: '2024-01-17 15:20:00',
    updatedAt: '2024-01-17 16:10:00',
    description: '老同学聚会娱乐'
  }
]

// 获取用户参与的回合列表
export const getUserRounds = (userId) => {
  // 直接使用详情页的mockRoundData，确保数据同源
  const roundInfo = mockRoundData.roundInfo
  const participants = mockRoundData.participants
  const gameRecords = mockRoundData.gameRecords
  
  // 计算参与者累计金额（与详情页逻辑完全一致）
  const participantsWithAmounts = participants.map(participant => {
    const totalAmount = gameRecords.reduce((total, record) => {
      const amount = record.participantAmounts?.[participant.id] || 0
      return total + amount
    }, 0)
    
    return {
      ...participant,
      totalAmount,
      isCurrentUser: participant.id === userId
    }
  })
  
  // 台板不再在回合列表中显示
  
  return [{
    // 使用详情页的roundInfo作为基础数据
    id: roundInfo.id,
    name: roundInfo.name,
    status: roundInfo.status,
    creatorId: roundInfo.creator.id,
    creatorName: roundInfo.creator.name,
    participantCount: roundInfo.participantCount,
    maxParticipants: roundInfo.maxParticipants,
    createdAt: roundInfo.createdAt,
    description: roundInfo.description,
    
    // 用户角色信息
    isCreator: roundInfo.creator.id === userId,
    isParticipant: participants.some(p => p.id === userId),
    isSpectator: false,
    
    // 参与者和累计数据（与详情页完全同源）
    participants: participantsWithAmounts,
    recordCount: gameRecords.length,
    
    // 显示用的头像列表
    participantAvatars: participantsWithAmounts.map(p => p.avatar),
    spectatorAvatars: []
  }]
}

export default mockRoundData;