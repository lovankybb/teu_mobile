# TEU MOBILE - BÁO CÁO TỔNG QUAN DỰ ÁN & HƯỚNG DẪN VẬN HÀNH
> Ngày cập nhật: 18/11/2026 | Nhánh Git: `develop` | Tác giả: lovanky & Antigravity Assistant

---

## 1. Tổng Quan Dự Án & Công Nghệ Chủ Đạo

Dự án **Teu Mobile** là một nền tảng thương mại điện tử chuyên cung cấp điện thoại thông minh và phụ kiện công nghệ cao cấp.

### Công nghệ cốt lõi:
- **Backend**: Java 21, Spring Boot 3.3.5, Maven.
- **Database**: Microsoft SQL Server 2022 (quản lý di chuyển schema bằng **Flyway**).
- **Identity Provider (IdP)**: **Keycloak 26 (Quarkus)** kết hợp **Spring Security OAuth2 Resource Server** và **Authorization Code Flow with PKCE**.
- **Lưu trữ hình ảnh**: **Cloudinary SDK** (Direct unsigned/signed uploads, tối ưu hóa avatar, CDN toàn cầu).
- **Dữ liệu hành chính**: Phân cấp hành chính Việt Nam 3 cấp (Tỉnh/Thành phố, Quận/Huyện, Phường/Xã) tích hợp Open API hành chính chuẩn.
- **Mapping DTO**: **MapStruct 1.5.5** kết hợp **Lombok** (xử lý compile-time, zero reflection).
- **Frontend**: **React 18**, **Vite 6**, **Tailwind CSS 3.4**, **Lucide Icons**, **Axios**, **React Router DOM 6**, **keycloak-js 26**.
- **Testing & Code Coverage**: **JUnit 5**, **Mockito**, **Spring Security Test**, **MockMvc** (84/84 tests passed 100%) kết hợp **JaCoCo 0.8.12** (độ bao phủ dòng code **90%**, chỉ tiêu tối thiểu tự động **80%**).

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

File `docker-compose.yml` tại thư mục gốc quản lý các dịch vụ nền tảng:
1. **`sqlserver`**: Microsoft SQL Server 2022 Developer Edition, dữ liệu lưu vào volume `mssql_data`.
2. **`sqlserver-init`**: Container phụ trợ tự động chạy script `docker/sqlserver/init-db.sql` để tạo sẵn database `teu_mobile_db`.
3. **`keycloak-db`**: PostgreSQL 16 Alpine làm DB riêng cho Keycloak (tách biệt dữ liệu tài khoản với dữ liệu bán hàng).
4. **`keycloak`**: Keycloak 26.1.3 chạy chế độ `start-dev`, map cổng `8180:8080` để không đụng độ cổng `8080` của Spring Boot, tích hợp sẵn Theme Teu Mobile và Google Identity Provider.

---

## 4. Kiến Trúc Backend (Spring Boot 3)

### A. Custom Validators (`com.teumobile.validation`)
- `@ValidPhoneNumber`: Kiểm tra chuẩn 10 chữ số di động Việt Nam (đầu số 03, 05, 07, 08, 09 hoặc +84).
- `@ValidUsername`: Kiểm tra username từ 3 - 30 ký tự, không chứa khoảng trắng.
- `@ValidEmail`: Kiểm tra định dạng email theo chuẩn RFC 5322.

### B. Cơ Sở Dữ Liệu & Flyway Migration
- `db/migration/V1__create_users_table.sql`: Tạo bảng `users` (`id`, `keycloak_id`, `username`, `email`, `full_name`, `phone_number`, `address`, `avatar_url`, `created_at`, `updated_at`).
- `db/migration/V2__create_user_addresses_table.sql`: Tạo bảng `user_addresses` quản lý sổ địa chỉ giao hàng (`id`, `user_id`, `recipient_name`, `phone_number`, `province`, `district`, `ward`, `street_address`, `full_address`, `address_type`, `is_default`, `notes`, `created_at`, `updated_at`).
- Hibernate đặt chế độ `ddl-auto: validate` nhường toàn quyền quản lý schema cho Flyway.

