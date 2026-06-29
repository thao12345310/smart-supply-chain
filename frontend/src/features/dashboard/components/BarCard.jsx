import React from 'react';
import { Card, Empty } from 'antd';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
} from 'recharts';
import { palette } from '@/theme';

// Rút gọn số cho nhãn trục Y để không bị khung cắt mất chữ số (vd 120000000 -> "120 tr").
const compact = (v) => {
  const n = Number(v) || 0;
  const a = Math.abs(n);
  if (a >= 1e9) return `${(n / 1e9).toLocaleString('vi-VN', { maximumFractionDigits: 1 })} tỷ`;
  if (a >= 1e6) return `${(n / 1e6).toLocaleString('vi-VN', { maximumFractionDigits: 1 })} tr`;
  if (a >= 1e3) return `${(n / 1e3).toLocaleString('vi-VN', { maximumFractionDigits: 1 })} k`;
  return n.toLocaleString('vi-VN');
};
// Tooltip hiển thị số đầy đủ có phân tách hàng nghìn.
const full = (v) => (Number(v) || 0).toLocaleString('vi-VN');

/**
 * A titled card wrapping a single-series bar chart, with a graceful empty state.
 * data: [{ label, value }]
 */
export default function BarCard({ title, data = [], color = palette.primary, height = 300 }) {
  const rows = (data || []).map((p) => ({ label: p.label, value: Number(p.value) }));

  return (
    <Card title={title} style={{ marginTop: 16 }}>
      {rows.length === 0 ? (
        <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="Chưa có dữ liệu" />
      ) : (
        <ResponsiveContainer width="100%" height={height}>
          <BarChart data={rows} margin={{ top: 8, right: 16, bottom: 0, left: 8 }}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="label" />
            <YAxis width={72} tickFormatter={compact} />
            <Tooltip formatter={full} />
            <Bar dataKey="value" fill={color} />
          </BarChart>
        </ResponsiveContainer>
      )}
    </Card>
  );
}
