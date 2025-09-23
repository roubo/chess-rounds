# 圈子排行榜API接口设计

## 接口概述

本文档定义了圈子排行榜功能的所有API接口，包括圈子管理、成员管理和排行榜查询等功能。

## 通用响应格式

```json
{
    "code": 200,
    "message": "操作成功",
    "data": {},
    "timestamp": "2024-01-01T00:00:00Z"
}
```

## 错误码定义

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 409 | 资源冲突 |
| 500 | 服务器内部错误 |

## 1. 圈子管理接口

### 1.1 创建圈子

**接口地址：** `POST /api/circles`

**请求头：**
```
Content-Type: application/json
Authorization: Bearer {token}
```

**请求参数：**
```json
{
    "name": "我的圈子",
    "description": "圈子描述（可选）"
}
```

**参数说明：**
- `name`: 圈子名称，必填，1-100字符
- `description`: 圈子描述，可选，最多500字符

**响应示例：**
```json
{
    "code": 200,
    "message": "创建成功",
    "data": {
        "id": 1,
        "name": "我的圈子",
        "description": "圈子描述",
        "join_code": "A1B2C",
        "creator_id": 123,
        "member_count": 1,
        "max_members": 100,
        "is_active": true,
        "created_at": "2024-01-01T00:00:00Z"
    }
}
```

### 1.2 加入圈子

**接口地址：** `POST /api/circles/join`

**请求参数：**
```json
{
    "join_code": "A1B2C"
}
```

**参数说明：**
- `join_code`: 5位加入码，必填

**响应示例：**
```json
{
    "code": 200,
    "message": "加入成功",
    "data": {
        "circle": {
            "id": 1,
            "name": "我的圈子",
            "description": "圈子描述",
            "member_count": 2,
            "creator_nickname": "创建者昵称"
        },
        "member": {
            "role": "member",
            "joined_at": "2024-01-01T00:00:00Z"
        }
    }
}
```

**错误响应：**
```json
{
    "code": 404,
    "message": "加入码不存在或已失效"
}
```

```json
{
    "code": 409,
    "message": "您已经是该圈子的成员"
}
```

### 1.3 获取用户圈子列表

**接口地址：** `GET /api/circles/my`

**请求参数：**
- 无

**响应示例：**
```json
{
    "code": 200,
    "message": "获取成功",
    "data": {
        "circles": [
            {
                "id": 1,
                "name": "我的圈子",
                "description": "圈子描述",
                "join_code": "A1B2C",
                "member_count": 5,
                "max_members": 100,
                "role": "creator",
                "joined_at": "2024-01-01T00:00:00Z",
                "is_active": true
            },
            {
                "id": 2,
                "name": "朋友圈子",
                "description": "朋友们的游戏圈子",
                "join_code": null,
                "member_count": 8,
                "max_members": 100,
                "role": "member",
                "joined_at": "2024-01-02T00:00:00Z",
                "is_active": true
            }
        ],
        "total": 2
    }
}
```

### 1.4 获取圈子详情

**接口地址：** `GET /api/circles/{circleId}`

**路径参数：**
- `circleId`: 圈子ID

**响应示例：**
```json
{
    "code": 200,
    "message": "获取成功",
    "data": {
        "circle": {
            "id": 1,
            "name": "我的圈子",
            "description": "圈子描述",
            "join_code": "A1B2C",
            "creator_id": 123,
            "creator_nickname": "创建者昵称",
            "member_count": 5,
            "max_members": 100,
            "is_active": true,
            "created_at": "2024-01-01T00:00:00Z"
        },
        "current_user_role": "creator"
    }
}
```

### 1.5 退出圈子

**接口地址：** `DELETE /api/circles/{circleId}/members/me`

**路径参数：**
- `circleId`: 圈子ID

**响应示例：**
```json
{
    "code": 200,
    "message": "退出成功"
}
```

**错误响应：**
```json
{
    "code": 403,
    "message": "创建者不能退出圈子，请先转让圈子或解散圈子"
}
```

## 2. 排行榜接口

### 2.1 获取圈子排行榜

**接口地址：** `GET /api/circles/{circleId}/leaderboard`

**路径参数：**
- `circleId`: 圈子ID

**查询参数：**
- `sort`: 排序方式，可选值：`desc`（降序，默认）、`asc`（升序）
- `page`: 页码，默认1
- `size`: 每页数量，默认20，最大100

**请求示例：**
```
GET /api/circles/1/leaderboard?sort=desc&page=1&size=20
```

**响应示例：**
```json
{
    "code": 200,
    "message": "获取成功",
    "data": {
        "leaderboard": [
            {
                "rank": 1,
                "user_id": 123,
                "nickname": "张三",
                "avatar_url": "https://example.com/avatar1.jpg",
                "total_amount": 1500.00,
                "total_rounds": 25,
                "win_rounds": 18,
                "win_rate": 72.00,
                "last_play_time": "2024-01-01T00:00:00Z",
                "member_role": "creator",
                "joined_at": "2024-01-01T00:00:00Z"
            },
            {
                "rank": 2,
                "user_id": 456,
                "nickname": "李四",
                "avatar_url": "https://example.com/avatar2.jpg",
                "total_amount": 1200.00,
                "total_rounds": 20,
                "win_rounds": 12,
                "win_rate": 60.00,
                "last_play_time": "2024-01-01T00:00:00Z",
                "member_role": "member",
                "joined_at": "2024-01-01T00:00:00Z"
            }
        ],
        "pagination": {
            "page": 1,
            "size": 20,
            "total": 50,
            "total_pages": 3
        },
        "current_user_rank": {
            "rank": 5,
            "user_id": 789,
            "total_amount": 800.00,
            "total_rounds": 15,
            "win_rounds": 8,
            "win_rate": 53.33
        },
        "circle_info": {
            "id": 1,
            "name": "我的圈子",
            "member_count": 50
        }
    }
}
```

