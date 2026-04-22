# 宠物服务系统 (Pet Service System)

一个基于Spring Boot + Vue.js的全栈宠物服务平台，提供宠物服务预约、商家管理、用户管理等功能。

![alt text](image.png)

## 技术栈

- **前端**: Vue.js 3, Vite, Element Plus, Pinia
- **后端**: Spring Boot 3.2.5, MyBatis-Plus, JWT
- **数据库**: MySQL 8
- **缓存**: Redis 7
- **构建工具**: Maven (后端), npm (前端)

## 功能特性

### 用户端功能
- 用户注册/登录与JWT身份验证
- 宠物档案管理
- 服务预约与订单管理
- 个人资料与宠物管理
- 积分商城功能

### 服务商功能
- 服务项目管理
- 预约时间表管理
- 收入统计
- 评价管理
- 员工管理

### 管理员功能
- 用户管理
- 服务管理
- 订单管理
- 商家审核
- 数据统计
- 系统配置
- 日志管理

## 快速开始

### 使用Docker Compose启动（推荐）

1. 确保已安装Docker和Docker Compose
2. 克隆项目到本地
3. 在项目根目录下运行:

```bash
docker-compose up -d
```

系统将自动启动MySQL、Redis、后端和前端服务。

### 手动启动

#### 后端启动

1. 安装Java 17和Maven
2. 启动MySQL和Redis服务
3. 创建数据库并导入SQL文件

```bash
# 导航到后端目录
cd backend
# 构建并运行项目
mvn spring-boot:run
```

后端默认运行在 [http://localhost:8081](http://localhost:8081)

#### 前端启动

1. 安装Node.js (版本 >= 16)
2. 在新终端窗口中导航到前端目录

```bash
# 安装依赖
npm install
# 启动开发服务器
npm run dev
```

前端默认运行在 [http://localhost:5173](http://localhost:5173)

## 数据库配置

### 自动导入数据库

使用Docker Compose时，数据库会自动创建并初始化数据。

### 手动导入数据库

1. 在MySQL中创建名为 `pet_service` 的数据库
2. 执行SQL目录下的数据库初始化脚本

```sql
CREATE DATABASE pet_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pet_service;
SOURCE sql/pet_service.sql;
-- 如果需要测试数据，也可以执行
SOURCE sql/fix_test_data.sql;
```

### 数据库配置文件

修改 [backend/src/main/resources/application.yml](file:///c:/Users/GXF/Desktop/%E7%BC%96%E7%A8%8B/%E4%BB%A3%E7%A0%81/html/pet-service-system/backend/src/main/resources/application.yml) 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/pet_service?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
    username: root  # 修改为你的数据库用户名
    password: 123456  # 修改为你的数据库密码
```

## 项目结构

```
pet-service-system/
├── backend/              # Spring Boot后端
│   ├── src/main/java/com/petservice/
│   │   ├── controller/   # 控制器层
│   │   ├── service/      # 业务逻辑层
│   │   ├── mapper/       # 数据访问层
│   │   ├── entity/       # 实体类
│   │   ├── dto/          # 数据传输对象
│   │   └── config/       # 配置类
│   └── src/main/resources/
├── frontend/             # Vue.js前端
│   ├── src/
│   │   ├── views/        # 页面组件
│   │   ├── components/   # 通用组件
│   │   ├── api/          # API接口定义
│   │   ├── stores/       # Pinia状态管理
│   │   └── utils/        # 工具函数
├── sql/                  # 数据库脚本
└── docker-compose.yml    # Docker编排配置
```

## 环境要求

- Java 17+
- Node.js 16+
- MySQL 8+
- Redis 7+
- Maven 3.5+

## 开发说明

### 添加新功能

1. 后端：在controller目录添加控制器，在service目录添加业务逻辑
2. 前端：在views目录添加页面，在api目录添加API接口定义

### 接口文档

后端REST API遵循RESTful设计规范，使用JWT进行身份验证。

## 部署

### 生产环境部署

1. 构建后端JAR包：
```bash
cd backend
mvn clean package -DskipTests
java -jar target/pet-service-system-1.0.0.jar
```

2. 构建前端静态资源：
```bash
cd frontend
npm run build
```

## 贡献者

欢迎提交Issue和Pull Request帮助改进项目。

## 许可证

MIT License

## 项目截图

以下是宠物服务系统的界面截图:

![系统主界面](./image.png)

![登录页面](./image-1.png)

![用户主页](./image-2.png)

![管理员面板](./image-3.png)

![服务管理](./image-4.png)

![订单管理](./image-5.png)

![收入管理](./image-6.png)

![员工日程管理](./image-7.png)
