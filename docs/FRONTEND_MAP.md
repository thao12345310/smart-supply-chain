# Frontend Map — React căn bản gắn code thật (để ôn phản biện)

> Học React **vừa đủ căn bản** bằng chính code frontend của bạn. Mỗi khái niệm nền → trỏ tới **file:dòng thật** trong `frontend/src/`.
> Cách học: đọc doc này (tự học) + hỏi tôi giải thích trực tiếp bất kỳ dòng nào. Gốc FE: `frontend/src/`.

## 0. Stack & tư duy nền
- **React 18** (UI library) + **Vite 5** (dev server + bundler) + **Ant Design 5** (thư viện component sẵn) + **react-router-dom 6** (định tuyến) + **axios** (gọi HTTP) + **recharts** (biểu đồ dashboard).
- **Tư duy cốt lõi:** UI = **hàm của state**. Bạn không tự tay sửa DOM; bạn đổi *state* → React **tự vẽ lại** (re-render) phần thay đổi.
- **SPA (Single Page App):** 1 trang HTML duy nhất; router đổi "trang" ở client, không reload. Dữ liệu lấy qua REST API từ backend Spring Boot.
- **Không** Redux/Zustand/React Query → state chỉ là `useState` cục bộ trong mỗi trang + `localStorage` cho phiên đăng nhập. (Đây là điểm "đủ căn bản", dễ giải thích.)

## 1. App khởi động thế nào (đọc theo thứ tự này)
1. `index.html` có `<div id="root">` — điểm cắm của toàn app.
2. `src/main.jsx:5` — `createRoot(document.getElementById('root')).render(<App />)`: React "gắn" cây component vào `#root`.
3. `src/App.jsx:460` — `App()` trả về `<BrowserRouter>` bọc `<Routes>`: đây là **bộ định tuyến**, quyết định URL nào → component nào.
4. Mỗi route bọc 2 lớp bảo vệ rồi mới tới trang: `ProtectedRoute` (phải đăng nhập) → `RoleProtectedRoute` (đúng quyền) → `MainLayout` (khung sidebar) → trang thật.

## 2. 14 khái niệm React căn bản → code thật của bạn
| # | Khái niệm | "Là gì" trong React | Trong code của bạn |
|---|---|---|---|
| 1 | **Component** | 1 hàm trả về JSX = 1 mảnh UI tái dùng được | Mọi file trong `pages/`, vd `ProductList()` (`ProductList.jsx:8`) |
| 2 | **JSX** | "HTML viết trong JS"; `{biểu_thức}` để nhúng giá trị | `LoginPage.jsx:92` `<Title>DMS LOGIN</Title>`, `{error && ...}` |
| 3 | **Render / re-render** | React vẽ lại khi state/props đổi | `Table` vẽ lại khi `products` đổi (`ProductList.jsx:216`) |
| 4 | **Props** | Tham số truyền **từ cha xuống con** (1 chiều) | `<ProductForm open={open} onSave={handleSave} product={editing}/>` (`ProductList.jsx:230`) |
| 5 | **State — `useState`** | Biến "nhớ" giữa các lần render; đổi nó → re-render | `const [products, setProducts] = useState([])` (`ProductList.jsx:9-13`) |
| 6 | **Effect — `useEffect`** | Chạy "tác dụng phụ" (gọi API…) sau render; `[]` = chạy 1 lần lúc mount | `useEffect(() => { fetchProducts(); }, [])` (`ProductList.jsx:27-29`) |
| 7 | **Event handler** | Hàm chạy khi người dùng bấm/gõ | `onClick={handleAdd}` (`:208`), `onChange={e=>setSearchText(e.target.value)}` (`:204`) |
| 8 | **Render danh sách + `key`** | `.map()` mảng → nhiều phần tử; mỗi cái cần `key` ổn định | `columns` map sang ô; `rowKey="id"` cho Table (`:219`); `bgCircles.map((c,i)=>...key={i})` (`LoginPage.jsx:61`) |
| 9 | **Render có điều kiện** | `&&` / tam phân để hiện-ẩn | `{error && <Alert/>}` (`LoginPage.jsx:105`), `if(!token) return <Navigate/>` (`App.jsx:84`) |
| 10 | **Controlled input** | Ô input mà **state là nguồn sự thật** (`value`+`onChange`) | Ô tìm kiếm: `value={searchText}` (`ProductList.jsx:203-204`) |
| 11 | **Routing** | URL ↔ component; điều hướng bằng `useNavigate`, tham số `:id` | `<Route path="/products" element=...>` (`App.jsx:711`); `navigate('/sales-orders')` (`App.jsx:161`) |
| 12 | **Lifting/2 chiều giả** | Cha giữ state, con báo lên qua callback prop | `ProductForm` gọi `onSave(data)` → cha `handleSave` lưu API (`ProductList.jsx:62-77`) |
| 13 | **Side data fetch + async/await** | Gọi backend bằng Promise; `try/catch/finally` | `const res = await api.get('/products')` (`ProductList.jsx:15-25`) |
| 14 | **Component thư viện (Ant Design)** | Dùng `Table/Form/Modal/Button…` thay vì tự code | `Table` (`:216`), `Form`+`rules` (`LoginPage.jsx:115-142`), `Modal.confirm` (`:44`) |

