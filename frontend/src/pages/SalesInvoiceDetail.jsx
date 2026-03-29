import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { salesInvoiceApi } from '../services/api';
import { ROLES, hasAnyRole } from '../services/roleService';

/**
 * Sales Invoice Detail - Chi tiết Hóa đơn bán hàng
 */
export default function SalesInvoiceDetail() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [invoice, setInvoice] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [actionLoading, setActionLoading] = useState(false);
  const [showPaymentModal, setShowPaymentModal] = useState(false);
  const [paymentAmount, setPaymentAmount] = useState('');
  const [paymentMethod, setPaymentMethod] = useState('');
  const [paymentReference, setPaymentReference] = useState('');

  useEffect(() => {
    loadInvoice();
  }, [id]);

  const loadInvoice = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await salesInvoiceApi.getById(id);
      setInvoice(response.data);
    } catch (err) {
      setError(err.message || 'Không thể tải hóa đơn');
    } finally {
      setLoading(false);
    }
  };

  const handleIssue = async () => {
    if (!window.confirm('Xuất hóa đơn này cho khách hàng?')) return;
    try {
      setActionLoading(true);
      await salesInvoiceApi.issue(id);
      loadInvoice();
    } catch (err) {
      setError(err.message);
    } finally {
      setActionLoading(false);
    }
  };

  const handleRecordPayment = async () => {
    if (!paymentAmount || Number(paymentAmount) <= 0) {
      alert('Vui lòng nhập số tiền thanh toán hợp lệ');
      return;
    }
    try {
      setActionLoading(true);
      await salesInvoiceApi.recordPayment(id, paymentAmount, paymentMethod, paymentReference);
      setShowPaymentModal(false);
      setPaymentAmount('');
      setPaymentMethod('');
      setPaymentReference('');
      loadInvoice();
    } catch (err) {
      setError(err.message);
    } finally {
      setActionLoading(false);
    }
  };

  const handleCancel = async () => {
    if (!window.confirm('Hủy hóa đơn này?')) return;
    try {
      setActionLoading(true);
      await salesInvoiceApi.cancel(id);
      loadInvoice();
    } catch (err) {
      setError(err.message);
    } finally {
      setActionLoading(false);
    }
  };

  const getStatusBadge = (status) => {
    const styles = {
      DRAFT: { bg: '#f3f4f6', color: '#374151', label: 'Nháp' },
      ISSUED: { bg: '#fef3c7', color: '#92400e', label: 'Đã xuất' },
      PARTIALLY_PAID: { bg: '#dbeafe', color: '#1e40af', label: 'TT một phần' },
      PAID: { bg: '#d1fae5', color: '#065f46', label: 'Đã thanh toán' },
      OVERDUE: { bg: '#fee2e2', color: '#991b1b', label: 'Quá hạn' },
      CANCELLED: { bg: '#e5e7eb', color: '#6b7280', label: 'Đã hủy' },
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

  if (!invoice) return <div style={containerStyle}>Không tìm thấy hóa đơn</div>;

  return (
    <div style={containerStyle}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '24px' }}>
        <div>
          <button
            style={{ ...buttonStyle, backgroundColor: '#f3f4f6', color: '#374151', marginBottom: '12px' }}
            onClick={() => navigate('/sales-invoices')}
          >
            ← Quay lại
          </button>
          <h1 style={{ fontSize: '28px', fontWeight: '700', color: '#111827', margin: '0 0 8px 0' }}>
            Hóa đơn: {invoice.code}
          </h1>
          <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
            {getStatusBadge(invoice.status)}
            {invoice.overdue && (
              <span style={{ color: '#ef4444', fontWeight: '600' }}>
                Quá hạn {invoice.daysOverdue} ngày
              </span>
            )}
          </div>
        </div>
        
        {/* Nút hành động */}
        <div style={{ display: 'flex', gap: '12px' }}>
          {invoice.status === 'DRAFT' && hasAnyRole([ROLES.ADMIN, ROLES.ACCOUNTANT, ROLES.SALES_MANAGER]) && (
            <>
              <button
                style={{ ...buttonStyle, backgroundColor: '#10b981', color: 'white' }}
                onClick={handleIssue}
                disabled={actionLoading}
              >
                Xuất hóa đơn
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
          {(invoice.status === 'ISSUED' || invoice.status === 'PARTIALLY_PAID' || invoice.status === 'OVERDUE') && (
            <>
              {hasAnyRole([ROLES.ADMIN, ROLES.ACCOUNTANT]) && (
                <button
                  style={{ ...buttonStyle, backgroundColor: '#4f46e5', color: 'white' }}
                  onClick={() => {
                    setPaymentAmount(invoice.remainingAmount || 0);
                    setShowPaymentModal(true);
                  }}
                >
                  Ghi nhận thanh toán
                </button>
              )}
              {(invoice.paidAmount || 0) === 0 && hasAnyRole([ROLES.ADMIN, ROLES.ACCOUNTANT]) && (
                <button
                  style={{ ...buttonStyle, backgroundColor: '#ef4444', color: 'white' }}
                  onClick={handleCancel}
                  disabled={actionLoading}
                >
                  Hủy
                </button>
              )}
            </>
          )}
        </div>
      </div>

      {/* Thẻ thông tin */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '24px', marginBottom: '24px' }}>
        <div style={cardStyle}>
          <h3 style={{ fontSize: '14px', fontWeight: '600', color: '#6b7280', marginBottom: '16px' }}>Khách hàng</h3>
          <div style={{ fontSize: '16px', fontWeight: '600', color: '#111827' }}>{invoice.customerName}</div>
          <div style={{ fontSize: '14px', color: '#6b7280' }}>{invoice.customerCode}</div>
          {invoice.customerEmail && (
            <div style={{ fontSize: '14px', color: '#4b5563', marginTop: '8px' }}>{invoice.customerEmail}</div>
          )}
        </div>
        <div style={cardStyle}>
          <h3 style={{ fontSize: '14px', fontWeight: '600', color: '#6b7280', marginBottom: '16px' }}>Chi tiết hóa đơn</h3>
          <div style={{ display: 'grid', gap: '6px', fontSize: '14px' }}>
            <div><strong>Ngày HĐ:</strong> {formatDate(invoice.invoiceDate)}</div>
            <div><strong>Hạn TT:</strong> 
              <span style={{ color: invoice.overdue ? '#ef4444' : 'inherit', marginLeft: '4px' }}>
                {formatDate(invoice.dueDate)}
              </span>
            </div>
            <div><strong>Đơn bán hàng:</strong> 
              <span 
                style={{ color: '#4f46e5', cursor: 'pointer', marginLeft: '4px' }}
                onClick={() => navigate(`/sales-orders/${invoice.salesOrderId}`)}
              >
                {invoice.salesOrderCode}
              </span>
            </div>
          </div>
        </div>
        <div style={cardStyle}>
          <h3 style={{ fontSize: '14px', fontWeight: '600', color: '#6b7280', marginBottom: '16px' }}>Tổng kết thanh toán</h3>
          <div style={{ display: 'grid', gap: '8px', fontSize: '14px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span>Tổng tiền:</span>
              <span style={{ fontWeight: '600' }}>{formatCurrency(invoice.totalAmount)}</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span>Đã thanh toán:</span>
              <span style={{ fontWeight: '600', color: '#10b981' }}>{formatCurrency(invoice.paidAmount)}</span>
            </div>
            <div style={{ borderTop: '1px solid #e5e7eb', paddingTop: '8px', display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ fontWeight: '600' }}>Còn nợ:</span>
              <span style={{ fontWeight: '700', color: (invoice.remainingAmount || 0) > 0 ? '#ef4444' : '#10b981', fontSize: '18px' }}>
                {formatCurrency(invoice.remainingAmount)}
              </span>
            </div>
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
              <th style={{ padding: '12px', textAlign: 'center', borderBottom: '2px solid #e5e7eb', fontWeight: '600' }}>SL</th>
              <th style={{ padding: '12px', textAlign: 'right', borderBottom: '2px solid #e5e7eb', fontWeight: '600' }}>Đơn giá</th>
              <th style={{ padding: '12px', textAlign: 'right', borderBottom: '2px solid #e5e7eb', fontWeight: '600' }}>Thành tiền</th>
            </tr>
          </thead>
          <tbody>
            {(invoice.items || []).map((item) => (
              <tr key={item.id}>
                <td style={{ padding: '12px', borderBottom: '1px solid #e5e7eb' }}>
                  <div style={{ fontWeight: '600' }}>{item.productName}</div>
                  <div style={{ fontSize: '12px', color: '#6b7280' }}>{item.productCode}</div>
                </td>
                <td style={{ padding: '12px', borderBottom: '1px solid #e5e7eb', textAlign: 'center' }}>
                  {item.quantity}
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
              <td colSpan="3" style={{ padding: '16px 12px', textAlign: 'right', fontWeight: '700', fontSize: '16px' }}>
                Tổng cộng:
              </td>
              <td style={{ padding: '16px 12px', textAlign: 'right', fontWeight: '700', fontSize: '16px', color: '#4f46e5' }}>
                {formatCurrency(invoice.totalAmount)}
              </td>
            </tr>
          </tfoot>
        </table>
      </div>

      {/* Modal thanh toán */}
      {showPaymentModal && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0,0,0,0.5)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000,
        }}>
          <div style={{
            background: 'white',
            borderRadius: '16px',
            padding: '32px',
            width: '400px',
            maxWidth: '90%',
          }}>
            <h2 style={{ margin: '0 0 24px 0', fontSize: '20px', fontWeight: '600' }}>Ghi nhận thanh toán</h2>
            <div style={{ marginBottom: '16px' }}>
              <label style={{ display: 'block', marginBottom: '6px', fontWeight: '500' }}>Số tiền *</label>
              <input
                type="number"
                step="1000"
                min="1000"
                max={invoice.remainingAmount}
                value={paymentAmount}
                onChange={(e) => setPaymentAmount(e.target.value)}
                style={{
                  width: '100%',
                  padding: '10px 12px',
                  border: '1px solid #d1d5db',
                  borderRadius: '8px',
                  fontSize: '14px',
                }}
              />
            </div>
            <div style={{ marginBottom: '16px' }}>
              <label style={{ display: 'block', marginBottom: '6px', fontWeight: '500' }}>Hình thức TT</label>
              <select
                value={paymentMethod}
                onChange={(e) => setPaymentMethod(e.target.value)}
                style={{
                  width: '100%',
                  padding: '10px 12px',
                  border: '1px solid #d1d5db',
                  borderRadius: '8px',
                  fontSize: '14px',
                }}
              >
                <option value="">Chọn hình thức</option>
                <option value="Chuyển khoản">Chuyển khoản</option>
                <option value="Tiền mặt">Tiền mặt</option>
                <option value="Thẻ tín dụng">Thẻ tín dụng</option>
                <option value="Séc">Séc</option>
              </select>
            </div>
            <div style={{ marginBottom: '24px' }}>
              <label style={{ display: 'block', marginBottom: '6px', fontWeight: '500' }}>Mã giao dịch</label>
              <input
                type="text"
                value={paymentReference}
                onChange={(e) => setPaymentReference(e.target.value)}
                placeholder="Mã GD, số séc, ..."
                style={{
                  width: '100%',
                  padding: '10px 12px',
                  border: '1px solid #d1d5db',
                  borderRadius: '8px',
                  fontSize: '14px',
                }}
              />
            </div>
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
              <button
                style={{ ...buttonStyle, backgroundColor: '#f3f4f6', color: '#374151' }}
                onClick={() => setShowPaymentModal(false)}
              >
                Hủy
              </button>
              <button
                style={{ ...buttonStyle, backgroundColor: '#4f46e5', color: 'white' }}
                onClick={handleRecordPayment}
                disabled={actionLoading}
              >
                {actionLoading ? 'Đang lưu...' : 'Xác nhận'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Ghi chú */}
      {invoice.notes && (
        <div style={cardStyle}>
          <h2 style={{ fontSize: '18px', fontWeight: '600', marginBottom: '12px', color: '#111827' }}>Ghi chú</h2>
          <p style={{ color: '#4b5563', margin: 0 }}>{invoice.notes}</p>
        </div>
      )}
    </div>
  );
}
