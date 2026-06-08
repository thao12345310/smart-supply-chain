import React, { useEffect, useState } from "react";
import { Tabs, Table, Button, Modal, Input, message, Select, Card, Descriptions, Space, Tag, Row, Col, Statistic } from "antd";
import { useParams, useNavigate } from "react-router-dom";
import { ArrowLeftOutlined, PlusOutlined, DeleteOutlined, ThunderboltOutlined } from "@ant-design/icons";
import api from "../services/api";
import dayjs from "dayjs";
import { ROLES, hasAnyRole } from "../services/roleService";
import { getStatusConfig } from "../services/deliveryStatus";

export default function DeliveryPlanDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [plan, setPlan] = useState({});
  const [orders, setOrders] = useState([]);
  const [, setAvailableOrders] = useState([]);
  const [shippers, setShippers] = useState([]);
  const [trips, setTrips] = useState([]);
  const [loading, setLoading] = useState(false);

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
      // Chuyến mới tạo (id lớn hơn) hiển thị trên đầu
      setTrips((tripRes.data || []).slice().sort((a, b) => b.id - a.id));
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
    const available = res.data || [];
    setAvailableOrders(available);
    let chosen = [];
    Modal.confirm({
      title: "Thêm vận đơn vào đợt",
      content: (
        <Select
          mode="multiple"
          style={{ width: "100%" }}
          placeholder="Chọn vận đơn"
          optionFilterProp="children"
          onChange={(v) => { chosen = v; }}
        >
          {available.map((o) => (
            <Select.Option key={o.id} value={o.id}>
              {o.code}{o.salesOrderCode ? ` (${o.salesOrderCode})` : ""} — {o.customerName || ""}
            </Select.Option>
          ))}
        </Select>
      ),
      okText: "Thêm",
      cancelText: "Hủy",
      onOk: async () => {
        if (!chosen || chosen.length === 0) {
          message.warning("Chưa chọn vận đơn nào");
          return;
        }
        await api.post(`/delivery-plans/${id}/orders`, chosen);
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

  const handleAddShipper = async () => {
    if (id === "new") {
      message.warning("Vui lòng tạo đợt giao hàng trước");
      return;
    }
    let shipperUsers = [];
    try {
      const res = await api.get("/delivery-plans/shipper-users");
      shipperUsers = res.data || [];
    } catch {
      // ignore - rơi về nhập tay nếu không lấy được
    }
    let chosenUserId = null;
    let chosenName = "";
    Modal.confirm({
      title: "Thêm nhân viên giao hàng",
      content:
        shipperUsers.length > 0 ? (
          <Select
            style={{ width: "100%" }}
            placeholder="Chọn nhân viên giao hàng"
            onChange={(v) => { chosenUserId = v; }}
          >
            {shipperUsers.map((u) => (
              <Select.Option key={u.id} value={u.id}>
                {u.name}
              </Select.Option>
            ))}
          </Select>
        ) : (
          <Input
            placeholder="Tên nhân viên giao hàng"
            onChange={(e) => { chosenName = e.target.value; }}
          />
        ),
      okText: "Thêm",
      cancelText: "Hủy",
      onOk: async () => {
        if (!chosenUserId && !chosenName) {
          message.warning("Chưa chọn nhân viên giao hàng");
          return;
        }
        try {
          await api.post(`/delivery-plans/${id}/shippers`, {
            shipperUserId: chosenUserId,
            shipperName: chosenName,
            phone: "",
          });
          message.success("Đã thêm nhân viên giao hàng");
          fetchAll();
        } catch (e) {
          message.error(e.message || "Không thể thêm nhân viên giao hàng");
          return Promise.reject();
        }
      },
    });
  };

  const handleRemoveShipper = async (shipperId) => {
    if (id === "new") return;
    await api.delete(`/delivery-plans/${id}/shippers/${shipperId}`);
    message.success("Đã xóa nhân viên giao hàng");
    fetchAll();
  };

  // Tự động chia vận đơn của đợt cho các shipper đã thêm vào đợt
  const handleGenerateTrips = async () => {
    if (id === "new") {
      message.warning("Vui lòng tạo đợt giao hàng trước");
      return;
    }
    if (orders.length === 0) {
      message.warning("Đợt giao hàng chưa có vận đơn nào");
      return;
    }
    if (shippers.length === 0) {
      message.warning("Hãy thêm nhân viên giao hàng vào đợt trước");
      return;
    }
    Modal.confirm({
      title: "Tự động tạo chuyến",
      content: (
        <p>
          Chia {orders.length} vận đơn cho {shippers.length} nhân viên giao hàng của đợt
          (mỗi nhân viên một chuyến)?
        </p>
      ),
      okText: "Tạo chuyến",
      cancelText: "Hủy",
      onOk: async () => {
        try {
          const res = await api.post(`/delivery-plans/${id}/generate-trips`);
          message.success(`Đã tạo ${(res.data || []).length} chuyến giao hàng`);
          fetchAll();
        } catch (e) {
          message.error(e.message || "Không thể tạo chuyến");
        }
      },
    });
  };

  // Tạo 1 chuyến thủ công: chọn shipper + chọn vận đơn
  const handleAddTripManual = async () => {
    if (id === "new") {
      message.warning("Vui lòng tạo đợt giao hàng trước");
      return;
    }
    if (orders.length === 0) {
      message.warning("Đợt giao hàng chưa có vận đơn nào");
      return;
    }
    // Chỉ cho chọn vận đơn chưa được phân vào chuyến nào
    const unassignedOrders = orders.filter((o) => !o.assignedToTrip);
    if (unassignedOrders.length === 0) {
      message.warning("Tất cả vận đơn của đợt đã được phân chuyến");
      return;
    }
    // Tài xế chỉ chọn trong số nhân viên giao hàng đã thêm vào đợt (nhất quán với "Tự động tạo chuyến")
    const planShippers = shippers.filter((s) => s.shipperUserId != null);
    if (planShippers.length === 0) {
      message.warning("Hãy thêm nhân viên giao hàng vào đợt trước (tab Nhân viên GH)");
      return;
    }
    let chosenShipperId = null;
    let chosenOrderIds = [];
    Modal.confirm({
      title: "Tạo chuyến thủ công",
      width: 520,
      content: (
        <div>
          <p style={{ marginBottom: 4 }}>Nhân viên giao hàng:</p>
          <Select
            style={{ width: "100%", marginBottom: 12 }}
            placeholder="Chọn nhân viên giao hàng"
            onChange={(v) => { chosenShipperId = v; }}
          >
            {planShippers.map((s) => (
              <Select.Option key={s.id} value={s.shipperUserId}>{s.shipperName}</Select.Option>
            ))}
          </Select>
          <p style={{ marginBottom: 4 }}>Vận đơn cho chuyến:</p>
          <Select
            mode="multiple"
            style={{ width: "100%" }}
            placeholder="Chọn vận đơn"
            optionFilterProp="children"
            onChange={(v) => { chosenOrderIds = v; }}
          >
            {unassignedOrders.map((o) => (
              <Select.Option key={o.id} value={o.id}>
                {o.code}{o.salesOrderCode ? ` (${o.salesOrderCode})` : ""} — {o.customerName || ""}
              </Select.Option>
            ))}
          </Select>
        </div>
      ),
      okText: "Tạo chuyến",
      cancelText: "Hủy",
      onOk: async () => {
        if (!chosenShipperId) {
          message.warning("Chưa chọn nhân viên giao hàng");
          return Promise.reject();
        }
        if (!chosenOrderIds.length) {
          message.warning("Chưa chọn vận đơn");
          return Promise.reject();
        }
        try {
          await api.post(`/delivery-plans/${id}/trips`, {
            shipperId: chosenShipperId,
            orderIds: chosenOrderIds,
          });
          message.success("Đã tạo chuyến giao hàng");
          fetchAll();
        } catch (e) {
          message.error(e.message || "Không thể tạo chuyến");
          return Promise.reject();
        }
      },
    });
  };

  const handleRemoveTrip = async (tripId) => {
    try {
      await api.delete(`/delivery-trips/${tripId}`);
      message.success("Đã xóa chuyến");
      fetchAll();
    } catch (e) {
      message.error(e.message || "Không thể xóa chuyến");
    }
  };

  const statusConfig = getStatusConfig(plan.status);

  const orderColumns = [
    {
      title: "Mã vận đơn",
      dataIndex: "code",
      render: (text) => <strong>{text}</strong>,
    },
    { title: "Đơn bán", dataIndex: "salesOrderCode", render: (t) => t || "-" },
    { title: "Khách hàng", dataIndex: "customerName", render: (t) => t || "-" },
    { title: "Địa chỉ giao", dataIndex: "deliveryAddress", ellipsis: true, render: (t) => t || "-" },
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
        const config = getStatusConfig(status);
        return <Tag color={config.color}>{config.label}</Tag>;
      },
    },
    {
      title: "Thao tác",
      width: 100,
      render: (_, record) => (
        <Button
          danger
          size="small"
          icon={<DeleteOutlined />}
          onClick={() => handleRemoveTrip(record.id)}
        >
          Xóa
        </Button>
      ),
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
                {hasAnyRole([ROLES.ADMIN, ROLES.DELIVERY_ADMIN]) && (
                  <Button
                    type="primary"
                    icon={<PlusOutlined />}
                    onClick={handleAddOrder}
                    style={{ marginBottom: 16 }}
                  >
                    Thêm vận đơn
                  </Button>
                )}
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
                {hasAnyRole([ROLES.ADMIN, ROLES.DELIVERY_ADMIN]) && (
                  <Button
                    type="primary"
                    icon={<PlusOutlined />}
                    onClick={handleAddShipper}
                    style={{ marginBottom: 16 }}
                  >
                    Thêm nhân viên
                  </Button>
                )}
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
                {hasAnyRole([ROLES.ADMIN, ROLES.DELIVERY_ADMIN]) && (
                  <Space style={{ marginBottom: 16 }}>
                    <Button
                      type="primary"
                      icon={<ThunderboltOutlined />}
                      onClick={handleGenerateTrips}
                    >
                      Tự động tạo chuyến
                    </Button>
                    <Button icon={<PlusOutlined />} onClick={handleAddTripManual}>
                      Thêm thủ công
                    </Button>
                  </Space>
                )}
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
