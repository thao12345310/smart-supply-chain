import React, { useEffect, useState } from "react";
import { Tabs, Table, Button, Modal, Input, message, Select } from "antd";
import { useParams, useNavigate } from "react-router-dom";
import api from "../services/api";

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
      setOrders(orderRes.data);
      setShippers(shipperRes.data);
      setTrips(tripRes.data);
    } catch {
      message.error("Failed to load data");
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
    setAvailableOrders(res.data);
    Modal.confirm({
      title: "Thêm vận đơn vào đợt",
      content: (
        <Select
          mode="multiple"
          style={{ width: "100%" }}
          placeholder="Chọn vận đơn"
          onChange={(v) => setSelectedOrderIds(v)}
        >
          {res.data.map((o) => (
            <Select.Option key={o.id} value={o.id}>
              {o.code}
            </Select.Option>
          ))}
        </Select>
      ),
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
      title: "Thêm shipper",
      content: (
        <Input
          placeholder="Tên shipper"
          onChange={(e) => setShipperName(e.target.value)}
        />
      ),
      onOk: async () => {
        await api.post(`/delivery-plans/${id}/shippers`, {
          shipperName,
          phone: "",
        });
        message.success("Đã thêm shipper");
        fetchAll();
      },
    });
  };

  const handleRemoveShipper = async (shipperId) => {
    if (id === "new") return;
    await api.delete(`/delivery-plans/${id}/shippers/${shipperId}`);
    message.success("Đã xóa shipper");
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

  const orderColumns = [
    { title: "Mã phiếu", dataIndex: "code" },
    { title: "Người tạo", dataIndex: "creator" },
    {
      title: "Hành động",
      render: (_, record) => (
        <Button danger onClick={() => handleRemoveOrder(record.id)}>
          Remove
        </Button>
      ),
    },
  ];

  const shipperColumns = [
    { title: "Mã NHVG", dataIndex: "id" },
    { title: "Tên", dataIndex: "shipperName" },
    {
      title: "Hành động",
      render: (_, record) => (
        <Button danger onClick={() => handleRemoveShipper(record.id)}>
          Remove
        </Button>
      ),
    },
  ];

  const tripColumns = [
    { title: "Mã chuyến", dataIndex: "code" },
    { title: "NHVG", dataIndex: "shipperName" },
    { title: "Số vận đơn", dataIndex: "orderCount" },
  ];

  return (
    <div style={{ padding: 20 }}>
      <Button
        onClick={() => navigate("/delivery-plans")}
        style={{ marginBottom: 10 }}
      >
        ← Quay lại
      </Button>
      <h2>Chi tiết đợt giao hàng {plan.code}</h2>

      <Tabs
        items={[
          {
            key: "1",
            label: "Thông tin chung",
            children: (
              <>
                <p>
                  <b>Mã đợt:</b> {plan.code}
                </p>
                <p>
                  <b>Ngày tạo:</b> {plan.createdDate}
                </p>
                <p>
                  <b>Mô tả:</b> {plan.description}
                </p>
                <p>
                  <b>Trạng thái:</b> {plan.status}
                </p>
              </>
            ),
          },
          {
            key: "2",
            label: "DS vận đơn",
            children: (
              <>
                <Button
                  type="primary"
                  onClick={handleAddOrder}
                  style={{ marginBottom: 10 }}
                >
                  Thêm
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
            label: "DS Shipper",
            children: (
              <>
                <Button
                  type="primary"
                  onClick={handleAddShipper}
                  style={{ marginBottom: 10 }}
                >
                  Thêm
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
            label: "DS chuyến",
            children: (
              <>
                <Button
                  type="primary"
                  onClick={handleGenerateTrips}
                  style={{ marginRight: 8 }}
                >
                  Tự động tạo chuyến
                </Button>
                <Button type="default">Thêm</Button>
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
