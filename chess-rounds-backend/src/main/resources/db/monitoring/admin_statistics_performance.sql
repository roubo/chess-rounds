-- 管理员统计功能性能监控脚本
-- 用于监控统计查询的性能和索引使用情况

-- 1. 检查索引使用情况
SELECT 
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME,
    CARDINALITY,
    INDEX_TYPE
FROM information_schema.STATISTICS 
WHERE TABLE_SCHEMA = 'chess_rounds' 
    AND TABLE_NAME IN ('users', 'rounds', 'participants', 'records', 'ratings')
    AND INDEX_NAME LIKE 'idx_%'
ORDER BY TABLE_NAME, INDEX_NAME;

-- 2. 检查表大小和行数
SELECT 
    TABLE_NAME,
    TABLE_ROWS,
    ROUND(((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024), 2) AS 'Size_MB',
    ROUND((DATA_LENGTH / 1024 / 1024), 2) AS 'Data_MB',
    ROUND((INDEX_LENGTH / 1024 / 1024), 2) AS 'Index_MB'
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'chess_rounds'
    AND TABLE_NAME IN ('users', 'rounds', 'participants', 'records', 'ratings')
ORDER BY (DATA_LENGTH + INDEX_LENGTH) DESC;

-- 3. 分析统计查询的执行计划（示例查询）
-- 用户统计查询性能测试
EXPLAIN SELECT 
    COUNT(*) as total_users,
    COUNT(CASE WHEN status = 1 THEN 1 END) as active_users,
    COUNT(CASE WHEN last_login_at >= DATE_SUB(NOW(), INTERVAL 30 DAY) THEN 1 END) as recent_active_users
FROM users;

-- 回合统计查询性能测试
EXPLAIN SELECT 
    COUNT(*) as total_rounds,
    COUNT(CASE WHEN status = 'finished' THEN 1 END) as finished_rounds,
    SUM(CASE WHEN status = 'finished' THEN total_amount ELSE 0 END) as total_amount,
    AVG(CASE WHEN status = 'finished' THEN total_amount ELSE NULL END) as avg_amount
FROM rounds
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY);

-- 参与者统计查询性能测试
EXPLAIN SELECT 
    r.id,
    r.round_code,
    r.total_amount,
    COUNT(p.id) as participant_count,
    r.created_at
FROM rounds r
LEFT JOIN participants p ON r.id = p.round_id
WHERE r.status = 'finished'
    AND r.created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY r.id, r.round_code, r.total_amount, r.created_at
ORDER BY r.created_at DESC
LIMIT 20;

-- 4. 检查慢查询日志相关设置
SHOW VARIABLES LIKE 'slow_query_log%';
SHOW VARIABLES LIKE 'long_query_time';

-- 5. 查看当前连接和查询状态
SHOW STATUS LIKE 'Threads_connected';
SHOW STATUS LIKE 'Threads_running';
SHOW STATUS LIKE 'Slow_queries';
SHOW STATUS LIKE 'Questions';

-- 6. 检查索引效率
SELECT 
    s.TABLE_NAME,
    s.INDEX_NAME,
    s.COLUMN_NAME,
    s.CARDINALITY,
    t.TABLE_ROWS,
    ROUND((s.CARDINALITY / t.TABLE_ROWS) * 100, 2) AS 'Selectivity_%'
FROM information_schema.STATISTICS s
JOIN information_schema.TABLES t ON s.TABLE_SCHEMA = t.TABLE_SCHEMA AND s.TABLE_NAME = t.TABLE_NAME
WHERE s.TABLE_SCHEMA = 'chess_rounds'
    AND s.TABLE_NAME IN ('users', 'rounds', 'participants', 'records', 'ratings')
    AND s.INDEX_NAME LIKE 'idx_%'
    AND t.TABLE_ROWS > 0
ORDER BY s.TABLE_NAME, 'Selectivity_%' DESC;

-- 7. 检查未使用的索引（需要开启performance_schema）
-- SELECT 
--     object_schema,
--     object_name,
--     index_name
-- FROM performance_schema.table_io_waits_summary_by_index_usage 
-- WHERE object_schema = 'chess_rounds'
--     AND index_name IS NOT NULL
--     AND count_star = 0
-- ORDER BY object_name, index_name;

-- 8. 统计查询性能基准测试
-- 测试用户概览查询
SET @start_time = NOW(6);
SELECT 
    COUNT(*) as total_users,
    COUNT(CASE WHEN status = 1 THEN 1 END) as active_users,
    COUNT(CASE WHEN last_login_at >= DATE_SUB(NOW(), INTERVAL 30 DAY) THEN 1 END) as recent_active,
    COUNT(CASE WHEN created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) THEN 1 END) as new_users
FROM users;
SET @end_time = NOW(6);
SELECT TIMESTAMPDIFF(MICROSECOND, @start_time, @end_time) as 'User_Query_Microseconds';

-- 测试回合概览查询
SET @start_time = NOW(6);
SELECT 
    COUNT(*) as total_rounds,
    COUNT(CASE WHEN status = 'finished' THEN 1 END) as finished_rounds,
    SUM(CASE WHEN status = 'finished' THEN total_amount ELSE 0 END) as total_amount,
    COUNT(CASE WHEN created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) THEN 1 END) as recent_rounds
FROM rounds;
SET @end_time = NOW(6);
SELECT TIMESTAMPDIFF(MICROSECOND, @start_time, @end_time) as 'Round_Query_Microseconds';

-- 9. 建议的性能优化检查清单
/*
性能优化检查清单：

1. 索引使用率检查
   - 确保新创建的复合索引被正确使用
   - 检查是否有重复或冗余的索引

2. 查询性能基准
   - 用户统计查询应在 < 100ms 内完成
   - 回合统计查询应在 < 200ms 内完成
   - 复杂统计查询应在 < 500ms 内完成

3. 数据增长监控
   - 定期检查表大小增长趋势
   - 监控索引大小与数据大小的比例

4. 慢查询监控
   - 设置 long_query_time = 1 秒
   - 定期分析慢查询日志

5. 连接池监控
   - 监控数据库连接数
   - 确保连接池配置合理
*/