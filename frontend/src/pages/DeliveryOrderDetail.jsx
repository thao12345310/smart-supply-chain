import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, Descriptions, Table, Tag, Button, Space, message } from 'antd';
import { PrinterOutlined, ArrowLeftOutlined } from '@ant-design/icons';
import { deliveryOrderApi } from '../services/api';
import { getStatusConfig } from '../services/deliveryStatus';

export default function DeliveryOrderDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    (async () => {
      setLoading(true);
      try { const res = await deliveryOrderApi.getById(id); setData(res.data); }
      catch (e) { message.error(e.message); }
      finally { setLoading(false); }
    })();
  }, [id]);

  const itemCols = [
    { title: 'Mã SP', dataIndex: 'productCode' },
    { title: 'Tên sản phẩm', dataIndex: 'productName' },
    { title: 'Số lượng', dataIndex: 'quantity', align: 'right' },
    { title: 'ĐVT', dataIndex: 'unit' },
  ];

  if (!data) return <Card loading={loading} />;
  const sc = getStatusConfig(data.status);

  return (
    <Card
      loading={loading}
      title={`Vận đơn ${data.code}`}
      extra={
        <Space>
          <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/delivery-orders')}>Quay lại</Button>
          <Button type="primary" icon={<PrinterOutlined />} onClick={() => window.open(`/delivery-orders/${id}/print`, '_blank')}>In vận đơn</Button>
        </Space>
      }
    >
      <Descriptions bordered column={2} size="small">
        <Descriptions.Item label="Mã vận đơn">{data.code}</Descriptions.Item>
        <Descriptions.Item label="Trạng thái"><Tag color={sc.color}>{sc.label}</Tag></Descriptions.Item>
        <Descriptions.Item label="Khách hàng">{data.customerName || '-'}</Descriptions.Item>
        <Descriptions.Item label="Phiếu xuất">{data.goodsIssueCode || '-'}</Descriptions.Item>
        <Descriptions.Item label="Người nhận">{data.recipientName || '-'}</Descriptions.Item>
        <Descriptions.Item label="SĐT">{data.recipientPhone || '-'}</Descriptions.Item>
        <Descriptions.Item label="Ngày giao dự kiến">{data.plannedDate || '-'}</Descriptions.Item>
        <Descriptions.Item label="Địa chỉ giao" span={2}>{data.destinationAddress || '-'}</Descriptions.Item>
      </Descriptions>
      <Table style={{ marginTop: 16 }} rowKey={(_, i) => i} dataSource={data.items || []} columns={itemCols} pagination={false} title={() => 'Danh sách mặt hàng'} />
    </Card>
  );
}
