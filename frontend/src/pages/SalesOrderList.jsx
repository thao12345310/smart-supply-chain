import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { salesOrderApi } from '../services/api';

/**
 * Sales Order List Page - Danh sách Đơn bán hàng
 */
export default function SalesOrderList() {
  const navigate = useNavigate();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [stats, setStats] = useState({
    total: 0,
    open: 0,
    approved: 0,
    partiallyDelivered: 0,
    completed: 0,
    cancelled: 0,
  });

  useEffect(() => {
    loadOrders();
  }, [statusFilter]);

  const loadOrders = async () => {
    try {
      setLoading(true);
      setError(null);
      
      let response;
      if (statusFilter === 'ALL') {
        response = await salesOrderApi.getAll();
      } else {
        response = await salesOrderApi.getByStatus(statusFilter);
      }
      
      const orderList = response.data || [];
      setOrders(orderList);
      calculateStats(orderList);
    } catch (err) {
      console.error('Lỗi tải đơn bán hàng:', err);
      setError(err.message || 'Không thể tải danh sách đơn bán hàng');
    } finally {
      setLoading(false);
    }
  };

  const calculateStats = (orderList) => {
    const stats = {
      total: orderList.length,
      open: orderList.filter(o => o.status === 'ORDER_OPEN').length,
      approved: orderList.filter(o => o.status === 'ORDER_APPROVED').length,
      partiallyDelivered: orderList.filter(o => o.status === 'ORDER_PARTIALLY_DELIVERED').length,
      completed: orderList.filter(o => o.status === 'ORDER_COMPLETED').length,
      cancelled: orderList.filter(o => o.status === 'ORDER_CANCELLED').length,
    };
    setStats(stats);
  };

  const handleSearch = async () => {
    if (!searchQuery.trim()) {
      loadOrders();
      return;
    }
    
    try {
      setLoading(true);
      const response = await salesOrderApi.search(searchQuery);
      setOrders(response.data || []);
    } catch (err) {
      setError(err.message || 'Tìm kiếm thất bại');
    } finally {
      setLoading(false);
    }
  };

  const getStatusBadge = (status) => {
    const styles = {
      ORDER_OPEN: { bg: '#fef3c7', color: '#92400e', label: 'Chờ duyệt' },
      ORDER_APPROVED: { bg: '#dbeafe', color: '#1e40af', label: 'Đã duyệt' },
      ORDER_PARTIALLY_DELIVERED: { bg: '#e0e7ff', color: '#3730a3', label: 'Giao một phần' },
      ORDER_COMPLETED: { bg: '#d1fae5', color: '#065f46', label: 'Hoàn thành' },
      ORDER_CANCELLED: { bg: '#fee2e2', color: '#991b1b', label: 'Đã hủy' },
    };
    const style = styles[status] || { bg: '#f3f4f6', color: '#374151', label: status };
    
    return (
      <span style={{
        display: 'inline-block',
        padding: '4px 12px',
        borderRadius: '9999px',
        fontSize: '12px',
        fontWeight: '600',
        backgroundColor: style.bg,
        color: style.color,
      }}>
        {style.label}
      </span>
    );
  };

  const getPaymentBadge = (status) => {
    const styles = {
      UNPAID: { bg: '#fee2e2', color: '#991b1b', label: 'Chưa TT' },
      PARTIALLY_PAID: { bg: '#fef3c7', color: '#92400e', label: 'TT một phần' },
      PAID: { bg: '#d1fae5', color: '#065f46', label: 'Đã TT' },
    };
    const style = styles[status] || { bg: '#f3f4f6', color: '#374151', label: status };
    
    return (
      <span style={{
        display: 'inline-block',
        padding: '2px 8px',
        borderRadius: '4px',
        fontSize: '11px',
        fontWeight: '500',
        backgroundColor: style.bg,
        color: style.color,
      }}>
        {style.label}
      </span>
    );
  };

  const formatCurrency = (amount) => {
    if (amount == null) return '-';
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(amount);
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    });
  };

  const containerStyle = {
    padding: '24px',
    maxWidth: '1400px',
    margin: '0 auto',
    fontFamily: "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif",
  };

  const headerStyle = {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '24px',
  };

  const titleStyle = {
    fontSize: '28px',
    fontWeight: '700',
    color: '#111827',
    margin: 0,
  };

  const buttonStyle = {
    display: 'inline-flex',
    alignItems: 'center',
    gap: '8px',
    padding: '12px 24px',
    backgroundColor: '#4f46e5',
    color: 'white',
    border: 'none',
    borderRadius: '8px',
    fontSize: '14px',
    fontWeight: '600',
    cursor: 'pointer',
    transition: 'all 0.2s',
  };

  const statsContainerStyle = {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))',
    gap: '16px',
    marginBottom: '24px',
  };

  const statCardStyle = (color) => ({
    padding: '20px',
    background: 'white',
    borderRadius: '12px',
    boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
    borderLeft: `4px solid ${color}`,
  });

  const filterContainerStyle = {
    display: 'flex',
    gap: '16px',
    marginBottom: '24px',
    flexWrap: 'wrap',
    alignItems: 'center',
  };

  const searchInputStyle = {
    padding: '10px 16px',
    border: '1px solid #d1d5db',
    borderRadius: '8px',
    fontSize: '14px',
    width: '300px',
    outline: 'none',
  };

  const selectStyle = {
    padding: '10px 16px',
    border: '1px solid #d1d5db',
    borderRadius: '8px',
    fontSize: '14px',
    backgroundColor: 'white',
    cursor: 'pointer',
  };

  const tableContainerStyle = {
    background: 'white',
    borderRadius: '12px',
    boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
    overflow: 'hidden',
  };

  const tableStyle = {
    width: '100%',
    borderCollapse: 'collapse',
    fontSize: '14px',
  };

  const thStyle = {
    padding: '16px',
    textAlign: 'left',
    borderBottom: '2px solid #e5e7eb',
    backgroundColor: '#f9fafb',
    fontWeight: '600',
    color: '#374151',
  };

  const tdStyle = {
    padding: '16px',
    borderBottom: '1px solid #e5e7eb',
    color: '#4b5563',
  };

  const rowHoverStyle = {
    cursor: 'pointer',
    transition: 'background-color 0.2s',
  };

  const progressBarStyle = (percentage) => ({
    width: '100%',
    height: '8px',
    backgroundColor: '#e5e7eb',
    borderRadius: '4px',
    overflow: 'hidden',
  });

  const progressFillStyle = (percentage) => ({
    width: `${percentage}%`,
    height: '100%',
    backgroundColor: percentage >= 100 ? '#10b981' : '#3b82f6',
    transition: 'width 0.3s ease',
  });

  return (
    <div style={containerStyle}>
      {/* Header */}
      <div style={headerStyle}>
        <h1 style={titleStyle}>Đơn Bán Hàng</h1>
        <button
          style={buttonStyle}
          onClick={() => navigate('/sales-orders/new')}
          onMouseEnter={(e) => e.target.style.backgroundColor = '#4338ca'}
          onMouseLeave={(e) => e.target.style.backgroundColor = '#4f46e5'}
        >
          + Tạo đơn mới
        </button>
      </div>

      {/* Stats Cards */}
      <div style={statsContainerStyle}>
        <div style={statCardStyle('#f59e0b')}>
          <div style={{ fontSize: '24px', fontWeight: '700', color: '#111827' }}>{stats.open}</div>
          <div style={{ fontSize: '13px', color: '#6b7280', marginTop: '4px' }}>Chờ duyệt</div>
        </div>
        <div style={statCardStyle('#3b82f6')}>
          <div style={{ fontSize: '24px', fontWeight: '700', color: '#111827' }}>{stats.approved}</div>
          <div style={{ fontSize: '13px', color: '#6b7280', marginTop: '4px' }}>Sẵn sàng xuất</div>
        </div>
        <div style={statCardStyle('#8b5cf6')}>
          <div style={{ fontSize: '24px', fontWeight: '700', color: '#111827' }}>{stats.partiallyDelivered}</div>
          <div style={{ fontSize: '13px', color: '#6b7280', marginTop: '4px' }}>Giao một phần</div>
        </div>
        <div style={statCardStyle('#10b981')}>
          <div style={{ fontSize: '24px', fontWeight: '700', color: '#111827' }}>{stats.completed}</div>
          <div style={{ fontSize: '13px', color: '#6b7280', marginTop: '4px' }}>Hoàn thành</div>
        </div>
        <div style={statCardStyle('#6b7280')}>
          <div style={{ fontSize: '24px', fontWeight: '700', color: '#111827' }}>{stats.total}</div>
          <div style={{ fontSize: '13px', color: '#6b7280', marginTop: '4px' }}>Tổng đơn</div>
        </div>
      </div>

      {/* Filters */}
      <div style={filterContainerStyle}>
        <input
          type="text"
          placeholder="Tìm theo mã, tên, khách hàng..."
          style={searchInputStyle}
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
        />
        <button 
          style={{ ...buttonStyle, backgroundColor: '#6b7280', padding: '10px 20px' }}
          onClick={handleSearch}
        >
          Tìm kiếm
        </button>
        <select
          style={selectStyle}
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
        >
          <option value="ALL">Tất cả trạng thái</option>
          <option value="ORDER_OPEN">Chờ duyệt</option>
          <option value="ORDER_APPROVED">Đã duyệt</option>
          <option value="ORDER_PARTIALLY_DELIVERED">Giao một phần</option>
          <option value="ORDER_COMPLETED">Hoàn thành</option>
          <option value="ORDER_CANCELLED">Đã hủy</option>
        </select>
        <button
          style={{ ...buttonStyle, backgroundColor: '#10b981', padding: '10px 20px' }}
          onClick={loadOrders}
        >
          Làm mới
        </button>
      </div>

      {/* Error Display */}
      {error && (
        <div style={{
          padding: '16px',
          backgroundColor: '#fee2e2',
          color: '#991b1b',
          borderRadius: '8px',
          marginBottom: '16px',
        }}>
          {error}
        </div>
      )}

      {/* Loading State */}
      {loading ? (
        <div style={{ textAlign: 'center', padding: '48px', color: '#6b7280' }}>
          Đang tải dữ liệu...
        </div>
      ) : (
        /* Orders Table */
        <div style={tableContainerStyle}>
          <table style={tableStyle}>
            <thead>
              <tr>
                <th style={thStyle}>Mã đơn</th>
                <th style={thStyle}>Khách hàng</th>
                <th style={thStyle}>Ngày đặt</th>
                <th style={thStyle}>Trạng thái</th>
                <th style={thStyle}>Thanh toán</th>
                <th style={thStyle}>Tiến độ giao</th>
                <th style={thStyle}>Tổng tiền</th>
                <th style={thStyle}>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {orders.length === 0 ? (
                <tr>
                  <td colSpan="8" style={{ ...tdStyle, textAlign: 'center', padding: '48px' }}>
                    Không có đơn hàng nào
                  </td>
                </tr>
              ) : (
                orders.map((order) => (
                  <tr
                    key={order.id}
                    style={rowHoverStyle}
                    onClick={() => navigate(`/sales-orders/${order.id}`)}
                    onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f9fafb'}
                    onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'white'}
                  >
                    <td style={tdStyle}>
                      <span style={{ fontWeight: '600', color: '#4f46e5' }}>{order.code}</span>
                    </td>
                    <td style={tdStyle}>{order.customerName}</td>
                    <td style={tdStyle}>{formatDate(order.orderDate)}</td>
                    <td style={tdStyle}>{getStatusBadge(order.status)}</td>
                    <td style={tdStyle}>{getPaymentBadge(order.paymentStatus)}</td>
                    <td style={tdStyle}>
                      <div style={{ width: '120px' }}>
                        <div style={{ 
                          display: 'flex', 
                          justifyContent: 'space-between', 
                          marginBottom: '4px',
                          fontSize: '12px',
                          color: '#6b7280'
                        }}>
                          <span>{order.deliveredQuantity || 0}/{order.totalQuantity || 0}</span>
                          <span>{Math.round(order.deliveredPercentage || 0)}%</span>
                        </div>
                        <div style={progressBarStyle(order.deliveredPercentage || 0)}>
                          <div style={progressFillStyle(order.deliveredPercentage || 0)} />
                        </div>
                      </div>
                    </td>
                    <td style={tdStyle}>
                      <span style={{ fontWeight: '600' }}>{formatCurrency(order.grandTotal)}</span>
                    </td>
                    <td style={tdStyle}>
                      <button
                        style={{
                          padding: '6px 12px',
                          backgroundColor: '#f3f4f6',
                          border: 'none',
                          borderRadius: '6px',
                          fontSize: '12px',
                          fontWeight: '500',
                          color: '#374151',
                          cursor: 'pointer',
                        }}
                        onClick={(e) => {
                          e.stopPropagation();
                          navigate(`/sales-orders/${order.id}`);
                        }}
                      >
                        Xem
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
