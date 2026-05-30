import React, { useEffect, useState } from "react";
import { Table, Button, message, Tag, Card, Space, Popconfirm, Row, Col, Statistic, Progress } from "antd";
import { TruckOutlined, CheckCircleOutlined, CloseCircleOutlined, ClockCircleOutlined, ArrowLeftOutlined, EnvironmentOutlined } from "@ant-design/icons";
import { useNavigate } from "react-router-dom";
import api from "../services/api";
import dayjs from "dayjs";
import { getStatusConfig, getItemStatusConfig, normalizeStatus } from "../services/deliveryStatus";

export default function AssignedTrips() {
  const [trips, setTrips] = useState([]);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const fetchTrips = async () => {
    setLoading(true);
    try {
      const res = await api.get("/delivery-trips");
      // Chuyến mới tạo (id lớn hơn) hiển thị trên đầu
      setTrips((res.data || []).slice().sort((a, b) => b.id - a.id));
    } catch {
      message.error("Không thể tải danh sách chuyến giao hàng");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTrips();
  }, []);

  const updateItem = async (tripId, itemId, status) => {
    try {
      await api.put(`/delivery-trips/${tripId}/items/${itemId}/status`, null, { params: { status } });
      message.success(status === "Delivered" ? "Đã ghi nhận giao thành công" : "Đã ghi nhận giao thất bại");
      fetchTrips();
    } catch (e) {
      message.error(e.message || "Không thể cập nhật điểm giao");
    }
  };

  const stats = {
    total: trips.length,
    inProgress: trips.filter((t) => normalizeStatus(t.status) === "INPROGRESS").length,
    completed: trips.filter((t) => normalizeStatus(t.status) === "COMPLETED").length,
  };

  // Bảng các điểm giao trong 1 chuyến
  const renderItems = (trip) => {
    const tripDone = ["COMPLETED", "CANCELLED"].includes(normalizeStatus(trip.status));
    const itemColumns = [
      { title: "#", dataIndex: "sequence", width: 50, align: "center" },
      { title: "Mã vận đơn", dataIndex: "deliveryOrderCode", render: (t) => <strong>{t}</strong> },
      { title: "Khách hàng", dataIndex: "customerName", render: (t) => t || "-" },
      {
        title: "Địa chỉ giao",
        dataIndex: "deliveryAddress",
        render: (t) => (
          <span><EnvironmentOutlined style={{ color: "#1890ff", marginRight: 4 }} />{t || "-"}</span>
        ),
      },
      { title: "Hàng hoá", dataIndex: "products", render: (t) => t || "-" },
      {
        title: "Tình trạng",
        dataIndex: "status",
        width: 130,
        render: (status) => {
          const cfg = getItemStatusConfig(status);
          return <Tag color={cfg.color}>{cfg.label}</Tag>;
        },
      },
      {
        title: "Ghi nhận",
        width: 200,
        render: (_, item) => {
          const pending = normalizeStatus(item.status) === "PENDING";
          if (tripDone || !pending) return null;
          return (
            <Space>
              <Button type="primary" size="small" icon={<CheckCircleOutlined />}
                onClick={() => updateItem(trip.id, item.id, "Delivered")}>
                Thành công
              </Button>
              <Popconfirm title="Xác nhận giao thất bại điểm này?" okText="Thất bại" cancelText="Hủy"
                onConfirm={() => updateItem(trip.id, item.id, "Failed")}>
                <Button danger size="small" icon={<CloseCircleOutlined />}>Thất bại</Button>
              </Popconfirm>
            </Space>
          );
        },
      },
    ];
    return (
      <Table
        dataSource={trip.items || []}
        columns={itemColumns}
        rowKey="id"
        pagination={false}
        size="small"
      />
    );
  };

  const columns = [
    { title: "Mã chuyến", dataIndex: "code", render: (t) => <strong>{t}</strong> },
    { title: "Đợt giao hàng", dataIndex: "deliveryPlanDescription", render: (t) => t || "-" },
    {
      title: "Tiến độ",
      width: 160,
      render: (_, r) => {
        const total = r.totalItems || 0;
        const done = r.completedItems || 0;
        const percent = total ? Math.round((done / total) * 100) : 0;
        return <Progress percent={percent} size="small" format={() => `${done}/${total}`} />;
      },
    },
    {
      title: "Trạng thái",
      dataIndex: "status",
      width: 120,
      render: (status) => {
        const config = getStatusConfig(status);
        return <Tag color={config.color}>{config.label}</Tag>;
      },
    },
    {
      title: "Bắt đầu",
      dataIndex: "startedAt",
      width: 140,
      render: (t) => (t ? dayjs(t).format("DD/MM/YYYY HH:mm") : "-"),
    },
    {
      title: "Hoàn thành",
      dataIndex: "completedAt",
      width: 140,
      render: (t) => (t ? dayjs(t).format("DD/MM/YYYY HH:mm") : "-"),
    },
  ];

  return (
    <div style={{ padding: 20 }}>
      <Row gutter={16} style={{ marginBottom: 20 }}>
        <Col span={8}>
          <Card>
            <Statistic title="Tổng chuyến được giao" value={stats.total} prefix={<TruckOutlined />} />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic title="Đang giao" value={stats.inProgress} valueStyle={{ color: "#1890ff" }} prefix={<ClockCircleOutlined />} />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic title="Đã hoàn thành" value={stats.completed} valueStyle={{ color: "#52c41a" }} prefix={<CheckCircleOutlined />} />
          </Card>
        </Col>
      </Row>

      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 16 }}>
        <Space>
          <Button icon={<ArrowLeftOutlined />} onClick={() => navigate("/")}>
            Quay lại
          </Button>
          <h2 style={{ margin: 0 }}>Chuyến giao hàng của tôi</h2>
        </Space>
      </div>

      <Table
        dataSource={trips}
        columns={columns}
        rowKey="id"
        loading={loading}
        expandable={{
          expandedRowRender: renderItems,
          defaultExpandAllRows: true,
          rowExpandable: (r) => (r.items || []).length > 0,
        }}
        pagination={{ pageSize: 10, showTotal: (total) => `Tổng ${total} chuyến` }}
      />
    </div>
  );
}