> Học thuộc 14 dòng này = bạn "đọc hiểu" được mọi trang trong dự án, vì **mọi trang đều lặp lại đúng các pattern này**.

## 3. Vòng đời 1 trang danh sách (mental model phải thuộc)
Lấy `ProductList.jsx` làm chuẩn — mọi trang `*List` đều giống:
```
Mount trang  →  useEffect([]) chạy 1 lần  →  fetchProducts()
   → api.get('/products')  → [request interceptor gắn JWT]  → Backend
   → Backend trả {success,data}  → [response interceptor bóc .data]
   → setProducts(data)  ⇒ STATE đổi  ⇒ React RE-RENDER
   → <Table dataSource={products}> vẽ lại với dữ liệu mới
```
Tìm kiếm/sửa/xóa **không gọi lại server để lọc**: `filteredProducts` lọc ngay trên client (`:79-86`); sửa/xóa xong thì gọi `fetchProducts()` để tải lại (`:54,:73`).

## 4. Auth end-to-end FE ↔ BE (câu hỏi phản biện hay gặp)
1. **Đăng nhập:** `LoginPage.onFinish` → `authApi.login(values)` → `POST /api/auth/login` (`LoginPage.jsx:22-27`).
2. **Lưu phiên:** nhận `{accessToken, user}` → `localStorage.setItem('token'…)` + `user` (`:28-32`). *(Không dùng cookie; lưu ở localStorage.)*
3. **Mỗi request sau đó:** `api.js:17-26` **request interceptor** tự đọc token và gắn header `Authorization: Bearer <token>` — bạn không phải gắn tay ở từng trang.
4. **Backend kiểm:** `JwtAuthenticationFilter` verify chữ ký → set SecurityContext (xem `docs/SYSTEM_MAP.md` mục RBAC).
5. **Hết hạn/không quyền:** `api.js:43-51` **response interceptor** bắt `401/403` → xoá token+user (đẩy người dùng về trạng thái chưa đăng nhập).
6. **Bóc vỏ ApiResponse:** backend trả `{success,message,data}`; interceptor `:32-37` tự trả thẳng `data` cho component, nên trong trang bạn chỉ thấy `res.data` là payload sạch.

## 5. Phân quyền ở FE vs BE (điểm dễ bị hỏi vặn — trả lời cho đúng)
- **FE (`roleService.js`):** `ROLES` map đúng tên `ROLE_*` của backend (`:4-14`); `hasAnyRole()` đọc `user.roles` từ localStorage, admin được tất (`:21-35`).
- **Dùng để:** ẩn menu (`App.jsx` mỗi item có `hidden: !hasAnyRole([...])`), ẩn nút/cột (`ProductList.jsx:136,207`), chặn route ở client (`RoleProtectedRoute` `App.jsx:95-104`).
- **CÂU TRẢ LỜI CHUẨN:** *"Phân quyền ở frontend chỉ để **trải nghiệm** (ẩn thứ không dùng được). Nó **không phải lớp bảo mật** — vì người dùng có thể sửa localStorage hoặc gọi thẳng API. Quyền **thật** do **backend chốt** (`@PreAuthorize` + `SecurityConfig` + `DataFilterAspect`). FE ẩn, BE mới cấm (403)."* — Đây là câu ăn điểm, thể hiện bạn hiểu bảo mật.

