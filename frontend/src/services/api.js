import axios from 'axios';

// Create axios instance with base configuration
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

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
    if (error.response?.data?.message) {
      error.message = error.response.data.message;
    }
    return Promise.reject(error);
  }
);

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

export default api;

