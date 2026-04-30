import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Form, Input, DatePicker, Button, message, Space } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { deliveryPlanApi } from '../services/api';
import dayjs from 'dayjs';

export default function DeliveryPlanForm() {
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const [saving, setSaving] = useState(false);

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();

      const payload = {
        plannedDate: values.plannedDate ? values.plannedDate.format('YYYY-MM-DD') : null,
        description: values.description,
        notes: values.notes,
      };

      setSaving(true);
      const res = await deliveryPlanApi.create(payload);
      const newId = res.data?.id;
      message.success('Tạo đợt giao hàng thành công');
      navigate(newId ? `/delivery-plans/${newId}` : '/delivery-plans');
    } catch (err) {
      if (err?.errorFields) return;
      message.error(err.message || 'Tạo đợt giao hàng thất bại');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div style={{ padding: 24, maxWidth: 800, margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <Space>
          <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/delivery-plans')}>
            Quay lại
          </Button>
          <h1 style={{ fontSize: 22, fontWeight: 700, margin: 0 }}>Tạo đợt giao hàng mới</h1>
        </Space>
        <Button onClick={() => navigate('/delivery-plans')}>Hủy</Button>
      </div>

      <div style={{ background: 'white', borderRadius: 8, padding: 24, boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
        <Form form={form} layout="vertical">
          <Form.Item
            label="Ngày giao dự kiến"
            name="plannedDate"
            initialValue={dayjs()}
            rules={[{ required: true, message: 'Vui lòng chọn ngày giao dự kiến' }]}
          >
            <DatePicker
              format="DD/MM/YYYY"
              style={{ width: '100%' }}
              placeholder="Chọn ngày giao"
            />
          </Form.Item>

          <Form.Item label="Mô tả" name="description">
            <Input.TextArea rows={4} placeholder="Mô tả đợt giao hàng..." />
          </Form.Item>

          <Form.Item label="Ghi chú" name="notes">
            <Input placeholder="Ghi chú thêm..." />
          </Form.Item>
        </Form>
      </div>

      <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 12, marginTop: 24 }}>
        <Button onClick={() => navigate('/delivery-plans')}>Hủy</Button>
        <Button type="primary" onClick={handleSubmit} loading={saving}>
          Tạo đợt giao hàng
        </Button>
      </div>
    </div>
  );
}
