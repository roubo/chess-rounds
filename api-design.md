# 回合 API 接口设计文档

## API 概述

本文档定义了"回合"微信小程序的所有后端API接口，包括用户认证、回合管理、数据统计等功能模块。

### 基础信息

- **Base URL**: `https://api.airoubo.com`
- **API版本**: v1
- **数据格式**: JSON
- **字符编码**: UTF-8
- **时间格式**: ISO 8601 (yyyy-MM-dd'T'HH:mm:ss.SSS'Z')

### 通用响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": "2024-01-01T12:00:00.000Z",
  "requestId": "uuid-string"
}
```

### 状态码定义

| 状态码 | 说明 | 描述 |
|--------|------|------|
| 200 | SUCCESS | 请求成功 |
| 400 | BAD_REQUEST | 请求参数错误 |
| 401 | UNAUTHORIZED | 未授权访问 |
| 403 | FORBIDDEN | 禁止访问 |
| 404 | NOT_FOUND | 资源不存在 |
| 409 | CONFLICT | 资源冲突 |
| 500 | INTERNAL_ERROR | 服务器内部错误 |

## 1. 用户认证模块

### 1.1 微信登录

**接口**: `POST /api/v1/auth/wechat/login`

**描述**: 使用微信授权码进行登录

**请求参数**:
```json
{
  "code": "string",          // 微信授权码
  "encryptedData": "string", // 加密用户信息（可选）
  "iv": "string"             // 初始向量（可选）
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "jwt-token-string",
    "refreshToken": "refresh-token-string",
    "expiresIn": 7200,
    "user": {
      "id": "user-uuid",
      "openId": "wechat-openid",
      "nickname": "用户昵称",
      "avatar": "头像URL",
      "isNewUser": true
    }
  }
}
```

### 1.2 刷新Token

**接口**: `POST /api/v1/auth/refresh`

**请求参数**:
```json
{
  "refreshToken": "refresh-token-string"
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "Token刷新成功",
  "data": {
    "token": "new-jwt-token-string",
    "expiresIn": 7200
  }
}
```

### 1.3 获取用户信息

**接口**: `GET /api/v1/auth/profile`

**请求头**: `Authorization: Bearer {token}`

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": "user-uuid",
    "openId": "wechat-openid",
    "nickname": "用户昵称",
    "avatar": "头像URL",
    "createdAt": "2024-01-01T12:00:00.000Z",
    "lastLoginAt": "2024-01-01T12:00:00.000Z"
  }
}
```

## 2. 回合管理模块

### 2.1 创建回合

**接口**: `POST /api/v1/rounds`

**请求头**: `Authorization: Bearer {token}`

**请求参数**:
```json
{
  "multiplier": 1.5,           // 倍率
  "hasTableBoard": true,       // 是否有台板
  "participants": [            // 参与者列表
    {
      "userId": "user-uuid",
      "role": "PLAYER"          // PLAYER, TABLE_BOARD
    }
  ]
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "回合创建成功",
  "data": {
    "id": "round-uuid",
    "roundCode": "ABC12345",
    "creatorId": "user-uuid",
    "multiplier": 1.5,
    "hasTableBoard": true,
    "status": "WAITING",
    "participants": [
      {
        "userId": "user-uuid",
        "nickname": "用户昵称",
        "avatar": "头像URL",
        "role": "PLAYER",
        "joinedAt": "2024-01-01T12:00:00.000Z"
      }
    ],
    "createdAt": "2024-01-01T12:00:00.000Z"
  }
}
```

### 2.2 加入回合

**接口**: `POST /api/v1/rounds/{roundCode}/join`

**请求头**: `Authorization: Bearer {token}`

**请求参数**:
```json
{
  "role": "PLAYER"  // PLAYER, SPECTATOR
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "加入成功",
  "data": {
    "round": {
      "id": "round-uuid",
      "roundCode": "ABC12345",
      "status": "WAITING",
      "participants": [...],
      "spectators": [...]
    }
  }
}
```

### 2.3 开始回合

**接口**: `POST /api/v1/rounds/{roundId}/start`

**请求头**: `Authorization: Bearer {token}`

**响应数据**:
```json
{
  "code": 200,
  "message": "回合已开始",
  "data": {
    "round": {
      "id": "round-uuid",
      "status": "IN_PROGRESS",
      "startedAt": "2024-01-01T12:00:00.000Z",
      "aiAnalysis": {
        "summary": "本回合有你的最佳合伙人，是你的好运",
        "level": "GOOD_LUCK",
        "color": "#5D688A"
      }
    }
  }
}
```

