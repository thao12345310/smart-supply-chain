import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Form, Input, Select, DatePicker, InputNumber, Button, Table, message, Space, Alert, Card,
} from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { goodsReceiptApi, warehouseApi } from '../services/api';
import dayjs from 'dayjs';

export default function GoodsReceiptForm() {
  const navigate = useNavigate();
  const { poId, id } = useParams();

  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [warehouses, setWarehouses] = useState([]);
  const [items, setItems] = useState([]);

  useEffect(() => {
    if (!poId && id) {
      message.info('Chỉnh sửa phiếu nhập được thực hiện qua trang chi tiết');
      navigate(`/goods-receipts/${id}`);
      return;
    }
    if (poId) loadData();
  }, [poId, id]);

  const loadData = async () => {
    setLoading(true);
    try {
      const [warehousesRes, summaryRes] = await Promise.all([
        warehouseApi.getAll(),
        goodsReceiptApi.getReceivingSummary(poId),
      ]);
      setWarehouses(warehousesRes.data || []);

      const summary = summaryRes.data;
      form.setFieldsValue({
        warehouseId: summary.warehouseId,
        receiptDate: dayjs(),
      });

      setItems(
        (summary.items || []).map(item => ({
          ...item,
          receivedQuantity: 0,
          rejectedQuantity: 0,
          batchNumber: '',
          expiryDate: null,
          notes: '',
        })),
      );
    } catch (err) {
      message.error('Không thể tải thông tin đơn hàng: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleChangeItem = (index, field, value) => {
    setItems(prev => {
      const next = [...prev];
      next[index] = { ...next[index], [field]: value };
      if (field === 'receivedQuantity' || field === 'rejectedQuantity') {
        const received = next[index].receivedQuantity || 0;
        const rejected = next[index].rejectedQuantity || 0;
        next[index].acceptedQuantity = Math.max(0, received - rejected);
      }
      return next;
    });
  };

  const validateItems = () => {
    const activeItems = items.filter(i => (i.receivedQuantity || 0) > 0);

    if (activeItems.length === 0) {
      message.warning('Vui lòng nhập số lượng nhận cho ít nhất một sản phẩm');
      return false;
    }

    for (const item of activeItems) {
      if (!item.batchNumber?.trim()) {
        message.error(`Vui lòng nhập số lô cho sản phẩm "${item.productName}"`);
        return false;
      }
      if ((item.rejectedQuantity || 0) > item.receivedQuantity) {
        message.error(`Số lượng từ chối không thể lớn hơn số lượng nhận cho sản phẩm "${item.productName}"`);
        return false;
      }
    }

    return true;
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (!validateItems()) return;

      const payload = {
        purchaseOrderId: Number(poId),
        warehouseId: values.warehouseId,
        receiptDate: values.receiptDate ? values.receiptDate.format('YYYY-MM-DD') : null,
        deliveryNoteNumber: values.deliveryNoteNumber,
        invoiceNumber: values.invoiceNumber,
        notes: values.notes,
        items: items
          .filter(i => (i.receivedQuantity || 0) > 0)
          .map(i => ({
            purchaseOrderItemId: i.purchaseOrderItemId,
            productId: i.productId,
            receivedQuantity: i.receivedQuantity,
            rejectedQuantity: i.rejectedQuantity || 0,
            batchNumber: i.batchNumber,
            expiryDate: i.expiryDate ? i.expiryDate.format('YYYY-MM-DD') : null,
            notes: i.notes || null,
          })),
      };

      setSaving(true);
      const res = await goodsReceiptApi.create(payload);
      const newId = res.data?.id;
      message.success('Tạo phiếu nhập kho thành công');
      navigate(newId ? `/goods-receipts/${newId}` : '/goods-receipts');
    } catch (err) {
      if (err?.errorFields) return;
      message.error(err.message || 'Lưu phiếu nhập thất bại');
    } finally {
      setSaving(false);
    }
  };

  const columns = [
    { title: '#', width: 50, render: (_, __, index) => index + 1 },
    { title: 'Mã SP', dataIndex: 'productCode', width: 100 },
    { title: 'Tên sản phẩm', dataIndex: 'productName', ellipsis: true },
    { title: 'ĐVT', dataIndex: 'unit', width: 70 },
    {
      title: 'SL đặt',
      dataIndex: 'orderedQuantity',
      width: 80,
      align: 'center',
    },
    {
      title: 'Đã nhận',
      dataIndex: 'previouslyReceivedQuantity',
      width: 80,
      align: 'center',
      render: val => <span style={{ color: val > 0 ? '#52c41a' : '#999' }}>{val || 0}</span>,
    },
    {
      title: 'Còn lại',
      dataIndex: 'remainingQuantity',
      width: 80,
      align: 'center',
      render: val => <span style={{ color: val > 0 ? '#ff4d4f' : '#52c41a' }}>{val || 0}</span>,
    },
    {
      title: 'SL nhận',
      dataIndex: 'receivedQuantity',
      width: 100,
      render: (v, record, i) => (
        <InputNumber
          min={0}
          value={v}
          onChange={val => handleChangeItem(i, 'receivedQuantity', val)}
          style={{ width: '100%' }}
          disabled={record.remainingQuantity <= 0}
        />
      ),
    },
    {
      title: 'SL lỗi',
      dataIndex: 'rejectedQuantity',
      width: 80,
      render: (v, record, i) => (
        <InputNumber
          min={0}
          max={record.receivedQuantity || 0}
          value={v}
          onChange={val => handleChangeItem(i, 'rejectedQuantity', val)}
          style={{ width: '100%' }}
          disabled={!record.receivedQuantity}
        />
      ),
    },
    {
      title: 'Số lô *',
      dataIndex: 'batchNumber',
      width: 130,
      render: (v, record, i) => (
        <Input
          value={v}
          onChange={e => handleChangeItem(i, 'batchNumber', e.target.value)}
          placeholder="Nhập số lô"
          disabled={!record.receivedQuantity}
          status={record.receivedQuantity > 0 && !v ? 'error' : ''}
        />
      ),
    },
    {
      title: 'Hạn sử dụng',
      dataIndex: 'expiryDate',
      width: 150,
      render: (v, record, i) => (
        <DatePicker
          value={v}
          onChange={val => handleChangeItem(i, 'expiryDate', val)}
          format="DD/MM/YYYY"
          style={{ width: '100%' }}
          disabled={!record.receivedQuantity}
          placeholder="Chọn ngày"
        />
      ),
    },
  ];

  const totalReceiving = items.reduce((sum, i) => sum + (i.receivedQuantity || 0), 0);
  const totalRejected = items.reduce((sum, i) => sum + (i.rejectedQuantity || 0), 0);
  const totalAccepted = totalReceiving - totalRejected;

  if (loading) return <div style={{ padding: 24 }}>Đang tải...</div>;

  return (
    <div style={{ padding: 24, maxWidth: 1400, margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <Space>
          <Button icon={<ArrowLeftOutlined />} onClick={() => navigate(`/purchase-orders/${poId}`)}>
            Quay lại
          </Button>
          <h1 style={{ fontSize: 22, fontWeight: 700, margin: 0 }}>Tạo phiếu nhập kho</h1>
        </Space>
        <Button onClick={() => navigate(`/purchase-orders/${poId}`)}>Hủy</Button>
      </div>

      <div style={{ background: 'white', borderRadius: 8, padding: 24, marginBottom: 24, boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
        <h2 style={{ fontSize: 16, fontWeight: 600, marginBottom: 20 }}>Thông tin phiếu nhập</h2>
        <Form form={form} layout="vertical">
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 16 }}>
            <Form.Item
              label="Kho nhập"
              name="warehouseId"
              rules={[{ required: true, message: 'Vui lòng chọn kho' }]}
            >
              <Select placeholder="Chọn kho">
                {warehouses.map(w => (
                  <Select.Option key={w.id} value={w.id}>{w.name}</Select.Option>
                ))}
              </Select>
            </Form.Item>

            <Form.Item label="Ngày nhận hàng" name="receiptDate" initialValue={dayjs()}>
              <DatePicker format="DD/MM/YYYY" style={{ width: '100%' }} placeholder="Chọn ngày nhận" />
            </Form.Item>

            <Form.Item label="Số phiếu giao hàng" name="deliveryNoteNumber">
              <Input placeholder="Nhập số phiếu giao hàng" />
            </Form.Item>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
            <Form.Item label="Số hóa đơn" name="invoiceNumber">
              <Input placeholder="Nhập số hóa đơn" />
            </Form.Item>

            <Form.Item label="Ghi chú" name="notes">
              <Input.TextArea rows={2} placeholder="Ghi chú" />
            </Form.Item>
          </div>
        </Form>
      </div>

      <div style={{ background: 'white', borderRadius: 8, padding: 24, marginBottom: 24, boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
        <h2 style={{ fontSize: 16, fontWeight: 600, marginBottom: 16 }}>Danh sách sản phẩm</h2>

        <Alert
          message="Hướng dẫn nhập hàng"
          description="Nhập số lượng nhận và số lô (bắt buộc) cho từng sản phẩm. Nếu có sản phẩm lỗi, nhập vào cột 'SL lỗi'. Chỉ số lượng hàng tốt (SL nhận - SL lỗi) sẽ được nhập kho."
          type="info"
          showIcon
          style={{ marginBottom: 16 }}
        />

        <Table
          dataSource={items}
          columns={columns}
          pagination={false}
          rowKey="purchaseOrderItemId"
          scroll={{ x: 1100 }}
          size="small"
        />

        <Card style={{ marginTop: 16, background: '#f5f5f5' }}>
          <Space size="large">
            <span><strong>Tổng SL nhận:</strong> {totalReceiving}</span>
            <span>
              <strong>Tổng SL lỗi:</strong>{' '}
              <span style={{ color: '#ff4d4f' }}>{totalRejected}</span>
            </span>
            <span>
              <strong>Tổng SL nhập kho:</strong>{' '}
              <span style={{ color: '#52c41a' }}>{totalAccepted}</span>
            </span>
          </Space>
        </Card>
      </div>

      <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 12 }}>
        <Button onClick={() => navigate(`/purchase-orders/${poId}`)}>Hủy</Button>
        <Button type="primary" onClick={handleSubmit} loading={saving}>
          Lưu phiếu nhập
        </Button>
      </div>
    </div>
  );
}
