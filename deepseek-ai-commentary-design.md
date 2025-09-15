# DeepSeek 智能点评功能设计方案

## 1. 功能概述

本方案旨在为象棋回合系统集成DeepSeek AI智能点评功能，提供三个层次的智能分析：

1. **回合结束总结**：对整个回合进行客观的解说视角总结
2. **实时局面分析**：每局记录时提供解说视角和个人视角的分析
3. **个人历史总结**：结合用户历史数据的个人视角总结

## 2. 数据库结构分析

### 2.1 现有相关表结构

基于现有数据库设计，以下表与AI点评功能相关：

- **rounds表**：已有`ai_analysis`字段用于存储回合AI分析
- **records表**：已有`ai_comment`字段用于存储每局AI评论
- **participants表**：存储参与者信息和统计数据
- **user_statistics表**：存储用户历史统计数据
- **user_relationships表**：存储用户关系数据

### 2.2 需要新增的表结构

#### 2.2.1 AI分析记录表 (ai_analysis_records)

```sql
CREATE TABLE ai_analysis_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'AI分析记录ID',
    round_id BIGINT NOT NULL COMMENT '回合ID',
    record_id BIGINT COMMENT '记录ID（实时分析时关联）',
    user_id BIGINT COMMENT '用户ID（个人分析时关联）',
    analysis_type ENUM('round_summary', 'game_commentary', 'personal_insight') NOT NULL COMMENT '分析类型',
    perspective ENUM('objective', 'personal') NOT NULL COMMENT '视角类型：objective-客观解说，personal-个人视角',
    content TEXT NOT NULL COMMENT 'AI分析内容',
    metadata JSON COMMENT '分析元数据（包含统计数据、关键指标等）',
    ai_model VARCHAR(50) DEFAULT 'deepseek' COMMENT 'AI模型标识',
    processing_time INT COMMENT '处理耗时（毫秒）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    FOREIGN KEY (round_id) REFERENCES rounds(id) ON DELETE CASCADE,
    FOREIGN KEY (record_id) REFERENCES records(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_round_id (round_id),
    INDEX idx_record_id (record_id),
    INDEX idx_user_id (user_id),
    INDEX idx_analysis_type (analysis_type),
    INDEX idx_perspective (perspective),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI分析记录表';
```

#### 2.2.2 AI分析配置表 (ai_analysis_configs)

```sql
CREATE TABLE ai_analysis_configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    analysis_type ENUM('round_summary', 'game_commentary', 'personal_insight') NOT NULL COMMENT '分析类型',
    perspective ENUM('objective', 'personal') NOT NULL COMMENT '视角类型',
    prompt_template TEXT NOT NULL COMMENT 'AI提示词模板',
    is_enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    priority INT DEFAULT 0 COMMENT '优先级',
    max_tokens INT DEFAULT 1000 COMMENT '最大token数',
    temperature DECIMAL(3,2) DEFAULT 0.7 COMMENT '温度参数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_type_perspective (analysis_type, perspective),
    INDEX idx_analysis_type (analysis_type),
    INDEX idx_is_enabled (is_enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI分析配置表';
```

## 3. 功能设计详情

### 3.1 回合结束客观总结（解说视角）

#### 3.1.1 触发时机
- 回合状态从`playing`变更为`finished`时

#### 3.1.2 数据来源
- 回合基本信息（rounds表）
- 所有参与者信息（participants表）
- 所有游戏记录（records表）
- 参与者胜负统计

#### 3.1.3 分析内容
- **回合概况**：参与人数、总局数、总时长、总金额
- **精彩时刻**：最大单局胜负、连胜记录、逆转局面
- **参与者表现**：胜率排名、得分统计、表现特点
- **整体评价**：回合激烈程度、技术水平、娱乐性评估

#### 3.1.4 实现流程
```
1. 监听回合状态变更事件
2. 收集回合相关数据
3. 构建AI分析提示词
4. 调用DeepSeek API
5. 存储分析结果到ai_analysis_records表
6. 更新rounds表的ai_analysis字段
```

### 3.2 实时局面分析（解说视角 + 个人视角）

