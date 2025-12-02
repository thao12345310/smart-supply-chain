import React, { useEffect, useState } from "react";
import { Table, Button, Modal, message } from "antd";
import api from "../services/api";
import ProductForm from "./ProductForm";

export default function ProductList() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [editing, setEditing] = useState(null);
  const [open, setOpen] = useState(false);

  const fetchProducts = async () => {
    setLoading(true);
    try {
      const res = await api.get("/products");
      setProducts(res.data);
    } catch {
      message.error("Failed to fetch products");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, []);

  const handleAdd = () => {
    setEditing(null);
    setOpen(true);
  };

  const handleEdit = (product) => {
    setEditing(product);
    setOpen(true);
  };

  const handleDelete = (id) => {
    Modal.confirm({
      title: "Delete this product?",
      onOk: async () => {
        try {
          await api.delete(`/products/${id}`);
          message.success("Deleted successfully");
          fetchProducts();
        } catch {
          message.error("Delete failed");
        }
      },
    });
  };

  const handleSave = async (data) => {
    try {
      if (editing) {
        await api.put(`/products/${editing.id}`, data);
        message.success("Updated successfully");
      } else {
        await api.post("/products", data);
        message.success("Added successfully");
      }
      setOpen(false);
      fetchProducts();
    } catch {
      message.error("Save failed");
    }
  };

  const columns = [
    { title: "Code", dataIndex: "code" },
    { title: "Name", dataIndex: "name" },
    {
      title: "Supplier",
      render: (_, record) => (record.supplier ? record.supplier.name : "—"),
    },
    { title: "Description", dataIndex: "description" },
    { title: "Unit", dataIndex: "unit" },
    { title: "Price", dataIndex: "price" },
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
      <h2>Product Management</h2>
      <Button type="primary" onClick={handleAdd} style={{ marginBottom: 12 }}>
        Add Product
      </Button>
      <Table
        dataSource={products}
        columns={columns}
        rowKey="id"
        loading={loading}
        pagination={false}
      />
      <ProductForm
        open={open}
        onCancel={() => setOpen(false)}
        onSave={handleSave}
        product={editing}
      />
    </div>
  );
}
