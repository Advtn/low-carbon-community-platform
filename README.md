# 社区低碳生活积分激励平台

基于 `Spring Boot + Vue3 + MySQL` 的社区低碳行为激励系统，覆盖“行为上报 -> 管理员审核 -> 积分发放 -> 商城兑换 -> 订单履约 -> 排行榜展示”全流程。

## 一、项目亮点

- 全栈闭环：实现居民端、管理员端、鉴权、规则配置、商城兑换、审核流转的完整业务闭环。
- 并发安全：兑换流程引入悲观锁（用户与商品行级锁），避免高并发下超卖与积分扣减不一致。
- 查询优化：上报额度校验由“拉全量记录再统计”优化为数据库聚合查询，降低接口开销。
- 运营看板：新增管理员 `dashboard` 数据看板接口与前端概览卡片，支持实时运营监控。
- 可部署交付：提供 `Dockerfile + docker-compose`，一键启动 MySQL / 后端 / 前端。
- 可观测性：新增 `X-Request-Id` 请求追踪日志，便于排障与链路定位。

## 二、功能模块

### 居民端

- 行为上报（文字 + 图片凭证）
- 日上报限额校验
- 个人碳账本（积分流水）
- 积分商城兑换与订单查看
- 社区排行榜

### 管理员端

- 用户管理（增删改查）
- 行为规则管理（积分、减碳、日限、启停）
- 上报审核（通过 / 驳回）
- 商品管理（库存、上下架）
- 订单管理（完成 / 取消并退款）
- 运营概览看板（新增）

## 三、技术栈

- 后端：Spring Boot 3.3、Spring Web、MyBatis-Plus、Validation、MySQL
- 前端：Vue3、Vite、Vue Router、Axios
- 鉴权：Bearer Token（演示场景）
- 部署：Docker、Docker Compose、Nginx

## 四、工程化升级点（本次）

- 拆分前端页面逻辑：`views + composables + services + utils + styles`
- 新增管理员看板接口：`GET /api/admin/dashboard`
- 新增并发保护：兑换时对 `users`、`mall_items` 使用悲观锁
- 新增数据库索引（用户、上报、流水、订单核心查询路径）
- 新增请求日志过滤器（带 `X-Request-Id`）
- 配置环境变量化：数据库地址/账号/密码、上传目录、端口

## 五、持久层架构（MyBatis-Plus）

- 调用链路
  - `Controller -> Service(接口) -> ServiceImpl -> Repository(接口) -> RepositoryImpl -> Mapper -> MySQL`
- 分层职责
  - `entity`：数据库表映射模型，使用 `@TableName/@TableId/@TableField` 显式绑定字段。
  - `mapper`：基于 `BaseMapper<T>` 提供通用 CRUD，个别场景补充自定义 SQL（如 `for update` 行锁）。
  - `repository`：定义数据访问接口；`repository.impl` 中实现聚合、排序、状态过滤等查询细节。
  - `service`：定义业务接口；`service.impl` 中实现事务边界与业务规则（积分变更、库存扣减、审核发放、退款回滚）。
- 关键设计
  - 关联关系由对象关联改为 ID 关联（`userId/ruleId/itemId`），避免 ORM 懒加载问题，查询链路更可控。
  - 高频统计改为数据库聚合（`count/sum`），减少 Java 层遍历。
  - 兑换与退款流程使用悲观锁读取（`select ... for update`），保障并发下积分与库存一致性。
  - 写操作在服务实现层通过 `@Transactional` 管理，确保原子性。
- 主要文件
  - `backend/src/main/java/com/lowcarbon/platform/config/MybatisPlusConfig.java`
  - `backend/src/main/java/com/lowcarbon/platform/mapper/*.java`
  - `backend/src/main/java/com/lowcarbon/platform/repository/*.java`
  - `backend/src/main/java/com/lowcarbon/platform/repository/impl/*.java`
  - `backend/src/main/java/com/lowcarbon/platform/service/*.java`
  - `backend/src/main/java/com/lowcarbon/platform/service/impl/*.java`

## 六、目录结构

- `backend/`：Spring Boot 后端
- `frontend/`：Vue 前端
- `backend/sql/lowcarbon_mysql_init.sql`：MySQL 初始化脚本
- `docker-compose.yml`：一键部署配置

## 七、默认演示账号

- 管理员：`admin / admin123`
- 居民：`alice / 123456`
- 居民：`bob / 123456`

## 八、本地启动

### 1) 初始化数据库

```bash
mysql -uroot -p123456 < backend/sql/lowcarbon_mysql_init.sql
```

### 2) 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端地址：`http://localhost:8080`

### 3) 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端地址：`http://localhost:5173`

## 九、Docker 一键启动

```bash
docker compose up -d --build
```

访问地址：

- 前端：`http://localhost:5173`
- 后端：`http://localhost:8080`

停止：

```bash
docker compose down
```

## 十、核心接口

### 登录

- `POST /api/auth/login`

### 居民端

- `GET /api/user/profile`
- `GET /api/user/rules`
- `POST /api/user/reports`
- `GET /api/user/reports`
- `GET /api/user/ledger`
- `GET /api/user/mall/items`
- `POST /api/user/mall/redeem`
- `GET /api/user/mall/orders`
- `GET /api/user/leaderboard`

### 管理员端

- `GET /api/admin/dashboard`（新增）
- 用户：`/api/admin/users`（GET/POST/PUT/DELETE）
- 规则：`/api/admin/rules`（GET/POST/PUT/DELETE）
- 上报审核：
  - `GET /api/admin/reports/pending`
  - `POST /api/admin/reports/{id}/audit`
- 商品：`/api/admin/items`（GET/POST/PUT/DELETE）
- 订单：
  - `GET /api/admin/orders`
  - `POST /api/admin/orders/{id}/status`
