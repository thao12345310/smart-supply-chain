import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { salesInvoiceApi } from '../services/api';

/**
 * Sales Invoice List - Danh sách Hóa đơn bán hàng
 */
export default function SalesInvoiceList() {
  const navigate = useNavigate();
  const [invoices, setInvoices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [statusFilter, setStatusFilter] = useState('ALL');

  useEffect(() => {
    loadInvoices();
  }, [statusFilter]);

  const loadInvoices = async () => {
    try {
      setLoading(true);
      setError(null);
      
      let response;
      if (statusFilter === 'ALL') {
        response = await salesInvoiceApi.getAll();
      } else if (statusFilter === 'OVERDUE') {
        response = await salesInvoiceApi.getOverdue();
      } else if (statusFilter === 'UNPAID') {
        response = await salesInvoiceApi.getUnpaid();
      } else {
        response = await salesInvoiceApi.getByStatus(statusFilter);
      }
      
      setInvoices(response.data || []);
    } catch (err) {
      setError(err.message || 'Không thể tải danh sách hóa đơn');
    } finally {
      setLoading(false);
    }
  };

  const getStatusBadge = (status) => {
    const styles = {
      DRAFT: { bg: '#f3f4f6', color: '#374151', label: 'Nháp' },
      ISSUED: { bg: '#fef3c7', color: '#92400e', label: 'Đã xuất' },
      PARTIALLY_PAID: { bg: '#dbeafe', color: '#1e40af', label: 'TT một phần' },
      PAID: { bg: '#d1fae5', color: '#065f46', label: 'Đã TT' },
      OVERDUE: { bg: '#fee2e2', color: '#991b1b', label: 'Quá hạn' },
      CANCELLED: { bg: '#e5e7eb', color: '#6b7280', label: 'Đã hủy' },
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

  return (
    <div style={containerStyle}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <h1 style={{ fontSize: '28px', fontWeight: '700', color: '#111827', margin: 0 }}>
          Hóa Đơn Bán Hàng
        </h1>
      </div>

      {/* Bộ lọc */}
      <div style={{ display: 'flex', gap: '16px', marginBottom: '24px' }}>
        <select
          style={{
            padding: '10px 16px',
            border: '1px solid #d1d5db',
            borderRadius: '8px',
            fontSize: '14px',
          }}
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
        >
          <option value="ALL">Tất cả trạng thái</option>
          <option value="DRAFT">Nháp</option>
          <option value="ISSUED">Đã xuất</option>
          <option value="PARTIALLY_PAID">TT một phần</option>
          <option value="PAID">Đã thanh toán</option>
          <option value="OVERDUE">Quá hạn</option>
          <option value="UNPAID">Chưa thanh toán</option>
          <option value="CANCELLED">Đã hủy</option>
        </select>
        <button
          style={{
            padding: '10px 20px',
            backgroundColor: '#10b981',
            color: 'white',
            border: 'none',
            borderRadius: '8px',
            fontSize: '14px',
            fontWeight: '600',
            cursor: 'pointer',
          }}
          onClick={loadInvoices}
        >
          Làm mới
        </button>
      </div>

      {error && (
        <div style={{ padding: '16px', backgroundColor: '#fee2e2', color: '#991b1b', borderRadius: '8px', marginBottom: '16px' }}>
          {error}
        </div>
      )}

      {loading ? (
        <div style={{ textAlign: 'center', padding: '48px', color: '#6b7280' }}>
          Đang tải dữ liệu...
        </div>
      ) : (
        <div style={{
          background: 'white',
          borderRadius: '12px',
          boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
          overflow: 'hidden',
        }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
            <thead>
              <tr>
                <th style={{ padding: '16px', textAlign: 'left', borderBottom: '2px solid #e5e7eb', backgroundColor: '#f9fafb', fontWeight: '600' }}>Mã HĐ</th>
                <th style={{ padding: '16px', textAlign: 'left', borderBottom: '2px solid #e5e7eb', backgroundColor: '#f9fafb', fontWeight: '600' }}>Khách hàng</th>
                <th style={{ padding: '16px', textAlign: 'left', borderBottom: '2px solid #e5e7eb', backgroundColor: '#f9fafb', fontWeight: '600' }}>Ngày HĐ</th>
                <th style={{ padding: '16px', textAlign: 'left', borderBottom: '2px solid #e5e7eb', backgroundColor: '#f9fafb', fontWeight: '600' }}>Hạn TT</th>
                <th style={{ padding: '16px', textAlign: 'center', borderBottom: '2px solid #e5e7eb', backgroundColor: '#f9fafb', fontWeight: '600' }}>Trạng thái</th>
                <th style={{ padding: '16px', textAlign: 'right', borderBottom: '2px solid #e5e7eb', backgroundColor: '#f9fafb', fontWeight: '600' }}>Tổng tiền</th>
                <th style={{ padding: '16px', textAlign: 'right', borderBottom: '2px solid #e5e7eb', backgroundColor: '#f9fafb', fontWeight: '600' }}>Còn nợ</th>
                <th style={{ padding: '16px', textAlign: 'center', borderBottom: '2px solid #e5e7eb', backgroundColor: '#f9fafb', fontWeight: '600' }}>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {invoices.length === 0 ? (
                <tr>
                  <td colSpan="8" style={{ padding: '48px', textAlign: 'center', color: '#6b7280' }}>
                    Không có hóa đơn nào
                  </td>
                </tr>
              ) : (
                invoices.map((invoice) => (
                  <tr
                    key={invoice.id}
                    style={{ cursor: 'pointer' }}
                    onClick={() => navigate(`/sales-invoices/${invoice.id}`)}
                    onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f9fafb'}
                    onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'white'}
                  >
                    <td style={{ padding: '16px', borderBottom: '1px solid #e5e7eb' }}>
                      <span style={{ fontWeight: '600', color: '#4f46e5' }}>{invoice.code}</span>
                    </td>
                    <td style={{ padding: '16px', borderBottom: '1px solid #e5e7eb' }}>{invoice.customerName}</td>
                    <td style={{ padding: '16px', borderBottom: '1px solid #e5e7eb' }}>{formatDate(invoice.invoiceDate)}</td>
                    <td style={{ padding: '16px', borderBottom: '1px solid #e5e7eb' }}>
                      <span style={{ color: invoice.overdue ? '#ef4444' : 'inherit' }}>
                        {formatDate(invoice.dueDate)}
                      </span>
                    </td>
                    <td style={{ padding: '16px', borderBottom: '1px solid #e5e7eb', textAlign: 'center' }}>
                      {getStatusBadge(invoice.status)}
                    </td>
                    <td style={{ padding: '16px', borderBottom: '1px solid #e5e7eb', textAlign: 'right', fontWeight: '600' }}>
                      {formatCurrency(invoice.totalAmount)}
                    </td>
                    <td style={{ padding: '16px', borderBottom: '1px solid #e5e7eb', textAlign: 'right' }}>
                      <span style={{ 
                        color: (invoice.remainingAmount || 0) > 0 ? '#ef4444' : '#10b981',
                        fontWeight: '600'
                      }}>
                        {formatCurrency(invoice.remainingAmount)}
                      </span>
                    </td>
                    <td style={{ padding: '16px', borderBottom: '1px solid #e5e7eb', textAlign: 'center' }}>
                      <button
                        style={{
                          padding: '6px 12px',
                          backgroundColor: '#f3f4f6',
                          border: 'none',
                          borderRadius: '6px',
                          fontSize: '12px',
                          fontWeight: '500',
                          cursor: 'pointer',
                        }}
                        onClick={(e) => {
                          e.stopPropagation();
                          navigate(`/sales-invoices/${invoice.id}`);
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
