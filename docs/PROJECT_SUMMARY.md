# TEU MOBILE - BÁO CÁO TỔNG QUAN DỰ ÁN & HƯỚNG DẪN VẬN HÀNH
> Ngày cập nhật: 04/11/2026 | Nhánh Git: `develop` | Tác giả: lovanky & Antigravity Assistant

---

## 1. Tổng Quan Dự Án & Công Nghệ Chủ Đạo

Dự án **Teu Mobile** là một nền tảng thương mại điện tử chuyên cung cấp điện thoại thông minh và phụ kiện công nghệ cao cấp.

### Công nghệ cốt lõi:
- **Backend**: Java 21, Spring Boot 3.3.5, Maven.
- **Database**: Microsoft SQL Server 2022 (quản lý di chuyển schema bằng **Flyway**).
- **Identity Provider (IdP)**: **Keycloak 26 (Quarkus)** kết hợp **Spring Cloud OpenFeign** và **Spring Security OAuth2 Resource Server**.
- **Mapping DTO**: **MapStruct 1.5.5** kết hợp **Lombok** (xử lý compile-time, zero reflection).
- **Frontend**: **React 18**, **Vite 6**, **Tailwind CSS 3.4**, **Lucide Icons**, **Axios**, **React Router DOM 6**.
- **Testing**: **JUnit 5**, **Mockito**, **Spring Security Test**, **MockMvc** (28/28 tests passed).

---

## 2. Bảng Phân Bổ Cổng Dịch Vụ (Port Mapping)

| Dịch vụ | Công nghệ | Cổng Host | Mục đích sử dụng |
| :--- | :--- | :--- | :--- |
| **Frontend** | React + Vite | `5173` | Giao diện người dùng (`http://localhost:5173`) |
| **Backend** | Spring Boot | `8080` | REST API (`http://localhost:8080/api`) |
| **Keycloak Server** | Keycloak Quarkus | `8180` | Máy chủ xác thực OIDC / Admin (`http://localhost:8180`) |
| **Database Chính** | SQL Server 2022 | `1433` | Lưu dữ liệu website (`teu_mobile_db`) |
| **Keycloak DB** | PostgreSQL 16 | `5432` | DB nội bộ lưu session và tài khoản Keycloak |

---

## 3. Kiến Trúc Hạ Tầng (Docker Compose)

File `docker-compose.yml` tại thư mục gốc quản lý 3 dịch vụ nền tảng:
1. **`sqlserver`**: Microsoft SQL Server 2022 Developer Edition, dữ liệu lưu vào volume `mssql_data`.
2. **`sqlserver-init`**: Container phụ trợ tự động chạy script `docker/sqlserver/init-db.sql` để tạo sẵn database `teu_mobile_db`.
3. **`keycloak-db`**: PostgreSQL 16 Alpine làm DB riêng cho Keycloak (tách biệt dữ liệu tài khoản với dữ liệu bán hàng).
4. **`keycloak`**: Keycloak 26.1.3 chạy chế độ `start-dev`, map cổng `8180:8080` để không đụng độ cổng `8080` của Spring Boot.

---

## 4. Kiến Trúc Backend (Spring Boot 3)

### A. Custom Validators (`com.teumobile.validation`)
- `@ValidPhoneNumber`: Kiểm tra chuẩn 10 chữ số di động Việt Nam (đầu số 03, 05, 07, 08, 09 hoặc +84).
- `@ValidUsername`: Kiểm tra username từ 3 - 30 ký tự, không chứa khoảng trắng.
- `@ValidEmail`: Kiểm tra định dạng email theo chuẩn RFC 5322.

### B. Cơ Sở Dữ Liệu & Flyway Migration
- `db/migration/V1__create_users_table.sql`: Tạo bảng `users` (`id`, `keycloak_id`, `username`, `email`, `full_name`, `phone_number`, `address`, `created_at`, `updated_at`).
- Hibernate đặt chế độ `ddl-auto: validate` nhường toàn quyền quản lý schema cho Flyway.

### C. MapStruct DTO Mapping
- `UserMapper`: Chuyển đổi siêu tốc giữa `User` Entity và các DTOs (`UserProfileResponseDto`, `UserUpdateRequestDto`), tự động bỏ qua các trường null khi cập nhật.

