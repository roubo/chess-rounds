# 回合项目部署指南

## 部署概述

本指南详细说明如何将"回合"项目部署到生产环境，包括服务器配置、应用部署、域名配置和监控设置。

## 部署架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   微信小程序     │────│   Nginx反向代理   │────│  Spring Boot应用 │
│  (UniApp前端)   │    │  (SSL终端/负载)  │    │   (后端API)     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │                        │
                                │                        │
                       ┌─────────────────┐    ┌─────────────────┐
                       │   Let's Encrypt │    │   MySQL数据库   │
                       │   (SSL证书)     │    │   (数据存储)    │
                       └─────────────────┘    └─────────────────┘
```

## 前置条件

- 腾讯云服务器 (Ubuntu 20.04+)
- 阿里云域名 (airoubo.com)
- 微信小程序开发者账号
- DeepSeek API密钥

## 第一步：服务器环境准备

### 1.1 连接服务器

```bash
# 使用SSH连接服务器
ssh root@your-server-ip

# 更新系统
apt update && apt upgrade -y
```

### 1.2 执行环境配置脚本

按照 `server-setup.md` 文档中的步骤执行：

```bash
# 1. 安装基础软件
apt install -y curl wget vim git unzip software-properties-common

# 2. 配置时区
timedatectl set-timezone Asia/Shanghai

# 3. 配置防火墙
ufw enable
ufw allow ssh
ufw allow 80
ufw allow 443
ufw allow 3306

# 4. 安装Java 17
apt install -y openjdk-17-jdk
echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' >> /etc/environment
source /etc/environment

# 5. 安装MySQL
apt install -y mysql-server
mysql_secure_installation

# 6. 安装Nginx
apt install -y nginx

# 7. 安装Certbot
snap install --classic certbot
ln -s /snap/bin/certbot /usr/bin/certbot
```

## 第二步：数据库配置

### 2.1 创建数据库和用户

```bash
# 登录MySQL
mysql -u root -p
```

```sql
-- 创建数据库
CREATE DATABASE chess_rounds CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户（请替换为强密码）
CREATE USER 'chess_app'@'localhost' IDENTIFIED BY 'your_strong_password_here';
CREATE USER 'chess_app'@'%' IDENTIFIED BY 'your_strong_password_here';

-- 授予权限
GRANT ALL PRIVILEGES ON chess_rounds.* TO 'chess_app'@'localhost';
GRANT ALL PRIVILEGES ON chess_rounds.* TO 'chess_app'@'%';

-- 刷新权限
FLUSH PRIVILEGES;
EXIT;
```

### 2.2 配置MySQL远程访问

```bash
# 编辑MySQL配置
vim /etc/mysql/mysql.conf.d/mysqld.cnf

# 修改bind-address
# bind-address = 0.0.0.0

# 重启MySQL
systemctl restart mysql
```

## 第三步：域名和SSL配置

### 3.1 配置域名解析

在阿里云DNS控制台添加以下记录：

```
类型    主机记录    记录值
A       @          your-server-ip
A       www        your-server-ip
A       api        your-server-ip
A       mp         your-server-ip
```

### 3.2 申请SSL证书

```bash
# 停止Nginx
systemctl stop nginx

# 申请证书
certbot certonly --standalone \
  -d airoubo.com \
  -d www.airoubo.com \
  -d api.airoubo.com \
  -d mp.airoubo.com

# 配置自动续期
echo "0 2 * * * /usr/bin/certbot renew --quiet --post-hook 'systemctl reload nginx'" | crontab -
```

### 3.3 配置Nginx

```bash
# 创建Nginx配置文件
vim /etc/nginx/sites-available/chess-rounds
```

```nginx
# HTTP重定向到HTTPS
server {
    listen 80;
    server_name airoubo.com www.airoubo.com api.airoubo.com mp.airoubo.com;
    return 301 https://$server_name$request_uri;
}

