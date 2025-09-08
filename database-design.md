# 数据库设计文档

## 数据库概述

- **数据库名称**: chess_rounds
- **字符集**: utf8mb4
- **排序规则**: utf8mb4_unicode_ci
- **存储引擎**: InnoDB

## 表结构设计

### 1. 用户表 (users)

存储微信小程序用户信息

```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    openid VARCHAR(64) NOT NULL UNIQUE COMMENT '微信OpenID',
    unionid VARCHAR(64) COMMENT '微信UnionID',
    nickname VARCHAR(100) NOT NULL COMMENT '用户昵称',
    avatar_url VARCHAR(500) COMMENT '头像URL',
    gender TINYINT DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
    country VARCHAR(50) COMMENT '国家',
    province VARCHAR(50) COMMENT '省份',
    city VARCHAR(50) COMMENT '城市',
    language VARCHAR(20) DEFAULT 'zh_CN' COMMENT '语言',
    session_key VARCHAR(64) COMMENT '会话密钥',
    last_login_at TIMESTAMP COMMENT '最后登录时间',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_openid (openid),
    INDEX idx_unionid (unionid),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
```

### 2. 回合表 (rounds)

存储游戏回合信息

```sql
CREATE TABLE rounds (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '回合ID',
    round_code VARCHAR(32) NOT NULL UNIQUE COMMENT '回合码',
    creator_id BIGINT NOT NULL COMMENT '创建者用户ID',
    title VARCHAR(200) COMMENT '回合标题',
    multiplier DECIMAL(10,2) DEFAULT 1.00 COMMENT '倍率',
    has_table BOOLEAN DEFAULT FALSE COMMENT '是否有台板',
    table_user_id BIGINT COMMENT '台板用户ID',
    max_participants INT DEFAULT 4 COMMENT '最大参与人数',
    status ENUM('waiting', 'playing', 'finished') DEFAULT 'waiting' COMMENT '状态：waiting-等待中，playing-进行中，finished-已结束',
    start_time TIMESTAMP COMMENT '开始时间',
    end_time TIMESTAMP COMMENT '结束时间',
    total_amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '总金额',
    round_count INT DEFAULT 0 COMMENT '局数',
    ai_analysis TEXT COMMENT 'AI分析结果',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (table_user_id) REFERENCES users(id) ON DELETE SET NULL,
    
    INDEX idx_round_code (round_code),
    INDEX idx_creator_id (creator_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='回合表';
```

### 3. 参与者表 (participants)

存储回合参与者信息

```sql
CREATE TABLE participants (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '参与者ID',
    round_id BIGINT NOT NULL COMMENT '回合ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role ENUM('player', 'spectator', 'table') DEFAULT 'player' COMMENT '角色：player-参与者，spectator-旁观者，table-台板',
    nickname VARCHAR(100) COMMENT '参与时的昵称',
    avatar_url VARCHAR(500) COMMENT '参与时的头像',
    total_score DECIMAL(15,2) DEFAULT 0.00 COMMENT '总得分',
    win_count INT DEFAULT 0 COMMENT '胜利次数',
    lose_count INT DEFAULT 0 COMMENT '失败次数',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    left_at TIMESTAMP COMMENT '离开时间',
    
    FOREIGN KEY (round_id) REFERENCES rounds(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    UNIQUE KEY uk_round_user (round_id, user_id),
    INDEX idx_round_id (round_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='参与者表';
```

### 4. 记录表 (records)

存储游戏记录信息

```sql
CREATE TABLE records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    round_id BIGINT NOT NULL COMMENT '回合ID',
    recorder_id BIGINT NOT NULL COMMENT '记录者用户ID',
    record_type ENUM('win', 'lose', 'draw', 'special') DEFAULT 'win' COMMENT '记录类型：win-胜利，lose-失败，draw-平局，special-特殊',
    amount DECIMAL(15,2) NOT NULL COMMENT '金额',
    description VARCHAR(500) COMMENT '描述',
    participants JSON COMMENT '参与此记录的用户ID列表',
    sequence_number INT NOT NULL COMMENT '序号',
    ai_comment TEXT COMMENT 'AI评论',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    FOREIGN KEY (round_id) REFERENCES rounds(id) ON DELETE CASCADE,
    FOREIGN KEY (recorder_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_round_id (round_id),
    INDEX idx_recorder_id (recorder_id),
    INDEX idx_record_type (record_type),
    INDEX idx_created_at (created_at),
    INDEX idx_sequence (round_id, sequence_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='记录表';
```

### 5. 评价表 (ratings)

存储旁观者对参与者的评价