### D. Tích hợp OpenFeign gọi Keycloak (Authentication Gateway)
Backend đóng vai trò Gateway nhận request từ React và dùng OpenFeign gọi Keycloak:
- `KeycloakAuthFeignClient`:
  - `POST /realms/{realm}/protocol/openid-connect/token`: Đăng nhập cấp phát JWT (`grant_type=password`) và làm mới token (`grant_type=refresh_token`).
  - `POST /realms/{realm}/protocol/openid-connect/logout`: Thu hồi session.
- `KeycloakAdminFeignClient`:
  - `POST /admin/realms/{realm}/users`: Tạo tài khoản người dùng trực tiếp trên Keycloak qua Service Access Token được cấp bằng `client_credentials` (`client_id` và `client_secret` của Realm ứng dụng).
  - `GET /admin/realms/{realm}/users`: Tìm kiếm người dùng.
- `AuthService`: Tự động đồng bộ tài khoản vừa tạo trên Keycloak vào bảng `users` của SQL Server.

### E. Danh sách REST API Endpoints

#### Authentication (`/api/auth`) - Không cần Token:
- `POST /api/auth/register`: Đăng ký tài khoản mới (Body: `RegisterRequestDto`).
- `POST /api/auth/login`: Đăng nhập lấy JWT Access Token và Refresh Token (Body: `LoginRequestDto`).
- `POST /api/auth/refresh`: Làm mới token (Body: `RefreshTokenRequestDto`).
- `POST /api/auth/logout`: Đăng xuất.

#### User Profile (`/api/users`) - Yêu cầu Bearer Token:
- `GET /api/users/me`: Lấy thông tin hồ sơ của tài khoản đang đăng nhập.
- `PUT /api/users/me`: Cập nhật thông tin cá nhân (Họ tên, SĐT, Địa chỉ).
- `POST /api/users/sync`: Đồng bộ thủ công từ token vào SQL Server.
- `GET /api/users/{id}`: Xem thông tin user theo ID (Yêu cầu quyền `ROLE_ADMIN`).

#### Public Endpoints:
- `GET /api/public/health`: Kiểm tra trạng thái hoạt động của backend.

---

## 5. Kết Quả Kiểm Thử (Unit & Integration Tests)

Tổng cộng **28/28 tests passed 100%** qua lệnh `mvn test`:
1. `ValidatorTest` (6 tests): Kiểm tra số điện thoại, email, username hợp lệ và không hợp lệ.
2. `KeycloakJwtAuthenticationConverterTest` (2 tests): Kiểm tra bóc tách `realm_access.roles` sang quyền `ROLE_...`.
3. `UserMapperTest` (2 tests): Kiểm tra MapStruct chuyển đổi DTO và bảo toàn trường dữ liệu.
4. `UserServiceTest` (4 tests): Kiểm thử logic nghiệp vụ user với Mockito.
5. `AuthServiceTest` (4 tests): Kiểm thử logic OpenFeign đăng nhập, đăng ký, refresh token.
6. `UserControllerTest` (6 tests): MockMvc test bảo mật, 401 khi thiếu token, 403 khi thiếu role Admin.
7. `AuthControllerTest` (4 tests): MockMvc test luồng đăng nhập/đăng ký và bắt lỗi validation.

---

## 6. Thiết Kế UI/UX & Frontend (React + Tailwind CSS)

Thiết kế dựa trên file Figma `figma/login_page.png` với triết lý **Minimalist High-Tech with Dynamic Energy**:
- **Bảng màu**: Đỏ Coral (`#EA384D`) làm điểm nhấn hành động, chữ đen Slate 900, nền trắng tinh khiết kết hợp xám khói Slate 100.
- **Bo góc**: Mềm mại kiểu công nghệ hiện đại `rounded-xl` (12px) đến `rounded-2xl` (16px).
- **Atomic Components**:
  - `Button.jsx`: Nút primary đỏ coral, nút outline trắng viền xám cho Google, nút ghost.
  - `Input.jsx`: Bo góc lớn `rounded-xl`, focus ring đỏ coral, toggle ẩn/hiện mật khẩu, hiển thị thông báo lỗi.
  - `Checkbox.jsx`: Kiểu dáng checkbox *Remember me*.
  - `AuthLayout.jsx`: Bố cục chia đôi (Split-screen) với Artwork năng động mô phỏng chính xác Figma.
