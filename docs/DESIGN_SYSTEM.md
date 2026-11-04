# TEU MOBILE - DESIGN SYSTEM & UI/UX GUIDELINES
> Tài liệu phân tích và quy chuẩn thiết kế giao diện dựa trên bản vẽ Figma (`figma/login_page.png`).

---

## 1. Triết lý Thiết kế (Design Philosophy)
- **Phong cách chủ đạo**: **Minimalist High-Tech with Dynamic Energy** (Tối giản - Năng động - Công nghệ cao).
- **Trải nghiệm thị giác**:
  - Không gian thoáng đãng (Whitespace rộng rãi), lề đệm đầy đặn, tạo cảm giác sang trọng và đẳng cấp kiểu Apple Store / Nike Tech.
  - Kết hợp độ tương phản mạnh mẽ giữa nền sáng (Trắng/Xám tro) với nét vẽ Line-art đậm chất Comic/Cyberpunk và điểm xuyết màu nhấn **Đỏ Coral** đầy năng lượng.
- **Tính nhất quán**: Toàn bộ các trang (Đăng nhập, Đăng ký, Trang chủ, Chi tiết sản phẩm, Giỏ hàng, Hồ sơ cá nhân) đều tuân theo cùng hệ thống màu sắc, kiểu chữ và hình khối.

---

## 2. Bảng Màu Quy Chuẩn (Color Palette)

| Vai trò | Tên màu | Mã Hex | Tailwind Class | Ứng dụng |
| :--- | :--- | :--- | :--- | :--- |
| **Primary Accent** | Coral Red / Hot Crimson | `#EA384D` (hoặc `#E63946`) | `bg-primary`, `text-primary` | Nút hành động chính (Sign in, Mua ngay, Thêm giỏ), link nổi bật, giá khuyến mãi, badge "Hot/Sale". |
| **Primary Hover** | Deep Crimson | `#D92D41` | `hover:bg-primary-dark` | Trạng thái hover của nút bấm chính. |
| **Dark Neutral** | Slate 900 | `#0F172A` | `text-slate-900` | Tiêu đề chính (`WELCOME BACK`), tiêu đề sản phẩm, chữ quan trọng. |
| **Body Neutral** | Slate 700 | `#334155` | `text-slate-700` | Nội dung văn bản, label form, thông số kỹ thuật. |
| **Muted Neutral** | Slate 500 / 400 | `#64748B` | `text-slate-500` | Subtitle, placeholder input, ghi chú chân trang. |
| **Border Neutral** | Slate 200 | `#E2E8F0` | `border-slate-200` | Viền input, viền card sản phẩm, viền nút Google. |
| **Light Surface** | Slate 50 | `#F8FAFC` | `bg-slate-50` | Nền input, nền phụ, nền panel phụ. |
| **Pure White** | Pure White | `#FFFFFF` | `bg-white` | Nền chính của form và website. |

---

## 3. Kiểu Chữ & Thứ Bậc Văn Bản (Typography)
- **Font Family**: `Plus Jakarta Sans`, `Inter`, hoặc System Sans-serif hiện đại.
- **Tiêu đề chính (Headings)**:
  - Form Title (`WELCOME BACK`, `CREATE ACCOUNT`): `text-3xl font-bold uppercase tracking-wide text-slate-900`.
  - Section Title (`ĐIỆN THOẠI NỔI BẬT`): `text-2xl font-bold tracking-tight text-slate-900`.
- **Phụ đề (Subtitles)**:
  - `text-slate-500 text-sm font-normal mt-2`.
- **Form Labels**:
  - `text-sm font-semibold text-slate-800 mb-1.5`.
- **Nút bấm & Links**:
  - Nút chính: `text-base font-semibold text-white tracking-wide`.
  - Links: `text-sm font-medium text-slate-700 hover:text-primary transition-colors`.

---

## 4. Hình Khối & Hiệu Ứng (Shapes, Radii & Shadows)
- **Bo góc mềm mại (Soft Rounded Corners)**:
  - Input, Button, Card nhỏ: `rounded-xl` (12px).
  - Modal, Banner, Card sản phẩm: `rounded-2xl` (16px) đến `rounded-3xl` (24px).
  - Checkbox: `rounded-md` (6px) nhẹ nhàng.
- **Đổ bóng (Elevations)**:
  - `shadow-sm`: Viền input và nút thứ cấp.
  - `shadow-md` với blur mềm mại: Nút CTA chính khi hover (`hover:shadow-lg hover:shadow-red-500/25`).

---

## 5. Quy Chuẩn Bố Cục Từng Trang (Page-Specific Guidelines)

### A. Trang Xác Thực (Login / Register) - Bố cục Chia đôi (Split Screen)
- **Cột trái (Form Panel)**:
  - Chiếm 45% - 50% màn hình desktop (100% trên mobile).
  - Căn giữa dọc và ngang với lề thoáng đãng (`max-w-md mx-auto`).
  - Nút phụ "Sign in with Google" viền mảnh, nền trắng, logo Google đa sắc.
- **Cột phải (Visual Showcase Panel)**:
  - Chiếm 50% - 55% màn hình desktop (ẩn trên mobile).
  - Nền xám khói chuyển động nghệ thuật (`#F1F5F9` đến `#E2E8F0`).
  - Artwork minh họa line-art đen trắng kết hợp chi tiết Đỏ Coral.

### B. Header / Thanh Điều Hướng (Navbar)
- Nền trắng kính mờ (`bg-white/90 backdrop-blur-md`), dính cố định trên cùng (`sticky top-0 z-50`).
- Logo: Chữ "TEU MOBILE" in hoa đậm nét công nghệ, có thể kèm dấu chấm đỏ Coral thương hiệu.
- Thanh tìm kiếm: Bo góc `rounded-full` hoặc `rounded-xl`, viền mảnh xám nhạt.
- Giỏ hàng và Trạng thái Tài khoản: Dropdown avatar gọn gàng khi đã đăng nhập; nút "Đăng nhập" viền hoặc nền đỏ Coral khi chưa đăng nhập.

### C. Thẻ Sản Phẩm (Product Card)
- Nền trắng, viền `border border-slate-100`, bo góc `rounded-2xl`.
- Ảnh sản phẩm sạch sẽ trên nền trong suốt/xám nhạt.
- Giá tiền: Giá bán màu đỏ Coral `text-primary font-bold text-lg`, giá cũ gạch ngang `text-slate-400 line-through text-sm`.
- Nút "Thêm vào giỏ" / "Mua ngay": Icon tròn hoặc nút bo cong chuyển màu khi hover.