```sql
CREATE TABLE ratings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评价ID',
    round_id BIGINT NOT NULL COMMENT '回合ID',
    from_user_id BIGINT NOT NULL COMMENT '评价者用户ID',
    to_user_id BIGINT NOT NULL COMMENT '被评价者用户ID',
    rating_type ENUM('like', 'dislike') NOT NULL COMMENT '评价类型：like-赞，dislike-贬',
    comment VARCHAR(200) COMMENT '评价备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    FOREIGN KEY (round_id) REFERENCES rounds(id) ON DELETE CASCADE,
    FOREIGN KEY (from_user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (to_user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    UNIQUE KEY uk_rating (round_id, from_user_id, to_user_id),
    INDEX idx_round_id (round_id),
    INDEX idx_from_user (from_user_id),
    INDEX idx_to_user (to_user_id),
    INDEX idx_rating_type (rating_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';
```

### 6. 用户关系表 (user_relationships)

存储用户之间的关系统计

```sql
CREATE TABLE user_relationships (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关系ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    target_user_id BIGINT NOT NULL COMMENT '目标用户ID',
    relationship_type ENUM('partner', 'opponent') NOT NULL COMMENT '关系类型：partner-合伙人，opponent-对手',
    total_rounds INT DEFAULT 0 COMMENT '总回合数',
    win_rounds INT DEFAULT 0 COMMENT '胜利回合数',
    total_amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '总金额',
    win_amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '胜利金额',
    last_play_time TIMESTAMP COMMENT '最后游戏时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (target_user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    UNIQUE KEY uk_user_relationship (user_id, target_user_id, relationship_type),
    INDEX idx_user_id (user_id),
    INDEX idx_target_user (target_user_id),
    INDEX idx_relationship_type (relationship_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户关系表';
```

### 7. 用户统计表 (user_statistics)

存储用户统计数据

```sql
CREATE TABLE user_statistics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '统计ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    stat_date DATE NOT NULL COMMENT '统计日期',
    stat_type ENUM('daily', 'monthly', 'yearly') NOT NULL COMMENT '统计类型',
    total_rounds INT DEFAULT 0 COMMENT '总回合数',
    win_rounds INT DEFAULT 0 COMMENT '胜利回合数',
    total_amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '总金额',
    win_amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '胜利金额',
    max_single_win DECIMAL(15,2) DEFAULT 0.00 COMMENT '单次最大胜利',
    max_single_lose DECIMAL(15,2) DEFAULT 0.00 COMMENT '单次最大失败',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    UNIQUE KEY uk_user_stat (user_id, stat_date, stat_type),
    INDEX idx_user_id (user_id),
    INDEX idx_stat_date (stat_date),
    INDEX idx_stat_type (stat_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户统计表';
```

### 8. 系统配置表 (system_configs)

存储系统配置信息

```sql
CREATE TABLE system_configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_type ENUM('string', 'number', 'boolean', 'json') DEFAULT 'string' COMMENT '配置类型',
    description VARCHAR(500) COMMENT '配置描述',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_config_key (config_key),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';
```

## Flyway迁移脚本

### V1__Create_users_table.sql

```sql
-- 创建用户表
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    openid VARCHAR(64) NOT NULL UNIQUE COMMENT '微信OpenID',
    unionid VARCHAR(64) COMMENT '微信UnionID',
    nickname VARCHAR(100) NOT NULL COMMENT '用户昵称',
    avatar_url VARCHAR(500) COMMENT '头像URL',
    gender TINYINT DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
    country VARCHAR(50) COMMENT '国家',
    province VARCHAR(50) COMMENT '省份',
    city VARCHAR(50) COMMENT '城市',
    language VARCHAR(20) DEFAULT 'zh_CN' COMMENT '语言',
    session_key VARCHAR(64) COMMENT '会话密钥',
    last_login_at TIMESTAMP COMMENT '最后登录时间',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_openid (openid),
    INDEX idx_unionid (unionid),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
```

### V2__Create_rounds_table.sql

```sql
-- 创建回合表
CREATE TABLE rounds (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '回合ID',
    round_code VARCHAR(32) NOT NULL UNIQUE COMMENT '回合码',
    creator_id BIGINT NOT NULL COMMENT '创建者用户ID',
    title VARCHAR(200) COMMENT '回合标题',
    multiplier DECIMAL(10,2) DEFAULT 1.00 COMMENT '倍率',
    has_table BOOLEAN DEFAULT FALSE COMMENT '是否有台板',
    table_user_id BIGINT COMMENT '台板用户ID',
    max_participants INT DEFAULT 4 COMMENT '最大参与人数',
    status ENUM('waiting', 'playing', 'finished') DEFAULT 'waiting' COMMENT '状态：waiting-等待中，playing-进行中，finished-已结束',
    start_time TIMESTAMP COMMENT '开始时间',
    end_time TIMESTAMP COMMENT '结束时间',
    total_amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '总金额',
    round_count INT DEFAULT 0 COMMENT '局数',
    ai_analysis TEXT COMMENT 'AI分析结果',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (table_user_id) REFERENCES users(id) ON DELETE SET NULL,
    
    INDEX idx_round_code (round_code),
    INDEX idx_creator_id (creator_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='回合表';
```