### 2.2 刷新圈子排行榜

**接口地址：** `POST /api/circles/{circleId}/leaderboard/refresh`

**路径参数：**
- `circleId`: 圈子ID

**说明：** 手动触发排行榜缓存更新，通常在数据不一致时使用

**响应示例：**
```json
{
    "code": 200,
    "message": "排行榜刷新成功",
    "data": {
        "updated_at": "2024-01-01T00:00:00Z",
        "member_count": 50
    }
}
```

## 3. 圈子成员管理接口

### 3.1 获取圈子成员列表

**接口地址：** `GET /api/circles/{circleId}/members`

**路径参数：**
- `circleId`: 圈子ID

**查询参数：**
- `page`: 页码，默认1
- `size`: 每页数量，默认20
- `role`: 角色筛选，可选值：`creator`、`admin`、`member`

**响应示例：**
```json
{
    "code": 200,
    "message": "获取成功",
    "data": {
        "members": [
            {
                "user_id": 123,
                "nickname": "张三",
                "avatar_url": "https://example.com/avatar1.jpg",
                "role": "creator",
                "joined_at": "2024-01-01T00:00:00Z",
                "is_active": true
            }
        ],
        "pagination": {
            "page": 1,
            "size": 20,
            "total": 50,
            "total_pages": 3
        }
    }
}
```

### 3.2 移除圈子成员（仅创建者）

**接口地址：** `DELETE /api/circles/{circleId}/members/{userId}`

**路径参数：**
- `circleId`: 圈子ID
- `userId`: 用户ID

**权限要求：** 仅圈子创建者可操作

**响应示例：**
```json
{
    "code": 200,
    "message": "移除成功"
}
```

### 3.3 设置圈子管理员（仅创建者）

**接口地址：** `PUT /api/circles/{circleId}/members/{userId}/role`

**路径参数：**
- `circleId`: 圈子ID
- `userId`: 用户ID

**请求参数：**
```json
{
    "role": "admin"
}
```

**参数说明：**
- `role`: 角色，可选值：`admin`、`member`

**响应示例：**
```json
{
    "code": 200,
    "message": "角色设置成功"
}
```

## 4. 圈子设置接口

### 4.1 更新圈子信息（仅创建者和管理员）

**接口地址：** `PUT /api/circles/{circleId}`

**路径参数：**
- `circleId`: 圈子ID

**请求参数：**
```json
{
    "name": "新的圈子名称",
    "description": "新的圈子描述",
    "max_members": 200
}
```

**响应示例：**
```json
{
    "code": 200,
    "message": "更新成功",
    "data": {
        "id": 1,
        "name": "新的圈子名称",
        "description": "新的圈子描述",
        "max_members": 200,
        "updated_at": "2024-01-01T00:00:00Z"
    }
}
```

### 4.2 重新生成加入码（仅创建者）

**接口地址：** `POST /api/circles/{circleId}/regenerate-code`

**路径参数：**
- `circleId`: 圈子ID

**响应示例：**
```json
{
    "code": 200,
    "message": "加入码重新生成成功",
    "data": {
        "join_code": "X9Y8Z"
    }
}
```

### 4.3 解散圈子（仅创建者）

**接口地址：** `DELETE /api/circles/{circleId}`

**路径参数：**
- `circleId`: 圈子ID

**响应示例：**
```json
{
    "code": 200,
    "message": "圈子解散成功"
}
```

## 5. 统计接口

### 5.1 获取圈子统计信息

**接口地址：** `GET /api/circles/{circleId}/statistics`

**路径参数：**
- `circleId`: 圈子ID

**响应示例：**
```json
{
    "code": 200,
    "message": "获取成功",
    "data": {
        "circle_stats": {
            "total_members": 50,
            "active_members": 45,
            "total_rounds": 200,
            "total_amount": 50000.00,
            "avg_amount_per_member": 1111.11
        },
        "top_players": [
            {
                "user_id": 123,
                "nickname": "张三",
                "total_amount": 1500.00,
                "rank": 1
            }
        ],
        "recent_activities": [
            {
                "user_id": 456,
                "nickname": "李四",
                "action": "joined",
                "created_at": "2024-01-01T00:00:00Z"
            }
        ]
    }
}
```

## 接口权限说明

| 接口 | 权限要求 |
|------|----------|
| 创建圈子 | 登录用户 |
| 加入圈子 | 登录用户 |
| 获取用户圈子列表 | 登录用户 |
| 获取圈子详情 | 圈子成员 |
| 退出圈子 | 圈子成员（创建者除外） |
| 获取排行榜 | 圈子成员 |
| 刷新排行榜 | 圈子成员 |
| 获取成员列表 | 圈子成员 |
| 移除成员 | 圈子创建者 |
| 设置管理员 | 圈子创建者 |
| 更新圈子信息 | 圈子创建者或管理员 |
| 重新生成加入码 | 圈子创建者 |
| 解散圈子 | 圈子创建者 |
| 获取统计信息 | 圈子成员 |

## 实现注意事项

1. **权限验证**：每个接口都需要验证用户身份和权限
2. **数据缓存**：排行榜数据使用缓存表，定期更新
3. **并发控制**：加入圈子、退出圈子等操作需要考虑并发安全
4. **数据一致性**：成员数量等统计数据需要保持一致性
5. **性能优化**：排行榜查询使用索引和分页优化
6. **错误处理**：提供详细的错误信息和状态码
7. **日志记录**：记录关键操作的日志用于审计