-- 用户头像和昵称数据同步修复脚本
-- 用于修复circle_members表中的数据不一致问题

-- 1. 数据一致性检查
-- 查找circle_members表中与users表不一致的记录
SELECT 
    cm.id as circle_member_id,
    cm.user_id,
    cm.circle_id,
    cm.nickname as circle_nickname,
    u.nickname as user_nickname,
    cm.avatar_url as circle_avatar,
    u.avatar_url as user_avatar,
    cm.created_at as member_created_at,
    u.updated_at as user_updated_at
FROM circle_members cm
JOIN users u ON cm.user_id = u.id
WHERE cm.status = 1 
  AND (cm.nickname != u.nickname OR cm.avatar_url != u.avatar_url)
ORDER BY cm.user_id, cm.circle_id;

-- 2. 统计不一致数据数量
SELECT 
    COUNT(*) as inconsistent_count,
    COUNT(DISTINCT cm.user_id) as affected_users,
    COUNT(DISTINCT cm.circle_id) as affected_circles
FROM circle_members cm
JOIN users u ON cm.user_id = u.id
WHERE cm.status = 1 
  AND (cm.nickname != u.nickname OR cm.avatar_url != u.avatar_url);

-- 3. 备份现有数据（可选）
-- CREATE TABLE circle_members_backup_20240125 AS 
-- SELECT * FROM circle_members WHERE status = 1;

-- 4. 执行数据修复
-- 更新circle_members表中的昵称和头像，使其与users表保持一致
UPDATE circle_members cm
JOIN users u ON cm.user_id = u.id
SET 
    cm.nickname = u.nickname,
    cm.avatar_url = u.avatar_url,
    cm.updated_at = NOW()
WHERE cm.status = 1 
  AND (cm.nickname != u.nickname OR cm.avatar_url != u.avatar_url);

-- 5. 验证修复结果
-- 再次检查是否还有不一致的数据
SELECT 
    COUNT(*) as remaining_inconsistent_count
FROM circle_members cm
JOIN users u ON cm.user_id = u.id
WHERE cm.status = 1 
  AND (cm.nickname != u.nickname OR cm.avatar_url != u.avatar_url);

-- 6. 查看修复后的数据样本
SELECT 
    cm.id as circle_member_id,
    cm.user_id,
    cm.circle_id,
    cm.nickname,
    cm.avatar_url,
    cm.updated_at
FROM circle_members cm
WHERE cm.status = 1
ORDER BY cm.updated_at DESC
LIMIT 10;

-- 注意事项：
-- 1. 执行前请先备份数据
-- 2. 建议在测试环境先执行验证
-- 3. 生产环境执行时建议在低峰期进行
-- 4. 执行后需要验证应用功能是否正常