# 大学生社团管理系统

基于 Spring Boot 3 + Vue 3 + MySQL 的大学生社团管理系统，覆盖社团、成员、活动、活动审批、活动报名、经费申请审批、部长换届、公告与校园动态等业务流程。

## 技术栈

| 层 | 技术 |
|----|------|
| 后端 | Spring Boot 3.3、MyBatis Plus、MySQL 8、JWT、BCrypt、SpringDoc OpenAPI |
| 前端 | Vue 3、Vite、Element Plus、Pinia、Vue Router |
| 构建 | Maven、npm |

## 环境要求

- **JDK 21**
- **Maven 3.6+**
- **Node.js 16+**
- **MySQL 8.0+**

## 快速开始

### 1. 克隆并配置

```bash
git clone <仓库地址>
cd 大学生社团管理系统
```

后端的本地配置 `application.yml` 含数据库密码与 JWT 密钥，**不纳入 Git**。克隆后请从模板复制并填写：

```bash
copy campus-club-management\src\main\resources\application.yml.example ^
     campus-club-management\src\main\resources\application.yml
```

编辑 `application.yml`，修改数据库连接信息与 JWT 密钥（也可通过环境变量 `DB_PASSWORD`、`JWT_SECRET` 注入）。

### 2. 初始化数据库

创建数据库 `campus_club`，然后依次执行 `src/main/resources/db/schema.sql`（建表）与 `data.sql`（演示数据）：

```bash
mysql -u root -p < campus-club-management\src\main\resources\db\schema.sql
mysql -u root -p campus_club < campus-club-management\src\main\resources\db\data.sql
```

> 演示账号密码均为 `123456`，使用 BCrypt 加盐哈希存储。

### 3. 启动后端

```bash
cd campus-club-management
mvn spring-boot:run
```

后端启动在 `http://localhost:8080/api`。接口文档（Swagger UI）：`http://localhost:8080/api/swagger-ui.html`。

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端启动在 `http://localhost:5173`。

## 测试账号

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| `admin` | 123456 | 超级管理员 | 拥有所有权限 |
| `2021001` | 123456 | 社长 | 计算机协会社长 |
| `2021002` | 123456 | 副社长 | 计算机协会副社长 |
| `2022001` | 123456 | 部长 | 计算机协会技术部部长 / 篮球社社长 |
| `2022002` | 123456 | 普通成员 | 计算机协会成员 |
| `2023001` | 123456 | 学生 | 普通学生用户 |

## 主要功能

- **用户与权限**：基于角色（超级管理员 / 管理员 / 社长 / 副社长 / 部长 / 成员）与社团职位的两级权限控制，密码 BCrypt 加盐存储，JWT 鉴权。
- **社团管理**：社团与部门的增删改查；超级管理员可管理全部社团，社长/副社长可管理本社团部门。
- **活动管理**：部长发起活动需超级管理员审批；审批通过后公开展示并接受报名；负责人审批报名、控制人数。
- **经费申请**：部长向学校申请活动经费，超级管理员审批通过/拒绝，部长可查看审批结果。
- **部长换届**：部长推荐接任人选，超级管理员审批通过后自动交接职位与角色。
- **公告与动态**：全校公告限管理员发布，社团公告限本社团管理者；校园动态广场支持发布与浏览。

## 项目结构

```
campus-club-management/   # Spring Boot 后端
  src/main/java/org/example/campusclubmanagement/
    controller/   # 接口层
    service/      # 业务层（含 PermissionChecker 通用权限组件）
    mapper/       # 数据访问层
    entity/ dto/  # 实体与传输对象
    config/       # 配置（JWT 拦截、MyBatis Plus、WebMvc）
  src/main/resources/db/   # schema.sql / data.sql
frontend/                # Vue 3 前端
  src/api/       # 接口封装
  src/views/     # 页面
  src/router/    # 路由与权限
```
