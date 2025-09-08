# 安全与性能优化指南

## 概述

本指南详细说明"回合"项目的安全配置和性能优化方案，确保系统在生产环境中安全、稳定、高效运行。

## 第一部分：安全配置

### 1.1 服务器安全加固

#### 1.1.1 系统用户管理

```bash
# 创建专用应用用户
useradd -r -s /bin/false chess-app
usermod -L chess-app  # 锁定用户登录

# 创建应用目录并设置权限
mkdir -p /opt/chess-rounds
chown -R chess-app:chess-app /opt/chess-rounds
chmod 750 /opt/chess-rounds

# 修改systemd服务配置
vim /etc/systemd/system/chess-rounds.service
```

```ini
[Unit]
Description=Chess Rounds Spring Boot Application
After=network.target mysql.service
Requires=mysql.service

[Service]
Type=simple
User=chess-app
Group=chess-app
WorkingDirectory=/opt/chess-rounds/backend
ExecStart=/usr/bin/java -jar \
  -Xms512m -Xmx1024m \
  -Dspring.profiles.active=prod \
  -Dspring.config.additional-location=/opt/chess-rounds/config/ \
  /opt/chess-rounds/backend/chess-rounds-backend-1.0.0.jar
ExecStop=/bin/kill -TERM $MAINPID
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

# 安全配置
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ProtectHome=true
ReadWritePaths=/opt/chess-rounds

[Install]
WantedBy=multi-user.target
```

#### 1.1.2 SSH安全配置

```bash
# 编辑SSH配置
vim /etc/ssh/sshd_config
```

```
# SSH安全配置
Port 22
Protocol 2
PermitRootLogin no
PasswordAuthentication no
PubkeyAuthentication yes
PermitEmptyPasswords no
X11Forwarding no
MaxAuthTries 3
ClientAliveInterval 300
ClientAliveCountMax 2
AllowUsers your-admin-user

# 禁用不安全的算法
KexAlgorithms curve25519-sha256@libssh.org,diffie-hellman-group16-sha512
Ciphers chacha20-poly1305@openssh.com,aes256-gcm@openssh.com,aes128-gcm@openssh.com,aes256-ctr,aes192-ctr,aes128-ctr
MACs hmac-sha2-256-etm@openssh.com,hmac-sha2-512-etm@openssh.com,hmac-sha2-256,hmac-sha2-512
```

```bash
# 重启SSH服务
systemctl restart sshd
```

#### 1.1.3 防火墙配置

```bash
# 配置UFW防火墙
ufw --force reset
ufw default deny incoming
ufw default allow outgoing

# 允许必要端口
ufw allow 22/tcp    # SSH
ufw allow 80/tcp    # HTTP
ufw allow 443/tcp   # HTTPS

# 限制连接频率
ufw limit 22/tcp

# 启用防火墙
ufw --force enable

# 查看状态
ufw status verbose
```

#### 1.1.4 Fail2Ban配置

```bash
# 安装fail2ban
apt install -y fail2ban

# 创建本地配置
vim /etc/fail2ban/jail.local
```

```ini
[DEFAULT]
# 默认配置
bantime = 3600
findtime = 600
maxretry = 3
ignoreip = 127.0.0.1/8 ::1

# SSH保护
[sshd]
enabled = true
port = ssh
logpath = /var/log/auth.log
maxretry = 3
bantime = 7200

# Nginx保护
[nginx-http-auth]
enabled = true
filter = nginx-http-auth
logpath = /var/log/nginx/error.log
maxretry = 3

[nginx-limit-req]
enabled = true
filter = nginx-limit-req
logpath = /var/log/nginx/error.log
maxretry = 10
bantime = 600

# 自定义规则：API频率限制
[chess-api-limit]
enabled = true
filter = chess-api-limit
logpath = /opt/chess-rounds/logs/chess-rounds.log
maxretry = 50
bantime = 1800
findtime = 300
```

```bash
# 创建自定义过滤器
vim /etc/fail2ban/filter.d/chess-api-limit.conf
```

```ini
[Definition]
failregex = ^.*API_RATE_LIMIT.*client_ip=<HOST>.*$
ignoreregex =
```

```bash
# 启动fail2ban
systemctl enable fail2ban
systemctl start fail2ban

# 查看状态
fail2ban-client status
```

### 1.2 应用安全配置

#### 1.2.1 Spring Boot安全配置

