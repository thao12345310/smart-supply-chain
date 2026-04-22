import React, { useState, useEffect } from 'react';
import {
  Card, Table, Typography, Tag, Space, Input, Button, Tooltip, Badge, Row, Col, Statistic, Empty
} from 'antd';
import {
  HomeOutlined, EnvironmentOutlined, SearchOutlined, ReloadOutlined, AppstoreOutlined
} from '@ant-design/icons';
import { warehouseApi, inventoryApi } from '../services/api';

const { Title, Text } = Typography;

/**
 * WarehouseList - Quản lý kho hàng (Phân hệ 3 - Logistics)
 */
export default function WarehouseList() {
  const [warehouses, setWarehouses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const res = await warehouseApi.getAll();
      const list = Array.isArray(res.data) ? res.data : [];
      setWarehouses(list);
    } catch (e) {
      console.error('Failed to load warehouses', e);
    } finally {
      setLoading(false);
    }
  };

  const filtered = warehouses.filter(w => {
    if (!search) return true;
    const q = search.toLowerCase();
    return (w.name && w.name.toLowerCase().includes(q)) ||
           (w.code && w.code.toLowerCase().includes(q)) ||
           (w.address && w.address.toLowerCase().includes(q));
  });

  const columns = [
    {
      title: 'Mã kho', dataIndex: 'code', key: 'code', width: 120,
      render: (v) => <Tag color="processing">{v}</Tag>
    },
    {
      title: 'Tên kho', dataIndex: 'name', key: 'name', width: 200,
      render: (v) => <Text strong>{v}</Text>
    },
    { title: 'Địa chỉ', dataIndex: 'address', key: 'address' },
    {
      title: 'Trạng thái', dataIndex: 'active', key: 'active', width: 120,
      render: (v) => v !== false
        ? <Badge status="success" text="Hoạt động" />
        : <Badge status="default" text="Ngưng" />
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <Title level={3} style={{ margin: 0 }}>
          <HomeOutlined style={{ marginRight: 8, color: '#1890ff' }} />
          Quản lý kho hàng
        </Title>
        <Space>
          <Input
            placeholder="Tìm kho..."
            prefix={<SearchOutlined />}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            allowClear
            style={{ width: 250 }}
          />
          <Button icon={<ReloadOutlined />} onClick={loadData}>Tải lại</Button>
        </Space>
      </div>

      {/* Summary */}
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col xs={12} sm={8}>
          <Card bordered={false} size="small" style={{ borderRadius: 12 }}>
            <Statistic title="Tổng số kho" value={warehouses.length} prefix={<HomeOutlined />} />
          </Card>
        </Col>
        <Col xs={12} sm={8}>
          <Card bordered={false} size="small" style={{ borderRadius: 12 }}>
            <Statistic title="Đang hoạt động"
              value={warehouses.filter(w => w.active !== false).length}
              prefix={<AppstoreOutlined style={{ color: '#52c41a' }} />}
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
      </Row>

      <Card bordered={false} style={{ borderRadius: 12 }}>
        <Table
          dataSource={filtered}
          columns={columns}
          rowKey="id"
          loading={loading}
          pagination={{ pageSize: 20, showTotal: (t) => `Tổng ${t} kho` }}
        />
      </Card>
    </div>
  );
}
