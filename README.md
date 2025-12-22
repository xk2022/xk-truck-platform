好，我來幫你把 **xk-truck-platform + xk-pets-platform** 的共同架構「抽象成一份標準平台 README」，
再順便幫你 **切一刀：哪些是共用、哪些是專案獨立**，方便你之後做真正的模組化。

下面這份你可以想像成：

> 🧩「XK Backend Standard Platform」
> 所有 `xk-*-platform`（truck、pets、未來任何專案）都可以用同一套 README & 架構

---

## ✅ 一、標準共用 README（可貼到各專案根目錄）

你可以把下面整段複製成 `README.md`，
只要在「專案資訊」那一段，針對 truck / pets 各自填上就好。

---

````markdown
# 🌈 XK Backend Platform (Standard Architecture)

本專案採用 **模組化單體（Modular Monolith）+ Multi-Module Maven** 架構，  
所有 XK 系列專案（例如：`xk-truck-platform`、`xk-pets-platform`）皆共用 **相同的後端框架與開發模型**。

---

## 1️⃣ 專案資訊（依專案填寫）

> 這一段依照實際專案補充  
> 例如：
> - 專案名稱：`xk-truck-platform`
> - 角色：物流訂單 / 車輛 / 司機管理（TMS / FMS）
> - 專案名稱：`xk-pets-platform`
> - 角色：寵物媒合 / 服務上架 / 預約管理（PET-GO）

- **專案代號**：`xk-????-platform`
- **主要用途**：`<請填寫：例如「物流管理系統 / 寵物媒合平台 / …」>`
- **產品模組**（舉例）：
  - 🔐 UPMS（使用者 / 角色 / 權限）
  - 🧾 ADM（系統字典 / 參數）
  - 🚚 FMS（車輛 / 司機）/ 🐾 Service（服務上架）
  - 📦 Order / Booking（訂單 / 預約）

---

## 2️⃣ 統一技術棧（所有 XK 專案共用）

| 類別 | 技術 |
|------|------|
| 語言 | Java 21 |
| 框架 | Spring Boot 3.5.x |
| Core | Spring Framework 6.2.x |
| Web | spring-boot-starter-web |
| ORM | spring-boot-starter-data-jpa + Hibernate 6.6.x |
| 驗證 | spring-boot-starter-validation（Jakarta Validation 3） |
| 安全 | Spring Security 6.5.x + 自訂 JWT |
| DB | MySQL 8.x（HikariCP） |
| 文件 | springdoc-openapi-starter-webmvc-ui 2.8.x |
| JWT | io.jsonwebtoken: jjwt-api / impl / jackson 0.13.0 |
| 其他 | Lombok、Apache Commons Lang3 |

> ✅ 所有 `xk-*-platform` 專案都「固定」這一套技術版本，方便共用模組與文件。

---

## 3️⃣ 標準目錄結構（範本）

每一個平台專案（例如 `xk-truck-platform` / `xk-pets-platform`）的標準結構如下：

```text
xk-xxxx-platform/
│
├── pom.xml                     # Parent POM：版本 / 外掛 / 相依管理（共用規格）
│
├── xk-base/                    # ✅ 全系列共用：基礎框架模組
│   ├── pom.xml
│   └── src/main/java/com/xk/base
│       ├── config              # OpenAPI, SecurityProps, Jackson, 時區等共用設定
│       ├── domain/model        # BaseEntity、共用抽象類別
│       ├── exception           # BusinessException、GlobalExceptionHandler
│       ├── security            # JWT、安全自動配置、Filter、UserDetails 介面
│       ├── util                # XkBeanUtils 等共用工具
│       └── web                 # ApiResult<T>、共用 API 回傳格式
│
├── xk-upms/                    # ✅（建議抽共用）使用者 / 角色 / 權限模組
│   ├── controller
│   ├── domain
│   ├── repository
│   ├── service
│   └── dto
│
├── xk-adm/                     # ✅（建議抽共用）系統字典 / 參數設定
│
├── xk-<domain1>/               # 🔁 依產品不同，例如 truck = fms / order，pets = service / booking
│
├── xk-<domain2>/               # （依專案規劃）
│
└── xk-application/             # Spring Boot 啟動模組（App.java 入口）
````

> ✅ **規則：**
>
> * `xk-base` = 所有專案共用技術基底
> * `xk-upms` / `xk-adm` = 可重用的「業務中台」模組（建議共用）
> * 其餘 `xk-????` = 各專案自己的 Domain 模組（Truck / Pets 各自實作）

---

## 4️⃣ 共用架構與慣例（全部專案都要遵守）

### 4.1 分層概念

所有 XK 專案採用統一分層：

* `controller`：HTTP API 入口（只負責 request/response）
* `service`：業務邏輯（加 `@Transactional`）
* `repository`：資料庫操作（Spring Data JPA）
* `domain / model`：Entity / enum / value object
* `dto`：request / response / query 物件

---

### 4.2 ApiResult 統一回傳格式（`xk-base` 提供）

```java
public class ApiResult<T> {
    private int code;
    private String message;
    private T data;
    private String timestamp;
}
```

標準用法：

```java
return ApiResult.success(data);
return ApiResult.failure(HttpStatus.BAD_REQUEST, "錯誤訊息", errorDetail);
```

API 回應範例：

```json
{
  "code": 0,
  "message": "success",
  "data": { },
  "timestamp": "2025-01-01T00:00:00"
}
```

---

### 4.3 BaseEntity 共用欄位

所有 Entity 一律繼承 `BaseEntity`（於 `xk-base`）
典型欄位（依實作略有差異）：

```java
public abstract class BaseEntity {

