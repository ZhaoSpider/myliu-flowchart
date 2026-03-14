# MyLiu Flowchart API 接口文档

> 基础路径: `/api/v1`  
> 认证方式: JWT Bearer Token

---

## 接口实现状态总览

| 模块 | 已实现 | 待实现 | 完成率 |
|------|--------|--------|--------|
| 认证模块 | 0 | 4 | 0% |
| 文件管理模块 | 0 | 9 | 0% |
| **总计** | **0** | **13** | **0%** |

---

## 一、认证模块 (Auth)

### 1.1 用户登录

**状态**: ⏳ 待实现

```
POST /api/v1/auth/login
```

**请求体**:
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "admin",
      "nickname": "超级管理员",
      "email": "admin@myliu.com",
      "phone": null,
      "avatar": null,
      "status": 1,
      "createdAt": "2024-01-01 00:00:00"
    }
  }
}
```

**错误码**:
- `401`: 用户名或密码错误
- `403`: 账号已被禁用

---

### 1.2 用户注册

**状态**: ⏳ 待实现

```
POST /api/v1/auth/register
```

**请求体**:
```json
{
  "username": "newuser",
  "password": "password123",
  "confirmPassword": "password123",
  "email": "user@example.com",
  "nickname": "新用户"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": null
}
```

**错误码**:
- `400`: 两次密码不一致
- `409`: 用户名已存在
- `409`: 邮箱已被注册

---

### 1.3 获取当前用户信息

**状态**: ⏳ 待实现

```
GET /api/v1/auth/current
```

**请求头**:
```
Authorization: Bearer <token>
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "nickname": "超级管理员",
    "email": "admin@myliu.com",
    "phone": null,
    "avatar": null,
    "status": 1,
    "createdAt": "2024-01-01 00:00:00"
  }
}
```

**错误码**:
- `401`: 未登录或Token过期

---

### 1.4 退出登录

**状态**: ⏳ 待实现

```
POST /api/v1/auth/logout
```

**请求头**:
```
Authorization: Bearer <token>
```

**响应**:
```json
{
  "code": 200,
  "message": "退出成功",
  "data": null
}
```

---

## 二、文件管理模块 (Files)

### 2.1 获取文件列表

**状态**: ⏳ 待实现

```
GET /api/v1/files
```

**查询参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页数量，默认10 |
| keyword | string | 否 | 搜索关键词 |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "fileName": "流程图1",
        "filePath": "/files/1.json",
        "fileSize": 1024,
        "thumbnail": "/thumbnails/1.png",
        "creatorId": 1,
        "projectId": null,
        "status": 1,
        "createdAt": "2024-01-01 10:00:00",
        "updatedAt": "2024-01-01 12:00:00"
      }
    ],
    "total": 100,
    "page": 1,
    "size": 10
  }
}
```

---

### 2.2 获取文件详情

**状态**: ⏳ 待实现

```
GET /api/v1/files/{id}
```

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 文件ID |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "fileName": "流程图1",
    "filePath": "/files/1.json",
    "fileSize": 1024,
    "thumbnail": "/thumbnails/1.png",
    "creatorId": 1,
    "projectId": null,
    "status": 1,
    "createdAt": "2024-01-01 10:00:00",
    "updatedAt": "2024-01-01 12:00:00"
  }
}
```

**错误码**:
- `404`: 文件不存在

---

### 2.3 创建文件

**状态**: ⏳ 待实现

```
POST /api/v1/files
```

**请求体**:
```json
{
  "fileName": "新建流程图",
  "content": "{\"nodes\":[],\"edges\":[]}"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 1,
    "fileName": "新建流程图",
    "filePath": "/files/1.json",
    "fileSize": 0,
    "thumbnail": null,
    "creatorId": 1,
    "projectId": null,
    "status": 1,
    "createdAt": "2024-01-01 10:00:00",
    "updatedAt": "2024-01-01 10:00:00"
  }
}
```

---

### 2.4 更新文件信息

**状态**: ⏳ 待实现

```
PUT /api/v1/files/{id}
```

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 文件ID |

**请求体**:
```json
{
  "fileName": "重命名流程图"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "fileName": "重命名流程图",
    "updatedAt": "2024-01-01 12:00:00"
  }
}
```

**错误码**:
- `404`: 文件不存在
- `403`: 无权限修改

---

### 2.5 删除文件

**状态**: ⏳ 待实现

```
DELETE /api/v1/files/{id}
```

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 文件ID |

**响应**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

**错误码**:
- `404`: 文件不存在
- `403`: 无权限删除

---

### 2.6 保存文件内容

**状态**: ⏳ 待实现

```
PUT /api/v1/files/{id}/content
```

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 文件ID |

**请求体**:
```json
{
  "content": "{\"nodes\":[{\"id\":\"node1\",\"shape\":\"rect\",\"x\":100,\"y\":100,\"width\":100,\"height\":50,\"label\":\"开始\"}],\"edges\":[]}"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "保存成功",
  "data": null
}
```

---

### 2.7 获取文件内容

**状态**: ⏳ 待实现

```
GET /api/v1/files/{id}/content
```

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 文件ID |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": "{\"nodes\":[],\"edges\":[]}"
  }
}
```

