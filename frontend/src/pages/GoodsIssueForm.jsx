import { useState, useEffect } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { goodsIssueApi, salesOrderApi } from '../services/api';

/**
 * Goods Issue Form - Tạo Phiếu xuất kho
 */
export default function GoodsIssueForm() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [searchParams] = useSearchParams();
  const salesOrderId = searchParams.get('salesOrderId');
  const isEdit = Boolean(id);

  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [salesOrder, setSalesOrder] = useState(null);

  const [formData, setFormData] = useState({
    salesOrderId: salesOrderId || '',
    issueDate: new Date().toISOString().split('T')[0],
    deliveryNoteNumber: '',
    shippingMethod: '',
    trackingNumber: '',
    carrierName: '',
    notes: '',
    items: [],
  });

  useEffect(() => {
    if (salesOrderId) {
      loadSalesOrder(salesOrderId);
    }
    if (isEdit) {
      loadGoodsIssue();
    }
  }, [salesOrderId, id]);

  const loadSalesOrder = async (soId) => {
    try {
      setLoading(true);
      const response = await salesOrderApi.getIssueSummary(soId);
      const so = response.data;
      setSalesOrder(so);

      // Khởi tạo danh sách sản phẩm còn lại
      const items = (so.items || [])
        .filter(item => (item.remainingQuantity || 0) > 0)
        .map(item => ({
          salesOrderItemId: item.id,
          productId: item.productId,
          productCode: item.productCode,
          productName: item.productName,
          orderedQuantity: item.quantity,
          previouslyDelivered: item.deliveredQuantity || 0,
          remainingQuantity: item.remainingQuantity || 0,
          issuedQuantity: item.remainingQuantity || 0, // Mặc định xuất hết số còn lại
          batchNumber: '',
          notes: '',
        }));

      setFormData(prev => ({
        ...prev,
        salesOrderId: soId,
        warehouseId: so.warehouseId,
        deliveryAddressId: so.deliveryAddressId,
        items,
      }));
    } catch (err) {
      setError('Không thể tải đơn bán hàng: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const loadGoodsIssue = async () => {
    try {
      setLoading(true);
      const response = await goodsIssueApi.getById(id);
      const gi = response.data;

      if (gi.salesOrderId) {
        const soRes = await salesOrderApi.getById(gi.salesOrderId);
        setSalesOrder(soRes.data);
      }

      setFormData({
        salesOrderId: gi.salesOrderId,
        issueDate: gi.issueDate,
        deliveryNoteNumber: gi.deliveryNoteNumber || '',
        shippingMethod: gi.shippingMethod || '',
        trackingNumber: gi.trackingNumber || '',
        carrierName: gi.carrierName || '',
        notes: gi.notes || '',
        items: gi.items || [],
      });
    } catch (err) {
      setError('Không thể tải phiếu xuất: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleItemChange = (index, field, value) => {
    const newItems = [...formData.items];
    newItems[index] = { ...newItems[index], [field]: value };

    // Kiểm tra không vượt quá số lượng còn lại
    if (field === 'issuedQuantity') {
      const maxQty = newItems[index].remainingQuantity;
      if (Number(value) > maxQty) {
        newItems[index].issuedQuantity = maxQty;
      }
    }

    setFormData({ ...formData, items: newItems });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const validItems = formData.items.filter(item => Number(item.issuedQuantity) > 0);
    if (validItems.length === 0) {
      setError('Vui lòng nhập số lượng cho ít nhất một sản phẩm');
      return;
    }

    try {
      setSaving(true);
      setError(null);

      const payload = {
        ...formData,
        items: validItems.map(item => ({
          orderedQuantity: Number(item.orderedQuantity),
          salesOrderItemId: item.salesOrderItemId,
          productId: item.productId,
          issuedQuantity: Number(item.issuedQuantity),
          batchNumber: item.batchNumber,
          notes: item.notes,
        })),
      };

      if (isEdit) {
        await goodsIssueApi.update(id, payload);
        navigate(`/goods-issues/${id}`);
      } else {
        const response = await goodsIssueApi.create(payload);
        navigate(`/goods-issues/${response.data.id}`);
      }
    } catch (err) {
      setError(err.message || 'Không thể lưu phiếu xuất');
    } finally {
      setSaving(false);
    }
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
  };

  if (loading) return <div style={containerStyle}>Đang tải...</div>;

  return (
    <div style={containerStyle}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <h1 style={{ fontSize: '28px', fontWeight: '700', color: '#111827', margin: 0 }}>
          {isEdit ? 'Sửa phiếu xuất kho' : 'Tạo phiếu xuất kho'}
        </h1>
        <button
          style={{ ...buttonStyle, backgroundColor: '#f3f4f6', color: '#374151' }}
          onClick={() => navigate(-1)}
        >
          Hủy bỏ
        </button>
      </div>

      {error && (
        <div style={{ padding: '16px', backgroundColor: '#fee2e2', color: '#991b1b', borderRadius: '8px', marginBottom: '16px' }}>
          {error}
        </div>
      )}

      {/* Thông tin đơn bán hàng */}
      {salesOrder && (
        <div style={{ ...cardStyle, background: 'linear-gradient(135deg, #dbeafe 0%, #e0e7ff 100%)' }}>
          <h3 style={{ margin: '0 0 12px 0', fontSize: '16px', fontWeight: '600', color: '#1e40af' }}>
            Đơn bán hàng: {salesOrder.code}
          </h3>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '16px', fontSize: '14px' }}>
            <div><strong>Khách hàng:</strong> {salesOrder.customerName}</div>
            <div><strong>Kho:</strong> {salesOrder.warehouseName}</div>
            <div><strong>Địa chỉ giao:</strong> {salesOrder.deliveryAddressText}</div>
          </div>
        </div>
      )}

      <form onSubmit={handleSubmit}>
        {/* Thông tin vận chuyển */}
        <div style={cardStyle}>
          <h2 style={{ fontSize: '18px', fontWeight: '600', marginBottom: '20px', color: '#111827' }}>
            Thông tin vận chuyển
          </h2>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '20px' }}>
            <div>
              <label style={labelStyle}>Ngày xuất *</label>
              <input
                type="date"
                style={inputStyle}
                value={formData.issueDate}
                onChange={(e) => setFormData({ ...formData, issueDate: e.target.value })}
                required
              />
            </div>
            <div>
              <label style={labelStyle}>Số phiếu giao hàng</label>
              <input
                type="text"
                style={inputStyle}
                value={formData.deliveryNoteNumber}
                onChange={(e) => setFormData({ ...formData, deliveryNoteNumber: e.target.value })}
              />
            </div>
            <div>
              <label style={labelStyle}>Phương thức vận chuyển</label>
              <input
                type="text"
                style={inputStyle}
                value={formData.shippingMethod}
                onChange={(e) => setFormData({ ...formData, shippingMethod: e.target.value })}
                placeholder="VD: Nhanh, Tiêu chuẩn"
              />
            </div>
            <div>
              <label style={labelStyle}>Đơn vị vận chuyển</label>
              <input
                type="text"
                style={inputStyle}
                value={formData.carrierName}
                onChange={(e) => setFormData({ ...formData, carrierName: e.target.value })}
              />
            </div>
            <div>
              <label style={labelStyle}>Mã vận đơn</label>
              <input
                type="text"
                style={inputStyle}
                value={formData.trackingNumber}
                onChange={(e) => setFormData({ ...formData, trackingNumber: e.target.value })}
              />
            </div>
          </div>
        </div>

        {/* Sản phẩm xuất */}
        <div style={cardStyle}>
          <h2 style={{ fontSize: '18px', fontWeight: '600', marginBottom: '20px', color: '#111827' }}>
            Sản phẩm xuất kho
          </h2>

          {formData.items.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '40px', color: '#6b7280' }}>
              Không còn sản phẩm cần giao. Tất cả đã được giao hết.
            </div>
          ) : (
            <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
              <thead>
                <tr>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '2px solid #e5e7eb', fontWeight: '600' }}>Sản phẩm</th>
                  <th style={{ padding: '12px', textAlign: 'center', borderBottom: '2px solid #e5e7eb', fontWeight: '600' }}>Đặt hàng</th>
                  <th style={{ padding: '12px', textAlign: 'center', borderBottom: '2px solid #e5e7eb', fontWeight: '600' }}>Đã giao</th>
                  <th style={{ padding: '12px', textAlign: 'center', borderBottom: '2px solid #e5e7eb', fontWeight: '600' }}>Còn lại</th>
                  <th style={{ padding: '12px', textAlign: 'center', borderBottom: '2px solid #e5e7eb', fontWeight: '600', width: '120px' }}>SL xuất *</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '2px solid #e5e7eb', fontWeight: '600', width: '150px' }}>Số lô</th>
                </tr>
              </thead>
              <tbody>
                {formData.items.map((item, index) => (
                  <tr key={index}>
                    <td style={{ padding: '12px', borderBottom: '1px solid #e5e7eb' }}>
                      <div style={{ fontWeight: '600' }}>{item.productName}</div>
                      <div style={{ fontSize: '12px', color: '#6b7280' }}>{item.productCode}</div>
                    </td>
                    <td style={{ padding: '12px', borderBottom: '1px solid #e5e7eb', textAlign: 'center' }}>
                      {item.orderedQuantity}
                    </td>
                    <td style={{ padding: '12px', borderBottom: '1px solid #e5e7eb', textAlign: 'center' }}>
                      <span style={{ color: '#10b981', fontWeight: '600' }}>{item.previouslyDelivered}</span>
                    </td>
                    <td style={{ padding: '12px', borderBottom: '1px solid #e5e7eb', textAlign: 'center' }}>
                      <span style={{ color: '#f59e0b', fontWeight: '600' }}>{item.remainingQuantity}</span>
                    </td>
                    <td style={{ padding: '12px', borderBottom: '1px solid #e5e7eb' }}>
                      <input
                        type="number"
                        min="0"
                        max={item.remainingQuantity}
                        style={{ ...inputStyle, textAlign: 'center' }}
                        value={item.issuedQuantity}
                        onChange={(e) => handleItemChange(index, 'issuedQuantity', e.target.value)}
                      />
                    </td>
                    <td style={{ padding: '12px', borderBottom: '1px solid #e5e7eb' }}>
                      <input
                        type="text"
                        style={inputStyle}
                        value={item.batchNumber}
                        onChange={(e) => handleItemChange(index, 'batchNumber', e.target.value)}
                        placeholder="Tùy chọn"
                      />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        {/* Ghi chú */}
        <div style={cardStyle}>
          <h2 style={{ fontSize: '18px', fontWeight: '600', marginBottom: '12px', color: '#111827' }}>Ghi chú</h2>
          <textarea
            style={{ ...inputStyle, height: '100px', resize: 'vertical' }}
            value={formData.notes}
            onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
            placeholder="Ghi chú thêm..."
          />
        </div>

        {/* Nút hành động */}
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
          <button
            type="button"
            style={{ ...buttonStyle, backgroundColor: '#f3f4f6', color: '#374151' }}
            onClick={() => navigate(-1)}
          >
            Hủy bỏ
          </button>
          <button
            type="submit"
            style={{ ...buttonStyle, backgroundColor: '#4f46e5', color: 'white' }}
            disabled={saving}
          >
            {saving ? 'Đang lưu...' : (isEdit ? 'Cập nhật' : 'Tạo phiếu xuất')}
          </button>
        </div>
      </form>
    </div>
  );
}