```yaml
# application-prod.yml
spring:
  security:
    # CSRF保护
    csrf:
      enabled: true
    # 会话管理
    session:
      creation-policy: stateless
  
  # 数据源安全
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000

# 安全头配置
server:
  servlet:
    session:
      cookie:
        secure: true
        http-only: true
        same-site: strict
  error:
    include-stacktrace: never
    include-message: never

# JWT配置
app:
  jwt:
    secret: ${JWT_SECRET:your-production-jwt-secret-key-at-least-256-bits-long}
    expiration: 7200  # 2小时
    refresh-expiration: 604800  # 7天
  
  # 限流配置
  rate-limit:
    enabled: true
    default-limit: 100
    login-limit: 10
    create-round-limit: 5

# 日志安全
logging:
  level:
    org.springframework.security: WARN
    org.hibernate.SQL: WARN
    com.zaxxer.hikari: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

#### 1.2.2 JWT安全实现

```java
@Component
public class JwtTokenProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    
    @Value("${app.jwt.secret}")
    private String jwtSecret;
    
    @Value("${app.jwt.expiration}")
    private int jwtExpirationInMs;
    
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    public String generateToken(UserPrincipal userPrincipal) {
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationInMs * 1000L);
        
        return Jwts.builder()
                .setSubject(userPrincipal.getId())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .claim("role", userPrincipal.getAuthorities())
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    
    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return claims.getSubject();
    }
    
    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(authToken);
            return true;
        } catch (SecurityException ex) {
            logger.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty");
        }
        return false;
    }
}
```

#### 1.2.3 API限流实现

```java
@Component
public class RateLimitingFilter implements Filter {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final RateLimitProperties rateLimitProperties;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String clientIp = getClientIpAddress(httpRequest);
        String uri = httpRequest.getRequestURI();
        
        if (isRateLimited(clientIp, uri)) {
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.setContentType("application/json");
            
            String errorResponse = "{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\"}";
            httpResponse.getWriter().write(errorResponse);
            
            // 记录限流日志
            logger.warn("API_RATE_LIMIT: uri={}, client_ip={}", uri, clientIp);
            return;
        }
        