    @Id
    private String uuid;

    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    @Version
    private Long version;
}
```

---

### 4.4 XkBeanUtils 共用規範

`XkBeanUtils` 為所有專案統一使用的 Bean 工具：

```java
// DTO ↔ Entity 轉換
UserResp resp = XkBeanUtils.copyProperties(entity, UserResp::new);

// 部份更新（例如 PATCH）
XkBeanUtils.copyNonNullProperties(req, entity);
```

**規範：**

* 不要自己再 new 一堆 BeanUtils
* Create / Update / Patch 一律用 `XkBeanUtils`，避免邏輯分散

---

### 4.5 安全架構（JWT + Spring Security）

共用模組 `xk-base.security` 提供：

* `SecurityAutoConfig`：自動配置 Spring Security
* `JwtAuthFilter`：解析 Bearer Token
* `JwtService`：產生 / 驗證 JWT
* `SecurityProps`：白名單路徑、Token 參數
* 共用的 `XkUserDetailsService` interface
  → 各專案實作自己的 User 載入邏輯（Truck / Pets 各自有實作）

標準流程：

1. `POST /auth/login` 取得 JWT
2. 前端以 `Authorization: Bearer <token>` 呼叫 `/api/**`
3. `JwtAuthFilter` 驗證 Token → 注入 `SecurityContextHolder`
4. Controller 透過 `@AuthenticationPrincipal` 或 `SecurityContext` 取得登入者資訊

---

### 4.6 Swagger / OpenAPI 統一設定

共用規則：

* Security Scheme 名稱統一為 `bearerAuth`
* 採用 `Authorization: Bearer <token>` 模式
* 共用的 `OpenAPIConfig` 於 `xk-base.config` 中定義

Swagger UI 統一路徑：

```text
http://localhost:8080/swagger-ui/index.html
```

---

## 5️⃣ Domain 模組示範：Truck vs Pets 如何套進同一架構

### 5.1 共用領域模組（建議抽成共用）

這些是 **所有專案都會用到的**，建議做成「共用模組」，被 truck / pets 同時引用：

1. `xk-upms`（User / Role / Permission）

    * Truck：系統管理者 / 車隊管理者 / 調度 / 司機
    * Pets：系統管理者 / 店家 / 服務提供者 / 一般會員
2. `xk-adm`（字典與參數）

    * Truck：車種、車身型式、狀態、訂單來源、付款方式 …
    * Pets：服務分類、寵物種類、服務地區、費率類型 …

> ✅ 建議：
>
> * 你未來可以把 truck / pets 現有的 UPMS / ADM 抽成 **真正獨立專案**，
> * 或在 `xk-system` 大 Monorepo 裡變成共用 module，所有 app 依賴。

---

### 5.2 Truck 專案專屬模組（只屬於 xk-truck-platform）

建議命名：

* `xk-fms`：Fleet Management System（車輛 / 司機）
* `xk-tom`：Transport Order Management（運輸訂單）

職責：

* `xk-fms`：

    * Vehicle：車頭、拖板、車牌、里程、維修紀錄、狀態（空車 / 行駛 / 維修）
    * Driver：司機資料、駕照、可接單區域、排班
* `xk-tom`：

    * Order：運輸訂單（起訖地、貨櫃、貨品）
    * Flow：指派車輛 + 司機、狀態流轉（CREATED → ASSIGNED → IN_TRANSIT → COMPLETED）

---

### 5.3 Pets 專案專屬模組（只屬於 xk-pets-platform）

建議命名：

* `xk-service`：ServiceItem（服務上架 / 審核）
* `xk-booking`：預約流程
* `xk-provider`：店家 / 服務提供者資料

職責：

* `xk-service`：

    * ServiceItem：服務名稱、分類、價錢、時長、地區、狀態（DRAFT / PENDING_REVIEW / APPROVED / DISABLED）
* `xk-booking`：

    * 預約單：客戶、服務項目、時段、狀態（BOOKED / CONFIRMED / COMPLETED / CANCELED）
* `xk-provider`：

    * 店家/服務提供者資料、商業資訊、評價平均分數

---

## 6️⃣ 統一前端結構約定（Truck / Pets 同一套模型）

前端（React）統一用這個 pattern（不管是 Truck 還是 Pets）：

```text
src/app/pages
│
├─ <domain>/<feature>/
│  ├─ Overview.tsx       # 頁面入口（組裝 List + Filter + Modal）
│  ├─ List.tsx           # 列表 Table（支援分頁 / 搜尋）
│  ├─ FormModal.tsx      # ✅ 單一共用 Create + Edit 元件
│  ├─ Query.ts           # 型別定義：分頁 / 篩選條件
│  └─ Model.ts           # 資料模型（對應後端 DTO）
```

規則：

* 不再使用 `CreateModal.tsx` / `EditModal.tsx` 分開寫
* `FormModal` 用 `uuid` 是否存在決定「新增 / 編輯」
* 新增成功後用 `setListKey(prev => prev + 1)` 重新掛載 `List`

---

## 7️⃣ 環境設定與啟動（所有專案共用）

### 7.1 環境需求

* JDK 21
* Maven 3.9+
* MySQL 8.x

### 7.2 建立資料庫範例

```sql
CREATE DATABASE xk_????_dev
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

> `xk_truck_dev` / `xk_pets_dev` 依專案命名

### 7.3 `application-dev.yml` 範例

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/xk_????_dev?useSSL=false&serverTimezone=Asia/Taipei&characterEncoding=utf8
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2

  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect

jwt:
  secret: xkSecretKey
  expiration: 86400000
```

### 7.4 啟動命令

```bash
mvn clean package -DskipTests
java -jar xk-application/target/xk-application.jar --spring.profiles.active=dev
```

---

## 8️⃣ 給未來 GPT 的專用說明（關鍵段落）

> 每當你開新 GPT 專案協作時，可以直接貼這一段：

> 「這個專案是 XK 系列標準後端平台的一員，使用 Java 21 + Spring Boot 3.5，
> 採用模組化單體架構，root 為 `xk-????-platform`，
> 共有共用模組 `xk-base`（ApiResult / BaseEntity / JWT / OpenAPI）、
> 以及潛在共用模組 `xk-upms`（使用者 / 角色 / 權限）、`xk-adm`（字典與參數），
> 其他 domain 模組依專案不同（例如：truck = fms/order、pets = service/booking）。
> 所有 Controller 都使用 ApiResult 統一回應，DTO/Entity 轉換透過 XkBeanUtils。
> 請依照這套架構協助我新增 / 調整後端模組與前端頁面。」

```

---

## 🔪 幫你把「共用 vs 專案差異」切出來（摘要）

你之後如果要真的做「共用 repo」或「xk-system 大 Monorepo」，可以照這樣拆：

### 🟦 一、**100% 共用（應該抽成單獨共用模組 / Library）**

- `xk-base`
  - config（OpenAPI / Jackson / TimeZone）
  - web（ApiResult）
  - exception（BusinessException / GlobalExceptionHandler）
  - security（JWT / SecurityAutoConfig / JwtAuthFilter / XkUserDetails…）
  - util（XkBeanUtils）

> 這一塊未來可以做成：  
> 👉 `xk-platform-base`（獨立 git + 發布成 Maven artifact）

---

### 🟩 二、**跨專案共用業務（建議獨立 module，被各 app 引入）**

- `xk-upms`：User / Role / Permission / Auth
- `xk-adm`：Dictionary / Parameter

> Truck、Pets 都會用到，  
> 之後其他專案（例如 xk-rotaract, xk-erp）也都可以直接吃。

---

### 🟧 三、**Truck 專屬模組**

- `xk-fms`：Vehicle / Driver / Fleet
- `xk-tom`：Transport Order / Dispatch

---

### 🟨 四、**Pets 專屬模組**

- `xk-service`：ServiceItem 上架與審核
- `xk-booking`：預約單
- `xk-provider`：服務提供者 / 店家

---

如果你願意，**下一步** 我可以幫你做其中一個：

1. 寫一份「**xk-base + xk-upms + xk-adm 的獨立共用專案 README**」（準備抽成 `xk-platform-base`）  
2. 幫你畫一張「**XK 系列專案模組關係圖（含 truck / pets / 未來系統）**」  
3. 幫你把「目前 truck + pets 中的 UPMS / ADM 內容」整理成「**單一共用 ERD + 模型表**」

你可以直接回：  
👉「我要 1」 / 「我要 2」 / 「我要 3」 我就直接生給你。
```
