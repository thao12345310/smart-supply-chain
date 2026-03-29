import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { salesOrderApi, goodsIssueApi } from '../services/api';
import { ROLES, hasAnyRole } from '../services/roleService';

/**
 * Sales Order Detail - Chi tiết Đơn bán hàng
 */
export default function SalesOrderDetail() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [order, setOrder] = useState(null);
  const [goodsIssues, setGoodsIssues] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [actionLoading, setActionLoading] = useState(false);

  useEffect(() => {
    loadOrder();
  }, [id]);

  const loadOrder = async () => {
    try {
      setLoading(true);
      setError(null);
      const [orderRes, giRes] = await Promise.all([
        salesOrderApi.getById(id),
        goodsIssueApi.getBySalesOrderId(id),
      ]);
      setOrder(orderRes.data);
      setGoodsIssues(giRes.data || []);
    } catch (err) {
      setError(err.message || 'Không thể tải đơn hàng');
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = async () => {
    if (!window.confirm('Xác nhận duyệt đơn hàng này? Tồn kho sẽ được giữ lại.')) return;
    try {
      setActionLoading(true);
      await salesOrderApi.approve(id);
      loadOrder();
    } catch (err) {
      setError(err.message);
    } finally {
      setActionLoading(false);
    }
  };

  const handleReject = async () => {
    const reason = window.prompt('Nhập lý do từ chối:');
    if (!reason) return;
    try {
      setActionLoading(true);
      await salesOrderApi.reject(id, null, reason);
      loadOrder();
    } catch (err) {
      setError(err.message);
    } finally {
      setActionLoading(false);
    }
  };

  const handleCancel = async () => {
    const reason = window.prompt('Nhập lý do hủy:');
    if (!reason) return;
    try {
      setActionLoading(true);
      await salesOrderApi.cancel(id, reason);
      loadOrder();
    } catch (err) {
      setError(err.message);
    } finally {
      setActionLoading(false);
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
        padding: '6px 16px',
        borderRadius: '9999px',
        fontSize: '14px',
        fontWeight: '600',
        backgroundColor: style.bg,
        color: style.color,
      }}>
        {style.label}
      </span>
    );
  };

  const formatCurrency = (amount) => {
    if (amount == null) return '-';
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('vi-VN', {
      year: 'numeric', month: '2-digit', day: '2-digit',
    });
  };

  const containerStyle = {
    padding: '24px',
    maxWidth: '1400px',
    margin: '0 auto',
    fontFamily: "'Inter', -apple-system, sans-serif",
  };

  const cardStyle = {
    background: 'white',
    borderRadius: '12px',
    padding: '24px',
    marginBottom: '24px',
    boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
  };

  const buttonStyle = {
    padding: '10px 20px',
    borderRadius: '8px',
    fontSize: '14px',
    fontWeight: '600',
    cursor: 'pointer',
    border: 'none',
    transition: 'all 0.2s',
  };

  if (loading) {
    return <div style={containerStyle}>Đang tải...</div>;
  }

  if (error) {
    return (
      <div style={containerStyle}>
        <div style={{ padding: '16px', backgroundColor: '#fee2e2', color: '#991b1b', borderRadius: '8px' }}>
          {error}
        </div>
        <button style={{ marginTop: '16px' }} onClick={() => navigate('/sales-orders')}>
          Quay lại
        </button>
      </div>
    );
  }

  if (!order) {
    return <div style={containerStyle}>Không tìm thấy đơn hàng</div>;
  }

  return (
    <div style={containerStyle}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '24px' }}>
        <div>
          <button
            style={{ ...buttonStyle, backgroundColor: '#f3f4f6', color: '#374151', marginBottom: '12px' }}
            onClick={() => navigate('/sales-orders')}
          >
            ← Quay lại
          </button>
          <h1 style={{ fontSize: '28px', fontWeight: '700', color: '#111827', margin: '0 0 8px 0' }}>
            Đơn bán hàng: {order.code}
          </h1>
          <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
            {getStatusBadge(order.status)}
            <span style={{ color: '#6b7280' }}>Thanh toán: {order.paymentStatusDisplayName || order.paymentStatus}</span>
          </div>
        </div>
        
        {/* Nút hành động */}
        <div style={{ display: 'flex', gap: '12px' }}>
          {order.status === 'ORDER_OPEN' && (
            <>
              {hasAnyRole([ROLES.ADMIN, ROLES.SALES_MANAGER, ROLES.ACCOUNTANT]) && (
                <>
                  <button
                    style={{ ...buttonStyle, backgroundColor: '#10b981', color: 'white' }}
                    onClick={handleApprove}
                    disabled={actionLoading}
                  >
                    ✓ Duyệt
                  </button>
                  <button
                    style={{ ...buttonStyle, backgroundColor: '#ef4444', color: 'white' }}
                    onClick={handleReject}
                    disabled={actionLoading}
                  >
                    ✗ Từ chối
                  </button>
                </>
              )}
              {hasAnyRole([ROLES.ADMIN, ROLES.SALES_STAFF]) && (
                <button
                  style={{ ...buttonStyle, backgroundColor: '#4f46e5', color: 'white' }}
                  onClick={() => navigate(`/sales-orders/${id}/edit`)}
                >
                  Sửa
                </button>
              )}
            </>
          )}
          {(order.status === 'ORDER_APPROVED' || order.status === 'ORDER_PARTIALLY_DELIVERED') && (
            <>
              {hasAnyRole([ROLES.ADMIN, ROLES.WAREHOUSE_STAFF]) && (
                <button
                  style={{ ...buttonStyle, backgroundColor: '#4f46e5', color: 'white' }}
                  onClick={() => navigate(`/goods-issues/new?salesOrderId=${id}`)}
                >
                  + Tạo phiếu xuất
                </button>
              )}
              {hasAnyRole([ROLES.ADMIN, ROLES.SALES_STAFF, ROLES.SALES_MANAGER]) && (
                <button
                  style={{ ...buttonStyle, backgroundColor: '#f59e0b', color: 'white' }}
                  onClick={handleCancel}
                  disabled={actionLoading}
                >
                  Hủy đơn
                </button>
              )}
            </>
          )}
        </div>
      </div>

      {/* Thông tin đơn hàng */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '24px', marginBottom: '24px' }}>
        <div style={cardStyle}>
          <h3 style={{ fontSize: '14px', fontWeight: '600', color: '#6b7280', marginBottom: '16px' }}>Khách hàng</h3>
          <div style={{ fontSize: '16px', fontWeight: '600', color: '#111827' }}>{order.customerName}</div>
          <div style={{ fontSize: '14px', color: '#6b7280' }}>{order.customerCode}</div>
          {order.deliveryAddressText && (
            <div style={{ marginTop: '12px', fontSize: '14px', color: '#4b5563' }}>
              <strong>Giao tại:</strong> {order.deliveryAddressText}
            </div>
          )}
        </div>
        <div style={cardStyle}>
          <h3 style={{ fontSize: '14px', fontWeight: '600', color: '#6b7280', marginBottom: '16px' }}>Chi tiết đơn</h3>
          <div style={{ display: 'grid', gap: '8px', fontSize: '14px' }}>
            <div><strong>Ngày đặt:</strong> {formatDate(order.orderDate)}</div>
            <div><strong>Ngày giao dự kiến:</strong> {formatDate(order.expectedDeliveryDate)}</div>
            <div><strong>Kho:</strong> {order.warehouseName || '-'}</div>
          </div>
        </div>
        <div style={cardStyle}>
          <h3 style={{ fontSize: '14px', fontWeight: '600', color: '#6b7280', marginBottom: '16px' }}>Tiến độ giao hàng</h3>
          <div style={{ fontSize: '28px', fontWeight: '700', color: '#4f46e5' }}>
            {Math.round(order.deliveredPercentage || 0)}%
          </div>
          <div style={{ fontSize: '14px', color: '#6b7280' }}>
            {order.deliveredQuantity || 0} / {order.totalQuantity || 0} sản phẩm đã giao
          </div>
          <div style={{ 
            marginTop: '12px', 
            height: '8px', 
            backgroundColor: '#e5e7eb', 
            borderRadius: '4px',
            overflow: 'hidden' 
          }}>
            <div style={{
              width: `${order.deliveredPercentage || 0}%`,
              height: '100%',
              backgroundColor: order.deliveredPercentage >= 100 ? '#10b981' : '#4f46e5',
            }} />
          </div>
        </div>
      </div>

      {/* Chi tiết sản phẩm */}
      <div style={cardStyle}>
        <h2 style={{ fontSize: '18px', fontWeight: '600', marginBottom: '20px', color: '#111827' }}>
          Chi tiết sản phẩm
        </h2>
        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
          <thead>
            <tr>
              <th style={{ padding: '12px', textAlign: 'left', borderBottom: '2px solid #e5e7eb', fontWeight: '600' }}>Sản phẩm</th>
              <th style={{ padding: '12px', textAlign: 'center', borderBottom: '2px solid #e5e7eb', fontWeight: '600' }}>Đặt hàng</th>
              <th style={{ padding: '12px', textAlign: 'center', borderBottom: '2px solid #e5e7eb', fontWeight: '600' }}>Đã giao</th>
              <th style={{ padding: '12px', textAlign: 'center', borderBottom: '2px solid #e5e7eb', fontWeight: '600' }}>Còn lại</th>
              <th style={{ padding: '12px', textAlign: 'right', borderBottom: '2px solid #e5e7eb', fontWeight: '600' }}>Đơn giá</th>
              <th style={{ padding: '12px', textAlign: 'right', borderBottom: '2px solid #e5e7eb', fontWeight: '600' }}>Thành tiền</th>
            </tr>
          </thead>
          <tbody>
            {(order.items || []).map((item) => (
              <tr key={item.id}>
                <td style={{ padding: '12px', borderBottom: '1px solid #e5e7eb' }}>
                  <div style={{ fontWeight: '600' }}>{item.productName}</div>
                  <div style={{ fontSize: '12px', color: '#6b7280' }}>{item.productCode}</div>
                </td>
                <td style={{ padding: '12px', borderBottom: '1px solid #e5e7eb', textAlign: 'center' }}>
                  {item.quantity}
                </td>
                <td style={{ padding: '12px', borderBottom: '1px solid #e5e7eb', textAlign: 'center' }}>
                  <span style={{ color: '#10b981', fontWeight: '600' }}>{item.deliveredQuantity || 0}</span>
                </td>
                <td style={{ padding: '12px', borderBottom: '1px solid #e5e7eb', textAlign: 'center' }}>
                  <span style={{ color: item.remainingQuantity > 0 ? '#f59e0b' : '#10b981', fontWeight: '600' }}>
                    {item.remainingQuantity || 0}
                  </span>
                </td>
                <td style={{ padding: '12px', borderBottom: '1px solid #e5e7eb', textAlign: 'right' }}>
                  {formatCurrency(item.unitPrice)}
                </td>
                <td style={{ padding: '12px', borderBottom: '1px solid #e5e7eb', textAlign: 'right', fontWeight: '600' }}>
                  {formatCurrency(item.totalAmount)}
                </td>
              </tr>
            ))}
          </tbody>
          <tfoot>
            <tr>
              <td colSpan="5" style={{ padding: '12px', textAlign: 'right', fontWeight: '600' }}>Tạm tính:</td>
              <td style={{ padding: '12px', textAlign: 'right', fontWeight: '600' }}>{formatCurrency(order.totalAmount)}</td>
            </tr>
            {order.discountAmount > 0 && (
              <tr>
                <td colSpan="5" style={{ padding: '12px', textAlign: 'right' }}>Chiết khấu:</td>
                <td style={{ padding: '12px', textAlign: 'right', color: '#ef4444' }}>-{formatCurrency(order.discountAmount)}</td>
              </tr>
            )}
            {order.shippingCost > 0 && (
              <tr>
                <td colSpan="5" style={{ padding: '12px', textAlign: 'right' }}>Phí vận chuyển:</td>
                <td style={{ padding: '12px', textAlign: 'right' }}>{formatCurrency(order.shippingCost)}</td>
              </tr>
            )}
            <tr style={{ backgroundColor: '#f9fafb' }}>
              <td colSpan="5" style={{ padding: '16px 12px', textAlign: 'right', fontWeight: '700', fontSize: '16px' }}>
                Tổng cộng:
              </td>
              <td style={{ padding: '16px 12px', textAlign: 'right', fontWeight: '700', fontSize: '16px', color: '#4f46e5' }}>
                {formatCurrency(order.grandTotal)}
              </td>
            </tr>
          </tfoot>
        </table>
      </div>

      {/* Danh sách phiếu xuất */}
      {hasAnyRole([ROLES.ADMIN, ROLES.SALES_STAFF, ROLES.SALES_MANAGER, ROLES.WAREHOUSE_STAFF]) && (
        <div style={cardStyle}>
          <h2 style={{ fontSize: '18px', fontWeight: '600', marginBottom: '20px', color: '#111827' }}>
            Phiếu xuất kho ({goodsIssues.length})
          </h2>
          {goodsIssues.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '40px', color: '#6b7280' }}>
              Chưa có phiếu xuất kho nào.
            </div>
          ) : (
            <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
              <thead>
                <tr>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '2px solid #e5e7eb' }}>Mã</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '2px solid #e5e7eb' }}>Ngày xuất</th>
                  <th style={{ padding: '12px', textAlign: 'center', borderBottom: '2px solid #e5e7eb' }}>Trạng thái</th>
                  <th style={{ padding: '12px', textAlign: 'center', borderBottom: '2px solid #e5e7eb' }}>Số lượng</th>
                  <th style={{ padding: '12px', textAlign: 'right', borderBottom: '2px solid #e5e7eb' }}>Giá trị</th>
                  <th style={{ padding: '12px', textAlign: 'center', borderBottom: '2px solid #e5e7eb' }}>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {goodsIssues.map((gi) => (
                  <tr key={gi.id}>
                    <td style={{ padding: '12px', borderBottom: '1px solid #e5e7eb' }}>
                      <span style={{ fontWeight: '600', color: '#4f46e5' }}>{gi.code}</span>
                    </td>
                    <td style={{ padding: '12px', borderBottom: '1px solid #e5e7eb' }}>{formatDate(gi.issueDate)}</td>
                    <td style={{ padding: '12px', borderBottom: '1px solid #e5e7eb', textAlign: 'center' }}>
                      <span style={{
                        padding: '4px 12px',
                        borderRadius: '9999px',
                        fontSize: '12px',
                        fontWeight: '600',
                        backgroundColor: gi.status === 'CONFIRMED' ? '#d1fae5' : '#fef3c7',
                        color: gi.status === 'CONFIRMED' ? '#065f46' : '#92400e',
                      }}>
                        {gi.status === 'CONFIRMED' ? 'Đã xác nhận' : gi.status === 'DRAFT' ? 'Nháp' : gi.status}
                      </span>
                    </td>
                    <td style={{ padding: '12px', borderBottom: '1px solid #e5e7eb', textAlign: 'center' }}>
                      {gi.totalIssuedQuantity || 0}
                    </td>
                    <td style={{ padding: '12px', borderBottom: '1px solid #e5e7eb', textAlign: 'right' }}>
                      {formatCurrency(gi.totalAmount)}
                    </td>
                    <td style={{ padding: '12px', borderBottom: '1px solid #e5e7eb', textAlign: 'center' }}>
                      <button
                        style={{ ...buttonStyle, padding: '6px 12px', backgroundColor: '#f3f4f6', color: '#374151' }}
                        onClick={() => navigate(`/goods-issues/${gi.id}`)}
                      >
                        Xem
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {/* Ghi chú */}
      {order.notes && (
        <div style={cardStyle}>
          <h2 style={{ fontSize: '18px', fontWeight: '600', marginBottom: '12px', color: '#111827' }}>Ghi chú</h2>
          <p style={{ color: '#4b5563', margin: 0 }}>{order.notes}</p>
        </div>
      )}

      {/* Lý do từ chối/hủy */}
      {order.rejectionReason && (
        <div style={{ ...cardStyle, backgroundColor: '#fef2f2', borderLeft: '4px solid #ef4444' }}>
          <h2 style={{ fontSize: '18px', fontWeight: '600', marginBottom: '12px', color: '#991b1b' }}>
            Lý do từ chối/hủy
          </h2>
          <p style={{ color: '#991b1b', margin: 0 }}>{order.rejectionReason}</p>
        </div>
      )}
    </div>
  );
}