        chain.doFilter(request, response);
    }
    
    private boolean isRateLimited(String clientIp, String uri) {
        String key = "rate_limit:" + clientIp + ":" + uri;
        String count = redisTemplate.opsForValue().get(key);
        
        int limit = getRateLimitForUri(uri);
        
        if (count == null) {
            redisTemplate.opsForValue().set(key, "1", Duration.ofMinutes(1));
            return false;
        }
        
        int currentCount = Integer.parseInt(count);
        if (currentCount >= limit) {
            return true;
        }
        
        redisTemplate.opsForValue().increment(key);
        return false;
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
```

### 1.3 数据库安全

#### 1.3.1 MySQL安全配置

```bash
# 编辑MySQL配置
vim /etc/mysql/mysql.conf.d/mysqld.cnf
```

```ini
[mysqld]
# 基础安全配置
bind-address = 127.0.0.1
skip-networking = false
skip-show-database

# 禁用危险功能
local-infile = 0
secure-file-priv = /var/lib/mysql-files/

# 日志配置
general_log = 1
general_log_file = /var/log/mysql/general.log
log_error = /var/log/mysql/error.log
slow_query_log = 1
slow_query_log_file = /var/log/mysql/slow.log
long_query_time = 2

# 连接安全
max_connections = 200
max_connect_errors = 10
max_user_connections = 50

# SSL配置
ssl-ca = /etc/mysql/ssl/ca-cert.pem
ssl-cert = /etc/mysql/ssl/server-cert.pem
ssl-key = /etc/mysql/ssl/server-key.pem
require_secure_transport = ON
```

#### 1.3.2 数据库用户权限管理

```sql
-- 创建只读用户（用于备份和监控）
CREATE USER 'chess_readonly'@'localhost' IDENTIFIED BY 'strong_readonly_password';
GRANT SELECT ON chess_rounds.* TO 'chess_readonly'@'localhost';

-- 应用用户权限最小化
REVOKE ALL PRIVILEGES ON *.* FROM 'chess_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON chess_rounds.* TO 'chess_app'@'localhost';
GRANT CREATE TEMPORARY TABLES ON chess_rounds.* TO 'chess_app'@'localhost';

-- 禁用危险权限
REVOKE FILE ON *.* FROM 'chess_app'@'localhost';
REVOKE PROCESS ON *.* FROM 'chess_app'@'localhost';
REVOKE SUPER ON *.* FROM 'chess_app'@'localhost';

FLUSH PRIVILEGES;
```

#### 1.3.3 数据加密

```sql
-- 创建加密函数
DELIMITER //
CREATE FUNCTION encrypt_sensitive_data(input_text TEXT) 
RETURNS TEXT
READS SQL DATA
DETERMINISTIC
BEGIN
    RETURN AES_ENCRYPT(input_text, 'your-encryption-key');
END //

CREATE FUNCTION decrypt_sensitive_data(encrypted_text TEXT) 
RETURNS TEXT
READS SQL DATA
DETERMINISTIC
BEGIN
    RETURN AES_DECRYPT(encrypted_text, 'your-encryption-key');
END //
DELIMITER ;

-- 敏感数据加密存储
ALTER TABLE users ADD COLUMN encrypted_phone BLOB;
UPDATE users SET encrypted_phone = AES_ENCRYPT(phone, 'your-encryption-key') WHERE phone IS NOT NULL;
```

### 1.4 Nginx安全配置

```nginx
# /etc/nginx/sites-available/chess-rounds
server {
    listen 443 ssl http2;
    server_name api.airoubo.com mp.airoubo.com;
    
    # SSL配置
    ssl_certificate /etc/letsencrypt/live/airoubo.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/airoubo.com/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE-RSA-AES256-GCM-SHA384:ECDHE-RSA-CHACHA20-POLY1305;
    ssl_prefer_server_ciphers off;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;
    
    # HSTS
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains; preload" always;
    
    # 安全头
    add_header X-Frame-Options DENY always;
    add_header X-Content-Type-Options nosniff always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;
    add_header Content-Security-Policy "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self'; connect-src 'self' https://api.airoubo.com wss://api.airoubo.com" always;
    
    # 隐藏服务器信息
    server_tokens off;
    
    # 限制请求大小
    client_max_body_size 10M;
    client_body_buffer_size 128k;
    
    # 限制连接
    limit_conn_zone $binary_remote_addr zone=conn_limit_per_ip:10m;
    limit_req_zone $binary_remote_addr zone=req_limit_per_ip:10m rate=10r/s;
    
    limit_conn conn_limit_per_ip 20;
    limit_req zone=req_limit_per_ip burst=20 nodelay;
    
    # 禁止访问敏感文件
    location ~ /\. {
        deny all;
        access_log off;
        log_not_found off;
    }
    
    location ~ \.(sql|bak|backup|log)$ {
        deny all;
        access_log off;
        log_not_found off;
    }
    
    # API代理
    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 超时配置
        proxy_connect_timeout 30s;
        proxy_send_timeout 30s;
        proxy_read_timeout 30s;
        
        # 缓冲配置
        proxy_buffering on;
        proxy_buffer_size 4k;
        proxy_buffers 8 4k;
        proxy_busy_buffers_size 8k;
    }
    
    # 静态资源缓存
    location ~* \.(jpg|jpeg|png|gif|ico|css|js|woff|woff2)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
        add_header Vary Accept-Encoding;
        
        # 压缩
        gzip_static on;
    }
    
    # 健康检查（不记录日志）
    location /health {
        proxy_pass http://127.0.0.1:8080/actuator/health;
        access_log off;
    }
}
```

## 第二部分：性能优化

### 2.1 JVM性能调优

#### 2.1.1 JVM参数优化

```bash
# 编辑systemd服务文件
vim /etc/systemd/system/chess-rounds.service
```

```ini
ExecStart=/usr/bin/java -jar \
  # 内存配置
  -Xms1024m -Xmx2048m \
  -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m \
  
  # GC配置
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:G1HeapRegionSize=16m \
  -XX:+G1UseAdaptiveIHOP \
  -XX:G1MixedGCCountTarget=8 \
  
  # JIT编译优化
  -XX:+TieredCompilation \
  -XX:TieredStopAtLevel=4 \
  
  # 内存优化
  -XX:+UseStringDeduplication \
  -XX:+OptimizeStringConcat \
  
  # 监控和诊断
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/opt/chess-rounds/logs/ \
  -XX:+PrintGCDetails \
  -XX:+PrintGCTimeStamps \
  -XX:+PrintGCApplicationStoppedTime \
  -Xloggc:/opt/chess-rounds/logs/gc.log \
  -XX:+UseGCLogFileRotation \
  -XX:NumberOfGCLogFiles=5 \
  -XX:GCLogFileSize=10M \
  
  # 网络优化
  -Djava.net.preferIPv4Stack=true \
  -Dnetworkaddress.cache.ttl=60 \
  
  # 应用配置
  -Dspring.profiles.active=prod \
  -Dspring.config.additional-location=/opt/chess-rounds/config/ \
  /opt/chess-rounds/backend/chess-rounds-backend-1.0.0.jar
