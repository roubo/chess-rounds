-- 数据库同步脚本
-- 确保数据库与后端代码匹配，添加基础配置数据

-- 插入基础系统配置数据
INSERT INTO system_configs (config_key, config_value, config_type, is_active, description, created_at, updated_at) VALUES
('app.version', '1.0.0', 'STRING', 1, '应用版本号', NOW(), NOW()),
('app.name', 'Chess Rounds Backend', 'STRING', 1, '应用名称', NOW(), NOW()),
('game.default_multiplier', '1.0', 'NUMBER', 1, '默认倍率', NOW(), NOW()),
('game.max_participants', '8', 'NUMBER', 1, '最大参与人数', NOW(), NOW()),
('game.min_participants', '2', 'NUMBER', 1, '最小参与人数', NOW(), NOW()),
('notification.enabled', 'true', 'BOOLEAN', 1, '通知功能开关', NOW(), NOW()),
('ai.analysis_enabled', 'true', 'BOOLEAN', 1, 'AI分析功能开关', NOW(), NOW()),
('security.session_timeout', '3600', 'NUMBER', 1, '会话超时时间（秒）', NOW(), NOW())
ON DUPLICATE KEY UPDATE 
    config_value = VALUES(config_value),
    updated_at = NOW();

-- 验证配置数据
SELECT config_key, config_value, config_type, is_active FROM system_configs ORDER BY config_key;

-- 使用建议：
-- 1. 当前数据库结构已与代码完全匹配
-- 2. 可以在 application.yml 中设置相关配置
-- 3. 使用 Hibernate 的 ddl-auto: validate 验证结构