# API服务器配置
server {
    listen 443 ssl http2;
    server_name api.airoubo.com mp.airoubo.com;
    
    # SSL配置
    ssl_certificate /etc/letsencrypt/live/airoubo.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/airoubo.com/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;
    
    # 安全头
    add_header X-Frame-Options DENY;
    add_header X-Content-Type-Options nosniff;
    add_header X-XSS-Protection "1; mode=block";
    
    # 静态文件服务配置
    location /static/ {
        alias /home/junjian/data/emotion/chess-rounds/uploads/;
        expires 1y;
        add_header Cache-Control "public, immutable";
        add_header Vary Accept-Encoding;
        add_header Access-Control-Allow-Origin *;
        
        # 安全配置
        location ~* \.(php|jsp|asp|aspx|sh|pl|py)$ {
            deny all;
        }
    }
    
    # 代理配置
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
    }
    
    # 健康检查
    location /health {
        proxy_pass http://127.0.0.1:8080/actuator/health;
        access_log off;
    }
}

# 主域名配置（可选：静态页面或重定向）
server {
    listen 443 ssl http2;
    server_name airoubo.com www.airoubo.com;
    
    ssl_certificate /etc/letsencrypt/live/airoubo.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/airoubo.com/privkey.pem;
    
    root /var/www/html;
    index index.html;
    
    location / {
        try_files $uri $uri/ =404;
    }
}
```

```bash
# 启用配置
ln -s /etc/nginx/sites-available/chess-rounds /etc/nginx/sites-enabled/
rm /etc/nginx/sites-enabled/default

# 测试配置
nginx -t

# 启动Nginx
systemctl start nginx
systemctl enable nginx
```

## 第四步：后端应用部署

### 4.1 创建应用目录

```bash
# 创建目录结构
mkdir -p /home/junjian/data/emotion/chess-rounds/{backend,logs,config,backup,uploads}
cd /home/junjian/data/emotion/chess-rounds

# 设置nginx用户对uploads目录的权限
chown -R www-data:www-data /home/junjian/data/emotion/chess-rounds/uploads
chmod -R 755 /home/junjian/data/emotion/chess-rounds/uploads

# 确保nginx用户可以访问整个路径（关键步骤）
chmod 755 /home/junjian
chmod 755 /home/junjian/data
chmod 755 /home/junjian/data/emotion
chmod 755 /home/junjian/data/emotion/chess-rounds
```

### 4.2 构建Spring Boot应用

在本地开发环境：

```bash
# 克隆或创建项目
git clone your-repository-url chess-rounds-backend
cd chess-rounds-backend

# 构建项目
mvn clean package -DskipTests

# 上传JAR文件到服务器
scp target/chess-rounds-backend-1.0.0.jar root@your-server-ip:/home/junjian/data/emotion/chess-rounds/backend/
```

### 4.3 配置应用

```bash
# 创建生产环境配置文件
vim /home/junjian/data/emotion/chess-rounds/config/application-prod.yml
```

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/chess_rounds?useUnicode=true&characterEncoding=utf8&useSSL=true&serverTimezone=Asia/Shanghai
    username: chess_app
    password: your_strong_password_here
  
  data:
    redis:
      host: localhost
      port: 6379
      database: 0

logging:
  level:
    com.airoubo.chessrounds: INFO
    org.springframework.security: WARN
  file:
    name: /home/junjian/data/emotion/chess-rounds/logs/chess-rounds.log

app:
  jwt:
    secret: your-production-jwt-secret-key-at-least-256-bits-long
  wechat:
    app-id: wx402b5a6e5f74462a
    app-secret: your-wechat-app-secret
  ai:
    deepseek:
      api-key: your-deepseek-api-key

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

### 4.4 创建系统服务

```bash
# 创建systemd服务文件
vim /etc/systemd/system/chess-rounds.service
```

```ini
[Unit]
Description=Chess Rounds Spring Boot Application
After=network.target mysql.service
Requires=mysql.service

