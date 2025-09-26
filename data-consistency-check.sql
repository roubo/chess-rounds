-- 用户头像和昵称数据一致性检查脚本
-- 用于定期检查circle_members表与users表的数据一致性

-- 1. 检查昵称不一致的记录
SELECT 
    '昵称不一致' as issue_type,
    cm.id as circle_member_id,
    cm.user_id,
    cm.circle_id,
    c.name as circle_name,
    cm.nickname as circle_nickname,
    u.nickname as user_nickname,
    cm.updated_at as member_updated_at,
    u.updated_at as user_updated_at
FROM circle_members cm
JOIN users u ON cm.user_id = u.id
JOIN circles c ON cm.circle_id = c.id
WHERE cm.status = 1 
  AND cm.nickname != u.nickname
ORDER BY cm.user_id, cm.circle_id;

-- 2. 检查头像不一致的记录
SELECT 
    '头像不一致' as issue_type,
    cm.id as circle_member_id,
    cm.user_id,
    cm.circle_id,
    c.name as circle_name,
    cm.avatar_url as circle_avatar,
    u.avatar_url as user_avatar,
    cm.updated_at as member_updated_at,
    u.updated_at as user_updated_at
FROM circle_members cm
JOIN users u ON cm.user_id = u.id
JOIN circles c ON cm.circle_id = c.id
WHERE cm.status = 1 
  AND cm.avatar_url != u.avatar_url
ORDER BY cm.user_id, cm.circle_id;

-- 3. 综合统计报告
SELECT 
    'nickname' as field_name,
    COUNT(*) as inconsistent_count,
    COUNT(DISTINCT cm.user_id) as affected_users,
    COUNT(DISTINCT cm.circle_id) as affected_circles
FROM circle_members cm
JOIN users u ON cm.user_id = u.id
WHERE cm.status = 1 AND cm.nickname != u.nickname

UNION ALL

SELECT 
    'avatar_url' as field_name,
    COUNT(*) as inconsistent_count,
    COUNT(DISTINCT cm.user_id) as affected_users,
    COUNT(DISTINCT cm.circle_id) as affected_circles
FROM circle_members cm
JOIN users u ON cm.user_id = u.id
WHERE cm.status = 1 AND cm.avatar_url != u.avatar_url

UNION ALL

SELECT 
    'total' as field_name,
    COUNT(*) as inconsistent_count,
    COUNT(DISTINCT cm.user_id) as affected_users,
    COUNT(DISTINCT cm.circle_id) as affected_circles
FROM circle_members cm
JOIN users u ON cm.user_id = u.id
WHERE cm.status = 1 
  AND (cm.nickname != u.nickname OR cm.avatar_url != u.avatar_url);

-- 4. 按圈子统计不一致数据
SELECT 
    c.id as circle_id,
    c.name as circle_name,
    COUNT(*) as inconsistent_members,
    COUNT(DISTINCT cm.user_id) as affected_users
FROM circle_members cm
JOIN users u ON cm.user_id = u.id
JOIN circles c ON cm.circle_id = c.id
WHERE cm.status = 1 
  AND (cm.nickname != u.nickname OR cm.avatar_url != u.avatar_url)
GROUP BY c.id, c.name
ORDER BY inconsistent_members DESC;

-- 5. 按用户统计不一致数据
SELECT 
    u.id as user_id,
    u.nickname as current_nickname,
    u.avatar_url as current_avatar,
    COUNT(*) as inconsistent_circles
FROM circle_members cm
JOIN users u ON cm.user_id = u.id
WHERE cm.status = 1 
  AND (cm.nickname != u.nickname OR cm.avatar_url != u.avatar_url)
GROUP BY u.id, u.nickname, u.avatar_url
ORDER BY inconsistent_circles DESC;

-- 6. 检查最近更新的用户是否有数据不一致
SELECT 
    u.id as user_id,
    u.nickname,
    u.avatar_url,
    u.updated_at as user_updated_at,
    COUNT(cm.id) as total_circles,
    COUNT(CASE WHEN cm.nickname != u.nickname OR cm.avatar_url != u.avatar_url THEN 1 END) as inconsistent_circles
FROM users u
LEFT JOIN circle_members cm ON u.id = cm.user_id AND cm.status = 1
WHERE u.updated_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)  -- 最近7天更新的用户
GROUP BY u.id, u.nickname, u.avatar_url, u.updated_at
HAVING inconsistent_circles > 0
ORDER BY u.updated_at DESC;

-- 7. 数据质量评分
SELECT 
    ROUND(
        (1 - (
            SELECT COUNT(*) 
            FROM circle_members cm
            JOIN users u ON cm.user_id = u.id
            WHERE cm.status = 1 
              AND (cm.nickname != u.nickname OR cm.avatar_url != u.avatar_url)
        ) / (
            SELECT COUNT(*) 
            FROM circle_members 
            WHERE status = 1
        )) * 100, 2
    ) as data_consistency_score_percent;

-- 使用说明：
-- 1. 定期执行此脚本检查数据一致性
-- 2. 如果发现不一致数据，可以使用data-sync-fix.sql进行修复
-- 3. 建议每日或每周执行一次检查
-- 4. 数据一致性评分应该保持在95%以上