```

#### 2.1.2 GC监控脚本

```bash
# 创建GC监控脚本
vim /opt/chess-rounds/scripts/gc-monitor.sh
```

```bash
#!/bin/bash

# GC监控脚本
LOG_FILE="/opt/chess-rounds/logs/gc-monitor.log"
GC_LOG_FILE="/opt/chess-rounds/logs/gc.log"
ALERT_THRESHOLD=5000  # GC时间超过5秒告警

# 分析最近的GC日志
analyze_gc() {
    if [ -f "$GC_LOG_FILE" ]; then
        # 获取最近1分钟的GC信息
        recent_gc=$(tail -n 100 "$GC_LOG_FILE" | grep "$(date '+%Y-%m-%d %H:%M')" | tail -n 10)
        
        if [ -n "$recent_gc" ]; then
            # 计算平均GC时间
            avg_gc_time=$(echo "$recent_gc" | grep -oP '\d+\.\d+(?=ms)' | awk '{sum+=$1; count++} END {if(count>0) print sum/count; else print 0}')
            
            echo "$(date): Average GC time in last minute: ${avg_gc_time}ms" >> "$LOG_FILE"
            
            # 检查是否超过阈值
            if (( $(echo "$avg_gc_time > $ALERT_THRESHOLD" | bc -l) )); then
                echo "$(date): WARNING - High GC time detected: ${avg_gc_time}ms" >> "$LOG_FILE"
                # 可以添加告警逻辑
            fi
        fi
    fi
}

# 检查内存使用
check_memory() {
    pid=$(pgrep -f "chess-rounds-backend")
    if [ -n "$pid" ]; then
        memory_info=$(jstat -gc "$pid" | tail -n 1)
        echo "$(date): Memory info - $memory_info" >> "$LOG_FILE"
    fi
}

analyze_gc
check_memory
```

```bash
# 设置执行权限和定时任务
chmod +x /opt/chess-rounds/scripts/gc-monitor.sh

# 每分钟执行一次
crontab -e
* * * * * /opt/chess-rounds/scripts/gc-monitor.sh
```

### 2.2 数据库性能优化

#### 2.2.1 MySQL配置优化

```ini
# /etc/mysql/mysql.conf.d/mysqld.cnf
[mysqld]
# 基础配置
max_connections = 200
max_connect_errors = 1000
max_user_connections = 50

# 内存配置
innodb_buffer_pool_size = 1G
innodb_log_file_size = 256M
innodb_log_buffer_size = 16M
innodb_flush_log_at_trx_commit = 2
innodb_flush_method = O_DIRECT

# 查询缓存
query_cache_type = 1
query_cache_size = 64M
query_cache_limit = 2M

# 临时表
tmp_table_size = 64M
max_heap_table_size = 64M

# 排序和连接
sort_buffer_size = 2M
join_buffer_size = 2M
read_buffer_size = 1M
read_rnd_buffer_size = 4M

# 线程配置
thread_cache_size = 50
thread_stack = 256K

# 慢查询日志
slow_query_log = 1
slow_query_log_file = /var/log/mysql/slow.log
long_query_time = 1
log_queries_not_using_indexes = 1

# 二进制日志
log_bin = /var/log/mysql/mysql-bin.log
binlog_format = ROW
expire_logs_days = 7
max_binlog_size = 100M

# InnoDB优化
innodb_file_per_table = 1
innodb_open_files = 400
innodb_io_capacity = 400
innodb_flush_neighbors = 0
innodb_read_io_threads = 4
innodb_write_io_threads = 4
innodb_purge_threads = 1
innodb_adaptive_hash_index = 1
```

#### 2.2.2 索引优化

```sql
-- 分析现有索引使用情况
SELECT 
    t.TABLE_SCHEMA,
    t.TABLE_NAME,
    s.INDEX_NAME,
    s.COLUMN_NAME,
    s.SEQ_IN_INDEX,
    s.CARDINALITY,
    t.TABLE_ROWS
FROM 
    information_schema.STATISTICS s
JOIN 
    information_schema.TABLES t ON s.TABLE_SCHEMA = t.TABLE_SCHEMA 
    AND s.TABLE_NAME = t.TABLE_NAME