### 2.4 添加记录

**接口**: `POST /api/v1/rounds/{roundId}/records`

**请求头**: `Authorization: Bearer {token}`

**请求参数**:
```json
{
  "winnerId": "user-uuid",     // 赢家ID
  "loserId": "user-uuid",      // 输家ID
  "points": 100,               // 分数
  "description": "备注信息"     // 可选
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "记录添加成功",
  "data": {
    "record": {
      "id": "record-uuid",
      "roundId": "round-uuid",
      "winnerId": "user-uuid",
      "loserId": "user-uuid",
      "points": 100,
      "actualPoints": 150,      // 考虑倍率后的实际分数
      "description": "备注信息",
      "createdAt": "2024-01-01T12:00:00.000Z"
    },
    "aiAnalysis": {
      "summary": "连续获胜，状态火热！",
      "level": "HOT_STREAK",
      "color": "#F7A5A5"
    }
  }
}
```

### 2.5 结束回合

**接口**: `POST /api/v1/rounds/{roundId}/finish`

**请求头**: `Authorization: Bearer {token}`

**响应数据**:
```json
{
  "code": 200,
  "message": "回合已结束",
  "data": {
    "round": {
      "id": "round-uuid",
      "status": "FINISHED",
      "finishedAt": "2024-01-01T12:00:00.000Z",
      "finalScores": [
        {
          "userId": "user-uuid",
          "nickname": "用户昵称",
          "totalPoints": 250,
          "rank": 1
        }
      ],
      "aiSummary": {
        "summary": "精彩的对局！恭喜获胜者",
        "highlights": ["最大单局得分: 150分", "最佳表现: 用户A"]
      }
    }
  }
}
```

### 2.6 获取回合详情

**接口**: `GET /api/v1/rounds/{roundId}`

**请求头**: `Authorization: Bearer {token}` (可选，旁观者可不传)

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "round": {
      "id": "round-uuid",
      "roundCode": "ABC12345",
      "creatorId": "user-uuid",
      "multiplier": 1.5,
      "hasTableBoard": true,
      "status": "IN_PROGRESS",
      "participants": [...],
      "spectators": [...],
      "records": [...],
      "currentScores": [...],
      "createdAt": "2024-01-01T12:00:00.000Z",
      "startedAt": "2024-01-01T12:00:00.000Z"
    },
    "userRole": "PLAYER",        // PLAYER, SPECTATOR, NONE
    "canOperate": true           // 是否可以操作
  }
}
```

### 2.7 获取用户回合列表

**接口**: `GET /api/v1/rounds`

**请求头**: `Authorization: Bearer {token}`

**查询参数**:
- `status`: 回合状态 (WAITING, IN_PROGRESS, FINISHED)
- `page`: 页码 (默认1)
- `size`: 每页大小 (默认10)

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "rounds": [
      {
        "id": "round-uuid",
        "roundCode": "ABC12345",
        "status": "FINISHED",
        "participantCount": 4,
        "recordCount": 15,
        "myFinalScore": 250,
        "myRank": 1,
        "createdAt": "2024-01-01T12:00:00.000Z",
        "finishedAt": "2024-01-01T15:30:00.000Z"
      }
    ],
    "pagination": {
      "page": 1,
      "size": 10,
      "total": 25,
      "totalPages": 3
    }
  }
}
```

## 3. 旁观者功能模块

### 3.1 旁观者评价

**接口**: `POST /api/v1/rounds/{roundId}/spectator-rating`

**请求头**: `Authorization: Bearer {token}`

**请求参数**:
```json
{
  "targetUserId": "user-uuid",  // 被评价用户ID
  "ratingType": "LIKE"          // LIKE, DISLIKE
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "评价成功",
  "data": {
    "rating": {
      "id": "rating-uuid",
      "roundId": "round-uuid",
      "fromUserId": "user-uuid",
      "targetUserId": "user-uuid",
      "ratingType": "LIKE",
      "createdAt": "2024-01-01T12:00:00.000Z"
    }
  }
}
```

### 3.2 获取旁观链接

**接口**: `GET /api/v1/rounds/{roundId}/spectator-link`

