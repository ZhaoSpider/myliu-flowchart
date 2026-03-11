# 开发问题记录

> 本文档用于记录项目开发过程中遇到的所有技术问题、解决方案和难点，便于后续复习和回顾开发过程中的挑战。

## 使用说明

在开发过程中，每当遇到技术问题或难点时，请按照以下格式记录：

```markdown
## [问题编号] 问题标题

**发现日期**：YYYY-MM-DD
**发现阶段**：阶段名称（如：环境搭建、后端开发、前端开发等）
**问题类型**：环境配置 / 代码实现 / 架构设计 / 性能优化 / 安全问题 / 其他

### 问题描述
详细描述遇到的问题，包括：
- 问题背景
- 错误现象
- 相关代码片段（如有）

### 尝试的解决方案
记录尝试过的解决方案，包括失败的尝试。

### 最终解决方案
记录最终解决问题的方法。

### 经验总结
总结从这个问题中学到的经验。

### 相关资料
- 链接1
- 链接2
```

---

## 问题记录

---

## [001] 示例：Spring Boot启动报错 - 数据库连接失败

**发现日期**：2024-XX-XX
**发现阶段**：后端开发
**问题类型**：环境配置

### 问题描述
启动Spring Boot应用时，控制台报错：
```
com.mysql.cj.jdbc.exceptions.CommunicationsException: Communications link failure
```

### 尝试的解决方案
1. 检查MySQL服务是否启动 - 已启动
2. 检查用户名密码是否正确 - 正确
3. 检查防火墙端口 - 3306已开放

### 最终解决方案
发现是Docker容器网络问题，MySQL容器和应用容器不在同一网络中。
需要在docker-compose.yml中配置相同的networks。

### 经验总结
Docker容器之间通信需要配置相同的网络，容器名作为主机名使用。

### 相关资料
- [Docker网络配置文档](https://docs.docker.com/network/)

---

## [002] 示例：Vue3 + AntV X6 集成问题

**发现日期**：2024-XX-XX
**发现阶段**：前端开发
**问题类型**：代码实现

### 问题描述
在Vue3组件中集成AntV X6时，画布无法正常渲染，控制台报错：
```
Cannot read properties of undefined (reading 'appendChild')
```

### 尝试的解决方案
1. 检查DOM元素是否存在
2. 尝试在mounted中初始化
3. 检查X6版本兼容性

### 最终解决方案
Vue3的生命周期问题，需要在onMounted钩子中初始化X6实例：
```typescript
import { onMounted, ref } from 'vue'
import { Graph } from '@antv/x6'

const containerRef = ref<HTMLDivElement>()

onMounted(() => {
  const graph = new Graph({
    container: containerRef.value,
    // ...其他配置
  })
})
```

### 经验总结
Vue3中使用第三方DOM操作库时，务必确保DOM元素已挂载后再初始化。

### 相关资料
- [AntV X6官方文档](https://x6.antv.antgroup.com/)
- [Vue3生命周期](https://vuejs.org/guide/essentials/lifecycle.html)

---

## [003] 示例：MyBatis-Plus自动填充不生效

**发现日期**：2024-XX-XX
**发现阶段**：后端开发
**问题类型**：代码实现

### 问题描述
配置了MyBatis-Plus的自动填充功能，但创建时间和更新时间没有自动填充。

### 尝试的解决方案
1. 检查注解是否正确配置
2. 检查MetaObjectHandler是否被Spring管理

### 最终解决方案
MetaObjectHandler类需要添加@Component注解，确保被Spring扫描到：
```java
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdAt", Date.class, new Date());
        this.strictInsertFill(metaObject, "updatedAt", Date.class, new Date());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", Date.class, new Date());
    }
}
```

### 经验总结
MyBatis-Plus的自动填充处理器必须被Spring管理才能生效。

### 相关资料
- [MyBatis-Plus自动填充](https://baomidou.com/pages/4c6bcv/)

---

## 问题索引

| 编号 | 标题 | 类型 | 状态 |
|-----|------|------|------|
| 001 | Spring Boot启动报错 - 数据库连接失败 | 环境配置 | 已解决 |
| 002 | Vue3 + AntV X6 集成问题 | 代码实现 | 已解决 |
| 003 | MyBatis-Plus自动填充不生效 | 代码实现 | 已解决 |

---

## 待解决问题

> 记录尚未解决的问题，便于后续跟进

| 编号 | 标题 | 发现日期 | 优先级 |
|-----|------|---------|-------|
| - | - | - | - |

---

## 技术难点总结

### 1. 流程图绘制功能

**难点描述**：
实现类似draw.io的流程图绘制功能，需要处理复杂的图形交互。

**关键点**：
- 节点拖拽和连线
- 撤销/重做功能
- 图形导出
- 性能优化（大量节点）

**解决方案**：
使用AntV X6库，它提供了完善的图形编辑能力。

### 2. 版本控制功能

**难点描述**：
实现流程图文件的版本管理，支持版本对比和回滚。

**关键点**：
- 版本数据存储
- 版本对比算法
- 大文件处理

**解决方案**：
采用增量存储策略，只存储变更部分。

### 3. 权限管理

**难点描述**：
实现细粒度的权限控制，支持菜单、按钮、接口级别的权限。

**关键点**：
- RBAC模型设计
- 权限数据结构
- 前后端权限校验

**解决方案**：
采用RBAC（基于角色的访问控制）模型，结合Spring Security实现。

---

## 最佳实践记录

### 后端开发规范

1. **统一返回结果格式**
```java
@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
}
```

2. **全局异常处理**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        return Result.error(e.getMessage());
    }
}
```

3. **接口命名规范**
- 查询列表：`list` 或 `page`
- 查询详情：`get` 或 `detail`
- 新增：`add` 或 `save`
- 更新：`update` 或 `modify`
- 删除：`delete` 或 `remove`

### 前端开发规范

1. **组件命名**：使用PascalCase，如 `UserList.vue`
2. **API封装**：统一在api目录下管理
3. **状态管理**：使用Pinia，按模块划分

---

## 学习资源

### 官方文档
- [Spring Boot官方文档](https://spring.io/projects/spring-boot)
- [MyBatis-Plus官方文档](https://baomidou.com/)
- [Vue3官方文档](https://vuejs.org/)
- [AntV X6官方文档](https://x6.antv.antgroup.com/)
- [Element Plus官方文档](https://element-plus.org/)

### 推荐教程
- Spring Boot实战教程
- Vue3 + TypeScript企业级开发
- Docker容器化部署实践

---

## 更新日志

| 日期 | 更新内容 | 更新人 |
|-----|---------|-------|
| 2024-XX-XX | 创建文档模板 | - |

---

> 提示：请及时更新本文档，记录开发过程中的每一个问题和解决方案，这将是你宝贵的知识财富！
