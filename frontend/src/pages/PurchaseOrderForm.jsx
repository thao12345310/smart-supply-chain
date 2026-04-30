import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Form, Input, Select, DatePicker, InputNumber, Button, Table, message, Space,
} from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { purchaseOrderApi, supplierApi, warehouseApi, productApi } from '../services/api';
import dayjs from 'dayjs';

export default function PurchaseOrderForm() {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEdit = Boolean(id);

  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [suppliers, setSuppliers] = useState([]);
  const [warehouses, setWarehouses] = useState([]);
  const [products, setProducts] = useState([]);
  const [items, setItems] = useState([]);

  useEffect(() => {
    loadMasterData();
    if (isEdit) loadOrder();
  }, [id]);

  const loadMasterData = async () => {
    try {
      const [suppliersRes, warehousesRes, productsRes] = await Promise.all([
        supplierApi.getAll(),
        warehouseApi.getAll(),
        productApi.getAll(),
      ]);
      setSuppliers(suppliersRes.data || []);
      setWarehouses(warehousesRes.data || []);
      setProducts(productsRes.data || []);
    } catch (err) {
      message.error('Không thể tải dữ liệu danh mục');
    }
  };

  const loadOrder = async () => {
    setLoading(true);
    try {
      const res = await purchaseOrderApi.getById(id);
      const order = res.data;

      if (order.status !== 'ORDER_OPEN') {
        message.warning('Không thể sửa đơn hàng đã được xử lý');
        navigate(`/purchase-orders/${id}`);
        return;
      }

      form.setFieldsValue({
        supplierId: order.supplier?.id || order.supplierId,
        warehouseId: order.warehouse?.id || order.warehouseId,
        orderName: order.orderName,
        shippingCost: order.shippingCost || 0,
        taxType: order.taxType || '8%',
        deliveryDate: order.deliveryDate ? dayjs(order.deliveryDate) : null,
        notes: order.notes,
      });

      if (order.items?.length > 0) {
        setItems(order.items.map(item => ({
          productId: item.product?.id || item.productId,
          productCode: item.product?.code || item.productCode || '',
          productName: item.product?.name || item.productName || '',
          unit: item.unit || '',
          quantity: item.quantity || 1,
          unitPrice: item.unitPrice || 0,
          costBeforeTax: item.costBeforeTax || (item.unitPrice || 0) * (item.quantity || 0),
        })));
      }
    } catch (err) {
      message.error('Không thể tải đơn hàng');
    } finally {
      setLoading(false);
    }
  };

  const handleAddProduct = (productId) => {
    if (!productId) return;
    if (items.some(i => i.productId === productId)) {
      message.warning('Sản phẩm đã được thêm vào danh sách');
      return;
    }
    const product = products.find(p => p.id === productId);
    if (product) {
      setItems(prev => [...prev, {
        productId: product.id,
        productCode: product.code || '',
        productName: product.name,
        unit: '',
        quantity: 1,
        unitPrice: product.price || 0,
        costBeforeTax: product.price || 0,
      }]);
    }
  };

  const handleChangeItem = (index, field, value) => {
    setItems(prev => {
      const next = [...prev];
      next[index] = { ...next[index], [field]: value };
      if (field === 'quantity' || field === 'unitPrice') {
        next[index].costBeforeTax = (next[index].unitPrice || 0) * (next[index].quantity || 0);
      }
      return next;
    });
  };

  const handleRemoveItem = (index) => {
    setItems(prev => prev.filter((_, i) => i !== index));
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();

      if (items.length === 0) {
        message.warning('Vui lòng thêm ít nhất một sản phẩm');
        return;
      }

      if (!items.some(i => (i.quantity || 0) > 0)) {
        message.warning('Số lượng sản phẩm phải lớn hơn 0');
        return;
      }

      const payload = {
        supplierId: values.supplierId,
        warehouseId: values.warehouseId,
        orderName: values.orderName,
        shippingCost: values.shippingCost || 0,
        taxType: values.taxType || '8%',
        deliveryDate: values.deliveryDate ? values.deliveryDate.format('YYYY-MM-DD HH:mm') : null,
        notes: values.notes,
        items: items.map(i => ({
          productId: i.productId,
          quantity: i.quantity,
          unitPrice: i.unitPrice,
          unit: i.unit,
          costBeforeTax: i.costBeforeTax || (i.unitPrice || 0) * (i.quantity || 0),
        })),
      };

      setSaving(true);
      if (isEdit) {
        await purchaseOrderApi.update(id, payload);
        message.success('Cập nhật đơn hàng thành công');
        navigate(`/purchase-orders/${id}`);
      } else {
        const res = await purchaseOrderApi.create(payload);
        const newId = res.data?.id;
        message.success('Tạo đơn hàng thành công');
        navigate(newId ? `/purchase-orders/${newId}` : '/purchase-orders');
      }
    } catch (err) {
      if (err?.errorFields) return;
      message.error(err.message || 'Lưu đơn hàng thất bại');
    } finally {
      setSaving(false);
    }
  };

  const total = items.reduce(
    (sum, i) => sum + (i.costBeforeTax || (i.unitPrice || 0) * (i.quantity || 0)),
    0,
  );

  const columns = [
    { title: '#', width: 50, render: (_, __, index) => index + 1 },
    { title: 'Mã SP', dataIndex: 'productCode', width: 120 },
    { title: 'Tên sản phẩm', dataIndex: 'productName' },
    {
      title: 'Đơn vị',
      dataIndex: 'unit',
      width: 110,
      render: (v, _, i) => (
        <Input
          value={v}
          onChange={e => handleChangeItem(i, 'unit', e.target.value)}
          placeholder="Đơn vị"
        />
      ),
    },
    {
      title: 'Số lượng',
      dataIndex: 'quantity',
      width: 120,
      render: (v, _, i) => (
        <InputNumber
          min={1}
          value={v}
          onChange={val => handleChangeItem(i, 'quantity', val)}
          style={{ width: '100%' }}
        />
      ),
    },
    {
      title: 'Đơn giá (₫)',
      dataIndex: 'unitPrice',
      width: 150,
      render: (v, _, i) => (
        <InputNumber
          min={0}
          value={v}
          onChange={val => handleChangeItem(i, 'unitPrice', val)}
          formatter={value => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
          parser={value => value.replace(/\$\s?|(,*)/g, '')}
          style={{ width: '100%' }}
        />
      ),
    },
    {
      title: 'Thành tiền',
      width: 150,
      render: (_, record) => (
        <span style={{ fontWeight: 600 }}>
          {(record.costBeforeTax || (record.unitPrice || 0) * (record.quantity || 0)).toLocaleString('vi-VN')} ₫
        </span>
      ),
    },
    {
      title: '',
      width: 70,
      render: (_, __, i) => (
        <Button danger size="small" onClick={() => handleRemoveItem(i)}>Xóa</Button>
      ),
    },
  ];

  if (loading) return <div style={{ padding: 24 }}>Đang tải...</div>;

  return (
    <div style={{ padding: 24, maxWidth: 1200, margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <Space>
          <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/purchase-orders')}>
            Quay lại
          </Button>
          <h1 style={{ fontSize: 22, fontWeight: 700, margin: 0 }}>
            {isEdit ? 'Chỉnh sửa đơn mua hàng' : 'Tạo đơn mua hàng mới'}
          </h1>
        </Space>
        <Button onClick={() => navigate('/purchase-orders')}>Hủy</Button>
      </div>

      <div style={{ background: 'white', borderRadius: 8, padding: 24, marginBottom: 24, boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
        <h2 style={{ fontSize: 16, fontWeight: 600, marginBottom: 20 }}>Thông tin đơn hàng</h2>
        <Form form={form} layout="vertical">
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
            <Form.Item
              label="Nhà cung cấp"
              name="supplierId"
              rules={[{ required: true, message: 'Vui lòng chọn nhà cung cấp' }]}
            >
              <Select placeholder="Chọn nhà cung cấp" showSearch optionFilterProp="children">
                {suppliers.map(s => (
                  <Select.Option key={s.id} value={s.id}>{s.name}</Select.Option>
                ))}
              </Select>
            </Form.Item>

            <Form.Item
              label="Kho hàng"
              name="warehouseId"
              rules={[{ required: true, message: 'Vui lòng chọn kho hàng' }]}
            >
              <Select placeholder="Chọn kho hàng">
                {warehouses.map(w => (
                  <Select.Option key={w.id} value={w.id}>{w.name}</Select.Option>
                ))}
              </Select>
            </Form.Item>

            <Form.Item label="Tên đơn hàng" name="orderName">
              <Input placeholder="Nhập tên đơn hàng" />
            </Form.Item>

            <Form.Item label="Ngày giao hàng dự kiến" name="deliveryDate">
              <DatePicker
                showTime
                format="DD/MM/YYYY HH:mm"
                style={{ width: '100%' }}
                placeholder="Chọn ngày giao"
              />
            </Form.Item>

            <Form.Item label="Chi phí vận chuyển (₫)" name="shippingCost" initialValue={0}>
              <InputNumber
                min={0}
                style={{ width: '100%' }}
                formatter={v => `${v}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                parser={v => v.replace(/\$\s?|(,*)/g, '')}
                placeholder="0"
              />
            </Form.Item>

            <Form.Item label="Loại thuế" name="taxType" initialValue="8%">
              <Select>
                <Select.Option value="8%">8% VAT</Select.Option>
                <Select.Option value="10%">10% VAT</Select.Option>
                <Select.Option value="0%">Không thuế</Select.Option>
              </Select>
            </Form.Item>
          </div>

          <Form.Item label="Ghi chú" name="notes">
            <Input.TextArea rows={3} placeholder="Ghi chú thêm..." />
          </Form.Item>
        </Form>
      </div>

      <div style={{ background: 'white', borderRadius: 8, padding: 24, marginBottom: 24, boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
        <h2 style={{ fontSize: 16, fontWeight: 600, marginBottom: 16 }}>Sản phẩm cần mua</h2>

        <div style={{ marginBottom: 16 }}>
          <Select
            showSearch
            placeholder="Tìm và chọn sản phẩm để thêm..."
            style={{ width: '100%' }}
            onChange={handleAddProduct}
            value={null}
            filterOption={(input, option) =>
              option.children.toLowerCase().includes(input.toLowerCase())
            }
          >
            {products.map(p => (
              <Select.Option key={p.id} value={p.id}>{p.code} - {p.name}</Select.Option>
            ))}
          </Select>
        </div>

        <Table
          dataSource={items}
          columns={columns}
          pagination={false}
          rowKey="productId"
          size="small"
          scroll={{ x: 850 }}
          locale={{ emptyText: 'Chưa có sản phẩm. Tìm và chọn sản phẩm ở trên để thêm.' }}
        />

        {items.length > 0 && (
          <div style={{ textAlign: 'right', marginTop: 16, fontSize: 16, fontWeight: 600 }}>
            Tổng tiền (trước thuế): {total.toLocaleString('vi-VN')} ₫
          </div>
        )}
      </div>

      <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 12 }}>
        <Button onClick={() => navigate('/purchase-orders')}>Hủy</Button>
        <Button type="primary" onClick={handleSubmit} loading={saving}>
          {isEdit ? 'Cập nhật' : 'Tạo đơn hàng'}
        </Button>
      </div>
    </div>
  );
}
