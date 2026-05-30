/**
 * Chuẩn hoá & cấu hình hiển thị trạng thái cho phân hệ Giao hàng.
 *
 * Backend dùng lẫn lộn hoa/thường giữa đợt (delivery_plan: "Created"/"InProgress"/"Completed")
 * và chuyến (delivery_triproute: "CREATED"/"IN_PROGRESS"/...). Hàm normalize gộp về 1 key.
 */
export const DELIVERY_STATUS_CONFIG = {
  DRAFT: { color: "blue", label: "Nháp" },
  CREATED: { color: "blue", label: "Mới tạo" },
  PENDING: { color: "orange", label: "Chờ xử lý" },
  INPROGRESS: { color: "processing", label: "Đang giao" },
  COMPLETED: { color: "success", label: "Hoàn thành" },
  CANCELLED: { color: "default", label: "Đã hủy" },
};

// Trạng thái của 1 điểm giao (vận đơn trong chuyến)
export const ITEM_STATUS_CONFIG = {
  PENDING: { color: "default", label: "Chờ giao" },
  DELIVERED: { color: "success", label: "Giao thành công" },
  FAILED: { color: "error", label: "Giao thất bại" },
};

export const normalizeStatus = (status) =>
  (status || "").toString().toUpperCase().replace(/[\s_-]/g, "");

export const getStatusConfig = (status) =>
  DELIVERY_STATUS_CONFIG[normalizeStatus(status)] || { color: "default", label: status || "-" };

export const getItemStatusConfig = (status) =>
  ITEM_STATUS_CONFIG[normalizeStatus(status)] || { color: "default", label: status || "-" };

// Dùng cho thống kê ở màn danh sách đợt
export const isStatus = (status, target) => normalizeStatus(status) === normalizeStatus(target);
