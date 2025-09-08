# 云服务器环境配置指南

## 服务器基本信息
- **系统**: Ubuntu
- **用户**: root
- **域名**: airoubo.com
- **云服务商**: 腾讯云

## 1. 系统基础配置

### 1.1 更新系统包
```bash
# 更新包列表
apt update

# 升级系统包
apt upgrade -y

# 安装基础工具
apt install -y curl wget vim git unzip software-properties-common
```

### 1.2 配置时区
```bash
# 设置时区为中国标准时间
timedatectl set-timezone Asia/Shanghai

# 验证时区设置
date
```

### 1.3 配置防火墙
```bash
# 启用UFW防火墙
ufw enable

# 允许SSH连接
ufw allow ssh

# 允许HTTP和HTTPS
ufw allow 80
ufw allow 443

# 允许MySQL远程连接（可选，建议限制IP）
ufw allow 3306

# 查看防火墙状态
ufw status
```

## 2. Java环境安装

### 2.1 安装OpenJDK 17
```bash
# 安装OpenJDK 17
apt install -y openjdk-17-jdk

# 验证安装
java -version
javac -version

# 配置JAVA_HOME环境变量
echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' >> /etc/environment
echo 'export PATH=$PATH:$JAVA_HOME/bin' >> /etc/environment

# 重新加载环境变量
source /etc/environment

# 验证JAVA_HOME
echo $JAVA_HOME
```

## 3. MySQL数据库安装和配置

### 3.1 安装MySQL
```bash
# 安装MySQL服务器
apt install -y mysql-server

# 启动MySQL服务
systemctl start mysql
systemctl enable mysql

# 检查服务状态
systemctl status mysql
```

### 3.2 MySQL安全配置
```bash
# 运行安全配置脚本
mysql_secure_installation

# 配置选项说明：
# - 设置root密码：选择强密码
# - 移除匿名用户：Y
# - 禁止root远程登录：N（我们需要远程访问）
# - 移除test数据库：Y
# - 重新加载权限表：Y
```

### 3.3 配置MySQL远程访问
```bash
# 编辑MySQL配置文件
vim /etc/mysql/mysql.conf.d/mysqld.cnf

# 找到bind-address行，修改为：
# bind-address = 0.0.0.0

# 重启MySQL服务
systemctl restart mysql
```

### 3.4 创建数据库和用户
```sql
-- 登录MySQL
mysql -u root -p

-- 创建项目数据库
CREATE DATABASE chess_rounds CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建应用用户
CREATE USER 'chess_app'@'%' IDENTIFIED BY 'your_strong_password_here';

-- 授予权限
GRANT ALL PRIVILEGES ON chess_rounds.* TO 'chess_app'@'%';

-- 刷新权限
FLUSH PRIVILEGES;

-- 退出MySQL
EXIT;
```

## 4. Nginx安装和配置

### 4.1 安装Nginx
```bash
# 安装Nginx
apt install -y nginx

# 启动Nginx服务
systemctl start nginx
systemctl enable nginx

# 检查服务状态
systemctl status nginx
```

### 4.2 配置Nginx基础设置
```bash
# 创建项目配置目录
mkdir -p /etc/nginx/sites-available
mkdir -p /etc/nginx/sites-enabled

# 删除默认配置
rm -f /etc/nginx/sites-enabled/default
```

### 4.3 创建项目Nginx配置
```bash
# 创建项目配置文件
vim /etc/nginx/sites-available/chess-rounds
```

配置文件内容：
```nginx
server {
    listen 80;
    server_name airoubo.com www.airoubo.com api.airoubo.com mp.airoubo.com;
    
    # 重定向到HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name api.airoubo.com;
    
    # SSL证书配置（稍后配置）
    # ssl_certificate /etc/letsencrypt/live/airoubo.com/fullchain.pem;
    # ssl_certificate_key /etc/letsencrypt/live/airoubo.com/privkey.pem;
    
    # API代理配置
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

server {
    listen 443 ssl http2;
    server_name mp.airoubo.com;
    
    # SSL证书配置（稍后配置）
    # ssl_certificate /etc/letsencrypt/live/airoubo.com/fullchain.pem;
    # ssl_certificate_key /etc/letsencrypt/live/airoubo.com/privkey.pem;
    
    # 小程序后端代理配置
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

```bash
# 启用配置
ln -s /etc/nginx/sites-available/chess-rounds /etc/nginx/sites-enabled/

# 测试配置
nginx -t

# 重新加载Nginx
systemctl reload nginx
```

## 5. SSL证书配置（Let's Encrypt）

### 5.1 安装Certbot
```bash
# 安装snapd
apt install -y snapd

# 安装certbot
snap install --classic certbot

# 创建符号链接
ln -s /snap/bin/certbot /usr/bin/certbot
```

### 5.2 申请SSL证书
```bash
# 停止Nginx（临时）
systemctl stop nginx

# 申请证书
certbot certonly --standalone -d airoubo.com -d www.airoubo.com -d api.airoubo.com -d mp.airoubo.com

