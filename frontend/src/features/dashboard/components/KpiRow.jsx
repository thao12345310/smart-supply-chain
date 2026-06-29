import React from 'react';
import { Card, Row, Col, Statistic } from 'antd';

/**
 * A responsive row of KPI statistic cards.
 * items: [{ key, title, value, prefix?, suffix? }]
 */
export default function KpiRow({ items = [] }) {
  return (
    <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
      {items.map((it) => (
        <Col xs={12} sm={12} md={6} key={it.key}>
          <Card>
            <Statistic title={it.title} value={it.value} prefix={it.prefix} suffix={it.suffix} />
          </Card>
        </Col>
      ))}
    </Row>
  );
}