### C. MapStruct DTO Mapping
- `UserMapper`: Chuyển đổi siêu tốc giữa `User` Entity và `UserProfileResponseDto`, tự động xử lý cập nhật các trường không null.
- `UserAddressMapper`: Chuyển đổi hai chiều giữa `UserAddress` Entity và `AddressRequestDto` / `AddressResponseDto`.

### D. Kiến Trúc Xác Thực (Keycloak OIDC & PKCE)
- Phía Frontend sử dụng thư viện `keycloak-js` để thực hiện chuẩn **Authorization Code Flow with PKCE**, đảm bảo bảo mật tối đa cho Single Page App (SPA).
- Phía Backend đóng vai trò **OAuth2 Resource Server** xác thực Access Token thông qua `KeycloakJwtAuthenticationConverter`, phân giải claim `realm_access.roles` thành các GrantedAuthority `ROLE_*` của Spring Security.
- Tự động đồng bộ tài khoản người dùng từ Keycloak JWT và Google Identity Provider vào bảng `users` của SQL Server.

### E. Tích Hợp Cloudinary
- Tích hợp `CloudinaryService` cung cấp API cấp phát chữ ký bảo mật (Signature, Timestamp, API Key) cho phép Frontend tải ảnh trực tiếp lên Cloudinary.
- Tự động xóa ảnh đại diện cũ trên Cloudinary khi người dùng tải ảnh mới để tiết kiệm dung lượng lưu trữ.
- Hỗ trợ đồng bộ tự động URL avatar từ tài khoản Google OAuth2 sang cơ sở dữ liệu.

### F. Danh Sách REST API Endpoints

#### User Profile (`/api/users`) - Yêu cầu Bearer Token:
- `GET /api/users/me`: Lấy thông tin hồ sơ của tài khoản đang đăng nhập.
- `PUT /api/users/me`: Cập nhật thông tin cá nhân (Họ tên, SĐT, Địa chỉ cơ bản).
- `POST /api/users/sync`: Đồng bộ tài khoản từ Token Keycloak/Google vào SQL Server.
- `GET /api/users/avatar/signature`: Lấy chữ ký bảo mật để tải ảnh trực tiếp lên Cloudinary.
- `PATCH /api/users/me/avatar`: Cập nhật URL ảnh đại diện sau khi upload Cloudinary thành công.
- `GET /api/users/{id}`: Xem thông tin user theo ID (Yêu cầu quyền `ROLE_ADMIN`).

#### Sổ Địa Chỉ Nhận Hàng (`/api/users/addresses`) - Yêu cầu Bearer Token:
- `GET /api/users/addresses`: Lấy toàn bộ danh sách địa chỉ giao hàng của người dùng.
- `GET /api/users/addresses/{id}`: Xem chi tiết một địa chỉ theo ID.
- `POST /api/users/addresses`: Thêm địa chỉ mới vào sổ địa chỉ (tự động xử lý địa chỉ mặc định đầu tiên).
- `PUT /api/users/addresses/{id}`: Chỉnh sửa thông tin địa chỉ nhận hàng.
- `DELETE /api/users/addresses/{id}`: Xóa địa chỉ khỏi sổ địa chỉ (tự động chuyển giao quyền mặc định nếu xóa địa chỉ mặc định).
- `PATCH /api/users/addresses/{id}/default`: Đặt một địa chỉ làm địa chỉ mặc định nhận hàng.

#### Public Endpoints:
- `GET /api/public/health`: Kiểm tra trạng thái hoạt động của backend.

---

## 5. Kết Quả Kiểm Thử (Unit & Integration Tests)

