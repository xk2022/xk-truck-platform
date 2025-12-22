# 🚚 XK Truck Platform

XK Truck Platform 是一個以 **Spring Boot 3.x + Java 21** 為核心的  
**車輛運輸與派遣管理系統（MVP）**，採用 **多模組單體架構**，  
為日後擴充為微服務版本（2026+）預留完整彈性。

---

## 📁 專案結構（Maven Multi-Module）

xk-truck-platform/
│
├── pom.xml # Parent POM (dependencyManagement / modules)
│
├── xk-base/ # 共用核心模組
│ ├── security/ # JWT、SecurityConfig、自動配置
│ ├── web/ # ApiResult、GlobalExceptionHandler、ResponseAdvice
│ ├── exception/ # 共用例外（ResourceNotFound、BusinessException）
│ └── ... # 其他共用元件
│
└── xk-truck/ # 主業務模組（運輸系統 MVP）
├── api/ # REST API Controllers
├── domain/ # 實體與商業邏輯層
├── repository/ # JPA Repository
├── service/ # 業務服務層
├── config/ # 模組專屬設定（Security、Seed、Swagger）
├── web/ # 模組級攔截器與例外處理
└── resources/
└── application.yml

---

## ⚙️ 技術棧（Tech Stack）

| 項目 | 使用技術 | 備註 |
|------|-----------|------|
| 後端框架 | **Spring Boot 3.3.x** | Java 21, Maven |
| 安全機制 | Spring Security 6.x + JWT (JJWT 0.13.0) | Token 驗證 |
| ORM / DB | Spring Data JPA + MySQL 8.x | 支援 Docker Compose 啟動 |
| 文件系統 | Springdoc OpenAPI 2.x | Swagger UI 文件 |
| JSON | Jackson Databind | 統一序列化格式 |
| 日誌 | SLF4J + Logback | 預設於 `resources/logback.xml` |
| 工具 | Lombok | 精簡樣板程式碼 |

---

## 🚀 啟動方式


1️⃣ 先啟動 MySQL（Docker Compose）
bash
docker compose up -d

2️⃣ 編譯與安裝所有模組
mvn clean install -U

3️⃣ 啟動主應用（xk-truck）
cd xk-truck
mvn spring-boot:run

4️⃣ 驗證服務啟動成功

Swagger UI → http://localhost:8080/swagger-ui.html

API Docs → http://localhost:8080/v3/api-docs

Health Check → http://localhost:8080/actuator/health

🔐 安全機制說明（Security）

使用 JWT 進行 Token 驗證

預設放行：

/auth/login

/swagger-ui/**

/v3/api-docs/**

/actuator/health

登入測試帳號（InMemoryUser）：

admin / admin123

dispatcher / dispatcher123

登入流程

POST /auth/login
→ 取得 JWT Token

後續 API 於 Header 加入

Authorization: Bearer {token}

🧩 API 統一回應格式（ApiResult）
{
  "code": 200,
  "message": "OK",
  "data": {},
  "errorDetails": null,
  "timestamp": "2025-10-29T10:30:00"
}


ApiResponseAdvice：自動包裝所有 API 回傳

TruckExceptionHandler：統一例外格式

TruckSecurityHandlerConfig：統一 401 / 403 回應

🌱 種子資料（Seed Config）

可在 application.yml 中開啟：

upms:
  seed:
    enabled: true


啟動後將自動建立：

預設角色（ADMIN / DISPATCH）

初始管理者帳號（admin / admin123）

🧱 開發順序建議（MVP 階段）
模組	功能	狀態
UPMS	使用者 / 角色 / 權限管理	⏳ 進行中
ADM	系統設定（字典檔 / 日誌）	🔜 下一階段
TRUCK	車輛管理 / 訂單管理	🔜 計畫中
REPORT	簡易報表導出	🕓 未開始
🧭 未來規劃（2025~2026）
項目	說明
模組拆分	xk-upms、xk-adm 對外獨立
微服務化	Gateway + Auth Service + Truck Service
前端整合	Angular 18 + Tailwind（預計2026Q1）
部署方案	Docker / NAS / Vercel / Kubernetes
SaaS 化	Template 平台：可快速複製客戶系統
👨‍💻 貢獻開發者
名稱	角色	聯絡方式
Louis 陳祿元	系統架構師 / 專案經理	GitHub

Hank	後端工程師	
Tim	全端工程師	
Lisa	專案管理 / 測試協調	
🧰 常用 IntelliJ 快捷鍵（macOS）
功能	快捷鍵
自動 Import	⌥ + Enter
自動排版	⌘ + ⌥ + L
最佳化 Import	⌃ + ⌥ + O
全域搜尋	⇧ + ⇧
折疊/展開程式碼	⌘ + - / +
🧾 License

Copyright © 2025 XploreKaleidoscope
All rights reserved.


---

## 🔍 補充說明
- 已對應你的實際開發結構（xk-base / xk-truck）  
- 採用「開發者導向」語氣（讓新成員立即可啟動）  
- 包含實際啟動命令、Swagger 測試方式、後續模組演進規劃  

---

要我幫你自動加上「版本徽章區塊」嗎？  
像是：

```markdown
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/SpringBoot-3.3.2-brightgreen)
![License](https://img.shields.io/badge/license-Private-lightgrey)
