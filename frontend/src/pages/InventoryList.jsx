import React, { useEffect, useState } from "react";
import { Table, message, Tag, Card, Statistic, Row, Col, Input, Select, Space, Progress } from "antd";
import { WarningOutlined, CheckCircleOutlined, SearchOutlined } from "@ant-design/icons";
import { inventoryApi } from "../services/api";
import api from "../services/api";

export default function InventoryList() {
  const [inventory, setInventory] = useState([]);
  const [loading, setLoading] = useState(false);
  const [warehouses, setWarehouses] = useState([]);
  const [warehouseFilter, setWarehouseFilter] = useState(null);
  const [searchText, setSearchText] = useState("");
  const [stats, setStats] = useState({ 
    total: 0, 
    lowStock: 0, 
    outOfStock: 0,
    totalValue: 0
  });

  const fetchWarehouses = async () => {
    try {
      const res = await api.get("/warehouses");
      setWarehouses(res.data || []);
    } catch (err) {
      console.error(err);
    }
  };

  const fetchInventory = async () => {
    setLoading(true);
    try {
      let res;
      if (warehouseFilter) {
        res = await inventoryApi.getByWarehouse(warehouseFilter);
      } else {
        res = await inventoryApi.getAll();
      }
      const data = res.data || [];
      setInventory(data);
      
      // Calculate stats
      setStats({
        total: data.length,
        lowStock: data.filter(i => i.quantityAvailable > 0 && i.quantityAvailable <= 10).length,
        outOfStock: data.filter(i => i.quantityAvailable <= 0).length,
        totalValue: data.reduce((sum, i) => sum + ((i.averageCost || 0) * (i.quantityOnHand || 0)), 0),
      });
    } catch (err) {
      console.error(err);
      message.error("Không thể tải danh sách tồn kho");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchWarehouses();
  }, []);

  useEffect(() => {
    fetchInventory();
  }, [warehouseFilter]);

  const getStockStatus = (record) => {
    const available = record.quantityAvailable || 0;
    const reorderLevel = record.reorderLevel || 10;
    
    if (available <= 0) {
      return { color: "red", label: "Hết hàng", priority: 3 };
    } else if (available <= reorderLevel) {
      return { color: "orange", label: "Sắp hết", priority: 2 };
    } else {
      return { color: "green", label: "Đủ hàng", priority: 1 };
    }
  };

  const filteredInventory = inventory.filter(item => {
    if (!searchText) return true;
    const search = searchText.toLowerCase();
    return (
      (item.productName || "").toLowerCase().includes(search) ||
      (item.productCode || "").toLowerCase().includes(search)
    );
  });

  const columns = [
    {
      title: "Mã SP",
      dataIndex: "productCode",
      width: 120,
      render: (text) => <strong>{text || "-"}</strong>,
      sorter: (a, b) => (a.productCode || "").localeCompare(b.productCode || ""),
    },
    {
      title: "Tên sản phẩm",
      dataIndex: "productName",
      ellipsis: true,
      sorter: (a, b) => (a.productName || "").localeCompare(b.productName || ""),
    },
    {
      title: "Kho",
      dataIndex: "warehouseName",
      width: 140,
    },
    {
      title: "Tồn kho",
      dataIndex: "quantityOnHand",
      width: 100,
      align: "center",
      sorter: (a, b) => (a.quantityOnHand || 0) - (b.quantityOnHand || 0),
      render: (val) => <strong>{val || 0}</strong>,
    },
    {
      title: "Đã đặt",
      dataIndex: "quantityReserved",
      width: 90,
      align: "center",
      render: (val) => val || 0,
    },
    {
      title: "Khả dụng",
      dataIndex: "quantityAvailable",
      width: 100,
      align: "center",
      sorter: (a, b) => (a.quantityAvailable || 0) - (b.quantityAvailable || 0),
      render: (val, record) => {
        const status = getStockStatus(record);
        return <span style={{ color: status.color === 'red' ? '#ff4d4f' : status.color === 'orange' ? '#faad14' : '#52c41a', fontWeight: 'bold' }}>{val || 0}</span>;
      },
    },
    {
      title: "Trạng thái",
      width: 110,
      render: (_, record) => {
        const status = getStockStatus(record);
        return <Tag color={status.color}>{status.label}</Tag>;
      },
      sorter: (a, b) => getStockStatus(a).priority - getStockStatus(b).priority,
    },
    {
      title: "Mức cảnh báo",
      dataIndex: "reorderLevel",
      width: 110,
      align: "center",
      render: (val) => val || "-",
    },
    {
      title: "Giá TB",
      dataIndex: "averageCost",
      width: 120,
      align: "right",
      render: (val) => val ? `${val.toLocaleString("vi-VN")} ₫` : "-",
    },
    {
      title: "Giá trị tồn",
      width: 140,
      align: "right",
      render: (_, record) => {
        const value = (record.averageCost || 0) * (record.quantityOnHand || 0);
        return value ? `${value.toLocaleString("vi-VN")} ₫` : "-";
      },
    },
  ];

  return (
    <div style={{ padding: 20 }}>
      {/* Statistics Cards */}
      <Row gutter={16} style={{ marginBottom: 20 }}>
        <Col span={6}>
          <Card>
            <Statistic title="Tổng sản phẩm" value={stats.total} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic 
              title="Sắp hết hàng" 
              value={stats.lowStock} 
              valueStyle={{ color: '#faad14' }}
              prefix={<WarningOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic 
              title="Hết hàng" 
              value={stats.outOfStock} 
              valueStyle={{ color: '#ff4d4f' }}
              prefix={<WarningOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic 
              title="Tổng giá trị tồn" 
              value={stats.totalValue} 
              suffix="₫"
              formatter={(val) => val.toLocaleString("vi-VN")}
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
        <h2 style={{ margin: 0 }}>Quản lý tồn kho</h2>
        <Space>
          <Input
            placeholder="Tìm sản phẩm..."
            prefix={<SearchOutlined />}
            style={{ width: 250 }}
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            allowClear
          />
          <Select
            placeholder="Lọc theo kho"
            allowClear
            style={{ width: 180 }}
            value={warehouseFilter}
            onChange={setWarehouseFilter}
          >
            {warehouses.map((w) => (
              <Select.Option key={w.id} value={w.id}>{w.name}</Select.Option>
            ))}
          </Select>
        </Space>
      </div>
      
      {/* Table */}
      <Table
        dataSource={filteredInventory}
        columns={columns}
        rowKey="id"
        loading={loading}
        pagination={{
          pageSize: 15,
          showSizeChanger: true,
          showTotal: (total) => `Tổng ${total} sản phẩm`,
        }}
        scroll={{ x: 1200 }}
        rowClassName={(record) => {
          const status = getStockStatus(record);
          if (status.priority === 3) return 'row-out-of-stock';
          if (status.priority === 2) return 'row-low-stock';
          return '';
        }}
      />

      <style>{`
        .row-out-of-stock {
          background-color: #fff2f0;
        }
        .row-low-stock {
          background-color: #fffbe6;
        }
      `}</style>
    </div>
  );
}