Tổng cộng **84/84 tests passed 100%** qua lệnh `mvn test`:
1. `ValidatorTest` (6 tests): Kiểm tra số điện thoại, email, username hợp lệ và không hợp lệ.
2. `KeycloakJwtAuthenticationConverterTest` (2 tests): Kiểm tra bóc tách `realm_access.roles` sang quyền `ROLE_...`.
3. `UserMapperTest` (2 tests): Kiểm tra MapStruct chuyển đổi DTO và bảo toàn trường dữ liệu.
4. `UserAddressMapperTest` (4 tests): Kiểm tra chuyển đổi 2 chiều giữa UserAddress Entity và Request/Response DTO, trích xuất userId và bảo toàn dữ liệu.
5. `GlobalExceptionHandlerTest` (5 tests): Kiểm thử bộ xử lý ngoại lệ tập trung (404 Not Found, 400 Bad Request, 403 Forbidden, 500 Internal Error, Validation Errors map).
6. `UserServiceTest` (11 tests): Kiểm thử toàn bộ nghiệp vụ user, cập nhật thông tin cá nhân, xử lý avatar Cloudinary, đồng bộ Google avatar và xử lý ngoại lệ not found.
7. `UserAddressServiceTest` (15 tests): Kiểm thử toàn diện nghiệp vụ sổ địa chỉ: tạo mới (địa chỉ đầu/phụ, kiểm soát tối đa 20), cập nhật (đổi mặc định, chặn uncheck mặc định), xóa (địa chỉ phụ, địa chỉ cuối cùng, gán fallback), chuyển mặc định và kiểm soát quyền sở hữu.
8. `CloudinaryServiceTest` (6 tests): Kiểm thử khởi tạo chữ ký signature, tải ảnh từ URL Google, xóa ảnh trên cloud.
9. `AuthServiceTest` (6 tests): Kiểm thử logic xác thực, đăng ký thành công, xử lý trùng lặp tài khoản, fallback khi thiếu Location Header và xử lý xung đột IdP.
10. `PublicControllerTest` (1 test): MockMvc test endpoint công khai `/api/public/health`.
11. `UserControllerTest` (13 tests): MockMvc test bảo mật, lấy hồ sơ, đồng bộ user (`/api/users/sync`), cập nhật hồ sơ, cập nhật avatar, xin chữ ký Cloudinary, phân quyền ADMIN.
12. `UserAddressControllerTest` (11 tests): MockMvc test toàn bộ luồng REST API sổ địa chỉ: danh sách, chi tiết theo ID, thêm mới, sửa, xóa, gán mặc định và bắt lỗi validation/not found.
13. `AuthControllerTest` (2 tests): MockMvc test luồng đăng ký tài khoản và bắt lỗi payload validation.

### Báo Cáo Đo Lường Độ Bao Phủ Mã Nguồn (JaCoCo Code Coverage)
- **Công cụ**: `jacoco-maven-plugin:0.8.12` tích hợp trực tiếp vào vòng đời Maven (phase `test`).
- **Độ bao phủ toàn dự án**: **89% Instruction Coverage** | **90% Line Coverage** (467/519 dòng).
- **Chi tiết độ bao phủ theo Package**:
  - `com.teumobile.controller`: **100%** (Tất cả Controllers đều được bao phủ 100%).
  - `com.teumobile.exception`: **100%** (Tất cả Exception Handlers đều được bao phủ 100%).
  - `com.teumobile.security`: **96%** (Bộ chuyển đổi Token Keycloak và phân quyền).
  - `com.teumobile.service.impl`: **87%** (Nghiệp vụ UserService, UserAddressService, CloudinaryService, AuthService).
  - `com.teumobile.validation`: **84%** (Các Custom Validators).
- **Quy tắc kiểm soát chất lượng (Quality Gate Check)**: Đặt giới hạn tối thiểu **80% Line Coverage** (`BUNDLE` level). Nếu độ bao phủ dưới 80%, bản build Maven sẽ tự động bị chặn và báo lỗi `BUILD FAILURE`.
- **Đường dẫn báo cáo HTML trực quan**: `backend/target/site/jacoco/index.html`.

---

## 6. Thiết Kế UI/UX & Frontend (React + Tailwind CSS)

