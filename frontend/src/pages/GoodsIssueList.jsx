import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { goodsIssueApi } from '../services/api';

/**
 * Goods Issue List - Danh sách Phiếu xuất kho
 */
export default function GoodsIssueList() {
  const navigate = useNavigate();
  const [issues, setIssues] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [statusFilter, setStatusFilter] = useState('ALL');

  useEffect(() => {
    loadIssues();
  }, [statusFilter]);

  const loadIssues = async () => {
    try {
      setLoading(true);
      setError(null);
      
      let response;
      if (statusFilter === 'ALL') {
        response = await goodsIssueApi.getAll();
      } else {
        response = await goodsIssueApi.getByStatus(statusFilter);
      }
      
      const data = response.data || [];
      // Sắp xếp phiếu mới nhất lên đầu (theo ngày xuất, sau đó theo mã/ID giảm dần)
      data.sort((a, b) => {
        const dateDiff = new Date(b.issueDate || 0) - new Date(a.issueDate || 0);
        if (dateDiff !== 0) return dateDiff;
        return (b.id || 0) - (a.id || 0);
      });
      setIssues(data);
    } catch (err) {
      setError(err.message || 'Không thể tải danh sách phiếu xuất');
    } finally {
      setLoading(false);
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

  const headerStyle = {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '24px',
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
  };

  const tableContainerStyle = {
    background: 'white',
    borderRadius: '12px',
    boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
    overflow: 'hidden',
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

  return (
    <div style={containerStyle}>
      <div style={headerStyle}>
        <h1 style={{ fontSize: '28px', fontWeight: '700', color: '#111827', margin: 0 }}>
          Phiếu Xuất Kho
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
          <option value="CONFIRMED">Đã xác nhận</option>
          <option value="CANCELLED">Đã hủy</option>
        </select>
        <button
          style={{ ...buttonStyle, backgroundColor: '#10b981', padding: '10px 20px' }}
          onClick={loadIssues}
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
        <div style={tableContainerStyle}>
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
            <thead>
              <tr>
                <th style={thStyle}>Mã phiếu</th>
                <th style={thStyle}>Đơn bán hàng</th>
                <th style={thStyle}>Khách hàng</th>
                <th style={thStyle}>Ngày xuất</th>
                <th style={thStyle}>Trạng thái</th>
                <th style={thStyle}>Số lượng</th>
                <th style={thStyle}>Giá trị</th>
                <th style={thStyle}>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {issues.length === 0 ? (
                <tr>
                  <td colSpan="8" style={{ ...tdStyle, textAlign: 'center', padding: '48px' }}>
                    Không có phiếu xuất kho nào
                  </td>
                </tr>
              ) : (
                issues.map((issue) => (
                  <tr
                    key={issue.id}
                    style={{ cursor: 'pointer' }}
                    onClick={() => navigate(`/goods-issues/${issue.id}`)}
                    onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f9fafb'}
                    onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'white'}
                  >
                    <td style={tdStyle}>
                      <span style={{ fontWeight: '600', color: '#4f46e5' }}>{issue.code}</span>
                    </td>
                    <td style={tdStyle}>{issue.salesOrderCode}</td>
                    <td style={tdStyle}>{issue.customerName}</td>
                    <td style={tdStyle}>{formatDate(issue.issueDate)}</td>
                    <td style={tdStyle}>{getStatusBadge(issue.status)}</td>
                    <td style={tdStyle}>{issue.totalIssuedQuantity || 0}</td>
                    <td style={tdStyle}>{formatCurrency(issue.totalAmount)}</td>
                    <td style={tdStyle}>
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
                          navigate(`/goods-issues/${issue.id}`);
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