**请求头**: `Authorization: Bearer {token}`

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "spectatorCode": "ABC12345",
    "spectatorLink": "https://mp.airoubo.com/spectate?code=ABC12345",
    "qrCode": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA..."
  }
}
```

## 4. 数据统计模块

### 4.1 获取用户统计数据

**接口**: `GET /api/v1/statistics/user`

**请求头**: `Authorization: Bearer {token}`

**查询参数**:
- `period`: 统计周期 (TODAY, THIS_MONTH, THIS_YEAR)

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "period": "TODAY",
    "summary": {
      "totalRounds": 5,
      "totalWins": 3,
      "totalLosses": 2,
      "winRate": 0.6,
      "totalPoints": 1250,
      "averagePoints": 250
    },
    "trends": {
      "pointsChange": "+15%",
      "winRateChange": "+5%",
      "roundsChange": "+2"
    }
  }
}
```

### 4.2 获取月度图表数据

**接口**: `GET /api/v1/statistics/monthly-chart`

**请求头**: `Authorization: Bearer {token}`

**查询参数**:
- `year`: 年份 (默认当前年)

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "year": 2024,
    "monthlyData": [
      {
        "month": 1,
        "totalRounds": 15,
        "totalPoints": 3750,
        "winRate": 0.65,
        "averagePoints": 250
      }
    ],
    "chartConfig": {
      "colors": ["#5D688A", "#F7A5A5", "#FFDBB6"],
      "type": "line"
    }
  }
}
```

### 4.3 获取最佳合伙人

**接口**: `GET /api/v1/statistics/best-partners`

**请求头**: `Authorization: Bearer {token}`

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "bestPartners": [
      {
        "userId": "user-uuid",
        "nickname": "合伙人昵称",
        "avatar": "头像URL",
        "cooperationCount": 25,
        "winRate": 0.75,
        "totalPoints": 5000,
        "relationship": "最佳合伙人",
        "summary": "与你配合默契，胜率极高"
      }
    ]
  }
}
```

### 4.4 获取最强对手

**接口**: `GET /api/v1/statistics/strongest-opponents`

**请求头**: `Authorization: Bearer {token}`

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "strongestOpponents": [
      {
        "userId": "user-uuid",
        "nickname": "对手昵称",
        "avatar": "头像URL",
        "battleCount": 20,
        "myWinRate": 0.35,
        "averagePointsDiff": -150,
        "relationship": "最强对手",
        "summary": "实力强劲，需要加倍努力"
      }
    ]
  }
}
```

## 5. AI分析模块

### 5.1 获取回合AI分析

**接口**: `GET /api/v1/ai/round-analysis/{roundId}`

**请求头**: `Authorization: Bearer {token}`

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "analysis": {
      "roundSummary": "本回合有你的最佳合伙人，是你的好运",
      "level": "GOOD_LUCK",
      "color": "#5D688A",
      "dynamics": [
        {
          "recordId": "record-uuid",
          "analysis": "连续获胜，状态火热！",
          "level": "HOT_STREAK",
          "timestamp": "2024-01-01T12:00:00.000Z"
        }
      ],
      "finalSummary": "精彩的对局！你的表现可圈可点",
      "suggestions": ["继续保持良好状态", "注意与合伙人的配合"]
    }
  }
}
```

### 5.2 获取用户AI洞察

**接口**: `GET /api/v1/ai/user-insights`

**请求头**: `Authorization: Bearer {token}`

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "insights": {
      "playingStyle": "稳健型选手",
      "strengths": ["决策冷静", "配合默契"],
      "improvements": ["可以更加积极进攻"],
      "personalityAnalysis": "你是一个善于合作的玩家，在团队配合中表现出色",
      "recommendations": [
        "建议多与高手对战提升技巧",
        "保持现有的稳健风格"
      ]
    }
  }
}
```

## 6. 系统配置模块

### 6.1 获取系统配置

**接口**: `GET /api/v1/system/config`

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "config": {
      "maxParticipantsPerRound": 4,
      "defaultMultiplier": 1.0,
      "aiAnalysisEnabled": true,
      "spectatorRatingEnabled": true,
      "roundCodeLength": 8,
      "colors": {
        "primary": "#5D688A",
        "secondary": "#F7A5A5",
        "accent": "#FFDBB6",
        "background": "#FFF2EF"
      }
    }
  }
}
```

### 6.2 健康检查

**接口**: `GET /actuator/health`

