import React from 'react';
import { Card, Empty, Progress } from 'antd';
import { palette } from '@/theme';

const fmt = (n) => Number(n || 0).toLocaleString('vi-VN');

/**
 * Compact ranked list (rank + label + value with a proportional bar).
 * Turns the existing top-N arrays (top customers / suppliers / shippers) into a
 * denser, more readable block than a bare bar chart.
 *
 * data: [{ label, value }]
 */
export default function RankedList({ title, data = [], valueFormatter = fmt }) {
  const rows = (data || []).map((d) => ({ label: d.label, value: Number(d.value || 0) }));
  const max = rows.reduce((m, r) => Math.max(m, r.value), 0) || 1;

  return (
    <Card title={title} style={{ marginTop: 16 }}>
      {rows.length === 0 ? (
        <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="Chưa có dữ liệu" />
      ) : (
        rows.map((r, i) => (
          <div key={i} style={{ marginBottom: 12 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
              <span>
                <strong style={{ color: '#bfbfbf', marginRight: 8 }}>#{i + 1}</strong>
                {r.label}
              </span>
              <span style={{ fontWeight: 600 }}>{valueFormatter(r.value)}</span>
            </div>
            <Progress
              percent={Math.round((r.value / max) * 100)}
              showInfo={false}
              strokeColor={palette.primary}
              size="small"
            />
          </div>
        ))
      )}
    </Card>
  );
}