WHERE 
    t.TABLE_SCHEMA = 'chess_rounds'
ORDER BY 
    t.TABLE_NAME, s.INDEX_NAME, s.SEQ_IN_INDEX;

-- 查找未使用的索引
SELECT 
    object_schema,
    object_name,
    index_name
FROM 
    performance_schema.table_io_waits_summary_by_index_usage
WHERE 
    object_schema = 'chess_rounds'
    AND index_name IS NOT NULL
    AND count_star = 0
ORDER BY 
    object_schema, object_name;

-- 优化索引
-- 用户表索引
CREATE INDEX idx_users_open_id ON users(open_id);
CREATE INDEX idx_users_created_at ON users(created_at);
CREATE INDEX idx_users_last_login ON users(last_login_at);

-- 回合表索引
CREATE INDEX idx_rounds_creator_status ON rounds(creator_id, status);
CREATE INDEX idx_rounds_status_created ON rounds(status, created_at);
CREATE INDEX idx_rounds_code ON rounds(round_code);

-- 参与者表索引
CREATE INDEX idx_participants_user_role ON round_participants(user_id, role);
CREATE INDEX idx_participants_round_role ON round_participants(round_id, role);

-- 记录表索引
CREATE INDEX idx_records_round_created ON round_records(round_id, created_at);
CREATE INDEX idx_records_winner ON round_records(winner_id, created_at);
CREATE INDEX idx_records_loser ON round_records(loser_id, created_at);

-- 复合索引优化
CREATE INDEX idx_rounds_user_status_time ON rounds(creator_id, status, created_at);
CREATE INDEX idx_records_users_time ON round_records(winner_id, loser_id, created_at);
```

#### 2.2.3 查询优化

```sql
-- 创建性能监控视图
CREATE VIEW v_slow_queries AS
SELECT 
    query_time,
    lock_time,
    rows_sent,
    rows_examined,
    sql_text
FROM 
    mysql.slow_log
WHERE 
    start_time >= DATE_SUB(NOW(), INTERVAL 1 DAY)
ORDER BY 
    query_time DESC
LIMIT 20;

-- 优化常用查询
-- 1. 用户回合列表查询优化
EXPLAIN SELECT 
    r.id,
    r.round_code,
    r.status,
    r.created_at,
    COUNT(rp.user_id) as participant_count,
    COUNT(rr.id) as record_count
FROM 
    rounds r
    LEFT JOIN round_participants rp ON r.id = rp.round_id AND rp.role = 'PLAYER'
    LEFT JOIN round_records rr ON r.id = rr.round_id
WHERE 
    r.creator_id = 'user-uuid'
    AND r.status IN ('IN_PROGRESS', 'FINISHED')
GROUP BY 
    r.id, r.round_code, r.status, r.created_at
ORDER BY 
    r.created_at DESC
LIMIT 10;

-- 2. 统计数据查询优化
EXPLAIN SELECT 
    DATE(rr.created_at) as date,
    COUNT(*) as total_records,
    SUM(CASE WHEN rr.winner_id = 'user-uuid' THEN 1 ELSE 0 END) as wins,
    SUM(CASE WHEN rr.loser_id = 'user-uuid' THEN 1 ELSE 0 END) as losses,
    SUM(CASE WHEN rr.winner_id = 'user-uuid' THEN rr.actual_points ELSE -rr.actual_points END) as total_points
FROM 
    round_records rr
WHERE 
    (rr.winner_id = 'user-uuid' OR rr.loser_id = 'user-uuid')
    AND rr.created_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
GROUP BY 
    DATE(rr.created_at)
ORDER BY 
    date DESC;
```

### 2.3 应用层性能优化

#### 2.3.1 Redis缓存配置

```yaml
# application-prod.yml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5
          max-wait: 2000ms
        shutdown-timeout: 100ms

# 缓存配置
cache:
  redis:
    time-to-live: 600  # 10分钟
    cache-null-values: false
    key-prefix: "chess_rounds:"
  
  # 不同类型数据的缓存时间
  user-profile: 1800      # 30分钟
  round-details: 300      # 5分钟
  statistics: 600         # 10分钟
  ai-analysis: 3600       # 1小时
```

#### 2.3.2 缓存实现

```java
@Service
@CacheConfig(cacheNames = "chess_rounds")
public class RoundService {
    
