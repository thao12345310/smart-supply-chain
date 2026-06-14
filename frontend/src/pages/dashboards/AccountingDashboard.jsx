import React, { useEffect, useState } from 'react';
import { Card, Row, Col, Statistic, message } from 'antd';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { dashboardApi } from '../../services/api';

const fmt = (n) => Number(n || 0).toLocaleString('vi-VN');

export default function AccountingDashboard() {
  const [d, setD] = useState(null);
  useEffect(() => {
    (async () => {
      try { const res = await dashboardApi.getAccounting(); setD(res.data); }
      catch (e) { message.error(e.message); }
    })();
  }, []);

  return (
    <div style={{ padding: 24 }}>
      <Row gutter={16}>
        <Col span={6}><Card><Statistic title="Phải thu (AR)" value={fmt(d?.totalReceivable)} /></Card></Col>
        <Col span={6}><Card><Statistic title="Phải trả (AP)" value={fmt(d?.totalPayable)} /></Card></Col>
        <Col span={6}><Card><Statistic title="Tổng thu" value={fmt(d?.cashIn)} /></Card></Col>
        <Col span={6}><Card><Statistic title="Tổng chi" value={fmt(d?.cashOut)} /></Card></Col>
      </Row>
      <Card title="Dòng tiền theo tháng" style={{ marginTop: 16 }}>
        <ResponsiveContainer width="100%" height={320}>
          <BarChart data={(d?.cashFlowByMonth || []).map((p) => ({ label: p.label, value: Number(p.value) }))}>
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