#### 3.2.1 触发时机
- 每次新增游戏记录时

#### 3.2.2 解说视角分析

**数据来源：**
- 当前记录信息
- 回合当前状态
- 参与者当前得分情况
- 最近几局的趋势

**分析内容：**
- **局面描述**：当前记录的客观描述
- **影响分析**：对回合整体局势的影响
- **趋势预测**：基于当前数据的走势分析
- **技术点评**：从专业角度的技术分析

#### 3.2.3 个人视角分析

**数据来源：**
- 当前记录涉及的参与者信息
- 参与者在本回合的历史表现
- 参与者的个人统计数据
- 参与者与其他人的关系数据

**分析内容：**
- **个人表现**：针对每个参与者的表现分析
- **心理状态**：基于数据推测的心理变化
- **策略建议**：针对当前局面的个人建议
- **风险提醒**：个人需要注意的风险点

#### 3.2.4 实现流程
```
1. 监听记录新增事件
2. 并行执行解说视角和个人视角分析
   a. 解说视角：
      - 收集回合整体数据
      - 生成客观分析提示词
      - 调用DeepSeek API
   b. 个人视角：
      - 为每个参与者收集个人数据
      - 生成个人分析提示词
      - 批量调用DeepSeek API
3. 存储所有分析结果
4. 更新records表的ai_comment字段
```

### 3.3 个人历史总结（个人视角）

#### 3.3.1 触发时机
- 回合结束时，为每个参与者生成个人总结

#### 3.3.2 数据来源
- 参与者在本回合的完整表现
- 参与者的历史统计数据（user_statistics表）
- 参与者的关系数据（user_relationships表）
- 参与者最近的回合表现趋势

#### 3.3.3 分析内容
- **本回合表现**：在本回合中的详细表现分析
- **历史对比**：与个人历史数据的对比分析
- **进步评估**：技术水平和策略的进步情况
- **关系影响**：与其他参与者关系对表现的影响
- **未来建议**：基于历史数据的改进建议

#### 3.3.4 实现流程
```
1. 回合结束时触发
2. 为每个参与者收集历史数据
3. 生成个人历史分析提示词
4. 调用DeepSeek API
5. 存储个人总结结果
```

## 4. 技术实现方案

### 4.1 AI服务架构

#### 4.1.1 服务组件
- **AI分析服务**：负责调用DeepSeek API
- **数据收集服务**：负责收集和整理分析所需数据
- **提示词管理服务**：管理不同类型的AI提示词模板
- **结果存储服务**：负责存储和管理AI分析结果

#### 4.1.2 异步处理
- 使用消息队列处理AI分析任务
- 避免阻塞主业务流程
- 支持重试和错误处理

### 4.2 提示词设计

#### 4.2.1 回合总结提示词模板
```
你是一位专业的象棋解说员，请对以下回合进行客观、专业的总结分析：

回合信息：
- 回合标题：{title}
- 参与人数：{participant_count}
- 总局数：{total_games}
- 总时长：{duration}
- 总金额：{total_amount}

参与者表现：
{participants_performance}

游戏记录：
{game_records}

请从以下角度进行分析：
1. 回合整体概况和特点
2. 精彩时刻和关键转折点
3. 各参与者的表现特点
4. 技术水平和策略分析
5. 整体评价和总结

要求：
- 保持客观中立的解说视角
- 语言生动有趣，富有感染力
- 突出数据支撑的关键信息
- 字数控制在500-800字
```

#### 4.2.2 实时分析提示词模板
```
你是一位专业的象棋解说员，请对当前局面进行分析：

当前记录：
- 记录类型：{record_type}
- 涉及金额：{amount}
- 参与者：{participants}
- 描述：{description}

当前回合状态：
- 已进行局数：{current_games}
- 各参与者得分：{current_scores}
- 最近趋势：{recent_trend}

请从解说视角分析：
1. 当前局面的重要性
2. 对整体局势的影响
3. 可能的后续发展
4. 技术层面的点评

要求：
- 语言简洁有力
- 突出关键信息
- 字数控制在200-300字
```

