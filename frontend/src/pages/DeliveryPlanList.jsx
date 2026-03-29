import React, { useEffect, useState } from "react";
import { Table, Button, message, Tag, Card, Space, Row, Col, Statistic } from "antd";
import { PlusOutlined, TruckOutlined, CheckCircleOutlined, ClockCircleOutlined, ArrowLeftOutlined } from "@ant-design/icons";
import { useNavigate } from "react-router-dom";
import api from "../services/api";
import dayjs from "dayjs";
import { ROLES, hasAnyRole } from "../services/roleService";

// Status configuration
const STATUS_CONFIG = {
  DRAFT: { color: "blue", label: "Nháp", icon: <ClockCircleOutlined /> },
  PENDING: { color: "orange", label: "Chờ xử lý", icon: <ClockCircleOutlined /> },
  IN_PROGRESS: { color: "processing", label: "Đang giao", icon: <TruckOutlined /> },
  COMPLETED: { color: "success", label: "Hoàn thành", icon: <CheckCircleOutlined /> },
  CANCELLED: { color: "default", label: "Đã hủy", icon: null },
};

export default function DeliveryPlanList() {
  const [plans, setPlans] = useState([]);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const fetchPlans = async () => {
    setLoading(true);
    try {
      const res = await api.get("/delivery-plans");
      setPlans(res.data || []);
    } catch {
      message.error("Không thể tải danh sách kế hoạch giao hàng");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPlans();
  }, []);

  const getStats = () => ({
    total: plans.length,
    pending: plans.filter(p => p.status === 'PENDING' || p.status === 'DRAFT').length,
    inProgress: plans.filter(p => p.status === 'IN_PROGRESS').length,
    completed: plans.filter(p => p.status === 'COMPLETED').length,
  });

  const stats = getStats();

  const columns = [
    { 
      title: "Mã đợt giao hàng", 
      dataIndex: "code",
      width: 150,
      render: (text) => <strong>{text || "-"}</strong>,
    },
    { 
      title: "Ngày tạo", 
      dataIndex: "createdDate",
      width: 120,
      render: (text) => text ? dayjs(text).format("DD/MM/YYYY") : "-",
    },
    { 
      title: "Ngày giao dự kiến", 
      dataIndex: "plannedDate",
      width: 130,
      render: (text) => text ? dayjs(text).format("DD/MM/YYYY") : "-",
    },
    { 
      title: "Mô tả", 
      dataIndex: "description",
      ellipsis: true,
    },
    { 
      title: "Số đơn hàng", 
      dataIndex: "orderCount",
      width: 100,
      align: "center",
    },
    { 
      title: "Trạng thái", 
      dataIndex: "status",
      width: 120,
      render: (status) => {
        const config = STATUS_CONFIG[status] || { color: "default", label: status };
        return <Tag color={config.color}>{config.label}</Tag>;
      },
    },
  ];

  return (
    <div style={{ padding: 20 }}>
      {/* Statistics Cards */}
      <Row gutter={16} style={{ marginBottom: 20 }}>
        <Col span={6}>
          <Card>
            <Statistic title="Tổng đợt giao" value={stats.total} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic 
              title="Chờ xử lý" 
              value={stats.pending} 
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic 
              title="Đang giao" 
              value={stats.inProgress} 
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic 
              title="Hoàn thành" 
              value={stats.completed} 
              valueStyle={{ color: '#52c41a' }}
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
        <Space>
          <Button icon={<ArrowLeftOutlined />} onClick={() => navigate("/")}>
            Quay lại
          </Button>
          <h2 style={{ margin: 0 }}>Kế hoạch giao hàng</h2>
        </Space>
        {hasAnyRole([ROLES.ADMIN, ROLES.DELIVERY_ADMIN]) && (
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => navigate("/delivery-plans/new")}
          >
            Tạo đợt giao mới
          </Button>
        )}
      </div>

      {/* Table */}
      <Table
        dataSource={plans}
        columns={columns}
        rowKey="id"
        loading={loading}
        pagination={{
          pageSize: 10,
          showSizeChanger: true,
          showTotal: (total) => `Tổng ${total} đợt giao`,
        }}
        onRow={(record) => ({
          onClick: () => navigate(`/delivery-plans/${record.id}`),
          style: { cursor: "pointer" },
        })}
      />
    </div>
  );
}
