# 管理员统计接口设计文档

## 概述

为用户ID为1的管理员用户设计统计接口，提供平台运营数据的全面统计功能，包括回合状态统计、用户统计和总流水统计。

## API接口设计

### 1. 管理员统计总览接口

**接口路径**: `GET /api/admin/statistics/overview`

**权限要求**: 仅限用户ID为1的管理员访问

**请求参数**: 无

**响应数据结构**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "roundStatistics": {
      "waiting": {
        "count": 3,
        "roundIds": [204, 202, 201]
      },
      "playing": {
        "count": 1,
        "roundIds": [207]
      },
      "finished": {
        "count": 3,
        "roundIds": [206, 203, 200]
      }
    },
    "userStatistics": {
      "totalUsers": 27,
      "activeUsers": 27,
      "inactiveUsers": 0
    },
    "financialStatistics": {
      "totalTurnover": "5240.00",
      "currency": "CNY"
    },
    "timestamp": "2025-01-14T12:00:00Z"
  }
}
```

### 2. 回合详细统计接口

**接口路径**: `GET /api/admin/statistics/rounds`

**权限要求**: 仅限用户ID为1的管理员访问

**请求参数**:
- `status` (可选): 回合状态筛选 (WAITING/PLAYING/FINISHED)
- `page` (可选): 页码，默认1
- `size` (可选): 每页数量，默认20

**响应数据结构**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "statistics": {
      "waiting": {
        "count": 3,
        "roundIds": [204, 202, 201]
      },
      "playing": {
        "count": 1,
        "roundIds": [207]
      },
      "finished": {
        "count": 3,
        "roundIds": [206, 203, 200]
      }
    },
    "roundDetails": [
      {
        "id": 207,
        "roundCode": "0UUOY1",
        "status": "PLAYING",
        "gameType": "mahjong",
        "createdAt": "2025-09-15T01:57:47.949Z",
        "startTime": "2025-09-15T01:57:54.561Z",
        "endTime": null,
        "maxParticipants": 4,
        "currentParticipants": 2,
        "totalAmount": "0.00",
        "creatorId": 41,
        "creatorName": "微信用户"
      }
    ],
    "pagination": {
      "page": 1,
      "size": 20,
      "total": 7,
      "totalPages": 1
    }
  }
}
```

### 3. 用户详细统计接口

**接口路径**: `GET /api/admin/statistics/users`

**权限要求**: 仅限用户ID为1的管理员访问

**请求参数**:
- `status` (可选): 用户状态筛选
- `page` (可选): 页码，默认1
- `size` (可选): 每页数量，默认20
- `search` (可选): 用户昵称搜索

**响应数据结构**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "statistics": {
      "totalUsers": 27,
      "activeUsers": 27,
      "inactiveUsers": 0,
      "newUsersToday": 2,
      "newUsersThisWeek": 15
    },
    "userList": [
      {
        "id": 42,
        "nickname": "台板-0UUOY1",
        "createdAt": "2025-09-15T01:57:54.500Z",
        "lastLoginAt": null,
        "status": 1,
        "city": null,
        "province": null,
        "country": null,
        "isTableUser": true,
        "totalRounds": 1,
        "totalWinnings": "0.00"
      },
      {
        "id": 41,
        "nickname": "微信用户",
        "createdAt": "2025-09-15T01:57:14.649Z",
        "lastLoginAt": "2025-09-15T01:57:26.723Z",
        "status": 1,
        "city": null,
        "province": null,
        "country": null,
        "isTableUser": false,
        "totalRounds": 1,
        "totalWinnings": "0.00"
      }
    ],
    "pagination": {
      "page": 1,
      "size": 20,
      "total": 27,
      "totalPages": 2
    }
  }
}
```

### 4. 财务统计接口

**接口路径**: `GET /api/admin/statistics/financial`

**权限要求**: 仅限用户ID为1的管理员访问

**请求参数**:
- `startDate` (可选): 开始日期 (YYYY-MM-DD)
- `endDate` (可选): 结束日期 (YYYY-MM-DD)
- `gameType` (可选): 游戏类型筛选

**响应数据结构**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalTurnover": "5240.00",
    "finishedRoundsCount": 3,
    "averageTurnoverPerRound": "1746.67",
    "currency": "CNY",
    "periodStatistics": {
      "today": "0.00",
      "thisWeek": "5240.00",
      "thisMonth": "5240.00"
    },
    "gameTypeBreakdown": [
      {
        "gameType": "mahjong",
        "turnover": "5240.00",
        "roundsCount": 3,
        "percentage": 100.0
      }
    ],
    "dailyTrend": [
      {
        "date": "2025-09-15",
        "turnover": "0.00",
        "roundsCount": 0
      },
      {
        "date": "2025-09-14",
        "turnover": "2620.00",
        "roundsCount": 1
      }
    ]
  }
}
```

## 数据库查询实现

### 1. 回合状态统计查询

```sql
-- 获取各状态回合统计
SELECT 
    status, 
    COUNT(*) as count, 
    GROUP_CONCAT(id ORDER BY created_at DESC) as round_ids 
FROM rounds 
GROUP BY status 
ORDER BY status;
```

### 2. 用户统计查询

