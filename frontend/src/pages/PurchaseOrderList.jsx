import React, { useEffect, useState } from "react";
import { Table, Button, Modal, message } from "antd";
import api from "../services/api";
import PurchaseOrderForm from "./PurchaseOrderForm";

export default function PurchaseOrderList() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [editing, setEditing] = useState(null);
  const [open, setOpen] = useState(false);

  const fetchOrders = async () => {
    setLoading(true);
    try {
      const res = await api.get("/purchase-orders");
      setOrders(res.data);
    } catch {
      message.error("Failed to fetch purchase orders");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, []);

  const handleAdd = () => {
    setEditing(null);
    setOpen(true);
  };

  const handleEdit = (order) => {
    setEditing(order);
    setOpen(true);
  };

  const handleDelete = (id) => {
    Modal.confirm({
      title: "Delete this order?",
      onOk: async () => {
        try {
          await api.delete(`/purchase-orders/${id}`);
          message.success("Deleted successfully");
          fetchOrders();
        } catch {
          message.error("Delete failed");
        }
      },
    });
  };

  const handleSave = async (data) => {
    try {
      if (editing) {
        await api.put(`/purchase-orders/${editing.id}`, data);
        message.success("Updated successfully");
      } else {
        await api.post("/purchase-orders", data);
        message.success("Created successfully");
      }
      setOpen(false);
      fetchOrders();
    } catch {
      message.error("Save failed");
    }
  };

  const columns = [
    { title: "Code", dataIndex: "code" },
    { title: "Supplier", dataIndex: "supplierName" },
    { title: "Order Date", dataIndex: "orderDate" },
    { title: "Status", dataIndex: "status" },
    {
      title: "Actions",
      render: (_, record) => (
        <>
          <Button type="link" onClick={() => handleEdit(record)}>
            Edit
          </Button>
          <Button type="link" danger onClick={() => handleDelete(record.id)}>
            Delete
          </Button>
        </>
      ),
    },
  ];

  return (
    <div style={{ padding: 20 }}>
      <h2>Purchase Order Management</h2>
      <Table
        dataSource={orders}
        columns={columns}
        rowKey="id"
        loading={loading}
        pagination={false}
      />
      <PurchaseOrderForm
        open={open}
        onCancel={() => setOpen(false)}
        onSave={handleSave}
        order={editing}
      />
    </div>
  );
}
