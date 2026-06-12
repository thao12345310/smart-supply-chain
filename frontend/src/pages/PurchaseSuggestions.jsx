import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import {
  Table, Button, Select, Tag, message, Collapse, InputNumber, Empty, Spin, Space,
} from "antd";
import { ShoppingCartOutlined, ReloadOutlined } from "@ant-design/icons";
import { inventoryApi, warehouseApi } from "../services/api";

const rowKeyOf = (item) => `${item.productId}-${item.warehouseId}`;

export default function PurchaseSuggestions() {
  const navigate = useNavigate();
  const [groups, setGroups] = useState([]);
  const [loading, setLoading] = useState(false);
  const [warehouses, setWarehouses] = useState([]);
  const [warehouseFilter, setWarehouseFilter] = useState(null);
  // SL mua người dùng đã chỉnh, key theo rowKey
  const [qtyMap, setQtyMap] = useState({});
  // Các dòng được tick chọn, key theo supplier group index
  const [selectedMap, setSelectedMap] = useState({});

  useEffect(() => {
    warehouseApi.getAll()
      .then((res) => setWarehouses(res.data || []))
      .catch(() => message.error("Không thể tải danh sách kho"));
  }, []);

  useEffect(() => {
    fetchSuggestions();
  }, [warehouseFilter]);

  const fetchSuggestions = async () => {
    setLoading(true);
    try {
      const res = await inventoryApi.getPurchaseSuggestions(warehouseFilter);
      const data = res.data || [];
      setGroups(data);
      setQtyMap({});
      // Mặc định chọn toàn bộ mặt hàng trong từng nhóm
      const initial = {};
      data.forEach((g, idx) => {
        initial[idx] = (g.items || []).map(rowKeyOf);
      });
      setSelectedMap(initial);
    } catch (err) {
      console.error(err);
      message.error("Không thể tải đề xuất mua hàng");
    } finally {
      setLoading(false);
    }
  };

  const getQuantity = (item) =>
    qtyMap[rowKeyOf(item)] ?? item.suggestedQuantity ?? 1;

  const handleCreatePO = (group, groupIndex) => {
    const selectedKeys = selectedMap[groupIndex] || [];
    const selected = (group.items || []).filter((i) =>
      selectedKeys.includes(rowKeyOf(i))
    );
    if (selected.length === 0) {
      message.warning("Chọn ít nhất một mặt hàng để tạo đơn mua");
      return;
    }

    // Cùng sản phẩm thiếu ở nhiều kho → gộp 1 dòng, cộng dồn số lượng
    const merged = new Map();
    selected.forEach((item) => {
      const existing = merged.get(item.productId);
      if (existing) {
        existing.quantity += getQuantity(item);
      } else {
        merged.set(item.productId, {
          productId: item.productId,
          productCode: item.productCode,
          productName: item.productName,
          quantity: getQuantity(item),
          unitPrice: item.unitPrice || 0,
        });
      }
    });

    // Tất cả cùng 1 kho thì điền sẵn kho nhận hàng
    const warehouseIds = [...new Set(selected.map((i) => i.warehouseId))];
    const prefill = {
      supplierId: group.supplierId ?? undefined,
      warehouseId: warehouseIds.length === 1 ? warehouseIds[0] : undefined,
      notes: "Tạo từ đề xuất mua hàng (hàng dưới mức cảnh báo / hết hàng)",
      items: [...merged.values()],
    };
    navigate("/purchase-orders/new", { state: { prefill } });
  };

  const buildColumns = () => [
    {
      title: "Mã SP",
      dataIndex: "productCode",
      width: 110,
      render: (text) => <strong>{text || "-"}</strong>,
    },
    { title: "Tên sản phẩm", dataIndex: "productName", ellipsis: true },
    { title: "Kho", dataIndex: "warehouseName", width: 150 },
    {
      title: "Tồn kho",
      dataIndex: "quantityOnHand",
      width: 90,
      align: "center",
    },
    {
      title: "Khả dụng",
      dataIndex: "quantityAvailable",
      width: 95,
      align: "center",
      render: (val) => (
        <span style={{ color: val > 0 ? "#faad14" : "#ff4d4f", fontWeight: "bold" }}>
          {val}
        </span>
      ),
    },
    {
      title: "Hết hạn chờ hủy",
      dataIndex: "quantityExpired",
      width: 125,
      align: "center",
      render: (val) =>
        val > 0 ? <span style={{ color: "#ff4d4f" }}>{val}</span> : 0,
    },
    {
      title: "Mức cảnh báo",
      dataIndex: "reorderLevel",
      width: 115,
      align: "center",
      render: (val) => val ?? "-",
    },
    {
      title: "SL mua",
      width: 120,
      align: "center",
      render: (_, item) => (
        <InputNumber
          min={1}
          value={getQuantity(item)}
          onChange={(val) =>
            setQtyMap((prev) => ({ ...prev, [rowKeyOf(item)]: val || 1 }))
          }
          style={{ width: 90 }}
        />
      ),
    },
    {
      title: "Đơn giá",
      dataIndex: "unitPrice",
      width: 120,
      align: "right",
      render: (val) => (val ? `${val.toLocaleString("vi-VN")} ₫` : "-"),
    },
    {
      title: "Thành tiền",
      width: 130,
      align: "right",
      render: (_, item) => {
        const amount = (item.unitPrice || 0) * getQuantity(item);
        return <strong>{amount.toLocaleString("vi-VN")} ₫</strong>;
      },
    },
  ];

  const totalItems = groups.reduce((sum, g) => sum + (g.itemCount || 0), 0);

  const collapseItems = groups.map((group, idx) => {
    const selectedCount = (selectedMap[idx] || []).length;
    return {
      key: String(idx),
      label: (
        <Space>
          <strong>{group.supplierName || "Chưa có nhà cung cấp"}</strong>
          <Tag color={group.supplierName ? "blue" : "default"}>
            {group.itemCount} mặt hàng cần mua
          </Tag>
        </Space>
      ),
      extra: (
        <Button
          type="primary"
          size="small"
          icon={<ShoppingCartOutlined />}
          disabled={selectedCount === 0}
          onClick={(e) => {
            e.stopPropagation();
            handleCreatePO(group, idx);
          }}
        >
          Tạo đơn mua ({selectedCount})
        </Button>
      ),
      children: (
        <Table
          dataSource={group.items}
          columns={buildColumns()}
          rowKey={rowKeyOf}
          size="small"
          pagination={false}
          scroll={{ x: 1100 }}
          rowSelection={{
            selectedRowKeys: selectedMap[idx] || [],
            onChange: (keys) =>
              setSelectedMap((prev) => ({ ...prev, [idx]: keys })),
          }}
        />
      ),
    };
  });

  return (
    <div style={{ padding: 24 }}>
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginBottom: 16,
        }}
      >
        <h1 style={{ fontSize: 22, fontWeight: 700, margin: 0 }}>
          Đề xuất mua hàng
        </h1>
        <Space>
          <Select
            allowClear
            placeholder="Tất cả kho"
            style={{ width: 220 }}
            value={warehouseFilter}
            onChange={(val) => setWarehouseFilter(val ?? null)}
            options={warehouses.map((w) => ({ value: w.id, label: w.name }))}
          />
          <Button icon={<ReloadOutlined />} onClick={fetchSuggestions}>
            Làm mới
          </Button>
        </Space>
      </div>

      <p style={{ color: "#888", marginBottom: 16 }}>
        Các mặt hàng đang hết hàng hoặc khả dụng dưới mức cảnh báo (đã trừ hàng
        hết hạn chờ hủy), gom theo nhà cung cấp. Tick chọn mặt hàng, chỉnh số
        lượng rồi bấm "Tạo đơn mua" — đơn mua sẽ được điền sẵn thông tin.
      </p>

      {loading ? (
        <div style={{ textAlign: "center", padding: 48 }}>
          <Spin size="large" />
        </div>
      ) : groups.length === 0 ? (
        <Empty description="Không có mặt hàng nào cần mua — tồn kho đang ổn" />
      ) : (
        <>
          <div style={{ marginBottom: 12, color: "#555" }}>
            <strong>{groups.length}</strong> nhà cung cấp ·{" "}
            <strong>{totalItems}</strong> mặt hàng cần mua
          </div>
          <Collapse
            items={collapseItems}
            defaultActiveKey={groups.map((_, idx) => String(idx))}
          />
        </>
      )}
    </div>
  );
}
