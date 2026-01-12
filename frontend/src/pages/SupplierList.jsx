import React, { useEffect, useState } from "react";
import api from "../services/api";
import { Table, Button, Modal, message, Tag, Card, Space, Input, Row, Col, Statistic } from "antd";
import { PlusOutlined, SearchOutlined, EditOutlined, DeleteOutlined } from "@ant-design/icons";
import SupplierForm from "./SupplierForm";

export default function SupplierList() {
  const [suppliers, setSuppliers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [editing, setEditing] = useState(null);
  const [open, setOpen] = useState(false);
  const [searchText, setSearchText] = useState("");

  const fetchData = async () => {
    setLoading(true);
    try {
      const res = await api.get("/suppliers");
      setSuppliers(res.data || []);
    } catch (err) {
      message.error("Không thể tải danh sách nhà cung cấp");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleDelete = async (id, e) => {
    e?.stopPropagation();
    Modal.confirm({
      title: "Xác nhận xóa nhà cung cấp?",
      content: "Bạn có chắc muốn xóa nhà cung cấp này?",
      okText: "Xóa",
      cancelText: "Hủy",
      okButtonProps: { danger: true },
      onOk: async () => {
        try {
          await api.delete(`/suppliers/${id}`);
          message.success("Xóa thành công");
          fetchData();
        } catch (err) {
          message.error("Không thể xóa nhà cung cấp");
        }
      },
    });
  };

  const handleEdit = (supplier, e) => {
    e?.stopPropagation();
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
        message.success("Cập nhật thành công");
      } else {
        await api.post("/suppliers", data);
        message.success("Thêm nhà cung cấp thành công");
      }
      setOpen(false);
      setEditing(null);
      fetchData();
    } catch (err) {
      message.error("Lưu thất bại");
    }
  };

  const filteredSuppliers = suppliers.filter(item => {
    if (!searchText) return true;
    const search = searchText.toLowerCase();
    return (
      (item.name || "").toLowerCase().includes(search) ||
      (item.code || "").toLowerCase().includes(search) ||
      (item.contactName || "").toLowerCase().includes(search)
    );
  });

  const columns = [
    { 
      title: "Mã NCC", 
      dataIndex: "code",
      width: 120,
      render: (text) => <strong>{text || "-"}</strong>,
    },
    { 
      title: "Tên nhà cung cấp", 
      dataIndex: "name",
      ellipsis: true,
    },
    { 
      title: "Người liên hệ", 
      dataIndex: "contactName",
      width: 150,
    },
    { 
      title: "Điện thoại", 
      dataIndex: "phone",
      width: 130,
    },
    { 
      title: "Email", 
      dataIndex: "email",
      width: 180,
      ellipsis: true,
    },
    { 
      title: "Địa chỉ", 
      dataIndex: "address",
      ellipsis: true,
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
            <Statistic title="Tổng nhà cung cấp" value={suppliers.length} />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic 
              title="Đang hoạt động" 
              value={suppliers.filter(s => s.active !== false).length} 
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic 
              title="Ngừng hợp tác" 
              value={suppliers.filter(s => s.active === false).length} 
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
        <h2 style={{ margin: 0 }}>Quản lý nhà cung cấp</h2>
        <Space>
          <Input
            placeholder="Tìm nhà cung cấp..."
            prefix={<SearchOutlined />}
            style={{ width: 250 }}
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            allowClear
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            Thêm nhà cung cấp
          </Button>
        </Space>
      </div>

      {/* Table */}
      <Table
        dataSource={filteredSuppliers}
        columns={columns}
        rowKey="id"
        loading={loading}
        pagination={{
          pageSize: 10,
          showSizeChanger: true,
          showTotal: (total) => `Tổng ${total} nhà cung cấp`,
        }}
        scroll={{ x: 1100 }}
      />

      {/* Form Modal */}
      <SupplierForm
        open={open}
        onCancel={() => {
          setOpen(false);
          setEditing(null);
        }}
        onSave={handleSave}
        supplier={editing}
      />
    </div>
  );
}