    @Cacheable(key = "'round:' + #roundId", unless = "#result == null")
    public RoundDetailVO getRoundDetails(String roundId) {
        // 查询回合详情
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new NotFoundException("回合不存在"));
        
        // 查询参与者
        List<RoundParticipant> participants = participantRepository
                .findByRoundIdOrderByJoinedAt(roundId);
        
        // 查询记录
        List<RoundRecord> records = recordRepository
                .findByRoundIdOrderByCreatedAt(roundId);
        
        return RoundDetailVO.builder()
                .round(round)
                .participants(participants)
                .records(records)
                .build();
    }
    
    @CacheEvict(key = "'round:' + #roundId")
    public void updateRoundStatus(String roundId, RoundStatus status) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new NotFoundException("回合不存在"));
        
        round.setStatus(status);
        roundRepository.save(round);
    }
    
    @Cacheable(key = "'user_rounds:' + #userId + ':' + #status + ':' + #page", 
               unless = "#result.isEmpty()")
    public Page<RoundSummaryVO> getUserRounds(String userId, RoundStatus status, 
                                            Pageable pageable) {
        return roundRepository.findUserRounds(userId, status, pageable)
                .map(this::convertToSummaryVO);
    }
}

@Service
public class StatisticsService {
    
    @Cacheable(key = "'user_stats:' + #userId + ':' + #period", 
               unless = "#result == null")
    public UserStatisticsVO getUserStatistics(String userId, StatisticsPeriod period) {
        LocalDateTime startTime = getStartTimeForPeriod(period);
        
        // 查询统计数据
        List<Object[]> results = recordRepository
                .getUserStatistics(userId, startTime);
        
        return buildStatisticsVO(results);
    }
    
    @Cacheable(key = "'monthly_chart:' + #userId + ':' + #year", 
               unless = "#result.isEmpty()")
    public List<MonthlyDataVO> getMonthlyChart(String userId, int year) {
        return recordRepository.getMonthlyStatistics(userId, year)
                .stream()
                .map(this::convertToMonthlyDataVO)
                .collect(Collectors.toList());
    }
}
```

#### 2.3.3 数据库连接池优化

```yaml
# application-prod.yml
spring:
  datasource:
    hikari:
      # 连接池配置
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      max-lifetime: 1800000
      connection-timeout: 30000
      
      # 性能配置
      leak-detection-threshold: 60000
      validation-timeout: 5000
      
      # 连接测试
      connection-test-query: SELECT 1
      
      # 数据源属性
      data-source-properties:
        cachePrepStmts: true
        prepStmtCacheSize: 250
        prepStmtCacheSqlLimit: 2048
        useServerPrepStmts: true
        useLocalSessionState: true
        rewriteBatchedStatements: true
        cacheResultSetMetadata: true
        cacheServerConfiguration: true
        elideSetAutoCommits: true
        maintainTimeStats: false
```

### 2.4 前端性能优化

#### 2.4.1 小程序性能配置

```javascript
// app.js
App({
  onLaunch() {
    // 预加载关键资源
    this.preloadCriticalResources()
    
    // 启用性能监控
    this.enablePerformanceMonitoring()
  },
  
  preloadCriticalResources() {
    // 预加载用户信息
    const token = wx.getStorageSync('token')
    if (token) {
      this.globalData.userInfo = wx.getStorageSync('userInfo')
    }
    
    // 预加载系统配置
    wx.request({
      url: `${this.globalData.apiBase}/api/v1/system/config`,
      success: (res) => {
        if (res.data.code === 200) {
          this.globalData.systemConfig = res.data.data.config
          wx.setStorageSync('systemConfig', res.data.data.config)
        }
      }
    })
  },
  
  enablePerformanceMonitoring() {
    // 监控页面性能
    wx.onMemoryWarning(() => {
      console.warn('内存不足警告')
      // 清理不必要的缓存
      this.clearUnnecessaryCache()
    })
    
    // 监控网络状态
    wx.onNetworkStatusChange((res) => {
      this.globalData.networkType = res.networkType
      this.globalData.isConnected = res.isConnected
    })
  },
  
  clearUnnecessaryCache() {
    // 清理过期的缓存数据
    const cacheKeys = ['roundsList', 'statisticsData', 'chartData']
    cacheKeys.forEach(key => {
      const cacheData = wx.getStorageSync(key)
      if (cacheData && cacheData.timestamp) {
        const now = Date.now()
        if (now - cacheData.timestamp > 30 * 60 * 1000) { // 30分钟过期
          wx.removeStorageSync(key)
        }
      }
    })
  }
})
```

#### 2.4.2 图片优化

```javascript
// utils/imageOptimizer.js
class ImageOptimizer {
  static compressImage(src, quality = 0.8) {
    return new Promise((resolve, reject) => {
      wx.compressImage({
        src: src,
        quality: quality,
        success: resolve,
        fail: reject
      })
    })
  }
  
