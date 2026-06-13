import React, { useEffect, useState } from 'react';
import { Card, Table, Input, Tag, Button, message } from 'antd';
import { Link } from 'react-router-dom';
import { deliveryOrderApi } from '../services/api';
import { getStatusConfig } from '../services/deliveryStatus';

export default function DeliveryOrderList() {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [q, setQ] = useState('');

  const load = async () => {
    setLoading(true);
    try { const res = await deliveryOrderApi.getAll(); setData(res.data || []); }
    catch (e) { message.error(e.message); }
    finally { setLoading(false); }
  };
  useEffect(() => { load(); }, []);

  const filtered = data.filter((d) =>
    !q || (d.code || '').toLowerCase().includes(q.toLowerCase()) ||
    (d.destinationAddress || '').toLowerCase().includes(q.toLowerCase()));

  const columns = [
    { title: 'Mã vận đơn', dataIndex: 'code', render: (t, r) => <Link to={`/delivery-orders/${r.id}`}>{t}</Link> },
    { title: 'Địa chỉ giao', dataIndex: 'destinationAddress' },
    { title: 'Trạng thái', dataIndex: 'status', render: (s) => { const c = getStatusConfig(s); return <Tag color={c.color}>{c.label}</Tag>; } },
    { title: '', key: 'action', render: (_, r) => <Link to={`/delivery-orders/${r.id}`}><Button size="small">Chi tiết</Button></Link> },
  ];

  return (
    <Card title="Vận đơn" extra={<Input.Search placeholder="Tìm mã / địa chỉ" allowClear style={{ width: 280 }} onChange={(e) => setQ(e.target.value)} />}>
      <Table rowKey="id" loading={loading} dataSource={filtered} columns={columns} />
    </Card>
  );
}
