# 开发环境搭建指南

> 本文档详细说明如何在Windows环境下搭建企业级微网站项目的开发环境，所有环境均安装到非C盘位置，避免C盘空间不足的问题。

## 目录

- [一、环境安装路径规划](#一环境安装路径规划)
- [二、Java环境配置](#二java环境配置)
- [三、Node.js环境配置](#三nodejs环境配置)
- [四、Maven环境配置](#四maven环境配置)
- [五、IDE安装配置](#五ide安装配置)
- [六、环境验证](#六环境验证)
- [七、常见问题](#七常见问题)

---

## 一、环境安装路径规划

### 1.1 目录结构

```
D:\DevEnvironment\                   # 所有开发环境统一安装位置
├── Java\
│   └── jdk-17\                      # JDK 17 LTS
├── Node\
│   ├── nvm\                         # NVM-Windows安装目录
│   └── nodejs\                      # Node.js版本存放位置
├── Maven\
│   └── apache-maven-3.9.6\          # Maven安装目录
├── Repository\                      # Maven本地仓库
├── npm-cache\                       # npm缓存目录
└── npm-global\                      # npm全局模块目录
```

### 1.2 创建目录

打开PowerShell（以管理员身份运行），执行以下命令：

```powershell
# 创建开发环境根目录
$devRoot = "D:\DevEnvironment"

# 创建所有需要的目录
$directories = @(
    "$devRoot\Java",
    "$devRoot\Node\nvm",
    "$devRoot\Node\nodejs",
    "$devRoot\Maven",
    "$devRoot\Repository",
    "$devRoot\npm-cache",
    "$devRoot\npm-global"
)

foreach ($dir in $directories) {
    if (!(Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir -Force
        Write-Host "已创建: $dir" -ForegroundColor Green
    }
}

Write-Host "目录结构创建完成!" -ForegroundColor Cyan
```

---

## 二、Java环境配置

### 2.1 下载JDK

1. 访问 Eclipse Temurin 官网：https://adoptium.net/
2. 选择版本：**JDK 17 LTS**（长期支持版本）
3. 选择操作系统：**Windows**
4. 选择架构：**x64**
5. 选择包类型：**JDK**
6. 下载 `.msi` 安装包

### 2.2 安装JDK

1. 双击下载的 `.msi` 安装包
2. 在安装向导中，**自定义安装路径**为：`D:\DevEnvironment\Java\jdk-17`
3. 完成安装

### 2.3 配置环境变量

#### 方法一：通过GUI配置

1. 右键"此电脑" → "属性" → "高级系统设置"
2. 点击"环境变量"
3. 在"系统变量"中：
   - 新建 `JAVA_HOME`，值为 `D:\DevEnvironment\Java\jdk-17`
   - 编辑 `Path`，添加 `%JAVA_HOME%\bin`

#### 方法二：通过PowerShell配置

```powershell
# 设置JAVA_HOME环境变量
[Environment]::SetEnvironmentVariable("JAVA_HOME", "D:\DevEnvironment\Java\jdk-17", "Machine")

# 添加到PATH（需要管理员权限）
$path = [Environment]::GetEnvironmentVariable("Path", "Machine")
if ($path -notlike "*%JAVA_HOME%\bin*") {
    [Environment]::SetEnvironmentVariable("Path", $path + ";%JAVA_HOME%\bin", "Machine")
}

Write-Host "Java环境变量配置完成!" -ForegroundColor Green
```

### 2.4 验证安装

打开新的命令提示符窗口，执行：

```cmd
java -version
javac -version
```

预期输出：
```
openjdk version "17.0.x" 2024-xx-xx
OpenJDK Runtime Environment Temurin-17.0.x+x (build 17.0.x+x-LTS)
OpenJDK 64-Bit Server VM Temurin-17.0.x+x (build 17.0.x+x-LTS, mixed mode, sharing)
```

---

## 三、Node.js环境配置

### 3.1 下载NVM-Windows

NVM-Windows 是 Node.js 的版本管理工具，可以方便地切换不同版本的 Node.js。

1. 访问 GitHub 发布页：https://github.com/coreybutler/nvm-windows/releases
2. 下载最新的 `nvm-setup.exe`

### 3.2 安装NVM-Windows

1. 双击 `nvm-setup.exe`
2. 选择安装路径：`D:\DevEnvironment\Node\nvm`
3. 选择Node.js符号链接路径：`D:\DevEnvironment\Node\nodejs`

### 3.3 配置NVM镜像源

编辑 `D:\DevEnvironment\Node\nvm\settings.txt`，添加以下内容：

```ini
root: D:\DevEnvironment\Node\nvm
path: D:\DevEnvironment\Node\nodejs
node_mirror: https://npmmirror.com/mirrors/node/
npm_mirror: https://npmmirror.com/mirrors/npm/
```

### 3.4 安装Node.js

打开命令提示符（以管理员身份），执行：

```cmd
# 查看可用的Node.js版本
nvm list available

# 安装Node.js 20.x LTS
nvm install 20.11.0

# 使用该版本
nvm use 20.11.0

# 设置默认版本
nvm alias default 20.11.0
```

### 3.5 配置npm全局路径

避免npm将全局模块和缓存安装到C盘，执行以下命令：

```cmd
# 设置npm全局模块安装路径
npm config set prefix "D:\DevEnvironment\npm-global"

# 设置npm缓存路径
npm config set cache "D:\DevEnvironment\npm-cache"

# 设置国内镜像源（加速下载）
npm config set registry https://registry.npmmirror.com
```

### 3.6 配置npm环境变量

将npm全局模块路径添加到环境变量：

```powershell
# 添加npm全局模块到PATH
$path = [Environment]::GetEnvironmentVariable("Path", "Machine")
if ($path -notlike "*D:\DevEnvironment\npm-global*") {
    [Environment]::SetEnvironmentVariable("Path", $path + ";D:\DevEnvironment\npm-global", "Machine")
}
```

### 3.7 验证安装

```cmd
node -v
npm -v
npm config list
```

预期输出：
```
v20.11.0
10.x.x
; "user" config from C:\Users\用户名\.npmrc

cache = "D:\\DevEnvironment\\npm-cache"
prefix = "D:\\DevEnvironment\\npm-global"
registry = "https://registry.npmmirror.com/"
```

---

## 四、Maven环境配置

### 4.1 下载Maven

1. 访问 Maven官网：https://maven.apache.org/download.cgi
2. 下载 **Binary zip archive**（apache-maven-3.9.6-bin.zip）

### 4.2 安装Maven

1. 将下载的zip文件解压到 `D:\DevEnvironment\Maven\apache-maven-3.9.6`

### 4.3 配置Maven环境变量

```powershell
# 设置MAVEN_HOME环境变量
[Environment]::SetEnvironmentVariable("MAVEN_HOME", "D:\DevEnvironment\Maven\apache-maven-3.9.6", "Machine")

# 添加到PATH
$path = [Environment]::GetEnvironmentVariable("Path", "Machine")
if ($path -notlike "*%MAVEN_HOME%\bin*") {
    [Environment]::SetEnvironmentVariable("Path", $path + ";%MAVEN_HOME%\bin", "Machine")
}
```

### 4.4 配置Maven settings.xml

创建或编辑 `D:\DevEnvironment\Maven\apache-maven-3.9.6\conf\settings.xml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.2.0 https://maven.apache.org/xsd/settings-1.2.0.xsd">

    <!-- 本地仓库路径（重要：指定到D盘） -->
    <localRepository>D:\DevEnvironment\Repository</localRepository>

    <!-- 镜像配置 -->
    <mirrors>
        <mirror>
            <id>aliyun</id>
            <mirrorOf>central</mirrorOf>
            <name>阿里云公共仓库</name>
            <url>https://maven.aliyun.com/repository/public</url>
        </mirror>
        <mirror>
            <id>aliyun-spring</id>
            <mirrorOf>spring</mirrorOf>
            <name>阿里云Spring仓库</name>
            <url>https://maven.aliyun.com/repository/spring</url>
        </mirror>
    </mirrors>

    <!-- Java版本配置 -->
    <profiles>
        <profile>
            <id>jdk-17</id>
            <activation>
                <activeByDefault>true</activeByDefault>
                <jdk>17</jdk>
            </activation>
            <properties>
                <maven.compiler.source>17</maven.compiler.source>
                <maven.compiler.target>17</maven.compiler.target>
                <maven.compiler.compilerVersion>17</maven.compiler.compilerVersion>
            </properties>
        </profile>
    </profiles>
</settings>
```

### 4.5 验证安装

```cmd
mvn -v
```

预期输出：
```
Apache Maven 3.9.6
Maven home: D:\DevEnvironment\Maven\apache-maven-3.9.6
Java version: 17.0.x, vendor: Eclipse Adoptium
Java home: D:\DevEnvironment\Java\jdk-17
Default locale: zh_CN, platform encoding: GBK
OS name: "windows 11", version: "10.0", arch: "amd64"
```

---

## 五、IDE安装配置

### 5.1 IntelliJ IDEA（后端开发）

#### 下载安装

1. 访问 JetBrains官网：https://www.jetbrains.com/idea/download/
2. 下载 **IntelliJ IDEA Community Edition**（免费版）
3. 安装时选择安装路径：`D:\DevEnvironment\IDE\IntelliJ IDEA`

#### 推荐插件

| 插件名称 | 功能 |
|---------|------|
| Lombok | 简化Java代码 |
| MyBatisX | MyBatis代码生成和映射 |
| Spring Assistant | Spring Boot开发辅助 |
| Maven Helper | Maven依赖分析 |
| Alibaba Java Coding Guidelines | 阿里代码规范检查 |

#### 配置Maven

打开IDEA → Settings → Build, Execution, Deployment → Build Tools → Maven：
- Maven home path: `D:\DevEnvironment\Maven\apache-maven-3.9.6`
- User settings file: `D:\DevEnvironment\Maven\apache-maven-3.9.6\conf\settings.xml`
- Local repository: `D:\DevEnvironment\Repository`

### 5.2 Visual Studio Code（前端开发）

#### 下载安装

1. 访问 VS Code官网：https://code.visualstudio.com/
2. 下载 Windows版本安装包
3. 安装时选择安装路径：`D:\DevEnvironment\IDE\VSCode`

#### 推荐插件

| 插件名称 | 功能 |
|---------|------|
| Vue - Official (Volar) | Vue3语法支持 |
| ESLint | 代码规范检查 |
| Prettier | 代码格式化 |
| TypeScript Vue Plugin (Volar) | TypeScript支持 |
| Auto Close Tag | 自动闭合标签 |
| Auto Rename Tag | 自动重命名标签 |
| Path Intellisense | 路径智能提示 |

---

## 六、环境验证

### 6.1 一键验证脚本

创建 `D:\DevEnvironment\verify-env.ps1`：

```powershell
# 环境验证脚本
Write-Host "========== 开发环境验证 ==========" -ForegroundColor Cyan

# Java验证
Write-Host "`n[1/4] Java环境:" -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-Object -First 1
    Write-Host "  $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "  未安装或配置错误" -ForegroundColor Red
}

# Node.js验证
Write-Host "`n[2/4] Node.js环境:" -ForegroundColor Yellow
try {
    $nodeVersion = node -v
    $npmVersion = npm -v
    Write-Host "  Node.js: $nodeVersion" -ForegroundColor Green
    Write-Host "  npm: $npmVersion" -ForegroundColor Green
} catch {
    Write-Host "  未安装或配置错误" -ForegroundColor Red
}

# Maven验证
Write-Host "`n[3/4] Maven环境:" -ForegroundColor Yellow
try {
    $mvnVersion = mvn -v 2>&1 | Select-String "Apache Maven"
    Write-Host "  $mvnVersion" -ForegroundColor Green
} catch {
    Write-Host "  未安装或配置错误" -ForegroundColor Red
}

# 环境变量验证
Write-Host "`n[4/4] 环境变量:" -ForegroundColor Yellow
$envVars = @("JAVA_HOME", "MAVEN_HOME")
foreach ($var in $envVars) {
    $value = [Environment]::GetEnvironmentVariable($var, "Machine")
    if ($value) {
        Write-Host "  $var = $value" -ForegroundColor Green
    } else {
        Write-Host "  $var 未设置" -ForegroundColor Red
    }
}

Write-Host "`n========== 验证完成 ==========" -ForegroundColor Cyan
```

### 6.2 执行验证

```powershell
powershell -ExecutionPolicy Bypass -File "D:\DevEnvironment\verify-env.ps1"
```

---

## 七、常见问题

### Q1: 环境变量配置后不生效？

**解决方案**：
1. 关闭所有命令行窗口和IDE
2. 重新打开新的命令行窗口
3. 如果仍不生效，注销并重新登录Windows

### Q2: npm install 很慢？

**解决方案**：
```cmd
# 检查镜像源配置
npm config get registry

# 如果不是国内镜像，重新设置
npm config set registry https://registry.npmmirror.com
```

### Q3: Maven下载依赖很慢？

**解决方案**：
确认 `settings.xml` 中已配置阿里云镜像，参考 [4.4 配置Maven settings.xml](#44-配置maven-settingsxml)

### Q4: NVM命令找不到？

**解决方案**：
1. 确认NVM安装路径已添加到PATH
2. 重新打开命令行窗口
3. 以管理员身份运行命令提示符

### Q5: 如何切换Node.js版本？

```cmd
# 查看已安装的版本
nvm list

# 切换版本
nvm use 18.19.0

# 安装新版本
nvm install 21.6.0
```

### Q6: 如何清理npm缓存？

```cmd
# 清理npm缓存
npm cache clean --force

# 验证缓存已清理
npm cache verify
```

---

## 附录：环境变量汇总

| 变量名 | 值 |
|-------|---|
| JAVA_HOME | D:\DevEnvironment\Java\jdk-17 |
| MAVEN_HOME | D:\DevEnvironment\Maven\apache-maven-3.9.6 |
| Path（追加） | %JAVA_HOME%\bin |
| Path（追加） | %MAVEN_HOME%\bin |
| Path（追加） | D:\DevEnvironment\npm-global |

---

## 下一步

环境搭建完成后，请继续阅读：
- [Docker管理指南](./docker_management.md) - 学习Docker基本操作
- [部署指南](./deployment_guide.md) - 了解项目部署流程