  static async uploadOptimizedImage(filePath) {
    try {
      // 压缩图片
      const compressed = await this.compressImage(filePath, 0.7)
      
      // 上传图片
      return new Promise((resolve, reject) => {
        wx.uploadFile({
          url: `${getApp().globalData.apiBase}/api/v1/upload/image`,
          filePath: compressed.tempFilePath,
          name: 'file',
          header: {
            'Authorization': `Bearer ${wx.getStorageSync('token')}`
          },
          success: (res) => {
            const data = JSON.parse(res.data)
            if (data.code === 200) {
              resolve(data.data.url)
            } else {
              reject(new Error(data.message))
            }
          },
          fail: reject
        })
      })
    } catch (error) {
      throw error
    }
  }
  
  static preloadImages(imageUrls) {
    imageUrls.forEach(url => {
      wx.downloadFile({
        url: url,
        success: (res) => {
          // 图片预加载成功
          console.log(`预加载图片成功: ${url}`)
        }
      })
    })
  }
}

export default ImageOptimizer
```

#### 2.4.3 数据缓存策略

```javascript
// utils/cacheManager.js
class CacheManager {
  static set(key, data, expireTime = 30 * 60 * 1000) {
    const cacheData = {
      data: data,
      timestamp: Date.now(),
      expireTime: expireTime
    }
    
    try {
      wx.setStorageSync(key, cacheData)
    } catch (error) {
      console.error('缓存设置失败:', error)
      // 如果存储空间不足，清理旧缓存
      this.clearExpiredCache()
      try {
        wx.setStorageSync(key, cacheData)
      } catch (retryError) {
        console.error('重试缓存设置失败:', retryError)
      }
    }
  }
  
  static get(key) {
    try {
      const cacheData = wx.getStorageSync(key)
      if (!cacheData) return null
      
      const now = Date.now()
      if (now - cacheData.timestamp > cacheData.expireTime) {
        wx.removeStorageSync(key)
        return null
      }
      
      return cacheData.data
    } catch (error) {
      console.error('缓存获取失败:', error)
      return null
    }
  }
  
  static clearExpiredCache() {
    try {
      const info = wx.getStorageInfoSync()
      const now = Date.now()
      
      info.keys.forEach(key => {
        try {
          const cacheData = wx.getStorageSync(key)
          if (cacheData && cacheData.timestamp) {
            if (now - cacheData.timestamp > cacheData.expireTime) {
              wx.removeStorageSync(key)
            }
          }
        } catch (error) {
          // 忽略单个key的错误
        }
      })
    } catch (error) {
      console.error('清理过期缓存失败:', error)
    }
  }
  
  static getStorageUsage() {
    try {
      const info = wx.getStorageInfoSync()
      return {
        keys: info.keys.length,
        currentSize: info.currentSize,
        limitSize: info.limitSize,
        usage: (info.currentSize / info.limitSize * 100).toFixed(2) + '%'
      }
    } catch (error) {
      return null
    }
  }
}

export default CacheManager
```

### 2.5 监控和告警

#### 2.5.1 应用性能监控

```java
@Component
public class PerformanceMonitor {
    
    private final MeterRegistry meterRegistry;
    private final Logger logger = LoggerFactory.getLogger(PerformanceMonitor.class);
    
    @EventListener
    public void handleRequestEvent(RequestEvent event) {
        // 记录请求响应时间
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(Timer.builder("http.request.duration")
                .tag("uri", event.getUri())
                .tag("method", event.getMethod())
                .tag("status", String.valueOf(event.getStatus()))
                .register(meterRegistry));
        
        // 记录慢请求
        if (event.getDuration() > 2000) {
            logger.warn("Slow request detected: {} {} took {}ms", 
                    event.getMethod(), event.getUri(), event.getDuration());
        }
    }
    
