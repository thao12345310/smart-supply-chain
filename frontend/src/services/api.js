import axios from 'axios';

// Base URL lấy từ biến môi trường khi build (Vercel: VITE_API_BASE_URL).
// Khi dev local không set biến thì mặc định trỏ về backend localhost:8080.
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

// Create axios instance with base configuration
const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to attach JWT token to every request
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to handle API response format
api.interceptors.response.use(
  (response) => {
    // Check if response has the new ApiResponse format
    if (response.data && typeof response.data === 'object' && 'success' in response.data) {
      if (!response.data.success) {
        return Promise.reject(new Error(response.data.message || 'Operation failed'));
      }
      // Return the data field from ApiResponse
      return { ...response, data: response.data.data };
    }
    return response;
  },
  (error) => {
    // Handle error responses
    if (error.response?.status === 401 || error.response?.status === 403) {
      // Token might be invalid or expired
      if (window.location.pathname !== '/login') {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        // Redirect to login only if not already there
        // Actually, we should probably let the component handle the redirect if using a React context
      }
    }
    
    if (error.response?.data?.message) {
      error.message = error.response.data.message;
    }
    return Promise.reject(error);
  }
);

// ==================== Authentication API ====================
export const authApi = {
  // Login user
  login: (data) => api.post('/auth/login', data),
  
  // Register user
  register: (data) => api.post('/auth/register', data),
  
  // Refresh token
  refresh: (refreshToken) => api.post('/auth/refresh', { refreshToken }),
  
  // Get current user
  getCurrentUser: () => api.get('/auth/me'),
  
  // Logout (mostly client-side)
  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },
};

// ==================== Purchase Order API ====================
export const purchaseOrderApi = {
  // Get all purchase orders
  getAll: () => api.get('/purchase-orders'),
  
  // Get purchase order by ID
  getById: (id) => api.get(`/purchase-orders/${id}`),
  
  // Get purchase order by code
  getByCode: (code) => api.get(`/purchase-orders/code/${code}`),
  
  // Create new purchase order
  create: (data) => api.post('/purchase-orders', data),
  
  // Update purchase order
  update: (id, data) => api.put(`/purchase-orders/${id}`, data),
  
  // Delete purchase order
  delete: (id) => api.delete(`/purchase-orders/${id}`),
  
  // Approve purchase order
  approve: (id, approvedBy) => 
    api.put(`/purchase-orders/${id}/approve`, null, { params: { approvedBy } }),
  
  // Reject purchase order
  reject: (id, rejectedBy, reason) => 
    api.put(`/purchase-orders/${id}/reject`, null, { params: { rejectedBy, reason } }),
  
  // Cancel purchase order
  cancel: (id, reason) => 
    api.put(`/purchase-orders/${id}/cancel`, null, { params: { reason } }),
  
  // Process approval (approve or reject)
  processApproval: (id, data) => api.post(`/purchase-orders/${id}/approval`, data),
  
  // Get pending approval
  getPendingApproval: () => api.get('/purchase-orders/pending-approval'),
  
  // Get ready for goods receipt
  getReadyForReceipt: () => api.get('/purchase-orders/ready-for-receipt'),
  
  // Get by status
  getByStatus: (status) => api.get(`/purchase-orders/status/${status}`),
  
  // Get by supplier
  getBySupplierId: (supplierId) => api.get(`/purchase-orders/supplier/${supplierId}`),
  
  // Get by date range
  getByDateRange: (startDate, endDate) => 
    api.get('/purchase-orders/date-range', { params: { startDate, endDate } }),
};

// ==================== Goods Receipt API ====================
export const goodsReceiptApi = {
  // Get all goods receipts
  getAll: () => api.get('/goods-receipts'),
  
  // Get goods receipt by ID
  getById: (id) => api.get(`/goods-receipts/${id}`),
  
  // Get goods receipt by code
  getByCode: (code) => api.get(`/goods-receipts/code/${code}`),
  
  // Create new goods receipt
  create: (data) => api.post('/goods-receipts', data),
  
  // Update goods receipt
  update: (id, data) => api.put(`/goods-receipts/${id}`, data),
  
  // Delete goods receipt
  delete: (id) => api.delete(`/goods-receipts/${id}`),
  
  // Confirm goods receipt
  confirm: (id, confirmedBy) => 
    api.put(`/goods-receipts/${id}/confirm`, null, { params: { confirmedBy } }),
  
  // Cancel goods receipt
  cancel: (id) => api.put(`/goods-receipts/${id}/cancel`),
  
  // Get by purchase order
  getByPurchaseOrderId: (poId) => api.get(`/goods-receipts/purchase-order/${poId}`),
  
  // Get by warehouse
  getByWarehouseId: (warehouseId) => api.get(`/goods-receipts/warehouse/${warehouseId}`),
  
  // Get pending confirmation
  getPendingConfirmation: () => api.get('/goods-receipts/pending-confirmation'),
  
  // Get receiving summary for PO
  getReceivingSummary: (poId) => api.get(`/goods-receipts/receiving-summary/${poId}`),

  // Get by status
  getByStatus: (status) => api.get(`/goods-receipts/status/${status}`),
};

