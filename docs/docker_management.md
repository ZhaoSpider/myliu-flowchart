# Docker管理指南

> 本文档帮助您掌握Docker的基本操作，包括容器的启动、停止、数据备份等常用操作。特别针对您的MySQL容器（用户名：root，密码：xiong3410313）进行详细说明。

## 目录

- [一、Docker基础概念](#一docker基础概念)
- [二、Docker Desktop安装](#二docker-desktop安装)
- [三、常用Docker命令](#三常用docker命令)
- [四、MySQL容器管理](#四mysql容器管理)
- [五、Docker Compose使用](#五docker-compose使用)
- [六、数据备份与恢复](#六数据备份与恢复)
- [七、常见问题](#七常见问题)

---

## 一、Docker基础概念

### 1.1 什么是Docker？

Docker是一个容器化平台，可以将应用程序及其依赖打包到一个轻量级、可移植的容器中。简单来说：

- **镜像(Image)**：类似于"模板"，包含了运行应用所需的所有内容
- **容器(Container)**：镜像的运行实例，类似于一个轻量级的虚拟机
- **仓库(Registry)**：存储和分发镜像的地方，如Docker Hub

### 1.2 Docker vs 虚拟机

| 特性 | Docker容器 | 虚拟机 |
|-----|-----------|-------|
| 启动速度 | 秒级 | 分钟级 |
| 资源占用 | MB级别 | GB级别 |
| 性能 | 接近原生 | 有损耗 |
| 隔离性 | 进程级别 | 操作系统级别 |

### 1.3 为什么使用Docker？

1. **环境一致性**：开发、测试、生产环境完全一致
2. **快速部署**：一键启动所有服务
3. **资源隔离**：各服务互不影响
4. **版本管理**：方便回滚和升级

---

## 二、Docker Desktop安装

### 2.1 系统要求

- Windows 10/11 64位（Pro/Enterprise/Education版本）
- 启用Hyper-V和WSL2

### 2.2 安装步骤

1. **启用WSL2**（以管理员身份运行PowerShell）：

```powershell
# 启用WSL
wsl --install

# 如果已安装WSL，升级到WSL2
wsl --update

# 设置WSL2为默认版本
wsl --set-default-version 2
```

2. **下载Docker Desktop**：
   - 访问：https://www.docker.com/products/docker-desktop
   - 下载Windows版本

3. **安装Docker Desktop**：
   - 运行安装程序
   - 勾选"Use WSL 2 instead of Hyper-V"
   - 完成安装后重启电脑

4. **验证安装**：

```cmd
docker --version
docker run hello-world
```

### 2.3 配置Docker数据目录（避免C盘空间不足）

1. 打开Docker Desktop → Settings → Resources → Advanced
2. 修改"Disk image location"到D盘：`D:\DockerData`
3. 或者编辑配置文件 `C:\Users\用户名\.docker\daemon.json`：

```json
{
  "data-root": "D:\\DockerData",
  "registry-mirrors": [
    "https://docker.m.daocloud.io",
    "https://mirror.ccs.tencentyun.com"
  ]
}
```

---

## 三、常用Docker命令

### 3.1 镜像操作

```cmd
# 查看本地镜像
docker images

# 搜索镜像
docker search mysql

# 拉取镜像
docker pull mysql:8.0
docker pull redis:7-alpine
docker pull nginx:alpine

# 删除镜像
docker rmi mysql:8.0

# 删除所有未使用的镜像
docker image prune -a
```

### 3.2 容器操作

```cmd
# 查看运行中的容器
docker ps

# 查看所有容器（包括已停止的）
docker ps -a

# 启动容器
docker start 容器名或ID

# 停止容器
docker stop 容器名或ID

# 重启容器
docker restart 容器名或ID

# 删除容器（必须先停止）
docker rm 容器名或ID

# 强制删除运行中的容器
docker rm -f 容器名或ID

# 查看容器日志
docker logs 容器名或ID

# 实时查看日志
docker logs -f 容器名或ID

# 进入容器内部
docker exec -it 容器名或ID /bin/bash
# 或对于alpine镜像
docker exec -it 容器名或ID /bin/sh
```

### 3.3 容器资源监控

```cmd
# 查看容器资源使用情况
docker stats

# 查看容器详细信息
docker inspect 容器名或ID

# 查看容器端口映射
docker port 容器名或ID
```

---

## 四、MySQL容器管理

### 4.1 查看现有MySQL容器

```cmd
# 查看所有容器，找到MySQL
docker ps -a | findstr mysql
```

### 4.2 连接MySQL容器

**方法一：通过Docker命令行连接**

```cmd
# 进入MySQL容器
docker exec -it mysql容器名 mysql -uroot -pxiong3410313

# 执行SQL命令
mysql> SHOW DATABASES;
mysql> USE myliu_flowchart;
mysql> SHOW TABLES;
mysql> EXIT;
```

**方法二：通过Navicat等工具连接**

连接信息：
- 主机：`localhost` 或 `127.0.0.1`
- 端口：`3306`
- 用户名：`root`
- 密码：`xiong3410313`

### 4.3 MySQL容器常用操作

```cmd
# 启动MySQL容器
docker start mysql容器名

# 停止MySQL容器
docker stop mysql容器名

# 重启MySQL容器
docker restart mysql容器名

# 查看MySQL日志
docker logs mysql容器名

# 查看MySQL状态
docker exec -it mysql容器名 mysqladmin -uroot -pxiong3410313 status
```

### 4.4 创建新的MySQL容器（如需要）

如果需要创建新的MySQL容器：

```cmd
docker run -d \
  --name myliu-mysql \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=xiong3410313 \
  -e MYSQL_DATABASE=myliu_flowchart \
  -e TZ=Asia/Shanghai \
  -v D:/DockerVolumes/mysql:/var/lib/mysql \
  mysql:8.0 \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_unicode_ci
```

参数说明：
- `-d`：后台运行
- `--name`：容器名称
- `-p 3306:3306`：端口映射（主机端口:容器端口）
- `-e MYSQL_ROOT_PASSWORD`：设置root密码
- `-e MYSQL_DATABASE`：创建初始数据库
- `-v`：数据卷挂载（持久化数据）
- `--character-set-server`：字符集设置

---

## 五、Docker Compose使用

### 5.1 什么是Docker Compose？

Docker Compose是一个用于定义和运行多容器Docker应用的工具。通过一个YAML文件配置所有服务。

### 5.2 docker-compose.yml示例

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
      - D:/DockerVolumes/mysql:/var/lib/mysql
    command: --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci

  # Redis缓存
  redis:
    image: redis:7-alpine
    container_name: myliu-redis
    restart: unless-stopped
    ports:
      - "6379:6379"
    volumes:
      - D:/DockerVolumes/redis:/data

  # 后端服务
  backend:
    build:
      context: ../backend/flowchart-server
      dockerfile: Dockerfile
    container_name: myliu-backend
    restart: unless-stopped
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/myliu_flowchart
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: xiong3410313
    depends_on:
      - mysql
      - redis

  # Nginx反向代理
  nginx:
    image: nginx:alpine
    container_name: myliu-nginx
    restart: unless-stopped
    ports:
      - "80:80"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf
      - ./nginx/conf.d:/etc/nginx/conf.d
    depends_on:
      - backend
```

### 5.3 Docker Compose常用命令

```cmd
# 启动所有服务（后台运行）
docker-compose up -d

# 启动所有服务（前台运行，可看到日志）
docker-compose up

# 停止所有服务
docker-compose down

# 停止并删除数据卷
docker-compose down -v

# 重新构建并启动
docker-compose up -d --build

# 查看服务状态
docker-compose ps

# 查看服务日志
docker-compose logs

# 实时查看某个服务的日志
docker-compose logs -f backend

# 重启某个服务
docker-compose restart backend

# 进入某个服务的容器
docker-compose exec backend /bin/bash
```

---

## 六、数据备份与恢复

### 6.1 MySQL数据备份

**方法一：使用docker exec导出**

```cmd
# 备份所有数据库
docker exec myliu-mysql mysqldump -uroot -pxiong3410313 --all-databases > D:/backup/all_databases.sql

# 备份指定数据库
docker exec myliu-mysql mysqldump -uroot -pxiong3410313 myliu_flowchart > D:/backup/myliu_flowchart.sql

# 备份指定表
docker exec myliu-mysql mysqldump -uroot -pxiong3410313 myliu_flowchart sys_user sys_role > D:/backup/user_role.sql
```

**方法二：创建备份脚本**

创建 `D:\scripts\backup-mysql.ps1`：

```powershell
# MySQL备份脚本
$backupDir = "D:\backup\mysql"
$date = Get-Date -Format "yyyyMMdd_HHmmss"
$backupFile = "$backupDir\myliu_$date.sql"

# 创建备份目录
if (!(Test-Path $backupDir)) {
    New-Item -ItemType Directory -Path $backupDir -Force
}

# 执行备份
docker exec myliu-mysql mysqldump -uroot -pxiong3410313 --all-databases > $backupFile

# 压缩备份文件
Compress-Archive -Path $backupFile -DestinationPath "$backupFile.zip"
Remove-Item $backupFile

# 删除30天前的备份
Get-ChildItem $backupDir -Filter "*.zip" | Where-Object { $_.LastWriteTime -lt (Get-Date).AddDays(-30) } | Remove-Item -Force

Write-Host "备份完成: $backupFile.zip"
```

### 6.2 MySQL数据恢复

```cmd
# 恢复数据库
docker exec -i myliu-mysql mysql -uroot -pxiong3410313 < D:/backup/myliu_flowchart.sql

# 恢复到指定数据库
docker exec -i myliu-mysql mysql -uroot -pxiong3410313 myliu_flowchart < D:/backup/myliu_flowchart.sql
```

### 6.3 Docker数据卷管理

```cmd
# 查看所有数据卷
docker volume ls

# 查看数据卷详情
docker volume inspect 卷名

# 删除未使用的数据卷
docker volume prune

# 备份数据卷
docker run --rm -v 卷名:/data -v D:/backup:/backup alpine tar czf /backup/volume_backup.tar.gz /data

# 恢复数据卷
docker run --rm -v 卷名:/data -v D:/backup:/backup alpine tar xzf /backup/volume_backup.tar.gz -C /
```

---

## 七、常见问题

### Q1: Docker容器无法启动？

**排查步骤**：
```cmd
# 查看容器日志
docker logs 容器名

# 查看容器状态
docker ps -a

# 检查端口是否被占用
netstat -ano | findstr 3306
```

### Q2: MySQL容器连接不上？

**解决方案**：
1. 确认容器正在运行：`docker ps`
2. 确认端口映射正确：`docker port 容器名`
3. 检查防火墙设置
4. 确认密码正确

### Q3: Docker占用磁盘空间太大？

**解决方案**：
```cmd
# 清理未使用的镜像
docker image prune -a

# 清理未使用的容器
docker container prune

# 清理未使用的数据卷
docker volume prune

# 一键清理所有未使用资源
docker system prune -a
```

### Q4: 如何查看容器IP地址？

```cmd
# 方法一
docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' 容器名

# 方法二
docker inspect 容器名 | findstr IPAddress
```

### Q5: 容器之间如何通信？

在同一个Docker网络中的容器可以通过**容器名**相互访问：

```yaml
# docker-compose.yml中，服务名就是主机名
services:
  backend:
    environment:
      # 使用服务名mysql作为主机名
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/myliu_flowchart
```

### Q6: 如何修改已运行容器的配置？

Docker不支持直接修改运行中容器的配置，需要：
1. 停止并删除容器
2. 使用新配置重新创建容器
3. 或者使用 `docker commit` 保存为新镜像

### Q7: Windows下路径挂载问题？

Windows路径挂载注意事项：
- 使用正斜杠 `/` 或双反斜杠 `\\`
- 推荐使用正斜杠：`D:/DockerVolumes/mysql`
- 或使用 `${PWD}` 表示当前目录

---

## 附录：常用命令速查表

| 命令 | 说明 |
|-----|------|
| `docker ps` | 查看运行中的容器 |
| `docker ps -a` | 查看所有容器 |
| `docker images` | 查看本地镜像 |
| `docker pull 镜像名` | 拉取镜像 |
| `docker start 容器名` | 启动容器 |
| `docker stop 容器名` | 停止容器 |
| `docker restart 容器名` | 重启容器 |
| `docker rm 容器名` | 删除容器 |
| `docker logs 容器名` | 查看日志 |
| `docker exec -it 容器名 /bin/bash` | 进入容器 |
| `docker-compose up -d` | 启动所有服务 |
| `docker-compose down` | 停止所有服务 |
| `docker-compose logs -f` | 查看日志 |

---

## 下一步

Docker基础掌握后，请继续阅读：
- [部署指南](./deployment_guide.md) - 了解项目部署流程
- [开发问题记录](./development_issues.md) - 记录开发中的问题
