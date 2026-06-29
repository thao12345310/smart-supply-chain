import React from 'react';
import { Card, Row, Col, Statistic } from 'antd';
import { RightOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { hasAnyRole } from '@/services/roleService';
import { palette } from '@/theme';

/**
 * "Việc cần xử lý" — a row of actionable counters that turn each cluster dashboard
 * into an operational cockpit. Each card shows a count and navigates to the page
 * where the user resolves it. Cards with count > 0 are highlighted; cards the
 * current role cannot open (per `roles`) are hidden so we never render a dead link.
 *
 * items: [{ key, label, count, to?, roles? }]
 */
export default function ActionCenter({ items = [] }) {
  const navigate = useNavigate();
  const visible = items.filter((it) => !it.roles || hasAnyRole(it.roles));
  if (visible.length === 0) return null;

  return (
    <Card title="Việc cần xử lý" style={{ marginBottom: 16 }}>
      <Row gutter={[16, 16]}>
        {visible.map((it) => {
          const active = Number(it.count) > 0;
          const clickable = !!it.to;
          return (
            <Col xs={12} sm={8} md={6} key={it.key}>
              <Card
                size="small"
                hoverable={clickable}
                onClick={clickable ? () => navigate(it.to) : undefined}
                style={{
                  borderColor: active ? palette.warning : palette.border,
                  background: active ? '#FBF4E6' : '#FAFAFA',
                  cursor: clickable ? 'pointer' : 'default',
                }}
              >
                <Statistic
                  title={
                    <span>
                      {it.label}
                      {clickable && <RightOutlined style={{ fontSize: 10, marginLeft: 6 }} />}
                    </span>
                  }
                  value={Number(it.count || 0)}
                  valueStyle={{ color: active ? palette.warning : '#999', fontSize: 24 }}
                />
              </Card>
            </Col>
          );
        })}
      </Row>
    </Card>
  );
}