### V3__Create_participants_table.sql

```sql
-- 创建参与者表
CREATE TABLE participants (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '参与者ID',
    round_id BIGINT NOT NULL COMMENT '回合ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role ENUM('player', 'spectator', 'table') DEFAULT 'player' COMMENT '角色：player-参与者，spectator-旁观者，table-台板',
    nickname VARCHAR(100) COMMENT '参与时的昵称',
    avatar_url VARCHAR(500) COMMENT '参与时的头像',
    total_score DECIMAL(15,2) DEFAULT 0.00 COMMENT '总得分',
    win_count INT DEFAULT 0 COMMENT '胜利次数',
    lose_count INT DEFAULT 0 COMMENT '失败次数',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    left_at TIMESTAMP COMMENT '离开时间',
    
    FOREIGN KEY (round_id) REFERENCES rounds(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    UNIQUE KEY uk_round_user (round_id, user_id),
    INDEX idx_round_id (round_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='参与者表';
```

### V4__Create_records_table.sql

```sql
-- 创建记录表
CREATE TABLE records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    round_id BIGINT NOT NULL COMMENT '回合ID',
    recorder_id BIGINT NOT NULL COMMENT '记录者用户ID',
    record_type ENUM('win', 'lose', 'draw', 'special') DEFAULT 'win' COMMENT '记录类型：win-胜利，lose-失败，draw-平局，special-特殊',
    amount DECIMAL(15,2) NOT NULL COMMENT '金额',
    description VARCHAR(500) COMMENT '描述',
    participants JSON COMMENT '参与此记录的用户ID列表',
    sequence_number INT NOT NULL COMMENT '序号',
    ai_comment TEXT COMMENT 'AI评论',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    FOREIGN KEY (round_id) REFERENCES rounds(id) ON DELETE CASCADE,
    FOREIGN KEY (recorder_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_round_id (round_id),
    INDEX idx_recorder_id (recorder_id),
    INDEX idx_record_type (record_type),
    INDEX idx_created_at (created_at),
    INDEX idx_sequence (round_id, sequence_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='记录表';
```

### V5__Create_ratings_table.sql

```sql
-- 创建评价表
CREATE TABLE ratings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评价ID',
    round_id BIGINT NOT NULL COMMENT '回合ID',
    from_user_id BIGINT NOT NULL COMMENT '评价者用户ID',
    to_user_id BIGINT NOT NULL COMMENT '被评价者用户ID',
    rating_type ENUM('like', 'dislike') NOT NULL COMMENT '评价类型：like-赞，dislike-贬',
    comment VARCHAR(200) COMMENT '评价备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    FOREIGN KEY (round_id) REFERENCES rounds(id) ON DELETE CASCADE,
    FOREIGN KEY (from_user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (to_user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    UNIQUE KEY uk_rating (round_id, from_user_id, to_user_id),
    INDEX idx_round_id (round_id),
    INDEX idx_from_user (from_user_id),
    INDEX idx_to_user (to_user_id),
    INDEX idx_rating_type (rating_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';
```

### V6__Create_user_relationships_table.sql

```sql
-- 创建用户关系表
CREATE TABLE user_relationships (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关系ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    target_user_id BIGINT NOT NULL COMMENT '目标用户ID',
    relationship_type ENUM('partner', 'opponent') NOT NULL COMMENT '关系类型：partner-合伙人，opponent-对手',
    total_rounds INT DEFAULT 0 COMMENT '总回合数',
    win_rounds INT DEFAULT 0 COMMENT '胜利回合数',
    total_amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '总金额',
    win_amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '胜利金额',
    last_play_time TIMESTAMP COMMENT '最后游戏时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (target_user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    UNIQUE KEY uk_user_relationship (user_id, target_user_id, relationship_type),
    INDEX idx_user_id (user_id),
    INDEX idx_target_user (target_user_id),
    INDEX idx_relationship_type (relationship_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户关系表';
```

### V7__Create_user_statistics_table.sql