# 启动Nginx
systemctl start nginx
```

### 5.3 配置SSL证书自动续期
```bash
# 测试自动续期
certbot renew --dry-run

# 添加自动续期任务
crontab -e

# 添加以下行（每天凌晨2点检查续期）
0 2 * * * /usr/bin/certbot renew --quiet --post-hook "systemctl reload nginx"
```

### 5.4 更新Nginx配置启用SSL
```bash
# 编辑Nginx配置文件
vim /etc/nginx/sites-available/chess-rounds

# 取消SSL相关行的注释
# 重新加载Nginx
nginx -t
systemctl reload nginx
```

## 6. 创建应用目录结构

```bash
# 创建应用根目录
mkdir -p /opt/chess-rounds

# 创建子目录
mkdir -p /opt/chess-rounds/backend
mkdir -p /opt/chess-rounds/logs
mkdir -p /opt/chess-rounds/config
mkdir -p /opt/chess-rounds/backup

# 设置权限
chown -R root:root /opt/chess-rounds
chmod -R 755 /opt/chess-rounds
```

## 7. 系统服务配置

### 7.1 创建应用服务文件
```bash
vim /etc/systemd/system/chess-rounds.service
```

服务文件内容：
```ini
[Unit]
Description=Chess Rounds Spring Boot Application
After=network.target mysql.service

[Service]
Type=forking
User=root
Group=root
WorkingDirectory=/opt/chess-rounds/backend
ExecStart=/usr/bin/java -jar /opt/chess-rounds/backend/chess-rounds.jar
ExecStop=/bin/kill -TERM $MAINPID
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
# 重新加载systemd
systemctl daemon-reload

# 启用服务（暂时不启动，等应用部署后再启动）
systemctl enable chess-rounds
```

## 8. 日志配置

### 8.1 配置日志轮转
```bash
vim /etc/logrotate.d/chess-rounds
```

日志轮转配置：
```
/opt/chess-rounds/logs/*.log {
    daily
    missingok
    rotate 30
    compress
    delaycompress
    notifempty
    copytruncate
}
```

## 9. 监控和维护

### 9.1 安装系统监控工具
```bash
# 安装htop和iotop
apt install -y htop iotop

# 安装网络工具
apt install -y net-tools
```

### 9.2 创建维护脚本
```bash
# 创建备份脚本
vim /opt/chess-rounds/backup/backup.sh
```

备份脚本内容：
```bash
#!/bin/bash

# 数据库备份
DATE=$(date +"%Y%m%d_%H%M%S")
BACKUP_DIR="/opt/chess-rounds/backup"
DB_NAME="chess_rounds"
DB_USER="chess_app"
DB_PASS="your_strong_password_here"

# 创建数据库备份
mysqldump -u$DB_USER -p$DB_PASS $DB_NAME > $BACKUP_DIR/db_backup_$DATE.sql

# 压缩备份文件
gzip $BACKUP_DIR/db_backup_$DATE.sql

# 删除7天前的备份
find $BACKUP_DIR -name "db_backup_*.sql.gz" -mtime +7 -delete

echo "Database backup completed: db_backup_$DATE.sql.gz"
```

```bash
# 设置执行权限
chmod +x /opt/chess-rounds/backup/backup.sh

# 添加定时备份任务
crontab -e

# 添加以下行（每天凌晨3点备份）
0 3 * * * /opt/chess-rounds/backup/backup.sh
```

## 10. 安全加固

### 10.1 SSH安全配置
```bash
# 编辑SSH配置
vim /etc/ssh/sshd_config

# 建议修改的配置项：
# Port 22 (可以改为其他端口)
# PermitRootLogin yes (生产环境建议改为no)
# PasswordAuthentication yes (建议使用密钥认证)

# 重启SSH服务
systemctl restart ssh
```

### 10.2 安装fail2ban防护
```bash
# 安装fail2ban
apt install -y fail2ban

# 启动服务
systemctl start fail2ban
systemctl enable fail2ban

# 查看状态
fail2ban-client status
```

## 验证清单

- [ ] 系统包已更新
- [ ] 时区已设置为Asia/Shanghai
- [ ] 防火墙已配置
- [ ] Java 17已安装并配置环境变量
- [ ] MySQL已安装并配置远程访问
- [ ] 数据库和用户已创建
- [ ] Nginx已安装并配置
- [ ] SSL证书已申请并配置
- [ ] 应用目录结构已创建
- [ ] 系统服务文件已创建
- [ ] 日志轮转已配置
- [ ] 备份脚本已创建并设置定时任务
- [ ] 基础安全配置已完成

## 下一步

完成服务器环境配置后，可以开始：
1. 开发Spring Boot后端应用
2. 部署应用到服务器
3. 配置域名解析
4. 测试所有功能

---

**注意事项**：
1. 请将所有密码替换为强密码
2. 生产环境建议创建非root用户运行应用
3. 定期更新系统和应用
4. 监控服务器资源使用情况
5. 定期检查备份文件的完整性