-- 圈子排行榜功能数据库创建脚本
-- 执行前请确保已连接到 chess_rounds 数据库

USE chess_rounds;

-- 1. 创建圈子表
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

-- 2. 创建圈子成员表
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

-- 3. 创建圈子排行榜缓存表
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

-- 4. 创建触发器：当圈子成员加入时，自动增加圈子成员数量
DELIMITER $$
CREATE TRIGGER tr_circle_member_insert 
AFTER INSERT ON circle_members 
FOR EACH ROW 
BEGIN
    UPDATE circles 
    SET member_count = member_count + 1,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = NEW.circle_id;
END$$
DELIMITER ;

-- 5. 创建触发器：当圈子成员离开时，自动减少圈子成员数量
DELIMITER $$
CREATE TRIGGER tr_circle_member_delete 
AFTER DELETE ON circle_members 
FOR EACH ROW 
BEGIN
    UPDATE circles 
    SET member_count = member_count - 1,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = OLD.circle_id;
END$$
DELIMITER ;

-- 6. 创建存储过程：生成唯一的5位加入码
DELIMITER $$
CREATE PROCEDURE GenerateJoinCode(OUT join_code VARCHAR(5))
BEGIN
    DECLARE code_exists INT DEFAULT 1;
    DECLARE chars VARCHAR(32) DEFAULT 'ABCDEFGHJKMNPQRSTUVWXYZ23456789'; -- 排除易混淆字符
    DECLARE code_length INT DEFAULT 5;
    DECLARE i INT DEFAULT 1;
    DECLARE temp_code VARCHAR(5) DEFAULT '';
    
    WHILE code_exists = 1 DO
        SET temp_code = '';
        SET i = 1;
        
        -- 生成5位随机码
        WHILE i <= code_length DO
            SET temp_code = CONCAT(temp_code, SUBSTRING(chars, FLOOR(1 + RAND() * LENGTH(chars)), 1));
            SET i = i + 1;
        END WHILE;
        
        -- 检查是否已存在
        SELECT COUNT(*) INTO code_exists FROM circles WHERE join_code = temp_code;
    END WHILE;
    
    SET join_code = temp_code;
END$$
DELIMITER ;

-- 7. 创建存储过程：更新圈子排行榜缓存
DELIMITER $$
CREATE PROCEDURE UpdateCircleLeaderboard(IN circle_id_param BIGINT)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE user_id_var BIGINT;
    DECLARE total_amount_var DECIMAL(15,2);
    DECLARE total_rounds_var INT;
    DECLARE win_rounds_var INT;
    DECLARE win_rate_var DECIMAL(5,2);
    DECLARE last_play_time_var TIMESTAMP;
    DECLARE rank_var INT DEFAULT 1;
    
    -- 声明游标
    DECLARE leaderboard_cursor CURSOR FOR
        SELECT 
            cm.user_id,
            COALESCE(SUM(p.total_score * r.multiplier), 0) as total_amount,
            COUNT(DISTINCT p.round_id) as total_rounds,
            SUM(CASE WHEN p.total_score > 0 THEN 1 ELSE 0 END) as win_rounds,
            CASE 
                WHEN COUNT(DISTINCT p.round_id) > 0 
                THEN ROUND((SUM(CASE WHEN p.total_score > 0 THEN 1 ELSE 0 END) * 100.0 / COUNT(DISTINCT p.round_id)), 2)
                ELSE 0 
            END as win_rate,
            MAX(r.updated_at) as last_play_time
        FROM circle_members cm
        LEFT JOIN participants p ON cm.user_id = p.user_id
        LEFT JOIN rounds r ON p.round_id = r.id AND r.status = 'finished'
        WHERE cm.circle_id = circle_id_param AND cm.is_active = TRUE
        GROUP BY cm.user_id
        ORDER BY total_amount DESC;
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    -- 清空该圈子的缓存数据
    DELETE FROM circle_leaderboard_cache WHERE circle_id = circle_id_param;
    
    -- 打开游标
    OPEN leaderboard_cursor;
    
    read_loop: LOOP
        FETCH leaderboard_cursor INTO user_id_var, total_amount_var, total_rounds_var, win_rounds_var, win_rate_var, last_play_time_var;
        
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- 插入缓存数据
        INSERT INTO circle_leaderboard_cache (
            circle_id, user_id, total_amount, total_rounds, win_rounds, 
            win_rate, last_play_time, rank_position
        ) VALUES (
            circle_id_param, user_id_var, total_amount_var, total_rounds_var, 
            win_rounds_var, win_rate_var, last_play_time_var, rank_var
        );
        
        SET rank_var = rank_var + 1;
    END LOOP;
    
    CLOSE leaderboard_cursor;
END$$
DELIMITER ;

-- 8. 创建视图：圈子排行榜视图（便于查询）
CREATE VIEW v_circle_leaderboard AS
SELECT 
    clc.circle_id,
    clc.user_id,
    u.nickname,
    u.avatar_url,
    clc.total_amount,
    clc.total_rounds,
    clc.win_rounds,
    clc.win_rate,
    clc.last_play_time,
    clc.rank_position,
    c.name as circle_name,
    cm.role as member_role,
    cm.joined_at
FROM circle_leaderboard_cache clc
JOIN users u ON clc.user_id = u.id
JOIN circles c ON clc.circle_id = c.id
JOIN circle_members cm ON clc.circle_id = cm.circle_id AND clc.user_id = cm.user_id
WHERE c.is_active = TRUE AND cm.is_active = TRUE
ORDER BY clc.circle_id, clc.rank_position;

-- 9. 插入测试数据（可选）
-- 注意：这里需要确保users表中已有对应的用户数据

-- 示例：创建测试圈子（需要根据实际用户ID调整）
/*
INSERT INTO circles (name, description, join_code, creator_id) VALUES 
('测试圈子1', '这是一个测试圈子', 'TEST1', 1),
('朋友圈子', '朋友们的游戏圈子', 'FRND2', 2);

-- 示例：添加圈子成员
INSERT INTO circle_members (circle_id, user_id, role) VALUES 
(1, 1, 'creator'),
(1, 2, 'member'),
(1, 3, 'member'),
(2, 2, 'creator'),
(2, 1, 'member');
*/

-- 10. 创建定时任务事件（可选，用于定期更新排行榜缓存）
-- 注意：需要确保MySQL的事件调度器已启用 (SET GLOBAL event_scheduler = ON;)
/*
DELIMITER $$
CREATE EVENT ev_update_circle_leaderboard
ON SCHEDULE EVERY 1 HOUR
DO
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE circle_id_var BIGINT;
    
    DECLARE circle_cursor CURSOR FOR
        SELECT id FROM circles WHERE is_active = TRUE;
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN circle_cursor;
    
    update_loop: LOOP
        FETCH circle_cursor INTO circle_id_var;
        
        IF done THEN
            LEAVE update_loop;
        END IF;
        
        CALL UpdateCircleLeaderboard(circle_id_var);
    END LOOP;
    
    CLOSE circle_cursor;
END$$
DELIMITER ;
*/

-- 脚本执行完成提示
SELECT 'Circle Leaderboard Database Schema Created Successfully!' as message;