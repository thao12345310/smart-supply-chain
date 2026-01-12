import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { salesOrderApi, customerApi, productApi, warehouseApi } from '../services/api';

/**
 * Sales Order Form - Tạo/Sửa Đơn bán hàng
 */
export default function SalesOrderForm() {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEdit = Boolean(id);
  
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  
  // Master data
  const [customers, setCustomers] = useState([]);
  const [products, setProducts] = useState([]);
  const [warehouses, setWarehouses] = useState([]);
  const [addresses, setAddresses] = useState([]);
  
  // Form data
  const [formData, setFormData] = useState({
    orderName: '',
    customerId: '',
    deliveryAddressId: '',
    warehouseId: '',
    expectedDeliveryDate: '',
    discountAmount: 0,
    shippingCost: 0,
    notes: '',
    items: [],
  });

  useEffect(() => {
    loadMasterData();
    if (isEdit) {
      loadOrder();
    }
  }, [id]);

  const loadMasterData = async () => {
    try {
      const [customersRes, productsRes, warehousesRes] = await Promise.all([
        customerApi.getActive(),
        productApi.getAll(),
        warehouseApi.getAll(),
      ]);
      setCustomers(customersRes.data || []);
      setProducts(productsRes.data || []);
      setWarehouses(warehousesRes.data || []);
    } catch (err) {
      console.error('Lỗi tải dữ liệu:', err);
    }
  };

  const loadOrder = async () => {
    try {
      setLoading(true);
      const response = await salesOrderApi.getById(id);
      const order = response.data;
      setFormData({
        orderName: order.orderName || '',
        customerId: order.customerId || '',
        deliveryAddressId: order.deliveryAddressId || '',
        warehouseId: order.warehouseId || '',
        expectedDeliveryDate: order.expectedDeliveryDate || '',
        discountAmount: order.discountAmount || 0,
        shippingCost: order.shippingCost || 0,
        notes: order.notes || '',
        items: order.items || [],
      });
      if (order.customerId) {
        loadAddresses(order.customerId);
      }
    } catch (err) {
      setError('Không thể tải đơn hàng: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const loadAddresses = async (customerId) => {
    try {
      const response = await customerApi.getAddresses(customerId);
      setAddresses(response.data || []);
    } catch (err) {
      console.error('Lỗi tải địa chỉ:', err);
    }
  };

  const handleCustomerChange = (customerId) => {
    setFormData({ ...formData, customerId, deliveryAddressId: '' });
    if (customerId) {
      loadAddresses(customerId);
    } else {
      setAddresses([]);
    }
  };

  const handleAddItem = () => {
    setFormData({
      ...formData,
      items: [...formData.items, {
        productId: '',
        quantity: 1,
        unitPrice: 0,
        discountPercent: 0,
        taxPercent: 10,
        notes: '',
      }],
    });
  };

  const handleRemoveItem = (index) => {
    const newItems = [...formData.items];
    newItems.splice(index, 1);
    setFormData({ ...formData, items: newItems });
  };

  const handleItemChange = (index, field, value) => {
    const newItems = [...formData.items];
    newItems[index] = { ...newItems[index], [field]: value };
    
    // Tự động điền giá khi chọn sản phẩm
    if (field === 'productId') {
      const product = products.find(p => p.id === Number(value));
      if (product) {
        newItems[index].unitPrice = product.price || 0;
      }
    }
    
    setFormData({ ...formData, items: newItems });
  };

  const calculateItemTotal = (item) => {
    const qty = Number(item.quantity) || 0;
    const price = Number(item.unitPrice) || 0;
    const discount = Number(item.discountPercent) || 0;
    const tax = Number(item.taxPercent) || 0;
    
    let amount = qty * price;
    amount = amount * (1 - discount / 100);
    amount = amount * (1 + tax / 100);
    return amount;
  };

  const calculateTotal = () => {
    const itemsTotal = formData.items.reduce((sum, item) => sum + calculateItemTotal(item), 0);
    const discount = Number(formData.discountAmount) || 0;
    const shipping = Number(formData.shippingCost) || 0;
    return itemsTotal - discount + shipping;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!formData.customerId) {
      setError('Vui lòng chọn khách hàng');
      return;
    }
    
    if (formData.items.length === 0) {
      setError('Vui lòng thêm ít nhất một sản phẩm');
      return;
    }
    
    try {
      setSaving(true);
      setError(null);
      
      const payload = {
        ...formData,
        items: formData.items.map(item => ({
          ...item,
          productId: Number(item.productId),
          quantity: Number(item.quantity),
          unitPrice: Number(item.unitPrice),
          discountPercent: Number(item.discountPercent) || 0,
          taxPercent: Number(item.taxPercent) || 0,
        })),
      };
      
      if (isEdit) {
        await salesOrderApi.update(id, payload);
      } else {
        await salesOrderApi.create(payload);
      }
      
      navigate('/sales-orders');
    } catch (err) {
      setError(err.message || 'Không thể lưu đơn hàng');
    } finally {
      setSaving(false);
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(amount);
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

  const inputStyle = {
    width: '100%',
    padding: '10px 12px',
    border: '1px solid #d1d5db',
    borderRadius: '8px',
    fontSize: '14px',
    outline: 'none',
  };

  const labelStyle = {
    display: 'block',
    marginBottom: '6px',
    fontSize: '14px',
    fontWeight: '500',
    color: '#374151',
  };

  const buttonStyle = {
    padding: '12px 24px',
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

  return (
    <div style={containerStyle}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <h1 style={{ fontSize: '28px', fontWeight: '700', color: '#111827', margin: 0 }}>
          {isEdit ? 'Sửa đơn bán hàng' : 'Tạo đơn bán hàng mới'}
        </h1>
        <button
          style={{ ...buttonStyle, backgroundColor: '#f3f4f6', color: '#374151' }}
          onClick={() => navigate('/sales-orders')}
        >
          Hủy bỏ
        </button>
      </div>

      {error && (
        <div style={{ padding: '16px', backgroundColor: '#fee2e2', color: '#991b1b', borderRadius: '8px', marginBottom: '16px' }}>
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit}>
        {/* Thông tin khách hàng & giao hàng */}
        <div style={cardStyle}>
          <h2 style={{ fontSize: '18px', fontWeight: '600', marginBottom: '20px', color: '#111827' }}>
            Thông tin khách hàng
          </h2>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
            <div>
              <label style={labelStyle}>Khách hàng *</label>
              <select
                style={inputStyle}
                value={formData.customerId}
                onChange={(e) => handleCustomerChange(e.target.value)}
                required
              >
                <option value="">Chọn khách hàng</option>
                {customers.map(c => (
                  <option key={c.id} value={c.id}>{c.code} - {c.name}</option>
                ))}
              </select>
            </div>
            <div>
              <label style={labelStyle}>Địa chỉ giao hàng</label>
              <select
                style={inputStyle}
                value={formData.deliveryAddressId}
                onChange={(e) => setFormData({ ...formData, deliveryAddressId: e.target.value })}
              >
                <option value="">Chọn địa chỉ</option>
                {addresses.map(a => (
                  <option key={a.id} value={a.id}>{a.addressName} - {a.addressLine1}</option>
                ))}
              </select>
            </div>
            <div>
              <label style={labelStyle}>Tên đơn hàng</label>
              <input
                type="text"
                style={inputStyle}
                value={formData.orderName}
                onChange={(e) => setFormData({ ...formData, orderName: e.target.value })}
                placeholder="Nhập tên đơn hàng"
              />
            </div>
            <div>
              <label style={labelStyle}>Ngày giao dự kiến</label>
              <input
                type="date"
                style={inputStyle}
                value={formData.expectedDeliveryDate}
                onChange={(e) => setFormData({ ...formData, expectedDeliveryDate: e.target.value })}
              />
            </div>
            <div>
              <label style={labelStyle}>Kho xuất</label>
              <select
                style={inputStyle}
                value={formData.warehouseId}
                onChange={(e) => setFormData({ ...formData, warehouseId: e.target.value })}
              >
                <option value="">Chọn kho</option>
                {warehouses.map(w => (
                  <option key={w.id} value={w.id}>{w.code} - {w.name}</option>
                ))}
              </select>
            </div>
          </div>
        </div>

        {/* Chi tiết sản phẩm */}
        <div style={cardStyle}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
            <h2 style={{ fontSize: '18px', fontWeight: '600', color: '#111827', margin: 0 }}>
              Chi tiết sản phẩm
            </h2>
            <button
              type="button"
              style={{ ...buttonStyle, backgroundColor: '#10b981', color: 'white', padding: '8px 16px' }}
              onClick={handleAddItem}
            >
              + Thêm sản phẩm
            </button>
          </div>

          {formData.items.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '40px', color: '#6b7280' }}>
              Chưa có sản phẩm. Nhấn "Thêm sản phẩm" để thêm.
            </div>
          ) : (
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '2px solid #e5e7eb', fontWeight: '600' }}>Sản phẩm</th>
                  <th style={{ padding: '12px', textAlign: 'center', borderBottom: '2px solid #e5e7eb', fontWeight: '600', width: '100px' }}>SL</th>
                  <th style={{ padding: '12px', textAlign: 'right', borderBottom: '2px solid #e5e7eb', fontWeight: '600', width: '120px' }}>Đơn giá</th>
                  <th style={{ padding: '12px', textAlign: 'center', borderBottom: '2px solid #e5e7eb', fontWeight: '600', width: '80px' }}>CK %</th>
                  <th style={{ padding: '12px', textAlign: 'center', borderBottom: '2px solid #e5e7eb', fontWeight: '600', width: '80px' }}>Thuế %</th>
                  <th style={{ padding: '12px', textAlign: 'right', borderBottom: '2px solid #e5e7eb', fontWeight: '600', width: '120px' }}>Thành tiền</th>
                  <th style={{ padding: '12px', width: '60px' }}></th>
                </tr>
              </thead>
              <tbody>
                {formData.items.map((item, index) => (
                  <tr key={index}>
                    <td style={{ padding: '8px' }}>
                      <select
                        style={{ ...inputStyle, width: '100%' }}
                        value={item.productId}
                        onChange={(e) => handleItemChange(index, 'productId', e.target.value)}
                        required
                      >
                        <option value="">Chọn sản phẩm</option>
                        {products.map(p => (
                          <option key={p.id} value={p.id}>{p.code} - {p.name}</option>
                        ))}
                      </select>
                    </td>
                    <td style={{ padding: '8px' }}>
                      <input
                        type="number"
                        min="1"
                        style={{ ...inputStyle, textAlign: 'center' }}
                        value={item.quantity}
                        onChange={(e) => handleItemChange(index, 'quantity', e.target.value)}
                        required
                      />
                    </td>
                    <td style={{ padding: '8px' }}>
                      <input
                        type="number"
                        min="0"
                        step="0.01"
                        style={{ ...inputStyle, textAlign: 'right' }}
                        value={item.unitPrice}
                        onChange={(e) => handleItemChange(index, 'unitPrice', e.target.value)}
                        required
                      />
                    </td>
                    <td style={{ padding: '8px' }}>
                      <input
                        type="number"
                        min="0"
                        max="100"
                        style={{ ...inputStyle, textAlign: 'center' }}
                        value={item.discountPercent}
                        onChange={(e) => handleItemChange(index, 'discountPercent', e.target.value)}
                      />
                    </td>
                    <td style={{ padding: '8px' }}>
                      <input
                        type="number"
                        min="0"
                        style={{ ...inputStyle, textAlign: 'center' }}
                        value={item.taxPercent}
                        onChange={(e) => handleItemChange(index, 'taxPercent', e.target.value)}
                      />
                    </td>
                    <td style={{ padding: '8px', textAlign: 'right', fontWeight: '600' }}>
                      {formatCurrency(calculateItemTotal(item))}
                    </td>
                    <td style={{ padding: '8px' }}>
                      <button
                        type="button"
                        style={{ ...buttonStyle, backgroundColor: '#fee2e2', color: '#991b1b', padding: '6px 12px' }}
                        onClick={() => handleRemoveItem(index)}
                      >
                        ×
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        {/* Tổng kết & Ghi chú */}
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' }}>
          <div style={cardStyle}>
            <h2 style={{ fontSize: '18px', fontWeight: '600', marginBottom: '20px', color: '#111827' }}>
              Ghi chú
            </h2>
            <textarea
              style={{ ...inputStyle, height: '120px', resize: 'vertical' }}
              value={formData.notes}
              onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
              placeholder="Ghi chú thêm..."
            />
          </div>
          <div style={cardStyle}>
            <h2 style={{ fontSize: '18px', fontWeight: '600', marginBottom: '20px', color: '#111827' }}>
              Tổng kết đơn hàng
            </h2>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '12px' }}>
              <span>Chiết khấu:</span>
              <input
                type="number"
                min="0"
                style={{ ...inputStyle, width: '150px', textAlign: 'right' }}
                value={formData.discountAmount}
                onChange={(e) => setFormData({ ...formData, discountAmount: e.target.value })}
              />
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '12px' }}>
              <span>Phí vận chuyển:</span>
              <input
                type="number"
                min="0"
                style={{ ...inputStyle, width: '150px', textAlign: 'right' }}
                value={formData.shippingCost}
                onChange={(e) => setFormData({ ...formData, shippingCost: e.target.value })}
              />
            </div>
            <div style={{ borderTop: '2px solid #e5e7eb', paddingTop: '12px', marginTop: '12px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '18px', fontWeight: '700' }}>
                <span>Tổng cộng:</span>
                <span style={{ color: '#4f46e5' }}>{formatCurrency(calculateTotal())}</span>
              </div>
            </div>
          </div>
        </div>

        {/* Nút hành động */}
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '24px' }}>
          <button
            type="button"
            style={{ ...buttonStyle, backgroundColor: '#f3f4f6', color: '#374151' }}
            onClick={() => navigate('/sales-orders')}
          >
            Hủy bỏ
          </button>
          <button
            type="submit"
            style={{ ...buttonStyle, backgroundColor: '#4f46e5', color: 'white' }}
            disabled={saving}
          >
            {saving ? 'Đang lưu...' : (isEdit ? 'Cập nhật' : 'Tạo đơn hàng')}
          </button>
        </div>
      </form>
    </div>
  );
}
