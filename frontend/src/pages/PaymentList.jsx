import React, { useEffect, useState } from 'react';
import { Card, Table, Button, Modal, Form, Select, InputNumber, DatePicker, Input, Tag, message } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import { paymentApi, salesInvoiceApi } from '../services/api';

const TYPE_TAG = {
  RECEIPT: { color: 'green', label: 'Phiếu thu' },
  DISBURSEMENT: { color: 'volcano', label: 'Phiếu chi' },
};

export default function PaymentList() {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [open, setOpen] = useState(false);
  const [invoices, setInvoices] = useState([]);
  const [form] = Form.useForm();

  const load = async () => {
    setLoading(true);
    try {
      const res = await paymentApi.getAll();
      setData(res.data || []);
    } catch (e) { message.error(e.message); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const openModal = async () => {
    form.resetFields();
    form.setFieldsValue({ type: 'RECEIPT', paymentDate: dayjs() });
    try {
      const res = await salesInvoiceApi.getUnpaid();
      setInvoices(res.data || []);
    } catch { setInvoices([]); }
    setOpen(true);
  };

  const submit = async () => {
    const v = await form.validateFields();
    try {
      await paymentApi.create({
        type: v.type,
        amount: v.amount,
        paymentDate: v.paymentDate.format('YYYY-MM-DD'),
        method: v.method,
        salesInvoiceId: v.type === 'RECEIPT' ? v.salesInvoiceId : null,
        note: v.note,
      });
      message.success('Đã tạo phiếu');
      setOpen(false);
      load();
    } catch (e) { message.error(e.message); }
  };

  const columns = [
    { title: 'Mã phiếu', dataIndex: 'code' },
    { title: 'Loại', dataIndex: 'type', render: (t) => <Tag color={TYPE_TAG[t]?.color}>{TYPE_TAG[t]?.label || t}</Tag> },
    { title: 'Số tiền', dataIndex: 'amount', align: 'right', render: (a) => Number(a).toLocaleString('vi-VN') },
    { title: 'Ngày', dataIndex: 'paymentDate' },
    { title: 'Phương thức', dataIndex: 'method' },
    { title: 'Ghi chú', dataIndex: 'note' },
  ];

  return (
    <Card
      title="Phiếu thu / chi"
      extra={<Button type="primary" icon={<PlusOutlined />} onClick={openModal}>Tạo phiếu</Button>}
    >
      <Table rowKey="id" loading={loading} dataSource={data} columns={columns} />
      <Modal title="Tạo phiếu thu/chi" open={open} onOk={submit} onCancel={() => setOpen(false)} destroyOnClose>
        <Form form={form} layout="vertical">
          <Form.Item name="type" label="Loại phiếu" rules={[{ required: true }]}>
            <Select options={[{ value: 'RECEIPT', label: 'Phiếu thu (khách trả)' }, { value: 'DISBURSEMENT', label: 'Phiếu chi (trả NCC)' }]} />
          </Form.Item>
          <Form.Item noStyle shouldUpdate={(p, c) => p.type !== c.type}>
            {({ getFieldValue }) => getFieldValue('type') === 'RECEIPT' && (
              <Form.Item name="salesInvoiceId" label="Hóa đơn cần thu">
                <Select
                  allowClear
                  options={invoices.map((inv) => ({ value: inv.id, label: `${inv.code} — còn nợ ${Number(inv.remainingAmount || 0).toLocaleString('vi-VN')}` }))}
                />
              </Form.Item>
            )}
          </Form.Item>
          <Form.Item name="amount" label="Số tiền" rules={[{ required: true }]}>
            <InputNumber style={{ width: '100%' }} min={1} formatter={(v) => `${v}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')} />
          </Form.Item>
          <Form.Item name="paymentDate" label="Ngày" rules={[{ required: true }]}>
            <DatePicker style={{ width: '100%' }} format="YYYY-MM-DD" />
          </Form.Item>
          <Form.Item name="method" label="Phương thức"><Input placeholder="Tiền mặt / Chuyển khoản" /></Form.Item>
          <Form.Item name="note" label="Ghi chú"><Input.TextArea rows={2} /></Form.Item>
        </Form>
      </Modal>
    </Card>
  );
}
