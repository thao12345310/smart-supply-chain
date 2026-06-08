import React, { useEffect, useState } from "react";
import {
  Table, message, Tag, Card, Statistic, Row, Col,
  Input, InputNumber, Select, Space, Tabs
} from "antd";
import { WarningOutlined, SearchOutlined } from "@ant-design/icons";
import { inventoryApi, inventoryLotApi } from "../services/api";
import api from "../services/api";

const { TabPane } = Tabs;

export default function InventoryList() {
  const [activeTab, setActiveTab] = useState("overview");

  // ==================== Tab Tổng quan ====================
  const [inventory, setInventory] = useState([]);
  const [loading, setLoading] = useState(false);
  const [warehouses, setWarehouses] = useState([]);
  const [warehouseFilter, setWarehouseFilter] = useState(null);
  const [searchText, setSearchText] = useState("");
  const [stats, setStats] = useState({
    total: 0,
    lowStock: 0,
    outOfStock: 0,
    totalValue: 0,
  });

  // ==================== Tab Theo lô ====================
  const [lots, setLots] = useState([]);
  const [lotLoading, setLotLoading] = useState(false);
  const [products, setProducts] = useState([]);
  const [lotProductFilter, setLotProductFilter] = useState(null);
  const [lotWarehouseFilter, setLotWarehouseFilter] = useState(null);
  const [lotStatusFilter, setLotStatusFilter] = useState(null);
  const [lotSearchText, setLotSearchText] = useState("");

  const fetchWarehouses = async () => {
    try {
      const res = await api.get("/warehouses");
      setWarehouses(res.data || []);
    } catch (err) {
      console.error(err);
    }
  };

  const fetchProducts = async () => {
    try {
      const res = await api.get("/products");
      setProducts(res.data || []);
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
      setStats({
        total: data.length,
        // Dùng chung getStockStatus để thẻ thống kê khớp với trạng thái trong bảng
        lowStock: data.filter((i) => getStockStatus(i).priority === 2).length,
        outOfStock: data.filter((i) => getStockStatus(i).priority === 3).length,
        totalValue: data.reduce(
          (sum, i) => sum + (i.averageCost || 0) * (i.quantityOnHand || 0),
          0
        ),
      });
    } catch (err) {
      console.error(err);
      message.error("Không thể tải danh sách tồn kho");
    } finally {
      setLoading(false);
    }
  };

  const fetchLots = async () => {
    setLotLoading(true);
    try {
      const params = {};
      if (lotProductFilter) params.productId = lotProductFilter;
      if (lotWarehouseFilter) params.warehouseId = lotWarehouseFilter;
      const res = await inventoryLotApi.getAll(params);
      setLots(res.data || []);
    } catch (err) {
      console.error(err);
      message.error("Không thể tải danh sách lô hàng");
    } finally {
      setLotLoading(false);
    }
  };

  useEffect(() => {
    fetchWarehouses();
    fetchProducts();
  }, []);

  useEffect(() => {
    if (activeTab === "overview") fetchInventory();
  }, [warehouseFilter, activeTab]);

  useEffect(() => {
    if (activeTab === "lot") fetchLots();
  }, [lotProductFilter, lotWarehouseFilter, activeTab]);

  // ==================== Tổng quan helpers ====================
  const getStockStatus = (record) => {
    const available = record.quantityAvailable || 0;
    const reorderLevel = record.reorderLevel;
    if (available <= 0) return { color: "red", label: "Hết hàng", priority: 3 };
    // Chỉ cảnh báo "Sắp hết" khi sản phẩm đã được cấu hình Mức cảnh báo (reorderLevel)
    if (reorderLevel != null && available <= reorderLevel)
      return { color: "orange", label: "Sắp hết", priority: 2 };
    return { color: "green", label: "Đủ hàng", priority: 1 };
  };

  const handleReorderLevelChange = async (record, value) => {
    const newVal = value === "" || value == null ? null : Number(value);
    if (newVal === (record.reorderLevel ?? null)) return;
    try {
      await inventoryApi.updateReorderLevel(record.id, newVal);
      message.success("Đã cập nhật mức cảnh báo");
      fetchInventory();
    } catch (err) {
      console.error(err);
      message.error("Cập nhật mức cảnh báo thất bại");
    }
  };

  const filteredInventory = inventory.filter((item) => {
    if (!searchText) return true;
    const s = searchText.toLowerCase();
    return (
      (item.productName || "").toLowerCase().includes(s) ||
      (item.productCode || "").toLowerCase().includes(s)
    );
  });

  // ==================== Theo lô helpers ====================
  const getLotStatusTag = (status) => {
    if (status === "EXPIRED") return <Tag color="red">Hết HSD</Tag>;
    if (status === "EXPIRING_SOON") return <Tag color="orange">Sắp hết HSD</Tag>;
    return <Tag color="green">Còn hạn</Tag>;
  };

  const filteredLots = lots.filter((lot) => {
    if (lotStatusFilter && lot.status !== lotStatusFilter) return false;
    if (!lotSearchText) return true;
    const s = lotSearchText.toLowerCase();
    return (
      (lot.productName || "").toLowerCase().includes(s) ||
      (lot.productCode || "").toLowerCase().includes(s) ||
      (lot.lotNumber || "").toLowerCase().includes(s)
    );
  });

  // ==================== Cột bảng Tổng quan ====================
  const overviewColumns = [
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
    { title: "Kho", dataIndex: "warehouseName", width: 140 },
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
        const color =
          status.color === "red"
            ? "#ff4d4f"
            : status.color === "orange"
            ? "#faad14"
            : "#52c41a";
        return <span style={{ color, fontWeight: "bold" }}>{val || 0}</span>;
      },
    },
    {
      title: "Trạng thái",
      width: 110,
      render: (_, record) => {
        const s = getStockStatus(record);
        return <Tag color={s.color}>{s.label}</Tag>;
      },
      sorter: (a, b) => getStockStatus(a).priority - getStockStatus(b).priority,
    },
    {
      title: "Mức cảnh báo",
      dataIndex: "reorderLevel",
      width: 130,
      align: "center",
      render: (val, record) => (
        <InputNumber
          key={`${record.id}-${val ?? ""}`}
          min={0}
          defaultValue={val}
          placeholder="-"
          style={{ width: 90 }}
          onPressEnter={(e) => e.target.blur()}
          onBlur={(e) => handleReorderLevelChange(record, e.target.value)}
        />
      ),
    },
    {
      title: "Giá TB",
      dataIndex: "averageCost",
      width: 120,
      align: "right",
      render: (val) => (val ? `${val.toLocaleString("vi-VN")} ₫` : "-"),
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

  // ==================== Cột bảng Theo lô ====================
  const lotColumns = [
    {
      title: "Sản phẩm",
      dataIndex: "productName",
      ellipsis: true,
      sorter: (a, b) => (a.productName || "").localeCompare(b.productName || ""),
    },
    {
      title: "Mã SP",
      dataIndex: "productCode",
      width: 100,
      render: (text) => <strong>{text || "-"}</strong>,
    },
    { title: "Kho", dataIndex: "warehouseName", width: 140 },
    {
      title: "Số lô",
      dataIndex: "lotNumber",
      width: 130,
      render: (val) => <strong>{val || "-"}</strong>,
    },
    {
      title: "Ngày SX",
      dataIndex: "manufactureDate",
      width: 110,
      align: "center",
      render: (val) => val || "-",
    },
    {
      title: "HSD",
      dataIndex: "expiryDate",
      width: 110,
      align: "center",
      defaultSortOrder: "ascend",
      sorter: (a, b) => {
        if (!a.expiryDate) return 1;
        if (!b.expiryDate) return -1;
        return a.expiryDate.localeCompare(b.expiryDate);
      },
      render: (val) => val || "-",
    },
    {
      title: "SL nhập",
      dataIndex: "quantityReceived",
      width: 90,
      align: "center",
      render: (val) => (val != null ? Number(val).toLocaleString("vi-VN") : "-"),
    },
    {
      title: "SL còn",
      dataIndex: "quantityRemaining",
      width: 90,
      align: "center",
      render: (val, record) => {
        const qty = val != null ? Number(val) : 0;
        const color =
          record.status === "EXPIRED"
            ? "#ff4d4f"
            : record.status === "EXPIRING_SOON"
            ? "#faad14"
            : "#52c41a";
        return <span style={{ color, fontWeight: "bold" }}>{qty.toLocaleString("vi-VN")}</span>;
      },
    },
    {
      title: "Còn lại (ngày)",
      dataIndex: "daysUntilExpiry",
      width: 120,
      align: "center",
      render: (val) => {
        if (val == null) return "-";
        if (val < 0) return <span style={{ color: "#ff4d4f" }}>{val}</span>;
        if (val <= 30) return <span style={{ color: "#faad14" }}>{val}</span>;
        return <span style={{ color: "#52c41a" }}>{val}</span>;
      },
    },
    {
      title: "Trạng thái",
      dataIndex: "status",
      width: 130,
      render: (val) => getLotStatusTag(val),
      sorter: (a, b) => {
        const order = { EXPIRED: 0, EXPIRING_SOON: 1, FRESH: 2 };
        return (order[a.status] ?? 3) - (order[b.status] ?? 3);
      },
    },
  ];

  return (
    <div style={{ padding: 20 }}>
      {/* Thống kê tổng quan */}
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
              valueStyle={{ color: "#faad14" }}
              prefix={<WarningOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="Hết hàng"
              value={stats.outOfStock}
              valueStyle={{ color: "#ff4d4f" }}
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

      <h2 style={{ marginBottom: 16 }}>Quản lý tồn kho</h2>

      <Tabs activeKey={activeTab} onChange={setActiveTab}>
        {/* ==================== Tab Tổng quan ==================== */}
        <TabPane tab="Tổng quan" key="overview">
          <div
            style={{
              display: "flex",
              justifyContent: "flex-end",
              alignItems: "center",
              marginBottom: 16,
            }}
          >
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
                  <Select.Option key={w.id} value={w.id}>
                    {w.name}
                  </Select.Option>
                ))}
              </Select>
            </Space>
          </div>

          <Table
            dataSource={filteredInventory}
            columns={overviewColumns}
            rowKey="id"
            loading={loading}
            pagination={{
              pageSize: 15,
              showSizeChanger: true,
              showTotal: (total) => `Tổng ${total} sản phẩm`,
            }}
            scroll={{ x: 1200 }}
            rowClassName={(record) => {
              const s = getStockStatus(record);
              if (s.priority === 3) return "row-out-of-stock";
              if (s.priority === 2) return "row-low-stock";
              return "";
            }}
          />
        </TabPane>

        {/* ==================== Tab Theo lô ==================== */}
        <TabPane tab="Theo lô" key="lot">
          <div
            style={{
              display: "flex",
              justifyContent: "flex-end",
              alignItems: "center",
              marginBottom: 16,
            }}
          >
            <Space>
              <Input
                placeholder="Tìm sản phẩm / số lô..."
                prefix={<SearchOutlined />}
                style={{ width: 220 }}
                value={lotSearchText}
                onChange={(e) => setLotSearchText(e.target.value)}
                allowClear
              />
              <Select
                placeholder="Lọc sản phẩm"
                allowClear
                showSearch
                optionFilterProp="children"
                style={{ width: 200 }}
                value={lotProductFilter}
                onChange={setLotProductFilter}
              >
                {products.map((p) => (
                  <Select.Option key={p.id} value={p.id}>
                    {p.name}
                  </Select.Option>
                ))}
              </Select>
              <Select
                placeholder="Lọc theo kho"
                allowClear
                style={{ width: 160 }}
                value={lotWarehouseFilter}
                onChange={setLotWarehouseFilter}
              >
                {warehouses.map((w) => (
                  <Select.Option key={w.id} value={w.id}>
                    {w.name}
                  </Select.Option>
                ))}
              </Select>
              <Select
                placeholder="Trạng thái HSD"
                allowClear
                style={{ width: 150 }}
                value={lotStatusFilter}
                onChange={setLotStatusFilter}
              >
                <Select.Option value="FRESH">Còn hạn</Select.Option>
                <Select.Option value="EXPIRING_SOON">Sắp hết HSD</Select.Option>
                <Select.Option value="EXPIRED">Hết HSD</Select.Option>
              </Select>
            </Space>
          </div>

          <Table
            dataSource={filteredLots}
            columns={lotColumns}
            rowKey="id"
            loading={lotLoading}
            pagination={{
              pageSize: 15,
              showSizeChanger: true,
              showTotal: (total) => `Tổng ${total} lô hàng`,
            }}
            scroll={{ x: 1300 }}
            rowClassName={(record) => {
              if (record.status === "EXPIRED") return "row-lot-expired";
              if (record.status === "EXPIRING_SOON") return "row-lot-expiring";
              return "";
            }}
          />
        </TabPane>
      </Tabs>

      <style>{`
        .row-out-of-stock { background-color: #fff2f0; }
        .row-low-stock { background-color: #fffbe6; }
        .row-lot-expired { background-color: #fff2f0; }
        .row-lot-expiring { background-color: #fffbe6; }
      `}</style>
    </div>
  );
}
