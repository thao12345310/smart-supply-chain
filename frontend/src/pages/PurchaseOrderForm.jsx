import React, { useEffect, useState } from "react";
import {
  Form,
  Input,
  Select,
  DatePicker,
  InputNumber,
  Button,
  Table,
  message,
  Divider,
  Modal,
} from "antd";
import api from "../services/api";
import dayjs from "dayjs";

export default function PurchaseOrderForm({ open, onCancel, onSave, order }) {
  const [form] = Form.useForm();
  const [suppliers, setSuppliers] = useState([]);
  const [warehouses, setWarehouses] = useState([]);
  const [products, setProducts] = useState([]);
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(false);

  // ------------------------------
  // Fetch dữ liệu master
  // ------------------------------
  const fetchSuppliers = async () => {
    const res = await api.get("/suppliers");
    setSuppliers(res.data);
  };

  const fetchWarehouses = async () => {
    const res = await api.get("/warehouses");
    setWarehouses(res.data);
  };

  const fetchProducts = async () => {
    const res = await api.get("/products");
    setProducts(res.data);
  };

  useEffect(() => {
    fetchSuppliers();
    fetchWarehouses();
    fetchProducts();
  }, []);

  // Load dữ liệu khi edit
  useEffect(() => {
    if (order && open) {
      form.setFieldsValue({
        supplierId: order.supplier?.id || order.supplierId,
        warehouseId: order.warehouse?.id || order.warehouseId,
        orderName: order.orderName,
        shippingCost: order.shippingCost,
        taxType: order.taxType,
        deliveryDate: order.deliveryDate ? dayjs(order.deliveryDate) : null,
      });

      if (order.items && order.items.length > 0) {
        setItems(
          order.items.map((item) => ({
            productId: item.product?.id || item.productId,
            productCode: item.product?.code || "",
            productName: item.product?.name || item.productName,
            unit: item.unit || "Thùng",
            quantity: item.quantity || 1,
            unitPrice: item.unitPrice || 0,
            costBeforeTax:
              item.costBeforeTax ||
              (item.unitPrice || 0) * (item.quantity || 0),
          }))
        );
      }
    } else if (!order && open) {
      // Reset form khi tạo mới
      form.resetFields();
      setItems([]);
    }
  }, [order, open, form]);

  // ------------------------------
  // Xử lý thêm sản phẩm
  // ------------------------------
  const handleAddProduct = (productId) => {
    if (items.some((i) => i.productId === productId)) {
      message.warning("Sản phẩm đã được thêm");
      return;
    }
    const product = products.find((p) => p.id === productId);
    if (product) {
      const newItem = {
        productId: product.id,
        productCode: product.code || "",
        productName: product.name,
        unit: "Thùng",
        quantity: 1,
        unitPrice: product.price || 0,
        costBeforeTax: product.price || 0,
      };
      setItems([...items, newItem]);
    }
  };

  const handleChangeItem = (index, field, value) => {
    const newItems = [...items];
    newItems[index][field] = value;

    // Tự động tính costBeforeTax khi thay đổi quantity hoặc unitPrice
    if (field === "quantity" || field === "unitPrice") {
      const item = newItems[index];
      item.costBeforeTax = (item.unitPrice || 0) * (item.quantity || 0);
    }

    setItems(newItems);
  };

  const handleRemoveItem = (index) => {
    setItems(items.filter((_, i) => i !== index));
  };

  // ------------------------------
  // Submit đơn hàng
  // ------------------------------
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (items.length === 0) {
        message.warning("Vui lòng thêm ít nhất một sản phẩm");
        return;
      }

      const deliveryDate = values.deliveryDate
        ? values.deliveryDate.format("YYYY-MM-DD HH:mm")
        : null;

      const payload = {
        supplierId: values.supplierId,
        warehouseId: values.warehouseId,
        orderName: values.orderName,
        shippingCost: values.shippingCost || 0,
        taxType: values.taxType || "8%",
        deliveryDate: deliveryDate,
        items: items.map((i) => ({
          productId: i.productId,
          quantity: i.quantity,
          unitPrice: i.unitPrice,
          unit: i.unit,
          costBeforeTax:
            i.costBeforeTax || (i.unitPrice || 0) * (i.quantity || 0),
        })),
      };

      setLoading(true);
      await onSave(payload);
      setItems([]);
      form.resetFields();
    } catch (err) {
      console.error("Error saving purchase order:", err);
      message.error("Lưu đơn hàng thất bại");
    } finally {
      setLoading(false);
    }
  };

  const columns = [
    { title: "#", width: 50, render: (_, __, index) => index + 1 },
    { title: "Mã SP", dataIndex: "productCode", width: 120 },
    { title: "Tên sản phẩm", dataIndex: "productName" },
    {
      title: "Đơn vị",
      dataIndex: "unit",
      width: 120,
      render: (v, record, i) => (
        <Input
          value={v}
          onChange={(e) => handleChangeItem(i, "unit", e.target.value)}
          placeholder="Đơn vị"
        />
      ),
    },
    {
      title: "Số lượng",
      dataIndex: "quantity",
      width: 120,
      render: (v, record, i) => (
        <InputNumber
          min={1}
          value={v}
          onChange={(val) => handleChangeItem(i, "quantity", val)}
          style={{ width: "100%" }}
        />
      ),
    },
    {
      title: "Đơn giá",
      dataIndex: "unitPrice",
      width: 150,
      render: (v, record, i) => (
        <InputNumber
          min={0}
          value={v}
          onChange={(val) => handleChangeItem(i, "unitPrice", val)}
          formatter={(value) =>
            `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ",")
          }
          parser={(value) => value.replace(/\$\s?|(,*)/g, "")}
          style={{ width: "100%" }}
        />
      ),
    },
    {
      title: "Thành tiền",
      width: 150,
      render: (_, record) => (
        <span style={{ fontWeight: "bold" }}>
          {(
            record.costBeforeTax ||
            (record.unitPrice || 0) * (record.quantity || 0)
          ).toLocaleString("vi-VN")}{" "}
          ₫
        </span>
      ),
    },
    {
      title: "Thao tác",
      width: 100,
      render: (_, __, i) => (
        <Button danger size="small" onClick={() => handleRemoveItem(i)}>
          Xóa
        </Button>
      ),
    },
  ];

  const total = items.reduce(
    (sum, i) =>
      sum + (i.costBeforeTax || (i.unitPrice || 0) * (i.quantity || 0)),
    0
  );

  return (
    <Modal
      title={order ? "Chỉnh sửa đơn hàng mua" : "Tạo đơn hàng mua mới"}
      open={open}
      onCancel={onCancel}
      footer={null}
      width={1200}
      destroyOnClose
    >
      <Form form={form} layout="vertical">
        <div
          style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16 }}
        >
          <Form.Item
            label="Nhà cung cấp"
            name="supplierId"
            rules={[{ required: true, message: "Vui lòng chọn nhà cung cấp" }]}
          >
            <Select placeholder="Chọn nhà cung cấp">
              {suppliers.map((s) => (
                <Select.Option value={s.id} key={s.id}>
                  {s.name}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item
            label="Kho hàng"
            name="warehouseId"
            rules={[{ required: true, message: "Vui lòng chọn kho hàng" }]}
          >
            <Select placeholder="Chọn kho hàng">
              {warehouses.map((w) => (
                <Select.Option value={w.id} key={w.id}>
                  {w.name}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item label="Tên đơn hàng" name="orderName">
            <Input placeholder="Nhập tên đơn hàng" />
          </Form.Item>

          <Form.Item
            label="Chi phí vận chuyển"
            name="shippingCost"
            initialValue={0}
          >
            <InputNumber
              min={0}
              style={{ width: "100%" }}
              formatter={(value) =>
                `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ",")
              }
              parser={(value) => value.replace(/\$\s?|(,*)/g, "")}
              placeholder="0"
            />
          </Form.Item>

          <Form.Item label="Loại thuế" name="taxType" initialValue="8%">
            <Select>
              <Select.Option value="8%">8% VAT</Select.Option>
              <Select.Option value="10%">10% VAT</Select.Option>
              <Select.Option value="0%">0%</Select.Option>
            </Select>
          </Form.Item>

          <Form.Item label="Ngày giao hàng dự kiến" name="deliveryDate">
            <DatePicker
              showTime
              format="DD/MM/YYYY HH:mm"
              style={{ width: "100%" }}
              placeholder="Chọn ngày giao hàng"
            />
          </Form.Item>
        </div>
      </Form>

      <Divider />
      <h3>Sản phẩm cần mua</h3>

      <div style={{ display: "flex", gap: 12, marginBottom: 16 }}>
        <Select
          showSearch
          placeholder="Tìm kiếm sản phẩm..."
          style={{ flex: 1 }}
          onChange={handleAddProduct}
          filterOption={(input, option) =>
            option.children.toLowerCase().includes(input.toLowerCase())
          }
          allowClear
        >
          {products.map((p) => (
            <Select.Option value={p.id} key={p.id}>
              {p.code} - {p.name}
            </Select.Option>
          ))}
        </Select>
      </div>

      <Table
        dataSource={items}
        columns={columns}
        pagination={false}
        rowKey="productId"
        scroll={{ x: 800 }}
        size="small"
      />

      <div style={{ textAlign: "right", marginTop: 16 }}>
        <strong style={{ fontSize: 16 }}>
          Tổng tiền (trước thuế): {total.toLocaleString("vi-VN")} ₫
        </strong>
      </div>

      <div style={{ textAlign: "right", marginTop: 24 }}>
        <Button onClick={onCancel} style={{ marginRight: 8 }}>
          Hủy
        </Button>
        <Button type="primary" onClick={handleSubmit} loading={loading}>
          {order ? "Cập nhật" : "Tạo đơn hàng"}
        </Button>
      </div>
    </Modal>
  );
}
