import React, { useEffect, useState } from "react";
import {
  Table, message, Tag, Card, Statistic, Row, Col,
  Input, InputNumber, Select, Space, Tabs,
  Button, Modal, Alert, Descriptions
} from "antd";
import { WarningOutlined, SearchOutlined, DeleteOutlined } from "@ant-design/icons";
import { inventoryApi, inventoryLotApi } from "../services/api";
import api from "../services/api";
import { hasAnyRole, ROLES } from "../services/roleService";

const { TabPane } = Tabs;

export default function InventoryList() {
  const [activeTab, setActiveTab] = useState("overview");

  // Quyền xuất hủy: thủ kho hoặc admin (theo phân quyền nghiệp vụ kho)
  const canDispose = hasAnyRole([ROLES.WAREHOUSE_STAFF]);
  const currentUser = JSON.parse(localStorage.getItem("user") || "{}");

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

  // ==================== Xuất hủy lô ====================
  const [disposeLot, setDisposeLot] = useState(null); // lô đang xác nhận hủy
  const [disposeReason, setDisposeReason] = useState("");
  const [disposing, setDisposing] = useState(false);
  const [bulkDisposeOpen, setBulkDisposeOpen] = useState(false);
  const [bulkReason, setBulkReason] = useState("Hết hạn sử dụng");

  // ==================== Tab Lịch sử xuất hủy ====================
  const [disposals, setDisposals] = useState([]);
  const [disposalLoading, setDisposalLoading] = useState(false);
  const [disposalWarehouseFilter, setDisposalWarehouseFilter] = useState(null);

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

  useEffect(() => {
    if (activeTab === "disposal") fetchDisposals();
  }, [disposalWarehouseFilter, activeTab]);

  const fetchDisposals = async () => {
    setDisposalLoading(true);
    try {
      const res = await inventoryLotApi.getDisposals(disposalWarehouseFilter);
      setDisposals(res.data || []);
    } catch (err) {
      console.error(err);
      message.error("Không thể tải lịch sử xuất hủy");
    } finally {
      setDisposalLoading(false);
    }
  };

  // ==================== Xuất hủy handlers ====================
  const openDisposeModal = (lot) => {
    setDisposeLot(lot);
    setDisposeReason(lot.status === "EXPIRED" ? "Hết hạn sử dụng" : "");
  };

  const handleDisposeLot = async () => {
    if (!disposeReason.trim()) {
      message.warning("Vui lòng nhập lý do xuất hủy");
      return;
    }
    setDisposing(true);
    try {
      const res = await inventoryLotApi.disposeLot(disposeLot.id, {
        reason: disposeReason.trim(),
        disposedBy: currentUser.id,
      });
      message.success(
        `Đã lập phiếu xuất hủy ${res.data?.code || ""} cho lô ${disposeLot.lotNumber}`
      );
      setDisposeLot(null);
      fetchLots();
      fetchInventory();
    } catch (err) {
      console.error(err);
      message.error(err.message || "Xuất hủy thất bại");
    } finally {
      setDisposing(false);
    }
  };

  const handleBulkDispose = async () => {
    if (!bulkReason.trim()) {
      message.warning("Vui lòng nhập lý do xuất hủy");
      return;
    }
    setDisposing(true);
    try {
      // Hủy lần lượt đúng các lô đang hiển thị trong danh sách xác nhận
      // (tôn trọng bộ lọc kho/sản phẩm hiện tại), mỗi lô một phiếu xuất hủy
      let ok = 0;
      for (const lot of expiredLotsWithStock) {
        await inventoryLotApi.disposeLot(lot.id, {
          reason: bulkReason.trim(),
          disposedBy: currentUser.id,
        });
        ok += 1;
      }
      message.success(`Đã xuất hủy ${ok} lô hết hạn sử dụng`);
      setBulkDisposeOpen(false);
      fetchLots();
      fetchInventory();
    } catch (err) {
      console.error(err);
      message.error(err.message || "Xuất hủy thất bại");
    } finally {
      setDisposing(false);
    }
  };

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
    ...(canDispose
      ? [
          {
            title: "Thao tác",
            width: 120,
            align: "center",
            fixed: "right",
            render: (_, record) => {
              const qty = Number(record.quantityRemaining) || 0;
              if (qty <= 0) return <Tag>Đã xử lý</Tag>;
              return (
                <Button
                  danger
                  size="small"
                  icon={<DeleteOutlined />}
                  style={{ whiteSpace: "nowrap" }}
                  onClick={() => openDisposeModal(record)}
                >
                  Xuất hủy
                </Button>
              );
            },
          },
        ]
      : []),
  ];

  // Lô hết hạn còn tồn (theo bộ lọc kho hiện tại) — dùng cho cảnh báo và hủy hàng loạt
  const expiredLotsWithStock = lots.filter(
    (l) => l.status === "EXPIRED" && Number(l.quantityRemaining) > 0
  );

  // ==================== Cột bảng Lịch sử xuất hủy ====================
  const disposalColumns = [
    {
      title: "Mã phiếu",
      dataIndex: "code",
      width: 160,
      render: (text) => <strong>{text}</strong>,
    },
    {
      title: "Thời gian hủy",
      dataIndex: "disposedAt",
      width: 150,
      render: (val) => (val ? new Date(val).toLocaleString("vi-VN") : "-"),
      sorter: (a, b) => (a.disposedAt || "").localeCompare(b.disposedAt || ""),
    },
    { title: "Sản phẩm", dataIndex: "productName", ellipsis: true },
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
      title: "HSD",
      dataIndex: "expiryDate",
      width: 110,
      align: "center",
      render: (val) => val || "-",
    },
    {
      title: "SL hủy",
      dataIndex: "quantity",
      width: 90,
      align: "center",
      render: (val) =>
        val != null ? (
          <span style={{ color: "#ff4d4f", fontWeight: "bold" }}>
            {Number(val).toLocaleString("vi-VN")}
          </span>
        ) : (
          "-"
        ),
    },
    {
      title: "Giá trị hủy",
      width: 130,
      align: "right",
      render: (_, record) => {
        const value = (Number(record.quantity) || 0) * (record.unitCost || 0);
        return value ? `${value.toLocaleString("vi-VN")} ₫` : "-";
      },
    },
    { title: "Lý do", dataIndex: "reason", ellipsis: true },
    {
      title: "Người hủy",
      dataIndex: "disposedByName",
      width: 150,
      render: (val, record) => val || (record.disposedBy ? `#${record.disposedBy}` : "-"),
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
          {canDispose && expiredLotsWithStock.length > 0 && (
            <Alert
              type="error"
              showIcon
              style={{ marginBottom: 16 }}
              message={`Có ${expiredLotsWithStock.length} lô đã hết hạn sử dụng còn tồn kho (tổng ${expiredLotsWithStock
                .reduce((sum, l) => sum + (Number(l.quantityRemaining) || 0), 0)
                .toLocaleString("vi-VN")} đơn vị)`}
              description="Theo quy định quản lý hàng hóa có hạn sử dụng, hàng hết hạn phải được cách ly và lập phiếu xuất hủy, không được tiếp tục bán ra."
              action={
                <Button danger icon={<DeleteOutlined />} onClick={() => setBulkDisposeOpen(true)}>
                  Xuất hủy tất cả lô hết hạn
                </Button>
              }
            />
          )}
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
            scroll={{ x: 1450 }}
            rowClassName={(record) => {
              if (record.status === "EXPIRED") return "row-lot-expired";
              if (record.status === "EXPIRING_SOON") return "row-lot-expiring";
              return "";
            }}
          />
        </TabPane>

        {/* ==================== Tab Lịch sử xuất hủy ==================== */}
        <TabPane tab="Lịch sử xuất hủy" key="disposal">
          <div
            style={{
              display: "flex",
              justifyContent: "flex-end",
              alignItems: "center",
              marginBottom: 16,
            }}
          >
            <Select
              placeholder="Lọc theo kho"
              allowClear
              style={{ width: 180 }}
              value={disposalWarehouseFilter}
              onChange={setDisposalWarehouseFilter}
            >
              {warehouses.map((w) => (
                <Select.Option key={w.id} value={w.id}>
                  {w.name}
                </Select.Option>
              ))}
            </Select>
          </div>

          <Table
            dataSource={disposals}
            columns={disposalColumns}
            rowKey="id"
            loading={disposalLoading}
            pagination={{
              pageSize: 15,
              showSizeChanger: true,
              showTotal: (total) => `Tổng ${total} phiếu xuất hủy`,
            }}
            scroll={{ x: 1400 }}
          />
        </TabPane>
      </Tabs>

      {/* ==================== Modal xác nhận xuất hủy 1 lô ==================== */}
      <Modal
        title="Xác nhận xuất hủy lô hàng"
        open={!!disposeLot}
        onCancel={() => setDisposeLot(null)}
        onOk={handleDisposeLot}
        okText="Xác nhận xuất hủy"
        okButtonProps={{ danger: true, loading: disposing }}
        cancelText="Hủy bỏ"
      >
        {disposeLot && (
          <>
            <Alert
              type="warning"
              showIcon
              style={{ marginBottom: 16 }}
              message="Thao tác không thể hoàn tác"
              description="Toàn bộ số lượng còn lại của lô sẽ bị trừ khỏi tồn kho và ghi nhận vào sổ giao dịch (loại DISPOSAL) để đối soát."
            />
            <Descriptions column={1} size="small" bordered>
              <Descriptions.Item label="Sản phẩm">
                {disposeLot.productName} ({disposeLot.productCode})
              </Descriptions.Item>
              <Descriptions.Item label="Số lô">{disposeLot.lotNumber}</Descriptions.Item>
              <Descriptions.Item label="Kho">{disposeLot.warehouseName}</Descriptions.Item>
              <Descriptions.Item label="Hạn sử dụng">
                {disposeLot.expiryDate || "-"}{" "}
                {disposeLot.status === "EXPIRED" && <Tag color="red">Hết HSD</Tag>}
              </Descriptions.Item>
              <Descriptions.Item label="Số lượng hủy">
                <strong style={{ color: "#ff4d4f" }}>
                  {Number(disposeLot.quantityRemaining).toLocaleString("vi-VN")}
                </strong>
              </Descriptions.Item>
            </Descriptions>
            <div style={{ marginTop: 16 }}>
              <div style={{ marginBottom: 4 }}>
                Lý do xuất hủy <span style={{ color: "#ff4d4f" }}>*</span>
              </div>
              <Input.TextArea
                rows={2}
                maxLength={255}
                placeholder="VD: Hết hạn sử dụng / Hư hỏng do vận chuyển..."
                value={disposeReason}
                onChange={(e) => setDisposeReason(e.target.value)}
              />
            </div>
          </>
        )}
      </Modal>

      {/* ==================== Modal xuất hủy tất cả lô hết hạn ==================== */}
      <Modal
        title="Xuất hủy tất cả lô hết hạn sử dụng"
        open={bulkDisposeOpen}
        onCancel={() => setBulkDisposeOpen(false)}
        onOk={handleBulkDispose}
        okText={`Xác nhận hủy ${expiredLotsWithStock.length} lô`}
        okButtonProps={{ danger: true, loading: disposing }}
        cancelText="Hủy bỏ"
        width={680}
      >
        <Alert
          type="warning"
          showIcon
          style={{ marginBottom: 16 }}
          message="Thao tác không thể hoàn tác"
          description={`Hệ thống sẽ lập ${expiredLotsWithStock.length} phiếu xuất hủy (mỗi lô một phiếu) cho các lô trong danh sách dưới đây và trừ tồn kho tương ứng.`}
        />
        <Table
          dataSource={expiredLotsWithStock}
          rowKey="id"
          size="small"
          pagination={false}
          scroll={{ y: 240 }}
          columns={[
            { title: "Sản phẩm", dataIndex: "productName", ellipsis: true },
            { title: "Số lô", dataIndex: "lotNumber", width: 130 },
            { title: "Kho", dataIndex: "warehouseName", width: 130 },
            { title: "HSD", dataIndex: "expiryDate", width: 100, align: "center" },
            {
              title: "SL hủy",
              dataIndex: "quantityRemaining",
              width: 80,
              align: "center",
              render: (val) => (
                <span style={{ color: "#ff4d4f", fontWeight: "bold" }}>
                  {Number(val).toLocaleString("vi-VN")}
                </span>
              ),
            },
          ]}
        />
        <div style={{ marginTop: 16 }}>
          <div style={{ marginBottom: 4 }}>
            Lý do xuất hủy <span style={{ color: "#ff4d4f" }}>*</span>
          </div>
          <Input.TextArea
            rows={2}
            maxLength={255}
            value={bulkReason}
            onChange={(e) => setBulkReason(e.target.value)}
          />
        </div>
      </Modal>

      <style>{`
        .row-out-of-stock { background-color: #fff2f0; }
        .row-low-stock { background-color: #fffbe6; }
        .row-lot-expired { background-color: #fff2f0; }
        .row-lot-expiring { background-color: #fffbe6; }
      `}</style>
    </div>
  );
}
