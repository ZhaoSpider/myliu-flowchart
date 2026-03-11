---
name: myliu-dev-standard
description: MyLiu项目开发规范。在编写代码前主动加载，确保代码符合项目规范。
---

# MyLiu 项目开发规范

本技能定义了MyLiu企业级微网站项目的开发规范，所有代码必须遵循这些规范。

## 一、项目结构规范

### 1.1 目录命名
- 使用小写字母
- 多个单词使用连字符分隔：`flowchart-app`
- 避免使用缩写

### 1.2 文件命名

| 类型 | 规范 | 示例 |
|-----|------|------|
| Vue组件 | PascalCase | `UserList.vue` |
| TypeScript文件 | camelCase | `userService.ts` |
| 样式文件 | kebab-case | `user-list.css` |
| 配置文件 | kebab-case | `vite.config.ts` |

## 二、后端代码规范

### 2.1 包结构
```
com.myliu
├── controller      # 控制器
├── service         # 服务层
│   └── impl        # 服务实现
├── mapper          # MyBatis映射器
├── entity          # 实体类
├── dto             # 数据传输对象
├── vo              # 视图对象
├── config          # 配置类
├── security        # 安全相关
├── exception       # 异常处理
└── util            # 工具类
```

### 2.2 类命名规范

| 类型 | 后缀 | 示例 |
|-----|------|------|
| Controller | Controller | `UserController` |
| Service接口 | Service | `UserService` |
| Service实现 | ServiceImpl | `UserServiceImpl` |
| Mapper | Mapper | `UserMapper` |
| Entity | 无 | `User` |
| DTO | DTO | `UserCreateDTO` |
| VO | VO | `UserDetailVO` |

### 2.3 注释规范

```java
/**
 * 用户服务接口
 *
 * @author MyLiu
 * @since 1.0.0
 */
public interface UserService {

    /**
     * 根据ID获取用户
     *
     * @param id 用户ID
     * @return 用户详情
     */
    UserVO getById(Long id);
}
```

### 2.4 异常处理

```java
// 业务异常
throw new BusinessException("用户不存在");

// 参数校验
if (user == null) {
    throw new IllegalArgumentException("用户不能为空");
}
```

## 三、前端代码规范

### 3.1 组件结构

```vue
<template>
  <!-- 模板内容 -->
</template>

<script setup lang="ts">
// 1. 导入
import { ref, computed } from 'vue'
import type { User } from '@/types'

// 2. Props定义
interface Props {
  userId: number
}
const props = defineProps<Props>()

// 3. Emits定义
const emit = defineEmits<{
  update: [user: User]
}>()

// 4. 响应式状态
const loading = ref(false)

// 5. 计算属性
const displayName = computed(() => user.value?.name || '未知')

// 6. 方法
const handleClick = () => {
  emit('update', user.value)
}

// 7. 生命周期
onMounted(() => {
  loadData()
})
</script>

<style scoped>
/* 样式内容 */
</style>
```

### 3.2 TypeScript规范

```typescript
// 接口定义
interface User {
  id: number
  username: string
  email: string
  createdAt: Date
}

// 类型别名
type UserStatus = 'active' | 'inactive' | 'banned'

// 泛型使用
interface ApiResponse<T> {
  code: number
  message: string
  data: T
}
```

### 3.3 API规范

```typescript
// api/user.ts
import request from '@/utils/request'

export const userApi = {
  // 列表查询
  list: (params: UserQuery) =>
    request.get<PageResult<User>>('/users', { params }),

  // 获取详情
  get: (id: number) =>
    request.get<User>(`/users/${id}`),

  // 创建
  create: (data: UserCreateDTO) =>
    request.post<User>('/users', data),

  // 更新
  update: (id: number, data: UserUpdateDTO) =>
    request.put<User>(`/users/${id}`, data),

  // 删除
  delete: (id: number) =>
    request.delete<void>(`/users/${id}`)
}
```

## 四、数据库规范

### 4.1 表命名
- 使用小写字母
- 多个单词使用下划线分隔
- 模块前缀：`sys_`, `flowchart_`

### 4.2 字段命名

| 类型 | 前缀/后缀 | 示例 |
|-----|----------|------|
| 主键 | id | `id` |
| 外键 | _id | `user_id` |
| 时间 | _at | `created_at` |
| 状态 | status | `status` |
| 名称 | name | `username` |

### 4.3 必备字段

每个表必须包含：
- `id` - 主键
- `created_at` - 创建时间
- `updated_at` - 更新时间
- `deleted` - 逻辑删除标志

## 五、API接口规范

### 5.1 URL规范

```
GET    /api/v1/users          # 获取用户列表
GET    /api/v1/users/{id}     # 获取用户详情
POST   /api/v1/users          # 创建用户
PUT    /api/v1/users/{id}     # 更新用户
DELETE /api/v1/users/{id}     # 删除用户
```

### 5.2 响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    // 返回数据
  }
}
```

### 5.3 错误码规范

| 错误码 | 说明 |
|-------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未授权 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 500 | 服务器错误 |

## 六、Git提交规范

### 6.1 提交信息格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

### 6.2 Type类型

| 类型 | 说明 |
|-----|------|
| feat | 新功能 |
| fix | 修复bug |
| docs | 文档更新 |
| style | 代码格式 |
| refactor | 重构 |
| test | 测试 |
| chore | 构建/工具 |

### 6.3 示例

```
feat(user): 添加用户登录功能

- 实现JWT认证
- 添加登录接口
- 添加前端登录页面

Closes #123
```

## 七、代码审查清单

在提交代码前，请确认：

- [ ] 代码符合命名规范
- [ ] 添加了必要的注释
- [ ] 没有硬编码的敏感信息
- [ ] 异常处理完善
- [ ] 日志记录合理
- [ ] 单元测试通过
- [ ] 代码格式化
