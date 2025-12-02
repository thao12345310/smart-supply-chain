import React, { useEffect, useState } from "react";
import { Table, Button, message } from "antd";
import { useNavigate } from "react-router-dom";
import api from "../services/api";

export default function DeliveryPlanList() {
  const [plans, setPlans] = useState([]);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const fetchPlans = async () => {
    setLoading(true);
    try {
      const res = await api.get("/delivery-plans");
      setPlans(res.data);
    } catch {
      message.error("Failed to load delivery plans");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPlans();
  }, []);

  const columns = [
    { title: "Mã đợt GH", dataIndex: "code" },
    { title: "Ngày tạo", dataIndex: "createdDate" },
    { title: "Mô tả", dataIndex: "description" },
    { title: "Trạng thái", dataIndex: "status" },
  ];

  return (
    <div style={{ padding: 20 }}>
      <Button onClick={() => navigate("/")} style={{ marginBottom: 10 }}>
        ← Quay lại
      </Button>
      <h2>Giao hàng theo đợt</h2>
      <Button
        type="primary"
        onClick={() => navigate("/delivery-plans/new")}
        style={{ marginBottom: 12 }}
      >
        Thêm mới
      </Button>
      <Table
        dataSource={plans}
        columns={columns}
        rowKey="id"
        loading={loading}
        pagination={false}
        onRow={(record) => ({
          onClick: () => navigate(`/delivery-plans/${record.id}`),
        })}
      />
    </div>
  );
}
