# Spring Boot后端项目结构

## 项目目录结构

```
chess-rounds-backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── airoubo/
│   │   │           └── chessrounds/
│   │   │               ├── ChessRoundsApplication.java
│   │   │               ├── config/
│   │   │               │   ├── WebConfig.java
│   │   │               │   ├── SecurityConfig.java
│   │   │               │   ├── SwaggerConfig.java
│   │   │               │   └── RedisConfig.java
│   │   │               ├── controller/
│   │   │               │   ├── AuthController.java
│   │   │               │   ├── UserController.java
│   │   │               │   ├── RoundController.java
│   │   │               │   ├── RecordController.java
│   │   │               │   ├── StatsController.java
│   │   │               │   └── RatingController.java
│   │   │               ├── service/
│   │   │               │   ├── AuthService.java
│   │   │               │   ├── UserService.java
│   │   │               │   ├── RoundService.java
│   │   │               │   ├── RecordService.java
│   │   │               │   ├── StatsService.java
│   │   │               │   ├── RatingService.java
│   │   │               │   ├── WechatService.java
│   │   │               │   └── AiAnalysisService.java
│   │   │               ├── repository/
│   │   │               │   ├── UserRepository.java
│   │   │               │   ├── RoundRepository.java
│   │   │               │   ├── ParticipantRepository.java
│   │   │               │   ├── RecordRepository.java
│   │   │               │   └── RatingRepository.java
│   │   │               ├── entity/
│   │   │               │   ├── User.java
│   │   │               │   ├── Round.java
│   │   │               │   ├── Participant.java
│   │   │               │   ├── Record.java
│   │   │               │   └── Rating.java
│   │   │               ├── dto/
│   │   │               │   ├── request/
│   │   │               │   │   ├── LoginRequest.java
│   │   │               │   │   ├── CreateRoundRequest.java
│   │   │               │   │   ├── JoinRoundRequest.java
│   │   │               │   │   ├── AddRecordRequest.java
│   │   │               │   │   └── AddRatingRequest.java
│   │   │               │   └── response/
│   │   │               │       ├── LoginResponse.java
│   │   │               │       ├── UserResponse.java
│   │   │               │       ├── RoundResponse.java
│   │   │               │       ├── RecordResponse.java
│   │   │               │       ├── StatsResponse.java
│   │   │               │       └── ApiResponse.java
│   │   │               ├── enums/
│   │   │               │   ├── RoundStatus.java
│   │   │               │   ├── ParticipantRole.java
│   │   │               │   ├── RecordType.java
│   │   │               │   └── RatingType.java
│   │   │               ├── exception/
│   │   │               │   ├── GlobalExceptionHandler.java
│   │   │               │   ├── BusinessException.java
│   │   │               │   └── ErrorCode.java
│   │   │               └── util/
│   │   │                   ├── JwtUtil.java
│   │   │                   ├── WechatUtil.java
│   │   │                   ├── CodeGenerator.java
│   │   │                   └── DateUtil.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       ├── logback-spring.xml
│   │       └── db/
│   │           └── migration/
│   │               ├── V1__Create_users_table.sql
│   │               ├── V2__Create_rounds_table.sql
│   │               ├── V3__Create_participants_table.sql
│   │               ├── V4__Create_records_table.sql
│   │               └── V5__Create_ratings_table.sql
│   └── test/
│       └── java/
│           └── com/
│               └── airoubo/
│                   └── chessrounds/
│                       ├── ChessRoundsApplicationTests.java
│                       ├── controller/
│                       ├── service/
│                       └── repository/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
└── README.md
```

## Maven依赖配置 (pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    
    <groupId>com.airoubo</groupId>
    <artifactId>chess-rounds-backend</artifactId>
    <version>1.0.0</version>
    <name>chess-rounds-backend</name>
    <description>回合计数记录系统后端</description>
    
    <properties>
        <java.version>17</java.version>
        <mysql.version>8.0.33</mysql.version>
        <jwt.version>0.11.5</jwt.version>
        <fastjson.version>2.0.43</fastjson.version>
        <hutool.version>5.8.22</hutool.version>
        <springdoc.version>2.2.0</springdoc.version>
        <flyway.version>9.22.3</flyway.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        
        <!-- Database -->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>${mysql.version}</version>
        </dependency>
        
        <!-- Database Migration -->
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
            <version>${flyway.version}</version>
        </dependency>
        
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-mysql</artifactId>
            <version>${flyway.version}</version>
        </dependency>
        
        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jwt.version}</version>
        </dependency>
        
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        
        <!-- JSON Processing -->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>fastjson2</artifactId>
            <version>${fastjson.version}</version>
        </dependency>
        
        <!-- Utilities -->
        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
            <version>${hutool.version}</version>
        </dependency>
        
        <!-- HTTP Client -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
        </dependency>
        
        <!-- API Documentation -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>${springdoc.version}</version>
        </dependency>
        
        <!-- Development Tools -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        
        <!-- Test Dependencies -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>mysql</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
            
            <plugin>
                <groupId>org.flywaydb</groupId>
                <artifactId>flyway-maven-plugin</artifactId>
                <version>${flyway.version}</version>
                <configuration>
                    <url>jdbc:mysql://localhost:3306/chess_rounds</url>
                    <user>chess_app</user>
                    <password>your_password_here</password>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

## 应用配置文件

### application.yml (主配置文件)

