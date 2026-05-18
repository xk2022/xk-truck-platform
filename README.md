# 🚚 XK Truck Platform

本專案為 **模組化單體系統（Modular Monolith）**，基於 Spring Boot 3.5.7 + Java 21 + MySQL 8.x 建構，專注於 **物流訂單管理 × 車輛調派 × 權限控管（MVP 平台）**。

---

## 📋 目錄

- [系統目標與定位](#-一系統目標與定位)
- [技術棧](#-二技術棧)
- [專案結構](#-三專案結構)
- [核心模組說明](#-四核心模組說明)
- [環境需求與啟動](#-五環境需求與啟動)
- [API 文件](#-六api-文件)
- [開發規範](#-七開發規範)
- [專案狀態](#-八專案狀態)

---

## ✅ 一、系統目標與定位

本系統為企業內部使用之 **物流管理中台系統**，核心目標：

- ✅ 統一使用者 / 角色 / 權限管理（UPMS）
- ✅ 系統字典與參數設定（ADM）
- ✅ 車輛與司機管理（FMS）
- ✅ 客戶訂單、指派與狀態流轉（TOM / OrderCore）
- ✅ 前後端拆分、模組化單體架構
- ✅ 作為後續 ERP / 運輸管理系統（TMS）之基礎核心

---

## ✅ 二、技術棧

| 類別 | 技術 | 版本 |
|------|------|------|
| 語言 | Java | 21 |
| 框架 | Spring Boot | 3.5.7 |
| Core | Spring Framework | 6.2.x |
| Web | spring-boot-starter-web | - |
| ORM | Spring Data JPA + Hibernate | 6.6.x |
| 驗證 | Jakarta Validation | 3.x |
| 安全 | Spring Security + JWT | 6.5.x / 0.13.0 |
| 資料庫 | MySQL | 8.x |
| 連線池 | HikariCP | - |
| API 文件 | springdoc-openapi | 2.8.11 |
| 工具 | Lombok | 1.18.36 |
| 工具庫 | Apache Commons Lang3 | 3.18.0 |
| 建置工具 | Maven | 3.8.6+ |

---

## ✅ 三、專案結構

```
xk-truck-platform/
│
├── pom.xml                     # Parent POM（版本統一管理）
│
├── xk-base/                    # ✅ 共用核心模組
│   ├── config/                 # OpenAPI、SecurityProps、時區設定
│   ├── domain/model/           # BaseEntity、共用抽象類別
│   ├── exception/              # BusinessException、GlobalExceptionHandler
│   ├── security/               # JWT、SecurityAutoConfig、JwtAuthFilter
│   ├── util/                   # XkBeanUtils 等共用工具
│   └── web/                    # ApiResult<T>、統一 API 回傳格式
│
├── xk-truck/                   # ✅ 主業務模組（Spring Boot 啟動入口）
│   ├── config/                 # 專案專屬設定（Security、Seed、Swagger Groups）
│   ├── adm/                    # 系統字典與參數管理（ADM）
│   │   ├── controller/api/     # REST API 控制器
│   │   ├── domain/model/       # 實體模型（AdmDictCategory、AdmDictItem、SysParam）
│   │   ├── domain/repository/  # JPA Repository
│   │   └── domain/service/     # 業務服務層
│   │
│   ├── upms/                   # 使用者與權限管理（UPMS）
│   │   ├── controller/api/     # REST API 控制器（User、Role、Permission、Auth）
│   │   ├── domain/model/       # 實體模型（User、Role、Permission、UserRole、RolePermission）
│   │   ├── domain/repository/  # JPA Repository
│   │   ├── application/        # 應用服務層（AuthService、UserService、RoleService 等）
│   │   └── infra/security/     # Security 實作（DbUserDetailsService）
│   │
│   ├── fms/                    # 車隊管理系統（FMS）
│   │   ├── controller/api/     # REST API 控制器（Vehicle、Driver、Dispatch）
│   │   ├── domain/model/       # 實體模型（Vehicle、Driver、DispatchTask、VehicleAssignment 等）
│   │   ├── domain/repository/  # JPA Repository
│   │   ├── domain/service/     # 業務服務層（VehicleService、DriverService、DispatchService）
│   │   └── application/usecase/# Use Case 層（DriverRegisterUseCase）
│   │
│   ├── ordercore/              # 訂單核心（命運層）
│   │   ├── domain/model/       # OrderCore、OrderCoreStatus（跨 Domain 共同語言）
│   │   ├── application/        # Use Case（CreateOrderCoreUseCase）
│   │   └── infra/persistence/  # 實體與 Repository Adapter
│   │
│   ├── tom/                    # 運輸訂單管理（TOM）
│   │   ├── controller/api/     # REST API 控制器（TomOrderController）
│   │   ├── domain/model/       # Aggregate Root（TomOrder、TomOrderStatus、TomOrderType）
│   │   ├── application/        # Use Case 層（CreateTomOrderService、ManageTomOrderUseCase）
│   │   ├── application/dto/    # Command、Result DTO
│   │   ├── application/port/   # Port（Inbound / Outbound）
│   │   └── infra/persistence/  # 實體與 Repository Adapter
│   │
│   ├── web/                    # 模組級攔截器與例外處理
│   │   ├── ApiResponseAdvice.java
│   │   └── TruckExceptionHandler.java
│   │
│   └── resources/
│       ├── application.yml     # 主設定檔
│       ├── application-dev.yml # 開發環境設定
│       └── application-prod.yml# 生產環境設定
│
├── checkstyle.xml              # 程式碼風格檢查配置（目前註解）
└── docker-compose.yml          # Docker Compose 配置（MySQL）
```

---

## ✅ 四、核心模組說明

### 4.1 xk-base（共用核心模組）

提供所有 XK 系列專案共用的基礎框架：

- **web.ApiResult**：統一 API 回應格式
- **security**：JWT 認證與授權自動配置
- **exception**：統一例外處理機制
- **util.XkBeanUtils**：Bean 屬性複製工具（DTO ↔ Entity 轉換）
- **domain.model.BaseEntity**：所有 Entity 繼承的基礎類別（UUID、審計欄位）

### 4.2 UPMS（使用者與權限管理）

**核心功能：**
- 使用者帳號管理（建立、更新、查詢、啟用/停用）
- 角色管理（Role CRUD）
- 權限管理（Permission CRUD，支援 System-Resource-Action 模型）
- 角色權限指派（Role ↔ Permission 多對多關聯）
- 使用者角色指派（User ↔ Role 多對多關聯）
- JWT 登入認證

**核心模型：**
- `UpmsUser`：使用者主實體
- `UpmsRole`：角色
- `UpmsPermission`：權限（code 格式：`{SYSTEM}_{RESOURCE}_{ACTION}`）
- `UpmsUserRole`：使用者角色關聯表
- `UpmsRolePermission`：角色權限關聯表

### 4.3 ADM（系統字典與參數管理）

**核心功能：**
- 字典類別管理（AdmDictCategory）
- 字典項目管理（AdmDictItem）
- 系統參數管理（SysParam）

**應用場景：**
- 下拉選單資料來源
- 系統配置參數
- 多語系支援基礎

### 4.4 FMS（車隊管理系統）

**核心功能：**
- 車輛管理（Vehicle CRUD、狀態管理）
- 司機管理（Driver CRUD、上線狀態管理）
- 車輛司機綁定（VehicleAssignment）
- 派工任務管理（DispatchTask）
- 車輛里程記錄（VehicleOdometerRecord）
- 車輛維修記錄（VehicleMaintenanceRecord）

**核心模型：**
- `Vehicle`：車輛（車牌、車種、狀態、載重）
- `Driver`：司機（姓名、電話、駕照類型、上線狀態）
- `VehicleStatus`：車輛狀態（AVAILABLE、IN_USE、MAINTENANCE、RETIRED）
- `DriverStatus`：司機狀態（ACTIVE、SUSPENDED、INACTIVE）

### 4.5 OrderCore（訂單核心 - 命運層）

**設計理念：**
- 跨 Domain 的訂單共同語言
- 只描述訂單高階生命週期（OPEN、CANCELLED、CLOSED）
- 不包含任何業務流程細節

**核心模型：**
- `OrderCore`：訂單核心 Aggregate Root
- `OrderCoreStatus`：訂單高階狀態（命運層）

### 4.6 TOM（運輸訂單管理 - 流程層）

**核心功能：**
- 運輸訂單建立（與 OrderCore 協同）
- 訂單流程狀態管理（NEW → ACCEPTED → ASSIGNED）
- 訂單編號產生（OrderNoGeneratorPort）

**核心模型：**
- `TomOrder`：運輸訂單 Aggregate Root
- `TomOrderStatus`：TOM 流程狀態（NEW、ACCEPTED、ASSIGNED）
- `TomOrderType`：訂單類型

**設計原則：**
- TOM 與 OrderCore 共享 `orderUuid` 主鍵
- TOM 負責「流程面規則」，OrderCore 負責「命運層狀態」

---

## ✅ 五、環境需求與啟動

### 5.1 環境需求

- **JDK**：21 或以上
- **Maven**：3.8.6 或以上
- **MySQL**：8.x
- **Docker**（選用）：用於快速啟動 MySQL

### 5.2 資料庫設定

**建立資料庫：**
```sql
CREATE DATABASE xk_truck_dev
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

**Docker Compose 啟動 MySQL：**
```bash
docker compose up -d
```

### 5.3 編譯與啟動

**1. 編譯所有模組：**
```bash
mvn clean install -DskipTests
```

**2. 啟動應用：**
```bash
cd xk-truck
mvn spring-boot:run
```

或使用 IDE 直接執行 `App.java`

**3. 驗證服務啟動：**
- Swagger UI：http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON：http://localhost:8080/v3/api-docs
- Health Check：http://localhost:8080/actuator/health

### 5.4 環境變數（可選）

```bash
export DB_USERNAME=root
export DB_PASSWORD=your_password
export JWT_SECRET=your_secret_key
```

---

## ✅ 六、API 文件

### 6.1 Swagger UI

啟動後訪問：http://localhost:8080/swagger-ui/index.html

### 6.2 認證方式

1. 使用 `POST /auth/login` 取得 JWT Token
2. 在 Swagger UI 右上角點擊 **Authorize** 按鈕
3. 輸入：`Bearer <你的JWT_Token>`
4. 即可測試受保護的 API

### 6.3 API 群組

系統將 API 分為以下群組（在 Swagger UI 中可見）：
- **UPMS**：使用者與權限管理
- **ADM**：系統字典與參數
- **FMS**：車隊管理
- **TOM**：運輸訂單管理

---

## ✅ 七、開發規範

### 7.1 統一 API 回應格式

所有 Controller 回傳格式統一使用 `ApiResult<T>`：

```java
return ApiResult.success(data);
return ApiResult.failure(HttpStatus.BAD_REQUEST, "錯誤訊息", errorDetail);
```

### 7.2 DTO ↔ Entity 轉換

統一使用 `XkBeanUtils`：

```java
// Entity → DTO
UserResp resp = XkBeanUtils.copyProperties(entity, UserResp::new);

// DTO → Entity（建立）
User entity = XkBeanUtils.copyProperties(req, User::new);

// 部分更新（PATCH）
XkBeanUtils.copyNonNullProperties(updateReq, existingEntity);
```

### 7.3 分層架構

- **Controller**：只負責 HTTP 請求/回應處理，不包含業務邏輯
- **Service / UseCase**：業務邏輯層，標記 `@Transactional`
- **Repository**：資料存取層（Spring Data JPA）
- **Domain Model**：領域模型（Entity / Aggregate Root）

### 7.4 例外處理

- 業務例外：使用 `BusinessException`
- 資源不存在：使用 `ResourceNotFoundException`
- 統一由 `GlobalExceptionHandler` 處理並轉換為 `ApiResult`

### 7.5 分頁查詢

- 資料量 < 300 筆：可前端分頁
- 資料量 > 1000 筆：**必須後端分頁**
- 使用 `Pageable` 與 `Page<T>`（Spring Data）

### 7.6 程式碼風格

- 使用 Checkstyle 檢查（配置檔：`checkstyle.xml`，目前註解）
- 遵循 Java 命名規範
- 使用 Lombok 減少樣板程式碼

---

## ✅ 八、專案狀態

### 已完成功能

- ✅ UPMS 基礎功能（User、Role、Permission、Auth）
- ✅ ADM 基礎功能（字典、參數）
- ✅ FMS 基礎功能（Vehicle、Driver、Dispatch）
- ✅ OrderCore 與 TOM 基礎架構
- ✅ JWT 認證與授權
- ✅ Swagger / OpenAPI 文件
- ✅ 統一例外處理
- ✅ 資料庫審計欄位（BaseEntity）

### 進行中

- 🔄 TOM 訂單流程完整實作
- 🔄 FMS 派工邏輯完善

### 規劃中

- 📋 訂單狀態機完整實作
- 📋 車輛維修與保養記錄完整功能
- 📋 司機 Web App（手機版）
- 📋 前端 React 整合
- 📋 微服務拆分準備（2026+）

---

## 📚 相關文件

- [業務邏輯定義文件](./業務邏輯定義文件.md)
- [20260110後端程式碼優化建議](./20260110後端程式碼優化建議.md)

---

## 👥 開發團隊

本專案採用模組化單體架構，為未來微服務化預留彈性。

**技術架構：**
- 採用 DDD（Domain-Driven Design）設計理念
- 採用 Hexagonal Architecture（Port-Adapter）模式
- 採用 Clean Architecture 分層原則

---

**最後更新：2026-01-10**