```sql
-- 获取用户总数
SELECT COUNT(*) as total_users FROM users;

-- 获取用户列表（分页）
SELECT 
    id, nickname, created_at, last_login_at, status, 
    city, province, country,
    CASE WHEN nickname LIKE '台板-%' THEN 1 ELSE 0 END as is_table_user
FROM users 
ORDER BY created_at DESC 
LIMIT ? OFFSET ?;
```

### 3. 总流水统计查询

```sql
-- 获取已结束回合的总流水
SELECT SUM(ABS(pr.amount_change)) as total_turnover 
FROM participant_records pr 
JOIN rounds r ON pr.round_id = r.id 
WHERE r.status = 'FINISHED';

-- 按游戏类型统计流水
SELECT 
    r.game_type,
    SUM(ABS(pr.amount_change)) as turnover,
    COUNT(DISTINCT r.id) as rounds_count
FROM participant_records pr 
JOIN rounds r ON pr.round_id = r.id 
WHERE r.status = 'FINISHED'
GROUP BY r.game_type;
```

## 权限控制

### 中间件实现

```java
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) throws Exception {
        
        // 获取当前用户ID
        Long userId = getCurrentUserId(request);
        
        // 检查是否为管理员用户（ID为1）
        if (userId == null || !userId.equals(1L)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write(
                "{\"code\":403,\"message\":\"Access denied. Admin privileges required.\"}"
            );
            return false;
        }
        
        return true;
    }
}
```

### 注解方式权限控制

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AdminOnly {
}

@RestController
@RequestMapping("/api/admin/statistics")
public class AdminStatisticsController {
    
    @GetMapping("/overview")
    @AdminOnly
    public ResponseEntity<ApiResponse<StatisticsOverviewDTO>> getOverview() {
        // 实现逻辑
    }
}
```

## 服务层实现

### AdminStatisticsService

```java
@Service
public class AdminStatisticsService {
    
    @Autowired
    private RoundRepository roundRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ParticipantRecordRepository participantRecordRepository;
    
    public StatisticsOverviewDTO getStatisticsOverview() {
        // 获取回合统计
        Map<String, RoundStatisticsDTO> roundStats = getRoundStatistics();
        
        // 获取用户统计
        UserStatisticsDTO userStats = getUserStatistics();
        
        // 获取财务统计
        FinancialStatisticsDTO financialStats = getFinancialStatistics();
        
        return StatisticsOverviewDTO.builder()
            .roundStatistics(roundStats)
            .userStatistics(userStats)
            .financialStatistics(financialStats)
            .timestamp(Instant.now())
            .build();
    }
    
    private Map<String, RoundStatisticsDTO> getRoundStatistics() {
        // 实现回合统计逻辑
    }
    
    private UserStatisticsDTO getUserStatistics() {
        // 实现用户统计逻辑
    }
    
    private FinancialStatisticsDTO getFinancialStatistics() {
        // 实现财务统计逻辑
    }
}
```

## 前端集成说明

### 小程序端管理页面

1. **统计总览页面**
   - 显示回合状态卡片
   - 显示用户统计卡片
   - 显示财务统计卡片
   - 提供快速跳转链接

2. **回合管理页面**
   - 回合列表展示
   - 状态筛选功能
   - 点击进入回合详情
   - 支持回合操作（查看、结束等）

3. **用户管理页面**
   - 用户列表展示
   - 搜索和筛选功能
   - 用户详情查看
   - 用户状态管理

### 页面路由配置

```javascript
// pages.json
{
  "pages": [
    {
      "path": "pages/admin/statistics/overview",
      "style": {
        "navigationBarTitleText": "管理员统计"
      }
    },
    {
      "path": "pages/admin/rounds/list",
      "style": {
        "navigationBarTitleText": "回合管理"
      }
    },
    {
      "path": "pages/admin/users/list",
      "style": {
        "navigationBarTitleText": "用户管理"
      }
    }
  ]
}
```

## 性能优化

### 1. 缓存策略

- 统计数据缓存5分钟
- 用户列表缓存2分钟
- 回合列表实时查询

### 2. 数据库优化

- 为统计查询添加复合索引
- 使用分页查询避免大数据量
- 考虑使用视图优化复杂查询

### 3. 接口优化

- 支持数据压缩
- 实现增量更新
- 添加请求频率限制

## 监控和日志

### 1. 操作日志

记录管理员的所有统计查询操作，包括：
- 访问时间
- 查询参数
- 响应时间
- 数据量大小

### 2. 性能监控

- 接口响应时间监控
- 数据库查询性能监控
- 缓存命中率监控

## 安全考虑

1. **权限验证**: 严格验证用户ID为1
2. **数据脱敏**: 敏感信息适当脱敏
3. **访问日志**: 记录所有管理员操作
4. **频率限制**: 防止接口滥用
5. **HTTPS**: 强制使用HTTPS传输

## 部署说明

1. 确保数据库索引已创建
2. 配置缓存服务
3. 部署权限中间件
4. 配置监控和日志
5. 测试所有接口功能

## 测试用例

### 1. 功能测试

- 管理员用户访问测试
- 非管理员用户访问拒绝测试
- 各统计数据准确性测试
- 分页功能测试

### 2. 性能测试

- 大数据量查询性能测试
- 并发访问测试
- 缓存效果测试

### 3. 安全测试

- 权限绕过测试
- SQL注入测试
- 参数篡改测试

---

**文档版本**: v1.0  
**创建日期**: 2025-01-14  
**最后更新**: 2025-01-14  
**维护人员**: 开发团队