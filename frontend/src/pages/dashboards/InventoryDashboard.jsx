import React, { useEffect, useState } from 'react';
import { Card, Row, Col, Statistic, message } from 'antd';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { dashboardApi } from '../../services/api';

const fmt = (n) => Number(n || 0).toLocaleString('vi-VN');

export default function InventoryDashboard() {
  const [d, setD] = useState(null);
  useEffect(() => {
    (async () => {
      try { const res = await dashboardApi.getInventory(); setD(res.data); }
      catch (e) { message.error(e.message); }
    })();
  }, []);

  return (
    <div style={{ padding: 24 }}>
      <Row gutter={16}>
        <Col span={6}><Card><Statistic title="Giá trị tồn" value={fmt(d?.totalStockValue)} /></Card></Col>
        <Col span={6}><Card><Statistic title="Sắp hết hàng" value={d?.lowStockCount ?? 0} /></Card></Col>
        <Col span={6}><Card><Statistic title="Lô sắp hết hạn" value={d?.expiringSoonCount ?? 0} /></Card></Col>
        <Col span={6}><Card><Statistic title="Lô đã hết hạn" value={d?.expiredCount ?? 0} /></Card></Col>
      </Row>
      <Card title="Tồn theo kho" style={{ marginTop: 16 }}>
        <ResponsiveContainer width="100%" height={320}>
          <BarChart data={(d?.stockByWarehouse || []).map((p) => ({ label: p.label, value: Number(p.value) }))}>
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
