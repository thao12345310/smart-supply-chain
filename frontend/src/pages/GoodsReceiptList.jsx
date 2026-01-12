import React, { useEffect, useState } from "react";
import { Table, Button, message, Tag, Space, Select, Card, Statistic, Row, Col } from "antd";
import { PlusOutlined, CheckCircleOutlined, ClockCircleOutlined } from "@ant-design/icons";
import { goodsReceiptApi } from "../services/api";
import dayjs from "dayjs";
import { useNavigate } from "react-router-dom";

// Status configuration
const STATUS_CONFIG = {
  DRAFT: { color: "blue", label: "Nháp", icon: <ClockCircleOutlined /> },
  CONFIRMED: { color: "green", label: "Đã xác nhận", icon: <CheckCircleOutlined /> },
  CANCELLED: { color: "default", label: "Đã hủy", icon: null },
};

export default function GoodsReceiptList() {
  const [receipts, setReceipts] = useState([]);
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [statusFilter, setStatusFilter] = useState(null);
  const [stats, setStats] = useState({ total: 0, draft: 0, confirmed: 0 });

  const fetchReceipts = async () => {
    setLoading(true);
    try {
      let res;
      if (statusFilter) {
        res = await goodsReceiptApi.getByStatus(statusFilter);
      } else {
        res = await goodsReceiptApi.getAll();
      }
      const data = res.data || [];
      setReceipts(data);
      
      if (!statusFilter) {
        setStats({
          total: data.length,
          draft: data.filter(r => r.status === 'DRAFT').length,
          confirmed: data.filter(r => r.status === 'CONFIRMED').length,
        });
      }
    } catch (err) {
      console.error(err);
      message.error("Không thể tải danh sách phiếu nhập");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReceipts();
  }, [statusFilter]);

  const handleConfirm = async (id, e) => {
    e.stopPropagation();
    try {
      await goodsReceiptApi.confirm(id);
      message.success("Xác nhận phiếu nhập thành công!");
      fetchReceipts();
    } catch (err) {
      message.error(err.message || "Không thể xác nhận phiếu nhập");
    }
  };

  const handleCancel = async (id, e) => {
    e.stopPropagation();
    try {
      await goodsReceiptApi.cancel(id);
      message.success("Đã hủy phiếu nhập");
      fetchReceipts();
    } catch (err) {
      message.error(err.message || "Không thể hủy phiếu nhập");
    }
  };

  const handleDelete = async (id, e) => {
    e.stopPropagation();
    try {
      await goodsReceiptApi.delete(id);
      message.success("Xóa phiếu nhập thành công");
      fetchReceipts();
    } catch (err) {
      message.error(err.message || "Không thể xóa phiếu nhập");
    }
  };

  const columns = [
    {
      title: "Mã phiếu nhập",
      dataIndex: "code",
      width: 150,
      render: (text) => <strong>{text || "-"}</strong>,
    },
    {
      title: "Mã đơn hàng",
      dataIndex: "purchaseOrderCode",
      width: 150,
      render: (text, record) => (
        <a onClick={(e) => {
          e.stopPropagation();
          navigate(`/purchase-orders/${record.purchaseOrderId}`);
        }}>
          {text}
        </a>
      ),
    },
    {
      title: "Kho nhập",
      dataIndex: "warehouseName",
      width: 150,
    },
    {
      title: "Ngày nhận",
      dataIndex: "receiptDate",
      width: 120,
      render: (text) => (text ? dayjs(text).format("DD/MM/YYYY") : "-"),
    },
    {
      title: "Trạng thái",
      dataIndex: "status",
      width: 130,
      render: (status) => {
        const config = STATUS_CONFIG[status] || { color: "default", label: status };
        return <Tag color={config.color}>{config.label}</Tag>;
      },
    },
    {
      title: "SL nhận",
      dataIndex: "totalReceivedQuantity",
      width: 90,
      align: "center",
    },
    {
      title: "SL nhập kho",
      dataIndex: "totalAcceptedQuantity",
      width: 100,
      align: "center",
      render: (val) => <span style={{ color: '#52c41a' }}>{val || 0}</span>,
    },
    {
      title: "Tổng tiền",
      dataIndex: "totalAmount",
      width: 130,
      align: "right",
      render: (val) => val ? `${val.toLocaleString("vi-VN")} ₫` : "-",
    },
    {
      title: "Thao tác",
      width: 200,
      render: (_, record) => (
        <Space size="small">
          {record.status === 'DRAFT' && (
            <>
              <Button 
                size="small" 
                type="primary"
                onClick={(e) => handleConfirm(record.id, e)}
              >
                Xác nhận
              </Button>
              <Button 
                size="small" 
                onClick={(e) => handleCancel(record.id, e)}
              >
                Hủy
              </Button>
              <Button 
                size="small" 
                danger 
                onClick={(e) => handleDelete(record.id, e)}
              >
                Xóa
              </Button>
            </>
          )}
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
            <Statistic title="Tổng phiếu nhập" value={stats.total} />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic 
              title="Chờ xác nhận" 
              value={stats.draft} 
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic 
              title="Đã xác nhận" 
              value={stats.confirmed} 
              valueStyle={{ color: '#52c41a' }}
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
        <h2 style={{ margin: 0 }}>Quản lý phiếu nhập kho</h2>
        <Space>
          <Select
            placeholder="Lọc theo trạng thái"
            allowClear
            style={{ width: 180 }}
            value={statusFilter}
            onChange={setStatusFilter}
          >
            <Select.Option value="DRAFT">Nháp</Select.Option>
            <Select.Option value="CONFIRMED">Đã xác nhận</Select.Option>
            <Select.Option value="CANCELLED">Đã hủy</Select.Option>
          </Select>
        </Space>
      </div>
      
      {/* Table */}
      <Table
        dataSource={receipts}
        columns={columns}
        rowKey="id"
        loading={loading}
        pagination={{
          pageSize: 10,
          showSizeChanger: true,
          showTotal: (total) => `Tổng ${total} phiếu nhập`,
        }}
        scroll={{ x: 1200 }}
        onRow={(record) => ({
          onClick: () => navigate(`/goods-receipts/${record.id}`),
          style: { cursor: "pointer" },
        })}
      />
    </div>
  );
}
