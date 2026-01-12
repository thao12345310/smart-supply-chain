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

export default api;
