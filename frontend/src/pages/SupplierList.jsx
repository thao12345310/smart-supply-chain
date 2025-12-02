import React, { useEffect, useState } from "react";
import api from "../services/api";
import { Table, Button, Modal, message } from "antd";
import SupplierForm from "./SupplierForm";

export default function SupplierList() {
  const [suppliers, setSuppliers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [editing, setEditing] = useState(null);
  const [open, setOpen] = useState(false);

  const fetchData = async () => {
    setLoading(true);
    try {
      const res = await api.get("/suppliers");
      setSuppliers(res.data);
    } catch (err) {
      message.error("Failed to fetch suppliers");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleDelete = async (id) => {
    Modal.confirm({
      title: "Confirm delete?",
      onOk: async () => {
        try {
          await api.delete(`/suppliers/${id}`);
          message.success("Deleted successfully");
          fetchData();
        } catch (err) {
          message.error("Delete failed");
        }
      },
    });
  };

  const handleEdit = (supplier) => {
    setEditing(supplier);
    setOpen(true);
  };

  const handleAdd = () => {
    setEditing(null);
    setOpen(true);
  };

  const handleSave = async (data) => {
    try {
      if (editing) {
        await api.put(`/suppliers/${editing.id}`, data);
        message.success("Updated successfully");
      } else {
        await api.post("/suppliers", data);
        message.success("Added successfully");
      }
      setOpen(false);
      fetchData();
    } catch (err) {
      message.error("Save failed");
    }
  };

  const columns = [
    { title: "Code", dataIndex: "code" },
    { title: "Name", dataIndex: "name" },
    { title: "Contact", dataIndex: "contactName" },
    { title: "Phone", dataIndex: "phone" },
    { title: "Email", dataIndex: "email" },
    { title: "Address", dataIndex: "address" },
    {
      title: "Action",
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
      <h2>Supplier Management</h2>
      <Button type="primary" style={{ marginBottom: 12 }} onClick={handleAdd}>
        Add Supplier
      </Button>
      <Table
        dataSource={suppliers}
        columns={columns}
        rowKey="id"
        loading={loading}
        pagination={false}
      />
      <SupplierForm
        open={open}
        onCancel={() => setOpen(false)}
        onSave={handleSave}
        supplier={editing}
      />
    </div>
  );
}