## 6. Câu hỏi phản biện FE — trả lời gọn
- **React khác HTML/jQuery thường ở đâu?** → UI khai báo theo state; sửa state, React tự cập nhật DOM (Virtual DOM), không tự `document.getElementById` rồi sửa tay.
- **`useState` vs biến thường?** → biến thường mất sau mỗi render và **không** kích hoạt vẽ lại; `useState` giữ giá trị qua các render và đổi nó thì re-render.
- **`useEffect(...,[])` để làm gì?** → chạy đúng 1 lần sau khi component mount — nơi gọi API tải dữ liệu đầu trang.
- **Vì sao cần `key` khi `.map`?** → để React biết phần tử nào thêm/sửa/xoá, vẽ lại tối thiểu; ở Table là `rowKey="id"`.
- **`props` là gì? truyền ngược lên cha kiểu nào?** → dữ liệu chảy 1 chiều cha→con; con báo lên bằng cách gọi **hàm callback** mà cha truyền xuống (`onSave`).
- **Token nằm đâu, gắn vào request kiểu gì?** → localStorage; axios **request interceptor** gắn `Bearer` tự động cho mọi call.
- **SPA định tuyến ra sao?** → `react-router` đổi component theo URL ở client, không reload trang; `useNavigate` để chuyển trang bằng code.
- **State toàn cục quản lý thế nào?** → dự án không dùng Redux; phiên đăng nhập để ở localStorage, còn lại là state cục bộ mỗi trang (đủ cho quy mô này).
- **Gọi API tập trung ở đâu?** → `services/api.js`: 1 axios instance + 2 interceptor (gắn token / bóc ApiResponse + xử lý 401/403); mỗi nhóm nghiệp vụ 1 object (`productApi`, `salesOrderApi`…).
- **`import.meta.env.VITE_API_BASE_URL` là gì?** → biến môi trường lúc build của Vite; deploy Vercel trỏ về backend Render, dev thì mặc định `localhost:8080` (`api.js:5`).

## 7. Điểm yếu FE trung thực (+ câu trả lời nếu bị hỏi)
- **Token ở localStorage** → rủi ro XSS đọc được token. *Trả lời:* biết hạn chế; phương án tốt hơn là HttpOnly cookie + CSRF; phạm vi đồ án ưu tiên đơn giản.
- **Có gọi `authApi.refresh` nhưng chưa nối tự động** (`api.js:69`) → token hết hạn thì phải đăng nhập lại. *Trả lời:* đã chừa sẵn API refresh, chưa wire vì ngoài phạm vi.
- **Chưa có test FE / chưa code-splitting / import tất cả route 1 lần** (`App.jsx:22-68`) → bundle to. *Trả lời:* biết; có thể `React.lazy` để chia nhỏ.
- **Lặp code giữa các trang List/Form** → có thể trừu tượng hoá thành component dùng chung; chấp nhận để rõ ràng cho đồ án.

## 8. File "phải biết" (FE) + lệnh chạy
- `src/main.jsx` — điểm vào.
- `src/App.jsx` — router + `ProtectedRoute`/`RoleProtectedRoute` + `MainLayout`.
- `src/services/api.js` — axios instance + 2 interceptor + toàn bộ hàm gọi API.
- `src/services/roleService.js` — `ROLES` + `hasAnyRole`.
- `src/pages/LoginPage.jsx` — mẫu Form + state + async + lưu phiên.
- `src/pages/ProductList.jsx` — mẫu chuẩn của 1 trang dữ liệu (useState/useEffect/Table/Modal/CRUD).
```bash
# Chạy FE (cần backend chạy ở :8080):
cd frontend && npm install && npm run dev      # → http://localhost:5173
# Build production:
npm run build                                   # ra frontend/dist
```