#### 4.2.3 个人分析提示词模板
```
请为用户{nickname}提供个人视角的分析：

个人本回合表现：
{round_performance}

个人历史数据：
{historical_data}

与其他参与者关系：
{relationships}

请提供：
1. 本回合表现评价
2. 与历史数据对比
3. 优势和不足分析
4. 改进建议

要求：
- 采用鼓励性语调
- 提供具体可行的建议
- 字数控制在300-400字
```

### 4.3 API接口设计

#### 4.3.1 获取回合AI分析
```
GET /api/rounds/{roundId}/ai-analysis

Response:
{
  "roundSummary": {
    "content": "AI分析内容",
    "createdAt": "2024-01-01T10:00:00Z"
  },
  "gameCommentaries": [
    {
      "recordId": 1,
      "objectiveAnalysis": "解说视角分析",
      "personalAnalyses": [
        {
          "userId": 1,
          "content": "个人视角分析"
        }
      ]
    }
  ],
  "personalInsights": [
    {
      "userId": 1,
      "content": "个人历史总结"
    }
  ]
}
```

#### 4.3.2 获取实时AI评论
```
GET /api/records/{recordId}/ai-comment

Response:
{
  "objectiveAnalysis": "解说视角分析",
  "personalAnalyses": [
    {
      "userId": 1,
      "nickname": "用户昵称",
      "content": "个人视角分析"
    }
  ]
}
```

### 4.4 性能优化

#### 4.4.1 缓存策略
- Redis缓存AI分析结果
- 缓存用户历史数据摘要
- 缓存提示词模板

#### 4.4.2 批量处理
- 批量调用DeepSeek API
- 批量存储分析结果
- 合并相似的分析请求

#### 4.4.3 限流控制
- API调用频率限制
- 用户级别的分析频率控制
- 优先级队列处理

## 5. 数据流程图

```
游戏记录新增 → 触发实时分析
     ↓
数据收集服务 → 收集相关数据
     ↓
提示词生成 → 生成AI提示词
     ↓
DeepSeek API → 获取AI分析
     ↓
结果存储 → 存储到数据库
     ↓
前端展示 → 用户查看分析

回合结束 → 触发总结分析
     ↓
历史数据收集 → 收集完整数据
     ↓
批量AI分析 → 生成多维度分析
     ↓
结果整合 → 整合所有分析结果
     ↓
通知推送 → 推送给相关用户
```

## 6. 实施计划

### 6.1 第一阶段：基础设施
- 创建AI分析相关数据表
- 搭建AI服务基础架构
- 集成DeepSeek API
- 实现基础的提示词管理

### 6.2 第二阶段：核心功能
- 实现回合结束总结功能
- 实现实时局面分析功能
- 完善数据收集和处理逻辑

### 6.3 第三阶段：高级功能
- 实现个人历史总结功能
- 优化AI提示词和分析质量
- 添加个性化配置选项

### 6.4 第四阶段：优化完善
- 性能优化和缓存策略
- 用户体验优化
- 监控和日志完善

## 7. 风险评估与应对

### 7.1 技术风险
- **API调用失败**：实现重试机制和降级策略
- **响应时间过长**：异步处理和超时控制
- **成本控制**：设置调用频率限制和预算控制

### 7.2 业务风险
- **分析质量不佳**：持续优化提示词和反馈机制
- **用户接受度**：提供开关选项和个性化设置
- **数据隐私**：确保数据安全和用户隐私保护

## 8. 监控指标

### 8.1 技术指标
- AI API调用成功率
- 平均响应时间
- 分析结果生成速度
- 系统资源使用情况

### 8.2 业务指标
- 用户查看AI分析的频率
- 用户对AI分析的反馈评分
- AI分析功能的使用率
- 用户留存率变化

## 9. 总结

本设计方案充分利用现有数据库结构，通过新增必要的表和字段，实现了三个层次的DeepSeek AI智能点评功能。方案考虑了性能、可扩展性和用户体验，提供了完整的技术实现路径和风险控制措施。通过分阶段实施，可以逐步完善功能，为用户提供更加智能和个性化的游戏体验。