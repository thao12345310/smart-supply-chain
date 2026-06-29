import React from 'react';
import { Typography, Spin, Alert } from 'antd';

const { Title, Text } = Typography;

/**
 * Common wrapper for the per-cluster dashboards.
 * Provides a consistent page header plus unified loading / error states so every
 * cluster home looks and behaves the same.
 */
export default function DashboardShell({ title, subtitle, loading, error, children }) {
  return (
    <div style={{ padding: 24 }}>
      <div style={{ marginBottom: 16 }}>
        <Title level={3} style={{ marginBottom: 0 }}>{title}</Title>
        {subtitle && <Text type="secondary">{subtitle}</Text>}
      </div>
      {error && (
        <Alert type="error" showIcon message={error} style={{ marginBottom: 16 }} />
      )}
      <Spin spinning={!!loading}>
        {children}
      </Spin>
    </div>
  );
}
