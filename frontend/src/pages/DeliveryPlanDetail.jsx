import React, { useEffect, useState } from "react";
import { Tabs, Table, Button, Modal, Input, message, Select, Card, Descriptions, Space, Tag, Row, Col, Statistic } from "antd";
import { useParams, useNavigate } from "react-router-dom";
import { ArrowLeftOutlined, PlusOutlined, DeleteOutlined, ThunderboltOutlined } from "@ant-design/icons";
import api from "../services/api";
import dayjs from "dayjs";

// Status configuration
const STATUS_CONFIG = {
  DRAFT: { color: "blue", label: "Nháp" },
  PENDING: { color: "orange", label: "Chờ xử lý" },
  IN_PROGRESS: { color: "processing", label: "Đang giao" },
  COMPLETED: { color: "success", label: "Hoàn thành" },
  CANCELLED: { color: "default", label: "Đã hủy" },
};

export default function DeliveryPlanDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [plan, setPlan] = useState({});
  const [orders, setOrders] = useState([]);
  const [availableOrders, setAvailableOrders] = useState([]);
  const [shippers, setShippers] = useState([]);
  const [trips, setTrips] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedOrderIds, setSelectedOrderIds] = useState([]);
  const [shipperName, setShipperName] = useState("");

  const fetchAll = async () => {
    // Nếu id là "new", không fetch data
    if (id === "new") {
      return;
    }
    setLoading(true);
    try {
      const [planRes, orderRes, shipperRes, tripRes] = await Promise.all([
        api.get(`/delivery-plans/${id}`),
        api.get(`/delivery-plans/${id}/orders`),
        api.get(`/delivery-plans/${id}/shippers`),
        api.get(`/delivery-plans/${id}/trips`),
      ]);
      setPlan(planRes.data);
      setOrders(orderRes.data || []);
      setShippers(shipperRes.data || []);
      setTrips(tripRes.data || []);
    } catch {
      message.error("Không thể tải dữ liệu");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAll();
  }, [id]);

  const handleAddOrder = async () => {
    if (id === "new") {
      message.warning("Vui lòng tạo đợt giao hàng trước");
      return;
    }
    const res = await api.get("/delivery-orders");
    setAvailableOrders(res.data || []);
    Modal.confirm({
      title: "Thêm vận đơn vào đợt",
      content: (
        <Select
          mode="multiple"
          style={{ width: "100%" }}
          placeholder="Chọn vận đơn"
          onChange={(v) => setSelectedOrderIds(v)}
        >
          {(res.data || []).map((o) => (
            <Select.Option key={o.id} value={o.id}>
              {o.code}
            </Select.Option>
          ))}
        </Select>
      ),
      okText: "Thêm",
      cancelText: "Hủy",
      onOk: async () => {
        await api.post(`/delivery-plans/${id}/orders`, selectedOrderIds);
        message.success("Đã thêm vận đơn");
        fetchAll();
      },
    });
  };

  const handleRemoveOrder = async (orderId) => {
    if (id === "new") return;
    await api.delete(`/delivery-plans/${id}/orders/${orderId}`);
    message.success("Đã xóa vận đơn");
    fetchAll();
  };

  const handleAddShipper = () => {
    if (id === "new") {
      message.warning("Vui lòng tạo đợt giao hàng trước");
      return;
    }
    Modal.confirm({
      title: "Thêm nhân viên giao hàng",
      content: (
        <Input
          placeholder="Tên nhân viên giao hàng"
          onChange={(e) => setShipperName(e.target.value)}
        />
      ),
      okText: "Thêm",
      cancelText: "Hủy",
      onOk: async () => {
        await api.post(`/delivery-plans/${id}/shippers`, {
          shipperName,
          phone: "",
        });
        message.success("Đã thêm nhân viên giao hàng");
        fetchAll();
      },
    });
  };

  const handleRemoveShipper = async (shipperId) => {
    if (id === "new") return;
    await api.delete(`/delivery-plans/${id}/shippers/${shipperId}`);
    message.success("Đã xóa nhân viên giao hàng");
    fetchAll();
  };

  const handleGenerateTrips = async () => {
    if (id === "new") {
      message.warning("Vui lòng tạo đợt giao hàng trước");
      return;
    }
    await api.post(`/delivery-plans/${id}/generate-trips`);
    message.success("Đã tự động tạo chuyến giao hàng");
    fetchAll();
  };

  const statusConfig = STATUS_CONFIG[plan.status] || { color: "default", label: plan.status };

  const orderColumns = [
    { 
      title: "Mã vận đơn", 
      dataIndex: "code",
      render: (text) => <strong>{text}</strong>,
    },
    { title: "Người tạo", dataIndex: "creator" },
    { title: "Khách hàng", dataIndex: "customerName" },
    { title: "Địa chỉ giao", dataIndex: "deliveryAddress", ellipsis: true },
    {
      title: "Thao tác",
      width: 100,
      render: (_, record) => (
        <Button 
          danger 
          size="small"
          icon={<DeleteOutlined />}
          onClick={() => handleRemoveOrder(record.id)}
        >
          Xóa
        </Button>
      ),
    },
  ];

  const shipperColumns = [
    { title: "Mã NVGH", dataIndex: "id" },
    { title: "Tên nhân viên", dataIndex: "shipperName" },
    { title: "Điện thoại", dataIndex: "phone" },
    {
      title: "Thao tác",
      width: 100,
      render: (_, record) => (
        <Button 
          danger
          size="small"
          icon={<DeleteOutlined />} 
          onClick={() => handleRemoveShipper(record.id)}
        >
          Xóa
        </Button>
      ),
    },
  ];

  const tripColumns = [
    { title: "Mã chuyến", dataIndex: "code" },
    { title: "Nhân viên GH", dataIndex: "shipperName" },
    { title: "Số vận đơn", dataIndex: "orderCount", align: "center" },
    { 
      title: "Trạng thái", 
      dataIndex: "status",
      render: (status) => {
        const config = STATUS_CONFIG[status] || { color: "default", label: status };
        return <Tag color={config.color}>{config.label}</Tag>;
      },
    },
  ];

  return (
    <div style={{ padding: 20 }}>
      {/* Header */}
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 20 }}>
        <Space>
          <Button
            icon={<ArrowLeftOutlined />}
            onClick={() => navigate("/delivery-plans")}
          >
            Quay lại
          </Button>
          <h2 style={{ margin: 0 }}>Chi tiết đợt giao hàng: {plan.code}</h2>
          {plan.status && (
            <Tag color={statusConfig.color} style={{ fontSize: 14, padding: "4px 12px" }}>
              {statusConfig.label}
            </Tag>
          )}
        </Space>
      </div>

      {/* Summary Stats */}
      <Row gutter={16} style={{ marginBottom: 20 }}>
        <Col span={6}>
          <Card>
            <Statistic title="Số vận đơn" value={orders.length} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="Số nhân viên GH" value={shippers.length} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="Số chuyến xe" value={trips.length} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic 
              title="Ngày tạo" 
              value={plan.createdDate ? dayjs(plan.createdDate).format("DD/MM/YYYY") : "-"} 
            />
          </Card>
        </Col>
      </Row>

      <Tabs
        items={[
          {
            key: "1",
            label: "Thông tin chung",
            children: (
              <Card>
                <Descriptions bordered column={2}>
                  <Descriptions.Item label="Mã đợt">{plan.code}</Descriptions.Item>
                  <Descriptions.Item label="Trạng thái">
                    <Tag color={statusConfig.color}>{statusConfig.label}</Tag>
                  </Descriptions.Item>
                  <Descriptions.Item label="Ngày tạo">
                    {plan.createdDate ? dayjs(plan.createdDate).format("DD/MM/YYYY HH:mm") : "-"}
                  </Descriptions.Item>
                  <Descriptions.Item label="Ngày giao dự kiến">
                    {plan.plannedDate ? dayjs(plan.plannedDate).format("DD/MM/YYYY") : "-"}
                  </Descriptions.Item>
                  <Descriptions.Item label="Mô tả" span={2}>{plan.description || "-"}</Descriptions.Item>
                </Descriptions>
              </Card>
            ),
          },
          {
            key: "2",
            label: `Vận đơn (${orders.length})`,
            children: (
              <>
                <Button
                  type="primary"
                  icon={<PlusOutlined />}
                  onClick={handleAddOrder}
                  style={{ marginBottom: 16 }}
                >
                  Thêm vận đơn
                </Button>
                <Table
                  dataSource={orders}
                  columns={orderColumns}
                  rowKey="id"
                  pagination={false}
                  loading={loading}
                />
              </>
            ),
          },
          {
            key: "3",
            label: `Nhân viên GH (${shippers.length})`,
            children: (
              <>
                <Button
                  type="primary"
                  icon={<PlusOutlined />}
                  onClick={handleAddShipper}
                  style={{ marginBottom: 16 }}
                >
                  Thêm nhân viên
                </Button>
                <Table
                  dataSource={shippers}
                  columns={shipperColumns}
                  rowKey="id"
                  pagination={false}
                  loading={loading}
                />
              </>
            ),
          },
          {
            key: "4",
            label: `Chuyến xe (${trips.length})`,
            children: (
              <>
                <Space style={{ marginBottom: 16 }}>
                  <Button
                    type="primary"
                    icon={<ThunderboltOutlined />}
                    onClick={handleGenerateTrips}
                  >
                    Tự động phân chuyến
                  </Button>
                  <Button icon={<PlusOutlined />}>Thêm thủ công</Button>
                </Space>
                <Table
                  dataSource={trips}
                  columns={tripColumns}
                  rowKey="id"
                  pagination={false}
                  loading={loading}
                />
              </>
            ),
          },
        ]}
      />
    </div>
  );
}