// ==================== Inventory API ====================
export const inventoryApi = {
  // Get all inventory
  getAll: () => api.get('/inventory'),
  
  // Get by product
  getByProduct: (productId) => api.get(`/inventory/product/${productId}`),
  
  // Get by warehouse
  getByWarehouse: (warehouseId) => api.get(`/inventory/warehouse/${warehouseId}`),
  
  // Get by product and warehouse
  getByProductAndWarehouse: (productId, warehouseId) => 
    api.get(`/inventory/product/${productId}/warehouse/${warehouseId}`),
  
  // Get low stock
  getLowStock: (threshold = 10) => api.get('/inventory/low-stock', { params: { threshold } }),
  
  // Get needing reorder
  getNeedingReorder: () => api.get('/inventory/needing-reorder'),

  // Đề xuất mua hàng gom theo nhà cung cấp
  getPurchaseSuggestions: (warehouseId) =>
    api.get('/inventory/purchase-suggestions', { params: warehouseId ? { warehouseId } : {} }),

  // Update reorder (low-stock alert) level
  updateReorderLevel: (id, reorderLevel, reorderQuantity) =>
    api.patch(`/inventory/${id}/reorder-level`, { reorderLevel, reorderQuantity }),
  
  // Get transactions by product
  getTransactionsByProduct: (productId) => 
    api.get(`/inventory/transactions/product/${productId}`),
};

// ==================== Customer API ====================
export const customerApi = {
  // Get all customers
  getAll: () => api.get('/customers'),
  
  // Get active customers
  getActive: () => api.get('/customers/active'),
  
  // Get customer by ID
  getById: (id) => api.get(`/customers/${id}`),
  
  // Get customer by code
  getByCode: (code) => api.get(`/customers/code/${code}`),
  
  // Search customers
  search: (query) => api.get('/customers/search', { params: { q: query } }),
  
  // Create new customer
  create: (data) => api.post('/customers', data),
  
  // Update customer
  update: (id, data) => api.put(`/customers/${id}`, data),
  
  // Delete customer (soft delete)
  delete: (id) => api.delete(`/customers/${id}`),
  
  // Get delivery addresses
  getAddresses: (customerId) => api.get(`/customers/${customerId}/addresses`),
  
  // Add delivery address
  addAddress: (customerId, data) => api.post(`/customers/${customerId}/addresses`, data),
  
  // Update delivery address
  updateAddress: (addressId, data) => api.put(`/customers/addresses/${addressId}`, data),
  
  // Delete delivery address
  deleteAddress: (addressId) => api.delete(`/customers/addresses/${addressId}`),
  
  // Set default address
  setDefaultAddress: (customerId, addressId) => 
    api.put(`/customers/${customerId}/addresses/${addressId}/default`),
};

// ==================== Sales Order API ====================
export const salesOrderApi = {
  // Get all sales orders
  getAll: () => api.get('/sales-orders'),
  
  // Get sales order by ID
  getById: (id) => api.get(`/sales-orders/${id}`),
  
  // Get sales order by code
  getByCode: (code) => api.get(`/sales-orders/code/${code}`),
  
  // Create new sales order
  create: (data) => api.post('/sales-orders', data),
  
  // Update sales order
  update: (id, data) => api.put(`/sales-orders/${id}`, data),
  
  // Delete sales order
  delete: (id) => api.delete(`/sales-orders/${id}`),
  
  // Approve sales order
  approve: (id, approvedBy) => 
    api.put(`/sales-orders/${id}/approve`, null, { params: { approvedBy } }),
  
  // Reject sales order
  reject: (id, rejectedBy, reason) => 
    api.put(`/sales-orders/${id}/reject`, null, { params: { rejectedBy, reason } }),
  
  // Cancel sales order
  cancel: (id, reason) => 
    api.put(`/sales-orders/${id}/cancel`, null, { params: { reason } }),
  
  // Process approval (approve or reject)
  processApproval: (id, data) => api.post(`/sales-orders/${id}/approval`, data),
  
  // Get pending approval
  getPendingApproval: () => api.get('/sales-orders/pending-approval'),
  
  // Get ready for goods issue
  getReadyForIssue: () => api.get('/sales-orders/ready-for-issue'),
  
  // Get by status
  getByStatus: (status) => api.get(`/sales-orders/status/${status}`),
  
  // Get by customer
  getByCustomerId: (customerId) => api.get(`/sales-orders/customer/${customerId}`),
  
  // Get by date range
  getByDateRange: (startDate, endDate) => 
    api.get('/sales-orders/date-range', { params: { startDate, endDate } }),
  
  // Search sales orders
  search: (query) => api.get('/sales-orders/search', { params: { q: query } }),
  
  // Get issue summary (remaining quantities)
  getIssueSummary: (id) => api.get(`/sales-orders/${id}/issue-summary`),
};