[Service]
Type=simple
User=root
Group=root
WorkingDirectory=/home/junjian/data/emotion/chess-rounds/backend
ExecStart=/usr/bin/java -jar \
  -Xms512m -Xmx1024m \
  -Dspring.profiles.active=prod \
  -Dspring.config.additional-location=/home/junjian/data/emotion/chess-rounds/config/ \
  /home/junjian/data/emotion/chess-rounds/backend/chess-rounds-backend-1.0.0.jar
ExecStop=/bin/kill -TERM $MAINPID
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

```bash
# 重新加载systemd并启动服务
systemctl daemon-reload
systemctl enable chess-rounds
systemctl start chess-rounds

# 检查服务状态
systemctl status chess-rounds
```

## 第五步：数据库初始化

### 5.1 运行数据库迁移

```bash
# 如果使用Flyway，迁移会在应用启动时自动执行
# 也可以手动执行SQL脚本

# 检查应用日志确认迁移成功
journalctl -u chess-rounds -f
```

### 5.2 插入初始数据（可选）

```sql
-- 连接数据库
mysql -u chess_app -p chess_rounds

-- 插入系统配置
INSERT INTO system_configs (config_key, config_value, config_type, description) VALUES
('max_participants_per_round', '4', 'number', '每回合最大参与人数'),
('default_multiplier', '1.00', 'number', '默认倍率'),
('ai_analysis_enabled', 'true', 'boolean', '是否启用AI分析'),
('round_code_length', '8', 'number', '回合码长度'),
('spectator_rating_enabled', 'true', 'boolean', '是否启用旁观者评价功能');
```

## 第六步：前端部署

### 6.1 构建UniApp项目

在本地开发环境：

```bash
# 安装依赖
npm install

# 构建微信小程序
npm run build:mp-weixin

# 打包生成的文件在 dist/build/mp-weixin 目录
```

### 6.2 上传到微信开发者工具

1. 打开微信开发者工具
2. 导入项目，选择 `dist/build/mp-weixin` 目录
3. 配置小程序AppID
4. 修改API接口地址为生产环境地址
5. 上传代码并提交审核

### 6.3 配置小程序域名

在微信小程序管理后台配置服务器域名：

```
request合法域名：
https://api.airoubo.com
https://mp.airoubo.com

uploadFile合法域名：
https://api.airoubo.com

downloadFile合法域名：
https://api.airoubo.com
```

## 第七步：监控和日志

### 7.1 配置日志轮转

```bash
# 创建logrotate配置
vim /etc/logrotate.d/chess-rounds
```

```
/opt/chess-rounds/logs/*.log {
    daily
    missingok
    rotate 30
    compress
    delaycompress
    notifempty
    copytruncate
    su root root
}
```

### 7.2 设置监控脚本

```bash
# 创建健康检查脚本
vim /opt/chess-rounds/scripts/health-check.sh
```

```bash
#!/bin/bash

# 健康检查脚本
HEALTH_URL="https://api.airoubo.com/actuator/health"
LOG_FILE="/opt/chess-rounds/logs/health-check.log"

# 检查应用健康状态
response=$(curl -s -o /dev/null -w "%{http_code}" $HEALTH_URL)

if [ $response -eq 200 ]; then
    echo "$(date): Application is healthy" >> $LOG_FILE
else
    echo "$(date): Application health check failed (HTTP $response)" >> $LOG_FILE
    # 可以添加告警逻辑，如发送邮件或短信
    systemctl restart chess-rounds
fi
```

```bash
# 设置执行权限
chmod +x /opt/chess-rounds/scripts/health-check.sh

# 添加定时任务
crontab -e
# 每5分钟检查一次
*/5 * * * * /opt/chess-rounds/scripts/health-check.sh
```

### 7.3 配置备份脚本