**响应数据**:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    },
    "redis": {
      "status": "UP",
      "details": {
        "version": "6.2.6"
      }
    }
  }
}
```

## 7. 错误处理

### 7.1 参数验证错误

```json
{
  "code": 400,
  "message": "请求参数错误",
  "data": {
    "errors": [
      {
        "field": "multiplier",
        "message": "倍率必须大于0",
        "rejectedValue": -1
      }
    ]
  },
  "timestamp": "2024-01-01T12:00:00.000Z",
  "requestId": "uuid-string"
}
```

### 7.2 业务逻辑错误

```json
{
  "code": 409,
  "message": "回合已满员，无法加入",
  "data": {
    "errorCode": "ROUND_FULL",
    "maxParticipants": 4,
    "currentParticipants": 4
  },
  "timestamp": "2024-01-01T12:00:00.000Z",
  "requestId": "uuid-string"
}
```

### 7.3 认证错误

```json
{
  "code": 401,
  "message": "Token已过期，请重新登录",
  "data": {
    "errorCode": "TOKEN_EXPIRED",
    "expiredAt": "2024-01-01T10:00:00.000Z"
  },
  "timestamp": "2024-01-01T12:00:00.000Z",
  "requestId": "uuid-string"
}
```

## 8. 接口限流

### 8.1 限流规则

| 接口类型 | 限制 | 时间窗口 |
|----------|------|----------|
| 登录接口 | 10次/IP | 1分钟 |
| 创建回合 | 5次/用户 | 1分钟 |
| 添加记录 | 30次/用户 | 1分钟 |
| 查询接口 | 100次/用户 | 1分钟 |

### 8.2 限流响应

```json
{
  "code": 429,
  "message": "请求过于频繁，请稍后再试",
  "data": {
    "retryAfter": 60,
    "limit": 10,
    "remaining": 0,
    "resetTime": "2024-01-01T12:01:00.000Z"
  },
  "timestamp": "2024-01-01T12:00:00.000Z",
  "requestId": "uuid-string"
}
```

## 9. WebSocket 实时通信

### 9.1 连接地址

**WebSocket URL**: `wss://api.airoubo.com/ws/rounds/{roundId}`

**连接参数**:
- `token`: JWT认证令牌

### 9.2 消息格式

```json
{
  "type": "ROUND_UPDATE",
  "data": {
    "roundId": "round-uuid",
    "updateType": "NEW_RECORD",
    "payload": {
      "record": {...},
      "currentScores": [...]
    }
  },
  "timestamp": "2024-01-01T12:00:00.000Z"
}
```

### 9.3 消息类型

| 类型 | 说明 | 触发时机 |
|------|------|----------|
| ROUND_UPDATE | 回合更新 | 回合状态变化 |
| NEW_RECORD | 新记录 | 添加新记录 |
| PARTICIPANT_JOIN | 参与者加入 | 有人加入回合 |
| PARTICIPANT_LEAVE | 参与者离开 | 有人离开回合 |
| SPECTATOR_RATING | 旁观者评价 | 收到评价 |
| AI_ANALYSIS | AI分析 | AI分析完成 |

## 10. 数据模型

### 10.1 用户模型

```json
{
  "id": "string",
  "openId": "string",
  "unionId": "string",
  "nickname": "string",
  "avatar": "string",
  "gender": "number",
  "country": "string",
  "province": "string",
  "city": "string",
  "createdAt": "datetime",
  "updatedAt": "datetime",
  "lastLoginAt": "datetime"
}
```

### 10.2 回合模型

```json
{
  "id": "string",
  "roundCode": "string",
  "creatorId": "string",
  "multiplier": "decimal",
  "hasTableBoard": "boolean",
  "status": "enum",
  "maxParticipants": "number",
  "createdAt": "datetime",
  "startedAt": "datetime",
  "finishedAt": "datetime"
}
```

### 10.3 记录模型

```json
{
  "id": "string",
  "roundId": "string",
  "winnerId": "string",
  "loserId": "string",
  "points": "number",
  "actualPoints": "number",
  "description": "string",
  "createdAt": "datetime"
}
```

这个API设计文档提供了完整的接口规范，包括：
- 详细的请求/响应格式
- 完整的错误处理机制
- 实时通信方案
- 数据模型定义
- 安全和限流策略

开发团队可以根据这个文档进行前后端开发，确保接口的一致性和完整性。