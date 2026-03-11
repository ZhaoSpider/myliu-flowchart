# 项目部署指南

> 本文档详细说明如何将企业级微网站项目部署到Windows Server服务器上。适用于无部署经验的开发者，提供完整的步骤指导。

## 目录

- [一、部署架构](#一部署架构)
- [二、服务器准备](#二服务器准备)
- [三、安装Docker环境](#三安装docker环境)
- [四、项目构建](#四项目构建)
- [五、Docker部署](#五docker部署)
- [六、Nginx配置](#六nginx配置)
- [七、验证部署](#七验证部署)
- [八、运维管理](#八运维管理)
- [九、常见问题](#九常见问题)

---

## 一、部署架构

### 1.1 系统架构图

```
┌─────────────────────────────────────────────────────────────────────┐
│                         用户浏览器                                    │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      Nginx (反向代理)                                 │
│                      端口: 80/443                                    │
├─────────────────────────────────────────────────────────────────────┤
│  /              → 流程图应用 (flowchart-app)                         │
│  /admin         → 管理后台 (admin-app)                               │
│  /api           → 后端服务 (backend)                                 │
└─────────────────────────────────────────────────────────────────────┘
                                    │
            ┌───────────────────────┼───────────────────────┐
            ▼                       ▼                       ▼
┌───────────────────┐   ┌───────────────────┐   ┌───────────────────┐
│   前端静态资源      │   │   Spring Boot     │   │     MinIO         │
│   (Vue3应用)       │   │   端口: 8080       │   │   端口: 9000      │
└───────────────────┘   └───────────────────┘   └───────────────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    ▼                               ▼
        ┌───────────────────┐           ┌───────────────────┐
        │      MySQL        │           │      Redis        │
        │   端口: 3306      │           │   端口: 6379      │
        │   数据持久化       │           │   会话缓存         │
        └───────────────────┘           └───────────────────┘
```

### 1.2 容器规划

| 服务 | 容器名 | 端口 | 说明 |
|-----|-------|------|------|
| Nginx | myliu-nginx | 80, 443 | 反向代理、静态资源 |
| Backend | myliu-backend | 8080 | Spring Boot后端服务 |
| MySQL | myliu-mysql | 3306 | 数据库 |
| Redis | myliu-redis | 6379 | 缓存服务 |
| MinIO | myliu-minio | 9000, 9001 | 文件存储 |

---

## 二、服务器准备

### 2.1 服务器要求

| 配置项 | 最低要求 | 推荐配置 |
|-------|---------|---------|
| CPU | 2核 | 4核+ |
| 内存 | 4GB | 8GB+ |
| 磁盘 | 50GB | 100GB+ SSD |
| 操作系统 | Windows Server 2019+ | Windows Server 2022 |
| 网络 | 公网IP | 域名 + SSL证书 |

### 2.2 开放端口

确保服务器防火墙开放以下端口：

| 端口 | 用途 |
|-----|------|
| 80 | HTTP访问 |
| 443 | HTTPS访问 |
| 3306 | MySQL（可选，仅开发调试时开放） |
| 8080 | 后端服务（可选） |

**Windows防火墙配置**：

```powershell
# 开放80端口
netsh advfirewall firewall add rule name="HTTP" dir=in action=allow protocol=tcp localport=80

# 开放443端口
netsh advfirewall firewall add rule name="HTTPS" dir=in action=allow protocol=tcp localport=443

# 开放8080端口（可选）
netsh advfirewall firewall add rule name="Backend" dir=in action=allow protocol=tcp localport=8080
```

### 2.3 创建项目目录

```powershell
# 创建项目根目录
New-Item -ItemType Directory -Path "D:\myliu" -Force

# 创建数据存储目录
$directories = @(
    "D:\myliu\data\mysql",
    "D:\myliu\data\redis",
    "D:\myliu\data\minio",
    "D:\myliu\data\nginx\logs",
    "D:\myliu\config\nginx",
    "D:\myliu\backup"
)

foreach ($dir in $directories) {
    New-Item -ItemType Directory -Path $dir -Force
}
```

---

## 三、安装Docker环境

### 3.1 安装WSL2

```powershell
# 以管理员身份运行PowerShell

# 启用WSL
wsl --install

# 如果已安装，更新WSL
wsl --update

# 设置WSL2为默认版本
wsl --set-default-version 2
```

### 3.2 安装Docker Desktop

1. 下载Docker Desktop：https://www.docker.com/products/docker-desktop
2. 运行安装程序，选择"Use WSL 2 instead of Hyper-V"
3. 安装完成后重启服务器
4. 启动Docker Desktop并完成初始化

### 3.3 配置Docker

编辑 `C:\Users\Administrator\.docker\daemon.json`：

```json
{
  "data-root": "D:\\DockerData",
  "registry-mirrors": [
    "https://docker.m.daocloud.io",
    "https://mirror.ccs.tencentyun.com"
  ],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  }
}
```

### 3.4 验证安装

```cmd
docker --version
docker-compose --version
docker run hello-world
```

---

## 四、项目构建

### 4.1 获取项目代码

**方式一：从开发机上传**

使用远程桌面或FTP工具将整个项目目录上传到服务器 `D:\myliu`

**方式二：从Git仓库拉取**

```cmd
cd D:\myliu
git clone https://your-repo-url.git .
```

### 4.2 构建前端项目

在开发机上执行（需要有Node.js环境）：

```cmd
# 进入流程图应用目录
cd frontend/flowchart-app

# 安装依赖
npm install

# 构建生产版本
npm run build

# 构建产物在 dist/ 目录
```

```cmd
# 进入管理后台目录
cd frontend/admin-app

# 安装依赖
npm install

# 构建生产版本
npm run build

# 构建产物在 dist/ 目录
```

### 4.3 构建后端项目

在开发机上执行（需要有Java和Maven环境）：

```cmd
# 进入后端项目目录
cd backend/flowchart-server

# 构建JAR包（跳过测试）
mvn clean package -DskipTests

# 构建产物在 target/ 目录
# 文件名如：flowchart-server-1.0.0.jar
```

### 4.4 上传构建产物

将以下文件上传到服务器：

```
D:\myliu\
├── frontend\
│   ├── flowchart-app\dist\     # 流程图应用构建产物
│   └── admin-app\dist\         # 管理后台构建产物
├── backend\
│   └── flowchart-server\target\flowchart-server-1.0.0.jar
├── docker\
│   ├── docker-compose.yml
│   ├── Dockerfile
│   └── nginx\
│       └── nginx.conf
└── config\
    └── nginx\
        └── default.conf
```

---

## 五、Docker部署

### 5.1 创建后端Dockerfile

在 `D:\myliu\docker\` 目录创建 `Dockerfile`：

```dockerfile
# 后端服务Dockerfile
FROM eclipse-temurin:17-jre-alpine

# 设置工作目录
WORKDIR /app

# 设置时区
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

# 复制JAR包
COPY ../backend/flowchart-server/target/flowchart-server-1.0.0.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-Xms512m", "-Xmx1024m", "-jar", "app.jar"]
```

### 5.2 创建docker-compose.yml

在 `D:\myliu\docker\` 目录创建 `docker-compose.yml`：

```yaml
version: '3.8'

services:
  # MySQL数据库
  mysql:
    image: mysql:8.0
    container_name: myliu-mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: xiong3410313
      MYSQL_DATABASE: myliu_flowchart
      TZ: Asia/Shanghai
    ports:
      - "3306:3306"
    volumes:
      - D:/myliu/data/mysql:/var/lib/mysql
      - ./mysql/init.sql:/docker-entrypoint-initdb.d/init.sql
    command:
      - --character-set-server=utf8mb4
      - --collation-server=utf8mb4_unicode_ci
      - --default-authentication-plugin=mysql_native_password
    networks:
      - myliu-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Redis缓存
  redis:
    image: redis:7-alpine
    container_name: myliu-redis
    restart: unless-stopped
    ports:
      - "6379:6379"
    volumes:
      - D:/myliu/data/redis:/data
    command: redis-server --appendonly yes
    networks:
      - myliu-network
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

  # MinIO文件存储
  minio:
    image: minio/minio:latest
    container_name: myliu-minio
    restart: unless-stopped
    environment:
      MINIO_ROOT_USER: admin
      MINIO_ROOT_PASSWORD: admin123456
    ports:
      - "9000:9000"
      - "9001:9001"
    volumes:
      - D:/myliu/data/minio:/data
    command: server /data --console-address ":9001"
    networks:
      - myliu-network

  # 后端服务
  backend:
    build:
      context: ..
      dockerfile: docker/Dockerfile
    container_name: myliu-backend
    restart: unless-stopped
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/myliu_flowchart?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: xiong3410313
      SPRING_REDIS_HOST: redis
      SPRING_REDIS_PORT: 6379
      MINIO_ENDPOINT: http://minio:9000
      MINIO_ACCESS_KEY: admin
      MINIO_SECRET_KEY: admin123456
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_healthy
    networks:
      - myliu-network

  # Nginx反向代理
  nginx:
    image: nginx:alpine
    container_name: myliu-nginx
    restart: unless-stopped
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ../config/nginx/nginx.conf:/etc/nginx/nginx.conf
      - ../config/nginx/conf.d:/etc/nginx/conf.d
      - ../frontend/flowchart-app/dist:/usr/share/nginx/html/flowchart
      - ../frontend/admin-app/dist:/usr/share/nginx/html/admin
      - D:/myliu/data/nginx/logs:/var/log/nginx
    depends_on:
      - backend
    networks:
      - myliu-network

networks:
  myliu-network:
    driver: bridge
```

### 5.3 启动服务

```cmd
# 进入docker目录
cd D:\myliu\docker

# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

---

## 六、Nginx配置

### 6.1 创建nginx.conf

在 `D:\myliu\config\nginx\` 目录创建 `nginx.conf`：

```nginx
user  nginx;
worker_processes  auto;

error_log  /var/log/nginx/error.log notice;
pid        /var/run/nginx.pid;

events {
    worker_connections  1024;
}

http {
    include       /etc/nginx/mime.types;
    default_type  application/octet-stream;

    log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
                      '$status $body_bytes_sent "$http_referer" '
                      '"$http_user_agent" "$http_x_forwarded_for"';

    access_log  /var/log/nginx/access.log  main;

    sendfile        on;
    tcp_nopush      on;
    tcp_nodelay     on;
    keepalive_timeout  65;
    types_hash_max_size 2048;

    # Gzip压缩
    gzip  on;
    gzip_vary on;
    gzip_min_length 1k;
    gzip_types text/plain text/css text/javascript application/javascript application/json application/xml;

    # 包含其他配置
    include /etc/nginx/conf.d/*.conf;
}
```

### 6.2 创建default.conf

在 `D:\myliu\config\nginx\conf.d\` 目录创建 `default.conf`：

```nginx
# 后端服务上游
upstream backend {
    server backend:8080;
}

# HTTP服务
server {
    listen       80;
    server_name  localhost;

    # 安全头
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;

    # 流程图应用
    location / {
        root   /usr/share/nginx/html/flowchart;
        index  index.html;
        try_files $uri $uri/ /index.html;
    }

    # 管理后台
    location /admin {
        alias   /usr/share/nginx/html/admin;
        index  index.html;
        try_files $uri $uri/ /admin/index.html;
    }

    # API代理
    location /api {
        proxy_pass http://backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # WebSocket支持
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 30d;
        add_header Cache-Control "public, immutable";
    }

    # 健康检查
    location /health {
        access_log off;
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }
}
```

### 6.3 HTTPS配置（可选）

如果有SSL证书，创建 `D:\myliu\config\nginx\conf.d\ssl.conf`：

```nginx
server {
    listen       443 ssl;
    server_name  your-domain.com;

    ssl_certificate      /etc/nginx/ssl/cert.pem;
    ssl_certificate_key  /etc/nginx/ssl/key.pem;

    ssl_session_cache    shared:SSL:1m;
    ssl_session_timeout  5m;

    ssl_ciphers  HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers  on;

    # 其他配置同上...
}

# HTTP重定向到HTTPS
server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}
```

---

## 七、验证部署

### 7.1 检查服务状态

```cmd
# 查看所有容器状态
docker-compose ps

# 应该看到所有服务状态为 "Up" 或 "healthy"
```

### 7.2 检查服务健康

```cmd
# 检查MySQL
docker exec myliu-mysql mysqladmin -uroot -pxiong3410313 ping

# 检查Redis
docker exec myliu-redis redis-cli ping

# 检查后端
curl http://localhost:8080/actuator/health

# 检查Nginx
curl http://localhost/health
```

### 7.3 访问测试

在浏览器中访问：

| 地址 | 预期结果 |
|-----|---------|
| http://服务器IP | 流程图应用首页 |
| http://服务器IP/admin | 管理后台登录页 |
| http://服务器IP/api/doc.html | API文档页面 |

---

## 八、运维管理

### 8.1 日常运维命令

```cmd
# 查看日志
docker-compose logs -f [服务名]

# 重启单个服务
docker-compose restart backend

# 重启所有服务
docker-compose restart

# 更新服务
docker-compose pull
docker-compose up -d

# 查看资源使用
docker stats
```

### 8.2 数据备份

创建备份脚本 `D:\myliu\backup\backup.ps1`：

```powershell
# 备份脚本
$backupDir = "D:\myliu\backup"
$date = Get-Date -Format "yyyyMMdd_HHmmss"

# 备份MySQL
docker exec myliu-mysql mysqldump -uroot -pxiong3410313 --all-databases > "$backupDir\mysql_$date.sql"

# 备份配置文件
Copy-Item -Path "D:\myliu\config" -Destination "$backupDir\config_$date" -Recurse

# 压缩备份
Compress-Archive -Path "$backupDir\mysql_$date.sql", "$backupDir\config_$date" -DestinationPath "$backupDir\backup_$date.zip"

# 清理临时文件
Remove-Item "$backupDir\mysql_$date.sql"
Remove-Item "$backupDir\config_$date" -Recurse

# 删除30天前的备份
Get-ChildItem $backupDir -Filter "backup_*.zip" | Where-Object { $_.LastWriteTime -lt (Get-Date).AddDays(-30) } | Remove-Item -Force

Write-Host "备份完成: backup_$date.zip"
```

### 8.3 设置定时备份

使用Windows任务计划程序：

```powershell
# 创建定时任务（每天凌晨2点执行）
$action = New-ScheduledTaskAction -Execute "PowerShell.exe" -Argument "-File D:\myliu\backup\backup.ps1"
$trigger = New-ScheduledTaskTrigger -Daily -At 2am
Register-ScheduledTask -TaskName "MyLiuBackup" -Action $action -Trigger $trigger -User "SYSTEM"
```

### 8.4 监控告警

建议使用以下工具进行监控：
- **Portainer**：Docker可视化管理
- **Grafana + Prometheus**：性能监控
- **Uptime Kuma**：服务可用性监控

---

## 九、常见问题

### Q1: 服务启动失败？

**排查步骤**：
```cmd
# 查看容器日志
docker-compose logs [服务名]

# 检查容器状态
docker-compose ps

# 检查端口占用
netstat -ano | findstr 8080
```

### Q2: 无法访问服务？

**检查清单**：
1. 防火墙端口是否开放
2. Docker容器是否正常运行
3. Nginx配置是否正确
4. 后端服务是否正常启动

### Q3: 数据库连接失败？

**解决方案**：
```cmd
# 检查MySQL容器状态
docker exec myliu-mysql mysql -uroot -pxiong3410313 -e "SELECT 1"

# 检查网络连接
docker exec myliu-backend ping mysql

# 检查数据库是否存在
docker exec myliu-mysql mysql -uroot -pxiong3410313 -e "SHOW DATABASES"
```

### Q4: 前端页面空白？

**可能原因**：
1. 构建产物未正确上传
2. Nginx配置路径错误
3. API接口地址配置错误

**解决方案**：
```cmd
# 检查Nginx日志
docker logs myliu-nginx

# 进入Nginx容器检查文件
docker exec -it myliu-nginx ls /usr/share/nginx/html/flowchart
```

### Q5: 如何更新服务？

```cmd
# 拉取最新代码
git pull

# 重新构建前端
cd frontend/flowchart-app && npm run build

# 重新构建后端
cd backend/flowchart-server && mvn clean package -DskipTests

# 重新部署
docker-compose down
docker-compose up -d --build
```

---

## 附录：部署检查清单

| 检查项 | 状态 |
|-------|------|
| 服务器防火墙端口开放 | □ |
| Docker Desktop安装完成 | □ |
| 项目代码上传完成 | □ |
| 前端构建产物上传 | □ |
| 后端JAR包上传 | □ |
| 数据库初始化脚本执行 | □ |
| docker-compose启动成功 | □ |
| Nginx配置正确 | □ |
| 所有服务健康检查通过 | □ |
| 浏览器访问测试通过 | □ |
| 备份脚本配置完成 | □ |

---

## 下一步

部署完成后，请继续阅读：
- [Docker管理指南](./docker_management.md) - 日常运维操作
- [开发问题记录](./development_issues.md) - 记录遇到的问题
