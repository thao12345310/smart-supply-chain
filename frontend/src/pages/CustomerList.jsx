import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { customerApi } from '../services/api';

/**
 * Customer List - Danh sách Khách hàng
 */
export default function CustomerList() {
  const navigate = useNavigate();
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    loadCustomers();
  }, []);

  const loadCustomers = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await customerApi.getAll();
      setCustomers(response.data || []);
    } catch (err) {
      setError(err.message || 'Không thể tải danh sách khách hàng');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async () => {
    if (!searchQuery.trim()) {
      loadCustomers();
      return;
    }
    try {
      setLoading(true);
      const response = await customerApi.search(searchQuery);
      setCustomers(response.data || []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount) => {
    if (amount == null) return '-';
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
  };

  const containerStyle = {
    padding: '24px',
    maxWidth: '1400px',
    margin: '0 auto',
    fontFamily: "'Inter', -apple-system, sans-serif",
  };

  const buttonStyle = {
    padding: '12px 24px',
    backgroundColor: '#4f46e5',
    color: 'white',
    border: 'none',
    borderRadius: '8px',
    fontSize: '14px',
    fontWeight: '600',
    cursor: 'pointer',
  };

  return (
    <div style={containerStyle}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <h1 style={{ fontSize: '28px', fontWeight: '700', color: '#111827', margin: 0 }}>
          Khách Hàng
        </h1>
        <button style={buttonStyle} onClick={() => navigate('/customers/new')}>
          + Thêm khách hàng
        </button>
      </div>

      {/* Tìm kiếm & Bộ lọc */}
      <div style={{ display: 'flex', gap: '16px', marginBottom: '24px' }}>
        <input
          type="text"
          placeholder="Tìm theo tên, mã, email..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
          style={{
            flex: 1,
            maxWidth: '400px',
            padding: '10px 16px',
            border: '1px solid #d1d5db',
            borderRadius: '8px',
            fontSize: '14px',
          }}
        />
        <button
          style={{ ...buttonStyle, backgroundColor: '#6b7280', padding: '10px 20px' }}
          onClick={handleSearch}
        >
          Tìm kiếm
        </button>
        <button
          style={{ ...buttonStyle, backgroundColor: '#10b981', padding: '10px 20px' }}
          onClick={loadCustomers}
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
                <th style={{ padding: '16px', textAlign: 'left', borderBottom: '2px solid #e5e7eb', backgroundColor: '#f9fafb', fontWeight: '600' }}>Mã KH</th>
                <th style={{ padding: '16px', textAlign: 'left', borderBottom: '2px solid #e5e7eb', backgroundColor: '#f9fafb', fontWeight: '600' }}>Tên</th>
                <th style={{ padding: '16px', textAlign: 'left', borderBottom: '2px solid #e5e7eb', backgroundColor: '#f9fafb', fontWeight: '600' }}>Liên hệ</th>
                <th style={{ padding: '16px', textAlign: 'right', borderBottom: '2px solid #e5e7eb', backgroundColor: '#f9fafb', fontWeight: '600' }}>Hạn mức</th>
                <th style={{ padding: '16px', textAlign: 'right', borderBottom: '2px solid #e5e7eb', backgroundColor: '#f9fafb', fontWeight: '600' }}>Công nợ</th>
                <th style={{ padding: '16px', textAlign: 'center', borderBottom: '2px solid #e5e7eb', backgroundColor: '#f9fafb', fontWeight: '600' }}>Trạng thái</th>
                <th style={{ padding: '16px', textAlign: 'center', borderBottom: '2px solid #e5e7eb', backgroundColor: '#f9fafb', fontWeight: '600' }}>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {customers.length === 0 ? (
                <tr>
                  <td colSpan="7" style={{ padding: '48px', textAlign: 'center', color: '#6b7280' }}>
                    Không có khách hàng nào
                  </td>
                </tr>
              ) : (
                customers.map((customer) => (
                  <tr
                    key={customer.id}
                    style={{ cursor: 'pointer' }}
                    onClick={() => navigate(`/customers/${customer.id}`)}
                    onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f9fafb'}
                    onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'white'}
                  >
                    <td style={{ padding: '16px', borderBottom: '1px solid #e5e7eb' }}>
                      <span style={{ fontWeight: '600', color: '#4f46e5' }}>{customer.code}</span>
                    </td>
                    <td style={{ padding: '16px', borderBottom: '1px solid #e5e7eb' }}>
                      <div style={{ fontWeight: '600' }}>{customer.name}</div>
                      <div style={{ fontSize: '12px', color: '#6b7280' }}>{customer.email}</div>
                    </td>
                    <td style={{ padding: '16px', borderBottom: '1px solid #e5e7eb' }}>
                      <div>{customer.contactName}</div>
                      <div style={{ fontSize: '12px', color: '#6b7280' }}>{customer.phone}</div>
                    </td>
                    <td style={{ padding: '16px', borderBottom: '1px solid #e5e7eb', textAlign: 'right' }}>
                      {formatCurrency(customer.creditLimit)}
                    </td>
                    <td style={{ padding: '16px', borderBottom: '1px solid #e5e7eb', textAlign: 'right' }}>
                      <span style={{ color: (customer.currentBalance || 0) > 0 ? '#ef4444' : '#10b981', fontWeight: '600' }}>
                        {formatCurrency(customer.currentBalance)}
                      </span>
                    </td>
                    <td style={{ padding: '16px', borderBottom: '1px solid #e5e7eb', textAlign: 'center' }}>
                      <span style={{
                        display: 'inline-block',
                        padding: '4px 12px',
                        borderRadius: '9999px',
                        fontSize: '12px',
                        fontWeight: '600',
                        backgroundColor: customer.active ? '#d1fae5' : '#fee2e2',
                        color: customer.active ? '#065f46' : '#991b1b',
                      }}>
                        {customer.active ? 'Hoạt động' : 'Ngừng HĐ'}
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
                          navigate(`/customers/${customer.id}`);
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