---

### 2.8 获取版本历史

**状态**: ⏳ 待实现

```
GET /api/v1/files/{id}/versions
```

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 文件ID |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "fileId": 1,
      "versionNo": "1.0.0",
      "content": null,
      "changeLog": "初始版本",
      "creatorId": 1,
      "createdAt": "2024-01-01 10:00:00"
    },
    {
      "id": 2,
      "fileId": 1,
      "versionNo": "1.1.0",
      "content": null,
      "changeLog": "添加新节点",
      "creatorId": 1,
      "createdAt": "2024-01-01 12:00:00"
    }
  ]
}
```

---

### 2.9 导出文件

**状态**: ⏳ 待实现

```
GET /api/v1/files/{id}/export?format={format}
```

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 文件ID |

**查询参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| format | string | 是 | 导出格式: png / svg / json |

**响应**:
- `png`: `image/png` 二进制流
- `svg`: `image/svg+xml` 文本
- `json`: `application/json` 文本

**错误码**:
- `400`: 不支持的导出格式
- `404`: 文件不存在

---

## 三、数据结构定义

### User 用户
| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 用户ID |
| username | string | 用户名 |
| nickname | string | 昵称 |
| email | string | 邮箱 |
| phone | string | 手机号 |
| avatar | string | 头像URL |
| status | int | 状态: 0禁用 1启用 |
| createdAt | datetime | 创建时间 |

### FlowchartFile 流程图文件
| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 文件ID |
| fileName | string | 文件名 |
| filePath | string | 存储路径 |
| fileSize | long | 文件大小(字节) |
| thumbnail | string | 缩略图URL |
| creatorId | long | 创建者ID |
| projectId | long | 所属项目ID |
| status | int | 状态: 1草稿 2已发布 |
| createdAt | datetime | 创建时间 |
| updatedAt | datetime | 更新时间 |

### FlowchartVersion 版本记录
| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 版本ID |
| fileId | long | 文件ID |
| versionNo | string | 版本号 |
| content | string | 流程图JSON |
| changeLog | string | 变更说明 |
| creatorId | long | 创建者ID |
| createdAt | datetime | 创建时间 |

### FlowGraph 流程图数据
| 字段 | 类型 | 说明 |
|------|------|------|
| nodes | FlowNode[] | 节点列表 |
| edges | FlowEdge[] | 连线列表 |

### FlowNode 节点
| 字段 | 类型 | 说明 |
|------|------|------|
| id | string | 节点ID |
| shape | string | 形状类型 |
| x | number | X坐标 |
| y | number | Y坐标 |
| width | number | 宽度 |
| height | number | 高度 |
| label | string | 标签文本 |
| data | object | 自定义数据 |

### FlowEdge 连线
| 字段 | 类型 | 说明 |
|------|------|------|
| id | string | 连线ID |
| shape | string | 形状类型 |
| source | string | 源节点ID |
| target | string | 目标节点ID |
| label | string | 标签文本 |
| data | object | 自定义数据 |

---

## 四、通用响应格式

### 成功响应
```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

### 错误响应
```json
{
  "code": 400,
  "message": "错误描述",
  "data": null
}
```

### 错误码定义
| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录或Token过期 |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 409 | 资源冲突 |
| 500 | 服务器内部错误 |

---

## 五、实现优先级建议

### P0 - 核心功能（必须实现）
1. `POST /auth/login` - 用户登录
2. `GET /auth/current` - 获取当前用户
3. `GET /files` - 文件列表
4. `POST /files` - 创建文件
5. `PUT /files/{id}/content` - 保存内容
6. `GET /files/{id}/content` - 获取内容

### P1 - 重要功能
1. `POST /auth/register` - 用户注册
2. `DELETE /files/{id}` - 删除文件
3. `PUT /files/{id}` - 更新文件信息
4. `GET /files/{id}` - 文件详情

### P2 - 增强功能
1. `POST /auth/logout` - 退出登录
2. `GET /files/{id}/versions` - 版本历史
3. `GET /files/{id}/export` - 导出文件