- **Các màn hình đã hoàn thiện**:
  - `LoginPage.jsx`: Tái hiện 100% Figma (`WELCOME BACK`, Email, Password, Remember me, Forgot password, Sign in, Sign in with Google, Sign up link).
  - `RegisterPage.jsx`: Thừa hưởng cùng ngôn ngữ thiết kế (`CREATE ACCOUNT`).
  - `HomePage.jsx` & `Navbar.jsx`: Trang chủ mua sắm điện thoại (iPhone, Galaxy) và phụ kiện (sạc GaN, tai nghe AirPods), giỏ hàng, dropdown user.
  - `ProfilePage.jsx`: Xem và cập nhật thông tin cá nhân đồng bộ với backend.
- **Dịch vụ**: `api.js` (Axios interceptor JWT) và `AuthContext.jsx` (quản lý trạng thái đăng nhập).
- Build production: `npm run build` hoàn tất chỉ trong **2.27s**, không lỗi.

---

## 7. Hướng Dẫn Khởi Chạy Dự Án

### Bước 1: Khởi động Hạ tầng Docker (SQL Server + Keycloak)
```bash
docker compose up -d
```
*Truy cập Keycloak Admin tại: `http://localhost:8180` (User: `admin`, Pass: `admin`)*.

### Bước 2: Tạo database SQL Server (nếu chạy lần đầu)
```bash
docker exec -it teu-mobile-sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "TeuMobile@2026!" -C -Q "IF DB_ID('teu_mobile_db') IS NULL CREATE DATABASE teu_mobile_db;"
```

### Bước 3: Khởi chạy Backend (Spring Boot)
```bash
cd backend
mvn spring-boot:run
# Backend sẽ lắng nghe tại cổng http://localhost:8080
```

### Bước 4: Khởi chạy Frontend (React)
```bash
cd frontend
npm run dev
# Mở trình duyệt truy cập: http://localhost:5173/login
```

---

## 8. Cấu Trúc Thư Mục Toàn Bộ Dự Án

```text
teu-mobile/
├── .env                        # File biến môi trường hạ tầng (đã gitignore)
├── .env.example                # Template mẫu cho biến môi trường
├── .gitignore                  # Cấu hình loại trừ file nhạy cảm và build
├── docker-compose.yml          # Cấu hình SQL Server, Keycloak, Postgres
├── docs/
│   ├── DESIGN_SYSTEM.md        # Quy chuẩn thiết kế UI/UX theo Figma
│   └── PROJECT_SUMMARY.md      # Tài liệu tổng hợp toàn diện dự án
├── docker/
│   └── sqlserver/
│       └── init-db.sql         # Script SQL tạo database teu_mobile_db
├── figma/
│   └── login_page.png          # Ảnh thiết kế gốc giao diện Đăng nhập
├── backend/                    # Spring Boot 3 + Maven
│   ├── pom.xml                 # Cấu hình dependencies (Flyway, MapStruct, OpenFeign, OAuth2)
│   └── src/
│       ├── main/
│       │   ├── java/com/teumobile/
│       │   │   ├── TeuMobileApplication.java
│       │   │   ├── client/       # OpenFeign Clients (Auth, Admin)
│       │   │   ├── config/       # SecurityConfig, KeycloakProperties, FeignForm
│       │   │   ├── controller/   # AuthController, UserController, PublicController
│       │   │   ├── dto/          # DTOs (Auth, User, Keycloak representations)
│       │   │   ├── entity/       # User Entity (JPA)
│       │   │   ├── exception/    # GlobalExceptionHandler, ResourceNotFoundException
│       │   │   ├── mapper/       # UserMapper (MapStruct)
│       │   │   ├── repository/   # UserRepository
│       │   │   ├── security/     # KeycloakJwtAuthenticationConverter
│       │   │   ├── service/      # AuthService, UserService và implementations
│       │   │   └── validation/   # Custom Validators (Phone, Username, Email)
│       │   └── resources/
│       │       ├── application.yaml
│       │       └── db/migration/
│       │           └── V1__create_users_table.sql
│       └── test/                 # 28 Unit & Integration tests
└── frontend/                   # React 18 + Vite + Tailwind CSS
    ├── package.json
    ├── tailwind.config.js
    ├── vite.config.js
    └── src/
        ├── components/
        │   ├── layout/         # AuthLayout (Split-screen), Navbar
        │   └── ui/             # Button, Input, Checkbox
        ├── context/            # AuthContext.jsx
        ├── pages/              # LoginPage, RegisterPage, HomePage, ProfilePage
        └── services/           # api.js (Axios JWT Interceptor)
```