// ==================== Goods Issue API ====================
export const goodsIssueApi = {
  // Get all goods issues
  getAll: () => api.get('/goods-issues'),
  
  // Get goods issue by ID
  getById: (id) => api.get(`/goods-issues/${id}`),
  
  // Get goods issue by code
  getByCode: (code) => api.get(`/goods-issues/code/${code}`),
  
  // Create new goods issue
  create: (data) => api.post('/goods-issues', data),
  
  // Update goods issue
  update: (id, data) => api.put(`/goods-issues/${id}`, data),
  
  // Delete goods issue
  delete: (id) => api.delete(`/goods-issues/${id}`),
  
  // Confirm goods issue
  confirm: (id, confirmedBy) => 
    api.put(`/goods-issues/${id}/confirm`, null, { params: { confirmedBy } }),
  
  // Cancel goods issue
  cancel: (id) => api.put(`/goods-issues/${id}/cancel`),
  
  // Get by sales order
  getBySalesOrderId: (soId) => api.get(`/goods-issues/sales-order/${soId}`),
  
  // Get by status
  getByStatus: (status) => api.get(`/goods-issues/status/${status}`),
  
  // Get draft goods issues
  getDraft: () => api.get('/goods-issues/draft'),
  
  // Get by date range
  getByDateRange: (startDate, endDate) => 
    api.get('/goods-issues/date-range', { params: { startDate, endDate } }),
  
  // Search goods issues
  search: (query) => api.get('/goods-issues/search', { params: { q: query } }),
};

// ==================== Sales Invoice API ====================
export const salesInvoiceApi = {
  // Get all sales invoices
  getAll: () => api.get('/sales-invoices'),
  
  // Get sales invoice by ID
  getById: (id) => api.get(`/sales-invoices/${id}`),
  
  // Get sales invoice by code
  getByCode: (code) => api.get(`/sales-invoices/code/${code}`),
  
  // Issue invoice
  issue: (id, issuedBy) => 
    api.put(`/sales-invoices/${id}/issue`, null, { params: { issuedBy } }),
  
  // Record payment
  recordPayment: (id, amount, paymentMethod, paymentReference) => 
    api.post(`/sales-invoices/${id}/payment`, null, { 
      params: { amount, paymentMethod, paymentReference } 
    }),
  
  // Cancel invoice
  cancel: (id) => api.put(`/sales-invoices/${id}/cancel`),
  
  // Get by sales order
  getBySalesOrderId: (soId) => api.get(`/sales-invoices/sales-order/${soId}`),
  
  // Get by goods issue
  getByGoodsIssueId: (giId) => api.get(`/sales-invoices/goods-issue/${giId}`),
  
  // Get by customer
  getByCustomerId: (customerId) => api.get(`/sales-invoices/customer/${customerId}`),
  
  // Get customer outstanding amount
  getCustomerOutstanding: (customerId) => 
    api.get(`/sales-invoices/customer/${customerId}/outstanding`),
  
  // Get overdue invoices
  getOverdue: () => api.get('/sales-invoices/overdue'),
  
  // Get unpaid invoices
  getUnpaid: () => api.get('/sales-invoices/unpaid'),
  
  // Get by status
  getByStatus: (status) => api.get(`/sales-invoices/status/${status}`),
  
  // Get by date range
  getByDateRange: (startDate, endDate) => 
    api.get('/sales-invoices/date-range', { params: { startDate, endDate } }),
  
  // Search sales invoices
  search: (query) => api.get('/sales-invoices/search', { params: { q: query } }),
};

// ==================== Supplier API ====================
export const supplierApi = {
  // Get all suppliers
  getAll: () => api.get('/suppliers'),
  
  // Get supplier by ID
  getById: (id) => api.get(`/suppliers/${id}`),
};

