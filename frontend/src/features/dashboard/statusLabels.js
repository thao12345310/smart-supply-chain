/**
 * Backend cluster charts emit raw enum names (e.g. "ORDER_OPEN") as labels.
 * These maps turn them into readable Vietnamese for the charts. Unknown keys
 * fall back to the raw label so nothing ever renders blank.
 */
export const SO_STATUS_VI = {
  ORDER_OPEN: 'Chờ duyệt',
  ORDER_APPROVED: 'Đã duyệt',
  ORDER_PARTIALLY_DELIVERED: 'Giao một phần',
  ORDER_COMPLETED: 'Hoàn thành',
  ORDER_CANCELLED: 'Đã hủy',
};

export const PO_STATUS_VI = {
  ORDER_OPEN: 'Chờ duyệt',
  ORDER_APPROVED: 'Đã duyệt',
  ORDER_PARTIALLY_RECEIVED: 'Nhập một phần',
  ORDER_RECEIVED: 'Đã nhận',
  ORDER_COMPLETED: 'Hoàn thành',
  ORDER_CANCELLED: 'Đã hủy',
};

export const TRIP_STATUS_VI = {
  CREATED: 'Đã tạo',
  IN_PROGRESS: 'Đang giao',
  COMPLETED: 'Hoàn thành',
  CANCELLED: 'Đã hủy',
};

/** Map an array of {label,value} points through a label dictionary (raw fallback). */
export const mapPoints = (points, dict) =>
  (points || []).map((p) => ({ label: dict[p.label] || p.label, value: Number(p.value || 0) }));

/** Sum the values of points whose raw label is in `names` (uses raw enum names). */
export const sumStatus = (points, names) =>
  (points || [])
    .filter((p) => names.includes(p.label))
    .reduce((s, p) => s + Number(p.value || 0), 0);
