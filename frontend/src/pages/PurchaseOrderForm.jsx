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
} from "antd";
import api from "../services/api";
import dayjs from "dayjs";

export default function PurchaseOrderForm({ onSave }) {
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

  // ------------------------------
  // Xử lý thêm sản phẩm
  // ------------------------------
  const handleAddProduct = (productId) => {
    if (items.find((i) => i.productId === productId)) {
      message.warning("Product already added");
      return;
    }
    const product = products.find((p) => p.id === productId);
    if (product) {
      setItems([
        ...items,
        {
          productId: product.id,
          productName: product.name,
          unit: "Thùng",
          quantity: 1,
          unitPrice: product.price || 0,
          costBeforeTax: 0,
        },
      ]);
    }
  };

  const handleChangeItem = (index, field, value) => {
    const newItems = [...items];
    newItems[index][field] = value;
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
        message.warning("Please add at least one product");
        return;
      }
      const payload = {
        ...values,
        deliveryDate: values.deliveryDate
          ? values.deliveryDate.format("YYYY-MM-DD HH:mm")
          : null,
        items: items.map((i) => ({
          productId: i.productId,
          quantity: i.quantity,
          unitPrice: i.unitPrice,
          unit: i.unit,
          costBeforeTax: i.costBeforeTax,
        })),
      };

      setLoading(true);
      await api.post("/purchase-orders", payload);
      message.success("Purchase order created");
      setItems([]);
      form.resetFields();
    } catch (err) {
      message.error("Failed to create purchase order");
    } finally {
      setLoading(false);
    }
  };

  const columns = [
    { title: "#", render: (_, __, index) => index + 1 },
    { title: "Product Code", dataIndex: "productCode" },
    { title: "Product Name", dataIndex: "productName" },
    {
      title: "Unit",
      dataIndex: "unit",
      render: (v, record, i) => (
        <Input
          value={v}
          onChange={(e) => handleChangeItem(i, "unit", e.target.value)}
        />
      ),
    },
    {
      title: "Quantity",
      dataIndex: "quantity",
      render: (v, record, i) => (
        <InputNumber
          min={1}
          value={v}
          onChange={(val) => handleChangeItem(i, "quantity", val)}
        />
      ),
    },
    {
      title: "Unit Price",
      dataIndex: "unitPrice",
      render: (v, record, i) => (
        <InputNumber
          min={0}
          value={v}
          onChange={(val) => handleChangeItem(i, "unitPrice", val)}
        />
      ),
    },
    {
      title: "Cost Before Tax",
      render: (_, record, i) => (
        <span>
          {((record.unitPrice || 0) * (record.quantity || 0)).toLocaleString()}
        </span>
      ),
    },
    {
      title: "Action",
      render: (_, __, i) => (
        <Button danger onClick={() => handleRemoveItem(i)}>
          Remove
        </Button>
      ),
    },
  ];

  const total = items.reduce(
    (sum, i) => sum + (i.unitPrice || 0) * (i.quantity || 0),
    0
  );

  return (
    <div style={{ padding: 20 }}>
      <h2>Create Purchase Order</h2>
      <Form form={form} layout="vertical">
        <div
          style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16 }}
        >
          <Form.Item
            label="Supplier"
            name="supplierId"
            rules={[{ required: true, message: "Please select supplier" }]}
          >
            <Select placeholder="Select supplier">
              {suppliers.map((s) => (
                <Select.Option value={s.id} key={s.id}>
                  {s.name}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item
            label="Warehouse"
            name="warehouseId"
            rules={[{ required: true, message: "Please select warehouse" }]}
          >
            <Select placeholder="Select warehouse">
              {warehouses.map((w) => (
                <Select.Option value={w.id} key={w.id}>
                  {w.name}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item label="Shipping Cost" name="shippingCost" initialValue={0}>
            <InputNumber min={0} style={{ width: "100%" }} />
          </Form.Item>

          <Form.Item label="Tax Type" name="taxType" initialValue="8%">
            <Select>
              <Select.Option value="8%">8% VAT</Select.Option>
              <Select.Option value="10%">10% VAT</Select.Option>
              <Select.Option value="0%">0%</Select.Option>
            </Select>
          </Form.Item>

          <Form.Item label="Expected Delivery Date" name="deliveryDate">
            <DatePicker showTime style={{ width: "100%" }} />
          </Form.Item>

          <Form.Item label="Order Name" name="orderName">
            <Input />
          </Form.Item>
        </div>
      </Form>

      <Divider />
      <h3>Products to Purchase</h3>

      <div style={{ display: "flex", gap: 12, marginBottom: 8 }}>
        <Select
          showSearch
          placeholder="Search product..."
          style={{ flex: 1 }}
          onChange={handleAddProduct}
          filterOption={(input, option) =>
            option.children.toLowerCase().includes(input.toLowerCase())
          }
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
      />

      <div style={{ textAlign: "right", marginTop: 12 }}>
        <strong>Total (before tax): {total.toLocaleString()} ₫</strong>
      </div>

      <div style={{ textAlign: "right", marginTop: 16 }}>
        <Button type="primary" onClick={handleSubmit} loading={loading}>
          Save Purchase Order
        </Button>
      </div>
    </div>
  );
}