// ==================== Warehouse API ====================
export const warehouseApi = {
  // Get all warehouses
  getAll: () => api.get('/warehouses'),
  
  // Get warehouse by ID
  getById: (id) => api.get(`/warehouses/${id}`),
};

// ==================== Product API ====================
export const productApi = {
  // Get all products
  getAll: () => api.get('/products'),
  
  // Get product by ID
  getById: (id) => api.get(`/products/${id}`),
};

// ==================== Inventory Lot API ====================
export const inventoryLotApi = {
  getAll: (params) => api.get('/inventory-lots', { params }),
  getByProductWarehouse: (productId, warehouseId) =>
    api.get(`/inventory-lots/product/${productId}/warehouse/${warehouseId}`),
  getExpiringSoon: (days = 30) =>
    api.get('/inventory-lots/expiring-soon', { params: { days } }),
  getExpired: () => api.get('/inventory-lots/expired'),
  // Xuất hủy lô (hết HSD / hư hỏng)
  disposeLot: (lotId, data) => api.post(`/inventory-lots/${lotId}/dispose`, data),
  disposeExpired: (warehouseId, data) =>
    api.post('/inventory-lots/dispose-expired', data, {
      params: warehouseId ? { warehouseId } : {},
    }),
  getDisposals: (warehouseId) =>
    api.get('/inventory-lots/disposals', {
      params: warehouseId ? { warehouseId } : {},
    }),
};

// ==================== Delivery Plan API ====================
export const deliveryPlanApi = {
  getAll: () => api.get('/delivery-plans'),
  getById: (id) => api.get(`/delivery-plans/${id}`),
  create: (data) => api.post('/delivery-plans', data),
  update: (id, data) => api.put(`/delivery-plans/${id}`, data),
  delete: (id) => api.delete(`/delivery-plans/${id}`),
  addShipper: (id, data) => api.post(`/delivery-plans/${id}/shippers`, data),
  generateTrips: (id) => api.post(`/delivery-plans/${id}/generate-trips`),
};

// ==================== User Management (Admin) API ====================
export const userApi = {
  getAll: () => api.get('/users'),
  getRoles: () => api.get('/users/roles'),
  getById: (id) => api.get(`/users/${id}`),
  create: (data) => api.post('/users', data),
  update: (id, data) => api.put(`/users/${id}`, data),
  activate: (id) => api.put(`/users/${id}/activate`),
  deactivate: (id) => api.put(`/users/${id}/deactivate`),
  resetPassword: (id, password) => api.put(`/users/${id}/password`, { password }),
  delete: (id) => api.delete(`/users/${id}`),
};

// ==================== Dashboard & Reporting API ====================
export const dashboardApi = {
  // Get dashboard summary
  getSummary: () => api.get('/dashboard/summary'),
  
  // Get revenue chart data
  getRevenueChart: (startDate, endDate, groupBy = 'monthly') =>
    api.get('/dashboard/revenue-chart', { params: { startDate, endDate, groupBy } }),
  
  // Get inventory stock report (nhập-xuất-tồn)
  getInventoryReport: (startDate, endDate, warehouseId = null) =>
    api.get('/dashboard/inventory-report', { params: { startDate, endDate, warehouseId } }),
  
  // Get receivables report (công nợ)
  getReceivablesReport: (overdueOnly = false) =>
    api.get('/dashboard/receivables-report', { params: { overdueOnly } }),
  
  // Get top selling products
  getTopProducts: (startDate, endDate, limit = 10) =>
    api.get('/dashboard/top-products', { params: { startDate, endDate, limit } }),

  // Per-cluster dashboards
  getPurchase: () => api.get('/dashboard/purchase'),
  getSales: () => api.get('/dashboard/sales'),
  getInventory: () => api.get('/dashboard/inventory'),
  getDelivery: () => api.get('/dashboard/delivery'),
  getAccounting: () => api.get('/dashboard/accounting'),
};

// ==================== Payment & Accounting API ====================
export const paymentApi = {
  getAll: (type) => api.get('/payments', { params: type ? { type } : {} }),
  create: (data) => api.post('/payments', data),
};

export const accountingApi = {
  getTransactions: (startDate, endDate) =>
    api.get('/accounting/transactions', { params: { startDate, endDate } }),
  getLedger: (account) => api.get('/accounting/ledger', { params: { account } }),
};

// ==================== Delivery Order (Vận đơn) API ====================
export const deliveryOrderApi = {
  getAll: () => api.get('/delivery-orders'),
  getById: (id) => api.get(`/delivery-orders/${id}`),
};

export default api;