    @Scheduled(fixedRate = 60000) // 每分钟执行
    public void collectMetrics() {
        // 收集JVM指标
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        Gauge.builder("jvm.memory.used")
                .register(meterRegistry, usedMemory);
        
        Gauge.builder("jvm.memory.total")
                .register(meterRegistry, totalMemory);
        
        // 收集数据库连接池指标
        HikariDataSource dataSource = (HikariDataSource) this.dataSource;
        Gauge.builder("hikari.connections.active")
                .register(meterRegistry, dataSource.getHikariPoolMXBean().getActiveConnections());
        
        Gauge.builder("hikari.connections.idle")
                .register(meterRegistry, dataSource.getHikariPoolMXBean().getIdleConnections());
    }
}
```

#### 2.5.2 告警脚本

```bash
# 创建告警脚本
vim /opt/chess-rounds/scripts/alert-monitor.sh
```

```bash
#!/bin/bash

# 告警监控脚本
ALERT_LOG="/opt/chess-rounds/logs/alerts.log"
API_URL="https://api.airoubo.com/actuator/health"
DB_USER="chess_readonly"
DB_PASS="readonly_password"
DB_NAME="chess_rounds"

# 检查应用健康状态
check_app_health() {
    response=$(curl -s -o /dev/null -w "%{http_code}" "$API_URL")
    if [ "$response" != "200" ]; then
        echo "$(date): ALERT - Application health check failed (HTTP $response)" >> "$ALERT_LOG"
        send_alert "应用健康检查失败" "HTTP状态码: $response"
        return 1
    fi
    return 0
}

# 检查数据库连接
check_db_connection() {
    mysql -u"$DB_USER" -p"$DB_PASS" -h localhost "$DB_NAME" -e "SELECT 1" > /dev/null 2>&1
    if [ $? -ne 0 ]; then
        echo "$(date): ALERT - Database connection failed" >> "$ALERT_LOG"
        send_alert "数据库连接失败" "无法连接到MySQL数据库"
        return 1
    fi
    return 0
}

# 检查磁盘空间
check_disk_space() {
    usage=$(df /opt/chess-rounds | awk 'NR==2 {print $5}' | sed 's/%//')
    if [ "$usage" -gt 80 ]; then
        echo "$(date): ALERT - High disk usage: ${usage}%" >> "$ALERT_LOG"
        send_alert "磁盘空间不足" "磁盘使用率: ${usage}%"
        return 1
    fi
    return 0
}

# 检查内存使用
check_memory_usage() {
    memory_usage=$(free | awk 'NR==2{printf "%.2f", $3*100/$2}')
    if (( $(echo "$memory_usage > 85" | bc -l) )); then
        echo "$(date): ALERT - High memory usage: ${memory_usage}%" >> "$ALERT_LOG"
        send_alert "内存使用率过高" "内存使用率: ${memory_usage}%"
        return 1
    fi
    return 0
}

# 检查CPU使用率
check_cpu_usage() {
    cpu_usage=$(top -bn1 | grep "Cpu(s)" | awk '{print $2}' | sed 's/%us,//')
    if (( $(echo "$cpu_usage > 80" | bc -l) )); then
        echo "$(date): ALERT - High CPU usage: ${cpu_usage}%" >> "$ALERT_LOG"
        send_alert "CPU使用率过高" "CPU使用率: ${cpu_usage}%"
        return 1
    fi
    return 0
}

# 发送告警（可以集成钉钉、企业微信等）
send_alert() {
    local title="$1"
    local message="$2"
    
    # 这里可以集成实际的告警系统
    echo "$(date): ALERT - $title: $message" >> "$ALERT_LOG"
    
    # 示例：发送到钉钉机器人
    # curl -X POST "https://oapi.dingtalk.com/robot/send?access_token=YOUR_TOKEN" \
    #      -H "Content-Type: application/json" \
    #      -d "{\"msgtype\": \"text\", \"text\": {\"content\": \"$title: $message\"}}"
}

# 执行所有检查
check_app_health
check_db_connection
check_disk_space
check_memory_usage
check_cpu_usage

echo "$(date): Health check completed" >> "$ALERT_LOG"
```

```bash
# 设置执行权限和定时任务
chmod +x /opt/chess-rounds/scripts/alert-monitor.sh

# 每5分钟执行一次
crontab -e
*/5 * * * * /opt/chess-rounds/scripts/alert-monitor.sh
```

这个安全与性能优化指南提供了全面的配置方案，包括：

**安全方面**：
- 服务器安全加固
- 应用安全配置
- 数据库安全
- 网络安全

**性能方面**：
- JVM调优
- 数据库优化
- 缓存策略
- 前端优化
- 监控告警

按照这个指南实施，可以显著提升系统的安全性和性能表现。