import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { goodsIssueApi, salesInvoiceApi } from '../services/api';

/**
 * Goods Issue Detail - Chi tiết Phiếu xuất kho
 */
export default function GoodsIssueDetail() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [issue, setIssue] = useState(null);
  const [invoice, setInvoice] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [actionLoading, setActionLoading] = useState(false);

  useEffect(() => {
    loadIssue();
  }, [id]);

  const loadIssue = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await goodsIssueApi.getById(id);
      setIssue(response.data);
      
      // Tải hóa đơn liên quan
      if (response.data.status === 'CONFIRMED') {
        try {
          const invoiceRes = await salesInvoiceApi.getByGoodsIssueId(id);
          setInvoice(invoiceRes.data);
        } catch (e) {
          // Chưa có hóa đơn
        }
      }
    } catch (err) {
      setError(err.message || 'Không thể tải phiếu xuất');
    } finally {
      setLoading(false);
    }
  };

  const handleConfirm = async () => {
    if (!window.confirm('Xác nhận phiếu xuất này? Hệ thống sẽ:\n• Giảm tồn kho\n• Tạo hóa đơn tự động\n• Cập nhật trạng thái đơn hàng\n\nThao tác này không thể hoàn tác.')) {
      return;
    }
    try {
      setActionLoading(true);
      await goodsIssueApi.confirm(id);
      loadIssue();
    } catch (err) {
      setError(err.message);
    } finally {
      setActionLoading(false);
    }
  };

  const handleCancel = async () => {
    if (!window.confirm('Hủy phiếu xuất này?')) return;
    try {
      setActionLoading(true);
      await goodsIssueApi.cancel(id);
      loadIssue();
    } catch (err) {
      setError(err.message);
    } finally {
      setActionLoading(false);
    }
  };

  const getStatusBadge = (status) => {
    const styles = {
      DRAFT: { bg: '#fef3c7', color: '#92400e', label: 'Nháp' },
      CONFIRMED: { bg: '#d1fae5', color: '#065f46', label: 'Đã xác nhận' },
      CANCELLED: { bg: '#fee2e2', color: '#991b1b', label: 'Đã hủy' },
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
    maxWidth: '1200px',
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
  };

  if (loading) return <div style={containerStyle}>Đang tải...</div>;

  if (error) {
    return (
      <div style={containerStyle}>
        <div style={{ padding: '16px', backgroundColor: '#fee2e2', color: '#991b1b', borderRadius: '8px' }}>
          {error}
        </div>
      </div>
    );
  }

  if (!issue) return <div style={containerStyle}>Không tìm thấy phiếu xuất</div>;

  return (
    <div style={containerStyle}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '24px' }}>
        <div>
          <button
            style={{ ...buttonStyle, backgroundColor: '#f3f4f6', color: '#374151', marginBottom: '12px' }}
            onClick={() => navigate('/goods-issues')}
          >
            ← Quay lại
          </button>
          <h1 style={{ fontSize: '28px', fontWeight: '700', color: '#111827', margin: '0 0 8px 0' }}>
            Phiếu xuất: {issue.code}
          </h1>
          {getStatusBadge(issue.status)}
        </div>
        
        {/* Nút hành động */}
        <div style={{ display: 'flex', gap: '12px' }}>
          {issue.status === 'DRAFT' && (
            <>
              <button
                style={{ ...buttonStyle, backgroundColor: '#10b981', color: 'white' }}
                onClick={handleConfirm}
                disabled={actionLoading}
              >
                ✓ Xác nhận xuất
              </button>
              <button
                style={{ ...buttonStyle, backgroundColor: '#4f46e5', color: 'white' }}
                onClick={() => navigate(`/goods-issues/${id}/edit`)}
              >
                Sửa
              </button>
              <button
                style={{ ...buttonStyle, backgroundColor: '#ef4444', color: 'white' }}
                onClick={handleCancel}
                disabled={actionLoading}
              >
                Hủy
              </button>
            </>
          )}
        </div>
      </div>

      {/* Thẻ thông tin */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '24px', marginBottom: '24px' }}>
        <div style={cardStyle}>
          <h3 style={{ fontSize: '14px', fontWeight: '600', color: '#6b7280', marginBottom: '16px' }}>Đơn bán hàng</h3>
          <div 
            style={{ fontSize: '16px', fontWeight: '600', color: '#4f46e5', cursor: 'pointer' }}
            onClick={() => navigate(`/sales-orders/${issue.salesOrderId}`)}
          >
            {issue.salesOrderCode}
          </div>
          <div style={{ fontSize: '14px', color: '#4b5563', marginTop: '8px' }}>{issue.customerName}</div>
        </div>
        <div style={cardStyle}>
          <h3 style={{ fontSize: '14px', fontWeight: '600', color: '#6b7280', marginBottom: '16px' }}>Vận chuyển</h3>
          <div style={{ display: 'grid', gap: '6px', fontSize: '14px' }}>
            <div><strong>Ngày xuất:</strong> {formatDate(issue.issueDate)}</div>
            <div><strong>Phương thức:</strong> {issue.shippingMethod || '-'}</div>
            <div><strong>Đơn vị VC:</strong> {issue.carrierName || '-'}</div>
            <div><strong>Mã vận đơn:</strong> {issue.trackingNumber || '-'}</div>
          </div>
        </div>
        <div style={cardStyle}>
          <h3 style={{ fontSize: '14px', fontWeight: '600', color: '#6b7280', marginBottom: '16px' }}>Tổng kết</h3>
          <div style={{ fontSize: '28px', fontWeight: '700', color: '#4f46e5' }}>
            {issue.totalIssuedQuantity || 0}
          </div>
          <div style={{ fontSize: '14px', color: '#6b7280' }}>Tổng sản phẩm</div>
          <div style={{ marginTop: '12px', fontSize: '18px', fontWeight: '600' }}>
            {formatCurrency(issue.totalAmount)}
          </div>
        </div>
      </div>

      {/* Chi tiết sản phẩm */}
      <div style={cardStyle}>
        <h2 style={{ fontSize: '18px', fontWeight: '600', marginBottom: '20px', color: '#111827' }}>
          Sản phẩm đã xuất
        </h2>
        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
          <thead>
            <tr>
              <th style={{ padding: '12px', textAlign: 'left', borderBottom: '2px solid #e5e7eb', fontWeight: '600' }}>Sản phẩm</th>
              <th style={{ padding: '12px', textAlign: 'center', borderBottom: '2px solid #e5e7eb', fontWeight: '600' }}>SL xuất</th>
              <th style={{ padding: '12px', textAlign: 'left', borderBottom: '2px solid #e5e7eb', fontWeight: '600' }}>Số lô</th>
              <th style={{ padding: '12px', textAlign: 'right', borderBottom: '2px solid #e5e7eb', fontWeight: '600' }}>Đơn giá</th>
              <th style={{ padding: '12px', textAlign: 'right', borderBottom: '2px solid #e5e7eb', fontWeight: '600' }}>Thành tiền</th>
            </tr>
          </thead>
          <tbody>
            {(issue.items || []).map((item) => (
              <tr key={item.id}>
                <td style={{ padding: '12px', borderBottom: '1px solid #e5e7eb' }}>
                  <div style={{ fontWeight: '600' }}>{item.productName}</div>
                  <div style={{ fontSize: '12px', color: '#6b7280' }}>{item.productCode}</div>
                </td>
                <td style={{ padding: '12px', borderBottom: '1px solid #e5e7eb', textAlign: 'center', fontWeight: '600' }}>
                  {item.issuedQuantity}
                </td>
                <td style={{ padding: '12px', borderBottom: '1px solid #e5e7eb' }}>
                  {item.batchNumber || '-'}
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
            <tr style={{ backgroundColor: '#f9fafb' }}>
              <td colSpan="4" style={{ padding: '16px 12px', textAlign: 'right', fontWeight: '700', fontSize: '16px' }}>
                Tổng cộng:
              </td>
              <td style={{ padding: '16px 12px', textAlign: 'right', fontWeight: '700', fontSize: '16px', color: '#4f46e5' }}>
                {formatCurrency(issue.totalAmount)}
              </td>
            </tr>
          </tfoot>
        </table>
      </div>

      {/* Thông tin hóa đơn (nếu có) */}
      {invoice && (
        <div style={{ ...cardStyle, backgroundColor: '#f0fdf4', borderLeft: '4px solid #10b981' }}>
          <h2 style={{ fontSize: '18px', fontWeight: '600', marginBottom: '16px', color: '#065f46' }}>
            Hóa đơn đã tạo
          </h2>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr 1fr', gap: '16px', fontSize: '14px' }}>
            <div>
              <div style={{ color: '#6b7280' }}>Mã hóa đơn</div>
              <div style={{ fontWeight: '600', color: '#065f46' }}>{invoice.code}</div>
            </div>
            <div>
              <div style={{ color: '#6b7280' }}>Trạng thái</div>
              <div style={{ fontWeight: '600' }}>{invoice.status === 'PAID' ? 'Đã thanh toán' : invoice.status === 'ISSUED' ? 'Đã xuất' : invoice.status}</div>
            </div>
            <div>
              <div style={{ color: '#6b7280' }}>Số tiền</div>
              <div style={{ fontWeight: '600' }}>{formatCurrency(invoice.totalAmount)}</div>
            </div>
            <div>
              <div style={{ color: '#6b7280' }}>Hạn thanh toán</div>
              <div style={{ fontWeight: '600' }}>{formatDate(invoice.dueDate)}</div>
            </div>
          </div>
          <button
            style={{ ...buttonStyle, backgroundColor: '#10b981', color: 'white', marginTop: '16px' }}
            onClick={() => navigate(`/sales-invoices/${invoice.id}`)}
          >
            Xem hóa đơn
          </button>
        </div>
      )}

      {/* Ghi chú */}
      {issue.notes && (
        <div style={cardStyle}>
          <h2 style={{ fontSize: '18px', fontWeight: '600', marginBottom: '12px', color: '#111827' }}>Ghi chú</h2>
          <p style={{ color: '#4b5563', margin: 0 }}>{issue.notes}</p>
        </div>
      )}
    </div>
  );
}
