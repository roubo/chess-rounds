-- 修复circle_leaderboards表中win_rate字段的数据类型
-- 从DECIMAL(5,4)改为DECIMAL(5,2)以支持百分比格式

USE chess_rounds;

-- 修改win_rate字段的数据类型
ALTER TABLE circle_leaderboards 
MODIFY COLUMN win_rate DECIMAL(5,2) DEFAULT 0.00 COMMENT '胜率（百分比格式，如72.00表示72%）';

-- 验证修改结果
DESCRIBE circle_leaderboards;