Thiết kế dựa trên file Figma `figma/login_page.png` và quy chuẩn thương hiệu Teu Mobile:
- **Bảng màu**: Đỏ Coral (`#EA384D`) làm điểm nhấn hành động, chữ đen Slate 900, nền trắng tinh khiết kết hợp xám khói Slate 100.
- **Bo góc**: Mềm mại kiểu công nghệ hiện đại `rounded-xl` (12px) đến `rounded-2xl` (16px).
- **Hệ thống Components & Pages**:
  - `Navbar.jsx`: Thanh điều hướng chứa Logo thương hiệu, menu sản phẩm, giỏ hàng và User Avatar Dropdown.
  - `LoginPage.jsx` & `RegisterPage.jsx`: Đăng nhập/đăng ký qua Keycloak PKCE và Google One-tap.
  - `ProfilePage.jsx`: Trung tâm quản lý tài khoản người dùng:
    - Tab 1: **Thông tin cá nhân** (Họ tên, SĐT, Email), upload trực tiếp ảnh đại diện lên Cloudinary với preview, loading spinner và đồng bộ avatar Google.
    - Tab 2: **Sổ địa chỉ nhận hàng** với đầy đủ tính năng: danh sách địa chỉ, ghim địa chỉ mặc định, nhãn phân loại Nhà riêng / Văn phòng.
  - `AddressList.jsx`, `AddressCard.jsx`, `AddressModal.jsx`: Bộ giao diện sổ địa chỉ với phân cấp hành chính 3 cấp (Tỉnh/Thành -> Quận/Huyện -> Phường/Xã) tự động lọc theo cấp cha, tự động format địa chỉ đầy đủ.
  - `HomePage.jsx`: Trang chủ mua sắm flagship phone và phụ kiện cao cấp.
- **Services**:
  - `keycloak.js`: Quản lý phiên OIDC, silent refresh token, login, logout.
  - `api.js`: Axios instance tự động đính kèm Bearer JWT và xử lý lỗi xác thực.
  - `addressService.js`: Gọi các API backend cho sổ địa chỉ.
  - `locationService.js`: Tích hợp dữ liệu hành chính Việt Nam với bộ nhớ đệm (caching).
  - `cloudinaryService.js`: Upload ảnh trực tiếp từ trình duyệt lên Cloudinary qua Secure Signature.

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
# Mở trình duyệt truy cập: http://localhost:5173
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
│   ├── pom.xml                 # Cấu hình dependencies (Flyway, MapStruct, Cloudinary, OAuth2, JaCoCo)
│   └── src/
│       ├── main/
│       │   ├── java/com/teumobile/
│       │   │   ├── TeuMobileApplication.java
│       │   │   ├── client/       # OpenFeign Clients (Keycloak Admin)
│       │   │   ├── config/       # SecurityConfig, CloudinaryConfig, FeignConfig
│       │   │   ├── controller/   # UserController, UserAddressController, PublicController
│       │   │   ├── dto/          # DTOs (Profile, Address, Cloudinary, Keycloak)
│       │   │   ├── entity/       # User, UserAddress, AddressType (JPA)
│       │   │   ├── exception/    # GlobalExceptionHandler, ResourceNotFoundException
│       │   │   ├── mapper/       # UserMapper, UserAddressMapper (MapStruct)
│       │   │   ├── repository/   # UserRepository, UserAddressRepository
│       │   │   ├── security/     # KeycloakJwtAuthenticationConverter
│       │   │   ├── service/      # UserService, UserAddressService, CloudinaryService
│       │   │   └── validation/   # Custom Validators (Phone, Username, Email)
│       │   └── resources/
│       │       ├── application.yaml
│       │       └── db/migration/
│       │           ├── V1__create_users_table.sql
│       │           └── V2__create_user_addresses_table.sql
│       └── test/                 # 84 Unit & Integration tests
└── frontend/                   # React 18 + Vite + Tailwind CSS
    ├── package.json
    ├── tailwind.config.js
    ├── vite.config.js
    └── src/
        ├── components/
        │   ├── address/        # AddressCard, AddressList, AddressModal
        │   ├── layout/         # AuthLayout (Split-screen), Navbar
        │   └── ui/             # Button, Input, Checkbox
        ├── context/            # AuthContext.jsx
        ├── pages/              # LoginPage, RegisterPage, HomePage, ProfilePage
        └── services/           # api.js, keycloak.js, addressService.js, locationService.js, cloudinaryService.js
```
