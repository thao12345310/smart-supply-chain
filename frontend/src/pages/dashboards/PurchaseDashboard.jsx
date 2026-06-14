import React, { useEffect, useState } from 'react';
import { Card, Row, Col, Statistic, message } from 'antd';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { dashboardApi } from '../../services/api';

const fmt = (n) => Number(n || 0).toLocaleString('vi-VN');

export default function PurchaseDashboard() {
  const [d, setD] = useState(null);
  useEffect(() => {
    (async () => {
      try { const res = await dashboardApi.getPurchase(); setD(res.data); }
      catch (e) { message.error(e.message); }
    })();
  }, []);

  return (
    <div style={{ padding: 24 }}>
      <Row gutter={16}>
        <Col span={6}><Card><Statistic title="Tổng PO" value={d?.totalPO ?? 0} /></Card></Col>
        <Col span={6}><Card><Statistic title="Chờ duyệt" value={d?.pendingApproval ?? 0} /></Card></Col>
        <Col span={6}><Card><Statistic title="Chờ nhập" value={d?.pendingReceipt ?? 0} /></Card></Col>
        <Col span={6}><Card><Statistic title="Giá trị mua tháng này" value={fmt(d?.purchaseValueThisMonth)} /></Card></Col>
      </Row>
      <Card title="PO theo trạng thái" style={{ marginTop: 16 }}>
        <ResponsiveContainer width="100%" height={320}>
          <BarChart data={(d?.poByStatus || []).map((p) => ({ label: p.label, value: Number(p.value) }))}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="label" />
            <YAxis />
            <Tooltip />
            <Bar dataKey="value" fill="#1677ff" />
          </BarChart>
        </ResponsiveContainer>
      </Card>
      <Card title="Top nhà cung cấp" style={{ marginTop: 16 }}>
        <ResponsiveContainer width="100%" height={320}>
          <BarChart data={(d?.topSuppliers || []).map((p) => ({ label: p.label, value: Number(p.value) }))}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="label" />
            <YAxis />
            <Tooltip />
            <Bar dataKey="value" fill="#1677ff" />
          </BarChart>
        </ResponsiveContainer>
      </Card>
    </div>
  );
}