```bash
# 创建备份脚本
vim /opt/chess-rounds/scripts/backup.sh
```

```bash
#!/bin/bash

# 数据库备份脚本
DATE=$(date +"%Y%m%d_%H%M%S")
BACKUP_DIR="/opt/chess-rounds/backup"
DB_NAME="chess_rounds"
DB_USER="chess_app"
DB_PASS="your_strong_password_here"

# 创建备份目录
mkdir -p $BACKUP_DIR

# 数据库备份
mysqldump -u$DB_USER -p$DB_PASS $DB_NAME > $BACKUP_DIR/db_backup_$DATE.sql

# 压缩备份文件
gzip $BACKUP_DIR/db_backup_$DATE.sql

# 应用文件备份
tar -czf $BACKUP_DIR/app_backup_$DATE.tar.gz /opt/chess-rounds/backend/*.jar

# 删除7天前的备份
find $BACKUP_DIR -name "*backup_*.gz" -mtime +7 -delete
find $BACKUP_DIR -name "*backup_*.tar.gz" -mtime +7 -delete

echo "$(date): Backup completed - db_backup_$DATE.sql.gz, app_backup_$DATE.tar.gz"
```

```bash
# 设置执行权限
chmod +x /opt/chess-rounds/scripts/backup.sh

# 添加定时备份（每天凌晨3点）
crontab -e
0 3 * * * /opt/chess-rounds/scripts/backup.sh >> /opt/chess-rounds/logs/backup.log 2>&1
```

## 第八步：安全配置

### 8.1 配置防火墙规则

```bash
# 查看当前规则
ufw status

# 限制SSH访问（可选，建议指定IP）
# ufw delete allow ssh
# ufw allow from your-office-ip to any port 22

# 限制MySQL访问
ufw delete allow 3306
ufw allow from 127.0.0.1 to any port 3306
```

### 8.2 安装fail2ban

```bash
# 安装fail2ban
apt install -y fail2ban

# 配置fail2ban
vim /etc/fail2ban/jail.local
```

```ini
[DEFAULT]
bantime = 3600
findtime = 600
maxretry = 3

[sshd]
enabled = true
port = ssh
logpath = /var/log/auth.log
maxretry = 3

[nginx-http-auth]
enabled = true
filter = nginx-http-auth
logpath = /var/log/nginx/error.log
maxretry = 3
```

```bash
# 启动fail2ban
systemctl enable fail2ban
systemctl start fail2ban
```

## 第九步：性能优化

### 9.1 JVM调优

编辑服务文件，优化JVM参数：

```bash
vim /etc/systemd/system/chess-rounds.service
```

```ini
ExecStart=/usr/bin/java -jar \
  -Xms512m -Xmx1024m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/opt/chess-rounds/logs/ \
  -Dspring.profiles.active=prod \
  -Dspring.config.additional-location=/opt/chess-rounds/config/ \
  /opt/chess-rounds/backend/chess-rounds-backend-1.0.0.jar
```

### 9.2 MySQL优化

```bash
# 编辑MySQL配置
vim /etc/mysql/mysql.conf.d/mysqld.cnf
```

```ini
[mysqld]
# 基础配置
max_connections = 200
innodb_buffer_pool_size = 256M
innodb_log_file_size = 64M
innodb_flush_log_at_trx_commit = 2

# 查询缓存
query_cache_type = 1
query_cache_size = 32M

# 慢查询日志
slow_query_log = 1
slow_query_log_file = /var/log/mysql/slow.log
long_query_time = 2
```

### 9.3 Nginx优化

```bash
vim /etc/nginx/nginx.conf
```

```nginx
worker_processes auto;
worker_connections 1024;

# 启用gzip压缩
gzip on;
gzip_vary on;
gzip_min_length 1024;
gzip_types text/plain text/css application/json application/javascript text/xml application/xml;

# 缓存配置
location ~* \.(jpg|jpeg|png|gif|ico|css|js)$ {
    expires 1y;
    add_header Cache-Control "public, immutable";
}
```

