# 圈子排行榜功能设计方案

## 功能概述

圈子排行榜功能允许用户创建和加入圈子，在圈子内查看成员的金额排行榜（筹码*倍率之和）。每个用户可以加入多个圈子，并在不同圈子间切换查看排行榜。

## 数据库设计

### 1. 圈子表 (circles)

存储圈子基本信息

```sql
CREATE TABLE circles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '圈子ID',
    name VARCHAR(100) NOT NULL COMMENT '圈子名称',
    description VARCHAR(500) COMMENT '圈子描述',
    join_code VARCHAR(5) NOT NULL UNIQUE COMMENT '加入码（5位随机字母数字）',
    creator_id BIGINT NOT NULL COMMENT '创建者用户ID',
    member_count INT DEFAULT 1 COMMENT '成员数量',
    max_members INT DEFAULT 100 COMMENT '最大成员数',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否活跃',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_join_code (join_code),
    INDEX idx_creator_id (creator_id),
    INDEX idx_is_active (is_active),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='圈子表';
```

### 2. 圈子成员表 (circle_members)

存储圈子成员关系

```sql
CREATE TABLE circle_members (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '成员关系ID',
    circle_id BIGINT NOT NULL COMMENT '圈子ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role ENUM('creator', 'admin', 'member') DEFAULT 'member' COMMENT '角色：creator-创建者，admin-管理员，member-普通成员',
    nickname VARCHAR(100) COMMENT '在圈子中的昵称',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否活跃成员',
    
    FOREIGN KEY (circle_id) REFERENCES circles(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    UNIQUE KEY uk_circle_user (circle_id, user_id),
    INDEX idx_circle_id (circle_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role (role),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='圈子成员表';
```

### 3. 圈子排行榜缓存表 (circle_leaderboard_cache)

存储圈子排行榜缓存数据，提高查询性能

```sql
CREATE TABLE circle_leaderboard_cache (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '缓存ID',
    circle_id BIGINT NOT NULL COMMENT '圈子ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '总金额（筹码*倍率之和）',
    total_rounds INT DEFAULT 0 COMMENT '总回合数',
    win_rounds INT DEFAULT 0 COMMENT '胜利回合数',
    win_rate DECIMAL(5,2) DEFAULT 0.00 COMMENT '胜率',
    last_play_time TIMESTAMP COMMENT '最后游戏时间',
    rank_position INT DEFAULT 0 COMMENT '排名位置',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (circle_id) REFERENCES circles(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    UNIQUE KEY uk_circle_user_cache (circle_id, user_id),
    INDEX idx_circle_id (circle_id),
    INDEX idx_total_amount (total_amount),
    INDEX idx_rank_position (rank_position),
    INDEX idx_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='圈子排行榜缓存表';
```

## API接口设计

### 1. 圈子管理接口

#### 1.1 创建圈子
```
POST /api/circles
Content-Type: application/json

Request:
{
    "name": "我的圈子",
    "description": "圈子描述"
}

Response:
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
        "created_at": "2024-01-01T00:00:00Z"
    }
}
```

#### 1.2 加入圈子
```
POST /api/circles/join
Content-Type: application/json

Request:
{
    "join_code": "A1B2C"
}

Response:
{
    "code": 200,
    "message": "加入成功",
    "data": {
        "circle": {
            "id": 1,
            "name": "我的圈子",
            "description": "圈子描述",
            "member_count": 2
        }
    }
}
```

#### 1.3 获取用户圈子列表
```
GET /api/circles/my

Response:
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
                "role": "creator",
                "joined_at": "2024-01-01T00:00:00Z"
            }
        ]
    }
}
```

### 2. 排行榜接口

#### 2.1 获取圈子排行榜
```
GET /api/circles/{circleId}/leaderboard?sort=desc&page=1&size=20

Response:
{
    "code": 200,
    "message": "获取成功",
    "data": {
        "leaderboard": [
            {
                "rank": 1,
                "user_id": 123,
                "nickname": "用户昵称",
                "avatar_url": "头像URL",
                "total_amount": 1500.00,
                "total_rounds": 25,
                "win_rounds": 18,
                "win_rate": 72.00,
                "last_play_time": "2024-01-01T00:00:00Z"
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
            "total_amount": 800.00
        }
    }
}
```

## 前端UI设计

### 1. 我的页面Tab结构
在现有的"汇总"和"历史记录"tab之间添加"圈子排行榜"tab：
- 汇总
- **圈子排行榜** (新增)
- 历史记录

### 2. 圈子排行榜页面结构

#### 2.1 无圈子状态
```
┌─────────────────────────────────┐
│          圈子排行榜              │
├─────────────────────────────────┤
│                                 │
│        🏆                      │
│    还没有加入任何圈子            │
│   快来创建或加入一个圈子吧！      │
│                                 │
│  ┌─────────┐  ┌─────────┐      │
│  │ 创建圈子 │  │ 加入圈子 │      │
│  └─────────┘  └─────────┘      │
│                                 │
└─────────────────────────────────┘
```

#### 2.2 有圈子状态
```
┌─────────────────────────────────┐
│ 圈子选择器 ▼    排序 ▼          │
├─────────────────────────────────┤
│ 排名  头像  昵称      金额       │
├─────────────────────────────────┤
│  🥇   👤   张三    +1,500.00   │
│  🥈   👤   李四    +1,200.00   │
│  🥉   👤   王五    +1,000.00   │
│  4    👤   赵六     +800.00    │
│  5    👤   我       +600.00    │ ← 高亮当前用户
│  ...                           │
├─────────────────────────────────┤
│        ┌─────────┐              │
│        │ 管理圈子 │              │
│        └─────────┘              │
└─────────────────────────────────┘
```

### 3. 交互流程

#### 3.1 创建圈子流程
1. 点击"创建圈子"按钮
2. 弹出创建圈子表单：
   - 圈子名称（必填）
   - 圈子描述（可选）
3. 提交后显示创建成功，展示加入码
4. 自动刷新页面显示新圈子

#### 3.2 加入圈子流程
1. 点击"加入圈子"按钮
2. 弹出输入框，输入5位加入码
3. 验证加入码，成功后加入圈子
4. 自动刷新页面显示新圈子

#### 3.3 排行榜交互
1. 圈子切换：下拉选择不同圈子
2. 排序切换：正序/倒序切换按钮
3. 分页加载：支持上拉加载更多
4. 当前用户高亮显示

## 实现要点

### 1. 加入码生成规则
- 5位随机字母和数字组合
- 排除易混淆字符（0、O、1、I、l）
- 确保唯一性

### 2. 排行榜计算逻辑
- 金额计算：所有参与回合的筹码*倍率之和
- 实时更新：每次游戏结束后更新缓存
- 性能优化：使用缓存表避免复杂查询

### 3. 数据同步策略
- 游戏结束时触发排行榜更新
- 定时任务校验数据一致性
- 支持手动刷新排行榜

### 4. 权限控制
- 只有圈子成员可以查看排行榜
- 创建者可以管理圈子（踢出成员等）
- 支持圈子管理员角色

## 技术实现建议

### 1. 后端实现
- 使用Spring Boot + MyBatis
- Redis缓存热点数据
- 异步更新排行榜缓存

### 2. 前端实现
- 使用uni-app框架
- 组件化设计，复用现有组件
- 支持下拉刷新和上拉加载

### 3. 性能优化
- 排行榜数据缓存
- 分页查询减少数据量
- 图片懒加载优化体验