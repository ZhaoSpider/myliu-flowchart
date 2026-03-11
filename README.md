# MyLiu Flowchart - 企业级流程图绘制系统

一个基于 Vue3 + Spring Boot 的企业级流程图绘制系统，支持类似 draw.io 的自由绘图功能。

## 项目结构

```
myliu/
├── docs/                           # 项目文档
│   ├── environment_setup.md        # 环境搭建指南
│   ├── docker_management.md        # Docker管理指南
│   ├── deployment_guide.md         # 部署指南
│   └── development_issues.md       # 开发问题记录
├── frontend/                       # 前端项目
│   ├── flowchart-app/              # 流程图绘制系统
│   └── admin-app/                  # 后台管理系统
├── backend/                        # 后端项目
│   └── flowchart-server/           # Spring Boot服务
├── docker/                         # Docker配置
│   ├── docker-compose.yml
│   ├── nginx/
│   └── mysql/
├── .qoder/                         # Qoder配置
│   ├── agents/                     # 自定义智能体
│   └── skills/                     # 自定义技能
└── scripts/                        # 脚本文件
```

## 技术栈

### 前端
- **框架**: Vue 3.4 + TypeScript 5
- **UI组件**: Element Plus 2
- **状态管理**: Pinia
- **构建工具**: Vite 5
- **流程图库**: AntV X6

### 后端
- **框架**: Spring Boot 3.2
- **ORM**: MyBatis-Plus 3.5
- **安全**: Spring Security + JWT
- **数据库**: MySQL 8.0 + Redis 7
- **文件存储**: MinIO

### 部署
- **容器化**: Docker + Docker Compose
- **反向代理**: Nginx

## 快速开始

### 1. 环境准备

请先阅读 [环境搭建指南](docs/environment_setup.md) 完成开发环境配置。

### 2. 启动数据库

```bash
# 进入docker目录
cd docker

# 启动MySQL和Redis
docker-compose up -d mysql redis
```

### 3. 启动后端

```bash
# 进入后端目录
cd backend/flowchart-server

# 安装依赖并启动
mvn spring-boot:run
```

后端服务地址: http://localhost:8080
API文档地址: http://localhost:8080/doc.html

### 4. 启动前端

```bash
# 进入前端目录
cd frontend/flowchart-app

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端地址: http://localhost:5173

## Docker部署

详细部署步骤请参考 [部署指南](docs/deployment_guide.md)。

```bash
# 一键启动所有服务
cd docker
docker-compose up -d
```

## 功能特性

- [x] 流程图绘制（拖拽、连线、编辑）
- [x] 多种图形支持（矩形、圆形、菱形等）
- [x] 撤销/重做
- [x] 多格式导出（PNG、SVG、JSON）
- [x] 文件版本管理
- [x] 用户权限管理
- [ ] AI协作功能（预留接口）

## 开发规范

本项目遵循统一的开发规范，详见 [.qoder/skills/myliu-dev-standard.md](.qoder/skills/myliu-dev-standard.md)。

## 文档

- [环境搭建指南](docs/environment_setup.md)
- [Docker管理指南](docs/docker_management.md)
- [部署指南](docs/deployment_guide.md)
- [开发问题记录](docs/development_issues.md)

## 许可证

MIT License