```sql
-- 创建用户统计表
CREATE TABLE user_statistics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '统计ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    stat_date DATE NOT NULL COMMENT '统计日期',
    stat_type ENUM('daily', 'monthly', 'yearly') NOT NULL COMMENT '统计类型',
    total_rounds INT DEFAULT 0 COMMENT '总回合数',
    win_rounds INT DEFAULT 0 COMMENT '胜利回合数',
    total_amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '总金额',
    win_amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '胜利金额',
    max_single_win DECIMAL(15,2) DEFAULT 0.00 COMMENT '单次最大胜利',
    max_single_lose DECIMAL(15,2) DEFAULT 0.00 COMMENT '单次最大失败',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    UNIQUE KEY uk_user_stat (user_id, stat_date, stat_type),
    INDEX idx_user_id (user_id),
    INDEX idx_stat_date (stat_date),
    INDEX idx_stat_type (stat_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户统计表';
```

### V8__Create_system_configs_table.sql

```sql
-- 创建系统配置表
CREATE TABLE system_configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_type ENUM('string', 'number', 'boolean', 'json') DEFAULT 'string' COMMENT '配置类型',
    description VARCHAR(500) COMMENT '配置描述',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_config_key (config_key),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 插入默认配置
INSERT INTO system_configs (config_key, config_value, config_type, description) VALUES
('max_participants_per_round', '4', 'number', '每回合最大参与人数'),
('default_multiplier', '1.00', 'number', '默认倍率'),
('ai_analysis_enabled', 'true', 'boolean', '是否启用AI分析'),
('round_code_length', '8', 'number', '回合码长度'),
('spectator_rating_enabled', 'true', 'boolean', '是否启用旁观者评价功能');
```

## 数据库索引优化

### 复合索引建议

```sql
-- 用户查询优化
CREATE INDEX idx_users_status_login ON users(status, last_login_at);

-- 回合查询优化
CREATE INDEX idx_rounds_creator_status ON rounds(creator_id, status);
CREATE INDEX idx_rounds_status_created ON rounds(status, created_at);

-- 参与者查询优化
CREATE INDEX idx_participants_user_role ON participants(user_id, role);

-- 记录查询优化
CREATE INDEX idx_records_round_sequence ON records(round_id, sequence_number);
CREATE INDEX idx_records_recorder_created ON records(recorder_id, created_at);

-- 统计查询优化
CREATE INDEX idx_statistics_user_type_date ON user_statistics(user_id, stat_type, stat_date);
```

## 数据库性能优化建议

### 1. 分区策略

```sql
-- 对记录表按月分区（可选）
ALTER TABLE records PARTITION BY RANGE (YEAR(created_at) * 100 + MONTH(created_at)) (
    PARTITION p202401 VALUES LESS THAN (202402),
    PARTITION p202402 VALUES LESS THAN (202403),
    -- ... 更多分区
    PARTITION p_future VALUES LESS THAN MAXVALUE
);
```

### 2. 查询优化

- 使用适当的索引
- 避免SELECT *
- 使用LIMIT限制结果集
- 合理使用JOIN
- 定期分析慢查询日志

### 3. 数据归档策略

- 定期归档历史数据
- 保留最近6个月的热数据
- 历史数据可移至归档表

## 数据备份策略

### 1. 全量备份

```bash
# 每日全量备份
mysqldump -u chess_app -p chess_rounds > backup_$(date +%Y%m%d).sql
```

### 2. 增量备份

```bash
# 启用二进制日志
# 在my.cnf中添加：
# log-bin=mysql-bin
# binlog-format=ROW
```

### 3. 数据恢复

```bash
# 恢复全量备份
mysql -u chess_app -p chess_rounds < backup_20240101.sql

# 恢复增量备份
mysqlbinlog mysql-bin.000001 | mysql -u chess_app -p chess_rounds
```

## 数据库监控

### 关键指标监控

- 连接数
- 查询响应时间
- 慢查询数量
- 锁等待时间
- 磁盘使用率
- 内存使用率

### 监控SQL

```sql
-- 查看当前连接数
SHOW STATUS LIKE 'Threads_connected';

-- 查看慢查询
SHOW STATUS LIKE 'Slow_queries';

-- 查看表大小
SELECT 
    table_name,
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size (MB)'
FROM information_schema.tables 
WHERE table_schema = 'chess_rounds'
ORDER BY (data_length + index_length) DESC;
```

这个数据库设计提供了：
- 完整的业务数据模型
- 合理的表结构和索引
- 数据完整性约束
- 版本控制和迁移
- 性能优化建议
- 备份和监控策略

所有表都支持业务需求，并考虑了扩展性和性能优化。