```yaml
spring:
  profiles:
    active: dev
  application:
    name: chess-rounds-backend
  
  # 数据源配置
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  
  # JPA配置
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
  
  # Flyway配置
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
  
  # Redis配置
  data:
    redis:
      timeout: 3000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
  
  # Jackson配置
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: Asia/Shanghai
    default-property-inclusion: non_null

# 服务器配置
server:
  port: 8080
  servlet:
    context-path: /api
  compression:
    enabled: true
    mime-types: text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json

# 日志配置
logging:
  level:
    com.airoubo.chessrounds: INFO
    org.springframework.security: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

# 应用自定义配置
app:
  # JWT配置
  jwt:
    secret: your-jwt-secret-key-here-must-be-at-least-256-bits
    expiration: 604800000 # 7天
  
  # 微信小程序配置
  wechat:
    app-id: wx402b5a6e5f74462a
    app-secret: your-wechat-app-secret
    api-url: https://api.weixin.qq.com
  
  # AI分析配置
  ai:
    deepseek:
      api-key: your-deepseek-api-key
      api-url: https://api.deepseek.com
      model: deepseek-chat
      max-tokens: 1000
      timeout: 30000

# Actuator配置
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized

# API文档配置
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    tags-sorter: alpha
    operations-sorter: alpha
  info:
    title: 回合计数记录系统API
    description: 回合计数记录系统后端接口文档
    version: 1.0.0
    contact:
      name: AiRoubo
      url: https://airoubo.com
```

### application-dev.yml (开发环境配置)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/chess_rounds?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: chess_app
    password: dev_password_123
  
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
  
  jpa:
    show-sql: true

logging:
  level:
    com.airoubo.chessrounds: DEBUG
    org.springframework.web: DEBUG
  file:
    name: logs/chess-rounds-dev.log

app:
  jwt:
    secret: dev-jwt-secret-key-for-development-only-not-for-production
  wechat:
    app-id: dev-app-id
    app-secret: dev-app-secret
  ai:
    deepseek:
      api-key: dev-api-key
```

### application-prod.yml (生产环境配置)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/chess_rounds?useUnicode=true&characterEncoding=utf8&useSSL=true&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME:chess_app}
    password: ${DB_PASSWORD}
  
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      database: 0

logging:
  level:
    com.airoubo.chessrounds: INFO
    org.springframework.security: WARN
  file:
    name: /opt/chess-rounds/logs/chess-rounds.log

app:
  jwt:
    secret: ${JWT_SECRET}
  wechat:
    app-id: ${WECHAT_APP_ID}
    app-secret: ${WECHAT_APP_SECRET}
  ai:
    deepseek:
      api-key: ${DEEPSEEK_API_KEY}

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

## 日志配置 (logback-spring.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
    
    <!-- 控制台输出 -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            <charset>UTF-8</charset>
        </encoder>
    </appender>
    
    <!-- 文件输出 -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_FILE:-logs/chess-rounds.log}</file>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            <charset>UTF-8</charset>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_FILE:-logs/chess-rounds}.%d{yyyy-MM-dd}.%i.gz</fileNamePattern>
            <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>100MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
            <maxHistory>30</maxHistory>
            <totalSizeCap>3GB</totalSizeCap>
        </rollingPolicy>
    </appender>
    
    <!-- 错误日志 -->
    <appender name="ERROR_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_FILE:-logs/chess-rounds-error.log}</file>
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>ERROR</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            <charset>UTF-8</charset>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_FILE:-logs/chess-rounds-error}.%d{yyyy-MM-dd}.%i.gz</fileNamePattern>
            <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>100MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
    </appender>
    
    <!-- 开发环境配置 -->
    <springProfile name="dev">
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
            <appender-ref ref="FILE"/>
        </root>
        <logger name="com.airoubo.chessrounds" level="DEBUG"/>
    </springProfile>
    
    <!-- 生产环境配置 -->
    <springProfile name="prod">
        <root level="INFO">
            <appender-ref ref="FILE"/>
            <appender-ref ref="ERROR_FILE"/>
        </root>
        <logger name="com.airoubo.chessrounds" level="INFO"/>
    </springProfile>
</configuration>
```

## Docker配置

### Dockerfile

```dockerfile
FROM openjdk:17-jre-slim

VOLUME /tmp

COPY target/chess-rounds-backend-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app.jar"]
```

### docker-compose.yml (开发环境)

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: chess-rounds-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root123
      MYSQL_DATABASE: chess_rounds
      MYSQL_USER: chess_app
      MYSQL_PASSWORD: dev_password_123
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    command: --default-authentication-plugin=mysql_native_password
  
  redis:
    image: redis:7-alpine
    container_name: chess-rounds-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
  
  app:
    build: .
    container_name: chess-rounds-app
    depends_on:
      - mysql
      - redis
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: dev
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/chess_rounds?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
      SPRING_DATA_REDIS_HOST: redis
    volumes:
      - ./logs:/opt/chess-rounds/logs

volumes:
  mysql_data:
  redis_data:
```

## 下一步开发计划

1. **创建实体类和数据库迁移脚本**
2. **实现Repository层**
3. **实现Service层业务逻辑**
4. **实现Controller层API接口**
5. **配置安全认证**
6. **集成微信小程序登录**
7. **集成AI分析服务**
8. **编写单元测试**
9. **API文档完善**
10. **部署和运维配置**

这个项目结构提供了：
- 清晰的分层架构
- 完整的配置管理
- 数据库版本控制
- 日志管理
- 容器化部署
- API文档生成
- 测试框架
- 安全配置

所有配置都支持多环境部署，便于开发、测试和生产环境的管理。