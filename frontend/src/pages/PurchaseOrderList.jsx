import React, { useEffect, useState } from "react";
import { Table, Button, message, Tag, Space, Select, Card, Statistic, Row, Col } from "antd";
import { PlusOutlined, CheckCircleOutlined, ClockCircleOutlined, InboxOutlined } from "@ant-design/icons";
import { purchaseOrderApi } from "../services/api";
import PurchaseOrderForm from "./PurchaseOrderForm";
import dayjs from "dayjs";
import { useNavigate } from "react-router-dom";

// Status configuration
const STATUS_CONFIG = {
  ORDER_OPEN: { color: "blue", label: "Chờ duyệt", icon: <ClockCircleOutlined /> },
  ORDER_APPROVED: { color: "green", label: "Đã duyệt", icon: <CheckCircleOutlined /> },
  ORDER_PARTIALLY_RECEIVED: { color: "orange", label: "Nhận một phần", icon: <InboxOutlined /> },
  ORDER_COMPLETED: { color: "cyan", label: "Hoàn thành", icon: <CheckCircleOutlined /> },
  ORDER_CANCELLED: { color: "default", label: "Đã hủy", icon: null },
};

export default function PurchaseOrderList() {
  const [orders, setOrders] = useState([]);
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [editing, setEditing] = useState(null);
  const [open, setOpen] = useState(false);
  const [statusFilter, setStatusFilter] = useState(null);
  const [stats, setStats] = useState({ 
    total: 0, 
    pending: 0, 
    approved: 0, 
    completed: 0 
  });

  const fetchOrders = async () => {
    setLoading(true);
    try {
      let res;
      if (statusFilter) {
        res = await purchaseOrderApi.getByStatus(statusFilter);
      } else {
        res = await purchaseOrderApi.getAll();
      }
      const data = res.data || [];
      setOrders(data);
      
      // Calculate stats
      if (!statusFilter) {
        setStats({
          total: data.length,
          pending: data.filter(o => o.status === 'ORDER_OPEN').length,
          approved: data.filter(o => o.status === 'ORDER_APPROVED').length,
          completed: data.filter(o => o.status === 'ORDER_COMPLETED').length,
        });
      }
    } catch (err) {
      console.error(err);
      message.error("Không thể tải danh sách đơn hàng");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, [statusFilter]);

  const handleAdd = () => {
    setEditing(null);
    setOpen(true);
  };

  const handleEdit = (record, e) => {
    e.stopPropagation();
    if (record.status !== 'ORDER_OPEN') {
      message.warning("Chỉ có thể chỉnh sửa đơn hàng ở trạng thái 'Chờ duyệt'");
      return;
    }
    setEditing(record);
    setOpen(true);
  };

  const handleSave = async (data) => {
    try {
      if (editing) {
        await purchaseOrderApi.update(editing.id, data);
        message.success("Cập nhật thành công");
      } else {
        await purchaseOrderApi.create(data);
        message.success("Tạo đơn hàng thành công");
      }
      setOpen(false);
      setEditing(null);
      fetchOrders();
    } catch (err) {
      console.error(err);
      message.error(err.message || "Lưu thất bại");
    }
  };

  const handleDelete = async (id, e) => {
    e.stopPropagation();
    try {
      await purchaseOrderApi.delete(id);
      message.success("Xóa đơn hàng thành công");
      fetchOrders();
    } catch (err) {
      console.error(err);
      message.error(err.message || "Không thể xóa đơn hàng");
    }
  };

  const columns = [
    {
      title: "Mã đơn hàng",
      dataIndex: "code",
      width: 140,
      render: (text) => <strong>{text || "-"}</strong>,
    },
    {
      title: "Tên đơn hàng",
      dataIndex: "orderName",
      width: 180,
      ellipsis: true,
    },
    {
      title: "Nhà cung cấp",
      dataIndex: "supplierName",
      width: 180,
      ellipsis: true,
    },
    {
      title: "Kho hàng",
      dataIndex: "warehouseName",
      width: 140,
    },
    {
      title: "Ngày tạo",
      dataIndex: "createdDate",
      width: 110,
      render: (text) => (text ? dayjs(text).format("DD/MM/YYYY") : "-"),
    },
    {
      title: "Trạng thái",
      dataIndex: "status",
      width: 140,
      render: (status) => {
        const config = STATUS_CONFIG[status] || { color: "default", label: status };
        return <Tag color={config.color}>{config.label}</Tag>;
      },
    },
    {
      title: "Tiến độ nhận",
      width: 120,
      render: (_, record) => {
        const percentage = record.receivedPercentage || 0;
        return (
          <span style={{ 
            color: percentage >= 100 ? '#52c41a' : percentage > 0 ? '#faad14' : '#999' 
          }}>
            {percentage.toFixed(0)}%
          </span>
        );
      },
    },
    {
      title: "Tổng tiền",
      dataIndex: "totalAmount",
      width: 130,
      align: "right",
      render: (val) => val ? `${val.toLocaleString("vi-VN")} ₫` : "-",
    },
    {
      title: "Thao tác",
      width: 150,
      render: (_, record) => (
        <Space size="small">
          <Button 
            size="small" 
            onClick={(e) => handleEdit(record, e)}
            disabled={record.status !== 'ORDER_OPEN'}
          >
            Sửa
          </Button>
          <Button 
            size="small" 
            danger 
            onClick={(e) => handleDelete(record.id, e)}
            disabled={record.status !== 'ORDER_OPEN'}
          >
            Xóa
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: 20 }}>
      {/* Statistics Cards */}
      <Row gutter={16} style={{ marginBottom: 20 }}>
        <Col span={6}>
          <Card>
            <Statistic title="Tổng đơn hàng" value={stats.total} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic 
              title="Chờ duyệt" 
              value={stats.pending} 
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic 
              title="Đã duyệt" 
              value={stats.approved} 
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic 
              title="Hoàn thành" 
              value={stats.completed} 
              valueStyle={{ color: '#13c2c2' }}
            />
          </Card>
        </Col>
      </Row>

      {/* Header */}
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginBottom: 16,
        }}
      >
        <h2 style={{ margin: 0 }}>Quản lý đơn hàng mua</h2>
        <Space>
          <Select
            placeholder="Lọc theo trạng thái"
            allowClear
            style={{ width: 200 }}
            value={statusFilter}
            onChange={setStatusFilter}
          >
            <Select.Option value="ORDER_OPEN">Chờ duyệt</Select.Option>
            <Select.Option value="ORDER_APPROVED">Đã duyệt</Select.Option>
            <Select.Option value="ORDER_PARTIALLY_RECEIVED">Nhận một phần</Select.Option>
            <Select.Option value="ORDER_COMPLETED">Hoàn thành</Select.Option>
            <Select.Option value="ORDER_CANCELLED">Đã hủy</Select.Option>
          </Select>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            Tạo đơn hàng mới
          </Button>
        </Space>
      </div>
      
      {/* Table */}
      <Table
        dataSource={orders}
        columns={columns}
        rowKey="id"
        loading={loading}
        pagination={{
          pageSize: 10,
          showSizeChanger: true,
          showTotal: (total) => `Tổng ${total} đơn hàng`,
        }}
        scroll={{ x: 1200 }}
        onRow={(record) => ({
          onClick: () => navigate(`/purchase-orders/${record.id}`),
          style: { cursor: "pointer" },
        })}
      />
      
      {/* Form Modal */}
      <PurchaseOrderForm
        open={open}
        onCancel={() => {
          setOpen(false);
          setEditing(null);
        }}
        onSave={handleSave}
        order={editing}
      />
    </div>
  );
}
