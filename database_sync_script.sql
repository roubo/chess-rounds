-- 数据库同步脚本
-- 确保数据库表结构与后端代码实体类完全匹配
-- 执行此脚本后可以完全不使用Flyway

-- 1. 删除flyway_schema_history表（如果存在）
DROP TABLE IF EXISTS flyway_schema_history;

-- 2. 确保所有表结构正确
-- 由于当前表结构已经通过Hibernate自动创建且与实体类匹配，
-- 这里主要是添加一些可能缺失的索引和约束

-- 3. 添加缺失的索引（如果不存在）
-- users表索引
CREATE INDEX IF NOT EXISTS idx_users_openid ON users(openid);
CREATE INDEX IF NOT EXISTS idx_users_unionid ON users(unionid);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);

-- rounds表索引
CREATE INDEX IF NOT EXISTS idx_rounds_round_code ON rounds(round_code);
CREATE INDEX IF NOT EXISTS idx_rounds_creator_id ON rounds(creator_id);
CREATE INDEX IF NOT EXISTS idx_rounds_status ON rounds(status);
CREATE INDEX IF NOT EXISTS idx_rounds_created_at ON rounds(created_at);

-- participants表索引
CREATE INDEX IF NOT EXISTS idx_participants_round_id ON participants(round_id);
CREATE INDEX IF NOT EXISTS idx_participants_user_id ON participants(user_id);
CREATE INDEX IF NOT EXISTS idx_participants_role ON participants(role);
CREATE INDEX IF NOT EXISTS idx_participants_joined_at ON participants(joined_at);

-- records表索引
CREATE INDEX IF NOT EXISTS idx_records_round_id ON records(round_id);
CREATE INDEX IF NOT EXISTS idx_records_recorder_id ON records(recorder_id);
CREATE INDEX IF NOT EXISTS idx_records_record_type ON records(record_type);
CREATE INDEX IF NOT EXISTS idx_records_created_at ON records(created_at);
CREATE INDEX IF NOT EXISTS idx_records_sequence ON records(round_id, sequence_number);

-- system_configs表索引
CREATE INDEX IF NOT EXISTS idx_system_configs_config_key ON system_configs(config_key);
CREATE INDEX IF NOT EXISTS idx_system_configs_is_active ON system_configs(is_active);

-- user_statistics表索引
CREATE INDEX IF NOT EXISTS idx_user_statistics_user_id ON user_statistics(user_id);
CREATE INDEX IF NOT EXISTS idx_user_statistics_stat_date ON user_statistics(stat_date);
CREATE INDEX IF NOT EXISTS idx_user_statistics_stat_type ON user_statistics(stat_type);

-- user_relationships表索引
CREATE INDEX IF NOT EXISTS idx_user_relationships_user_id ON user_relationships(user_id);
CREATE INDEX IF NOT EXISTS idx_user_relationships_target_user_id ON user_relationships(target_user_id);
CREATE INDEX IF NOT EXISTS idx_user_relationships_relationship_type ON user_relationships(relationship_type);

-- ratings表索引
CREATE INDEX IF NOT EXISTS idx_ratings_round_id ON ratings(round_id);
CREATE INDEX IF NOT EXISTS idx_ratings_from_user_id ON ratings(from_user_id);
CREATE INDEX IF NOT EXISTS idx_ratings_to_user_id ON ratings(to_user_id);
CREATE INDEX IF NOT EXISTS idx_ratings_rating_type ON ratings(rating_type);

-- 4. 插入基础系统配置数据
INSERT IGNORE INTO system_configs (config_key, config_value, config_type, description, is_active, created_at) VALUES
('app.version', '1.0.0', 'STRING', '应用版本号', 1, NOW()),
('app.name', '象棋回合管理系统', 'STRING', '应用名称', 1, NOW()),
('round.max_participants', '8', 'NUMBER', '回合最大参与人数', 1, NOW()),
('round.default_multiplier', '1.0', 'NUMBER', '默认倍率', 1, NOW()),
('system.maintenance', 'false', 'BOOLEAN', '系统维护模式', 1, NOW()),
('wechat.app_id', '', 'STRING', '微信小程序AppID', 1, NOW()),
('wechat.app_secret', '', 'STRING', '微信小程序AppSecret', 0, NOW()),
('ai.enabled', 'true', 'BOOLEAN', '是否启用AI功能', 1, NOW()),
('notification.enabled', 'true', 'BOOLEAN', '是否启用通知功能', 1, NOW()),
('statistics.auto_update', 'true', 'BOOLEAN', '是否自动更新统计数据', 1, NOW());

-- 5. 验证表结构
SELECT 
    'users' as table_name, 
    COUNT(*) as column_count 
FROM information_schema.columns 
WHERE table_schema = DATABASE() AND table_name = 'users'
UNION ALL
SELECT 
    'rounds' as table_name, 
    COUNT(*) as column_count 
FROM information_schema.columns 
WHERE table_schema = DATABASE() AND table_name = 'rounds'
UNION ALL
SELECT 
    'participants' as table_name, 
    COUNT(*) as column_count 
FROM information_schema.columns 
WHERE table_schema = DATABASE() AND table_name = 'participants'
UNION ALL
SELECT 
    'records' as table_name, 
    COUNT(*) as column_count 
FROM information_schema.columns 
WHERE table_schema = DATABASE() AND table_name = 'records'
UNION ALL
SELECT 
    'system_configs' as table_name, 
    COUNT(*) as column_count 
FROM information_schema.columns 
WHERE table_schema = DATABASE() AND table_name = 'system_configs'
UNION ALL
SELECT 
    'user_statistics' as table_name, 
    COUNT(*) as column_count 
FROM information_schema.columns 
WHERE table_schema = DATABASE() AND table_name = 'user_statistics'
UNION ALL
SELECT 
    'user_relationships' as table_name, 
    COUNT(*) as column_count 
FROM information_schema.columns 
WHERE table_schema = DATABASE() AND table_name = 'user_relationships'
UNION ALL
SELECT 
    'ratings' as table_name, 
    COUNT(*) as column_count 
FROM information_schema.columns 
WHERE table_schema = DATABASE() AND table_name = 'ratings';

-- 脚本执行完成提示
SELECT '数据库同步脚本执行完成！' as message;
SELECT '现在可以完全不使用Flyway，直接使用Hibernate的DDL自动管理功能。' as note;
SELECT '建议在application-dev.yml中保持spring.jpa.hibernate.ddl-auto=create-drop用于开发环境。' as recommendation;
SELECT '在生产环境中建议使用ddl-auto=validate来验证表结构一致性。' as production_note;