## 第十步：验证部署

### 10.1 检查服务状态

```bash
# 检查所有服务
systemctl status nginx
systemctl status mysql
systemctl status chess-rounds

# 检查端口监听
netstat -tlnp | grep -E ':(80|443|3306|8080)'

# 检查应用日志
journalctl -u chess-rounds -n 50
tail -f /home/junjian/data/emotion/chess-rounds/logs/chess-rounds.log
```

### 10.2 API测试

```bash
# 测试健康检查
curl https://api.airoubo.com/actuator/health

# 测试API接口
curl -X GET https://api.airoubo.com/api/test
```

### 10.3 SSL证书验证

```bash
# 检查SSL证书
openssl s_client -connect api.airoubo.com:443 -servername api.airoubo.com

# 在线SSL检查
# https://www.ssllabs.com/ssltest/
```

## 故障排除

### 常见问题

1. **应用启动失败**
   ```bash
   # 查看详细日志
   journalctl -u chess-rounds -f
   # 检查配置文件
   # 检查数据库连接
   ```

2. **SSL证书问题**
   ```bash
   # 重新申请证书
   certbot renew --force-renewal
   systemctl reload nginx
   ```

3. **数据库连接问题**
   ```bash
   # 检查MySQL状态
   systemctl status mysql
   # 测试连接
   mysql -u chess_app -p -h localhost chess_rounds
   ```

4. **Nginx配置问题**
   ```bash
   # 测试配置
   nginx -t
   # 重新加载
   systemctl reload nginx
   ```

5. **文件权限问题（403 Forbidden或Permission denied）**
   ```bash
   # 检查nginx用户
   ps aux | grep nginx
   # 通常nginx运行用户是www-data
   
   # 检查文件权限
   ls -la /home/junjian/data/emotion/chess-rounds/uploads/
   
   # 修复权限问题 - 将uploads目录所有者改为nginx用户
   chown -R www-data:www-data /home/junjian/data/emotion/chess-rounds/uploads
   chmod -R 755 /home/junjian/data/emotion/chess-rounds/uploads
   
   # 确保nginx用户可以访问父目录路径（重要！）
   # nginx需要对整个路径都有执行权限
   chmod 755 /home/junjian
   chmod 755 /home/junjian/data
   chmod 755 /home/junjian/data/emotion
   chmod 755 /home/junjian/data/emotion/chess-rounds
   
   # 如果还有问题，可以临时给更宽松的权限测试
   # chmod 755 /home/junjian/data/emotion/chess-rounds/uploads
   # chmod 644 /home/junjian/data/emotion/chess-rounds/uploads/*
   
   # 重启nginx
   systemctl restart nginx
   
   # 测试访问
   curl -I https://api.airoubo.com/static/avatars/default.png
   ```

## 维护计划

### 日常维护

- 每日检查应用日志
- 每周检查系统资源使用情况
- 每月更新系统安全补丁
- 每季度检查备份完整性

### 更新部署

```bash
# 应用更新流程
# 1. 备份当前版本
cp /home/junjian/data/emotion/chess-rounds/backend/chess-rounds-backend-1.0.0.jar /home/junjian/data/emotion/chess-rounds/backup/

# 2. 停止应用
systemctl stop chess-rounds

# 3. 部署新版本
cp new-version.jar /home/junjian/data/emotion/chess-rounds/backend/chess-rounds-backend-1.0.0.jar

# 4. 启动应用
systemctl start chess-rounds

# 5. 验证部署
curl https://api.airoubo.com/actuator/health
```

这个部署指南提供了完整的生产环境部署流程，包括：
- 详细的服务器配置步骤
- 安全配置和优化建议
- 监控和备份策略
- 故障排除指南
- 维护计划

按照这个指南，可以成功将项目部署到生产环境并确保稳定运行。