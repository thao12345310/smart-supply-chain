import React, { useEffect, useState } from "react";
import { Table, Button, Modal, message, Tag, Card, Space, Input, Row, Col, Statistic } from "antd";
import { PlusOutlined, SearchOutlined, EditOutlined, DeleteOutlined } from "@ant-design/icons";
import api from "../services/api";
import ProductForm from "./ProductForm";

export default function ProductList() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [editing, setEditing] = useState(null);
  const [open, setOpen] = useState(false);
  const [searchText, setSearchText] = useState("");

  const fetchProducts = async () => {
    setLoading(true);
    try {
      const res = await api.get("/products");
      setProducts(res.data || []);
    } catch {
      message.error("Không thể tải danh sách sản phẩm");
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

  const handleEdit = (product, e) => {
    e?.stopPropagation();
    setEditing(product);
    setOpen(true);
  };

  const handleDelete = (id, e) => {
    e?.stopPropagation();
    Modal.confirm({
      title: "Xác nhận xóa sản phẩm?",
      content: "Bạn có chắc muốn xóa sản phẩm này?",
      okText: "Xóa",
      cancelText: "Hủy",
      okButtonProps: { danger: true },
      onOk: async () => {
        try {
          await api.delete(`/products/${id}`);
          message.success("Xóa sản phẩm thành công");
          fetchProducts();
        } catch {
          message.error("Không thể xóa sản phẩm");
        }
      },
    });
  };

  const handleSave = async (data) => {
    try {
      if (editing) {
        await api.put(`/products/${editing.id}`, data);
        message.success("Cập nhật thành công");
      } else {
        await api.post("/products", data);
        message.success("Thêm sản phẩm thành công");
      }
      setOpen(false);
      setEditing(null);
      fetchProducts();
    } catch {
      message.error("Lưu thất bại");
    }
  };

  const filteredProducts = products.filter(item => {
    if (!searchText) return true;
    const search = searchText.toLowerCase();
    return (
      (item.name || "").toLowerCase().includes(search) ||
      (item.code || "").toLowerCase().includes(search)
    );
  });

  const columns = [
    { 
      title: "Mã SP", 
      dataIndex: "code",
      width: 120,
      render: (text) => <strong>{text || "-"}</strong>,
    },
    { 
      title: "Tên sản phẩm", 
      dataIndex: "name",
      ellipsis: true,
    },
    {
      title: "Nhà cung cấp",
      width: 180,
      ellipsis: true,
      render: (_, record) => (record.supplier ? record.supplier.name : "—"),
    },
    { 
      title: "Mô tả", 
      dataIndex: "description",
      ellipsis: true,
    },
    { 
      title: "ĐVT", 
      dataIndex: "unit",
      width: 80,
    },
    { 
      title: "Giá bán", 
      dataIndex: "price",
      width: 130,
      align: "right",
      render: (val) => val ? `${val.toLocaleString("vi-VN")} ₫` : "-",
    },
    {
      title: "Trạng thái",
      dataIndex: "active",
      width: 100,
      render: (active) => (
        <Tag color={active !== false ? "green" : "default"}>
          {active !== false ? "Hoạt động" : "Ngừng"}
        </Tag>
      ),
    },
    {
      title: "Thao tác",
      width: 150,
      render: (_, record) => (
        <Space size="small">
          <Button 
            size="small" 
            icon={<EditOutlined />}
            onClick={(e) => handleEdit(record, e)}
          >
            Sửa
          </Button>
          <Button 
            size="small" 
            danger 
            icon={<DeleteOutlined />}
            onClick={(e) => handleDelete(record.id, e)}
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
        <Col span={8}>
          <Card>
            <Statistic title="Tổng sản phẩm" value={products.length} />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic 
              title="Đang hoạt động" 
              value={products.filter(p => p.active !== false).length} 
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic 
              title="Ngừng kinh doanh" 
              value={products.filter(p => p.active === false).length} 
              valueStyle={{ color: '#999' }}
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
        <h2 style={{ margin: 0 }}>Quản lý sản phẩm</h2>
        <Space>
          <Input
            placeholder="Tìm sản phẩm..."
            prefix={<SearchOutlined />}
            style={{ width: 250 }}
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            allowClear
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            Thêm sản phẩm
          </Button>
        </Space>
      </div>

      {/* Table */}
      <Table
        dataSource={filteredProducts}
        columns={columns}
        rowKey="id"
        loading={loading}
        pagination={{
          pageSize: 10,
          showSizeChanger: true,
          showTotal: (total) => `Tổng ${total} sản phẩm`,
        }}
        scroll={{ x: 1000 }}
      />

      {/* Form Modal */}
      <ProductForm
        open={open}
        onCancel={() => {
          setOpen(false);
          setEditing(null);
        }}
        onSave={handleSave}
        product={editing}
      />
    </div>
  );
}
