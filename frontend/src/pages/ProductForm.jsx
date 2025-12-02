import React, { useEffect, useState } from "react";
import { Modal, Form, Input, InputNumber, Select, message } from "antd";
import api from "../services/api";

export default function ProductForm({ open, onCancel, onSave, product }) {
  const [form] = Form.useForm();
  const [suppliers, setSuppliers] = useState([]);

  const fetchSuppliers = async () => {
    try {
      const res = await api.get("/suppliers");
      setSuppliers(res.data);
    } catch {
      message.error("Failed to load suppliers");
    }
  };

  useEffect(() => {
    fetchSuppliers();
  }, []);

  useEffect(() => {
    if (product) {
      form.setFieldsValue({
        ...product,
        supplierId: product.supplier?.id,
      });
    } else {
      form.resetFields();
    }
  }, [product]);

  const handleOk = () => {
    form.validateFields().then((values) => {
      const payload = {
        ...values,
        supplier: values.supplierId ? { id: values.supplierId } : null,
      };
      onSave(payload);
    });
  };

  return (
    <Modal
      title={product ? "Edit Product" : "Add Product"}
      open={open}
      onOk={handleOk}
      onCancel={onCancel}
      okText="Save"
    >
      <Form form={form} layout="vertical">
        <Form.Item
          label="Code"
          name="code"
          rules={[{ required: true, message: "Please enter code" }]}
        >
          <Input />
        </Form.Item>
        <Form.Item
          label="Name"
          name="name"
          rules={[{ required: true, message: "Please enter product name" }]}
        >
          <Input />
        </Form.Item>
        <Form.Item label="Description" name="description">
          <Input />
        </Form.Item>
        <Form.Item label="Unit" name="unit">
          <Input placeholder="e.g. piece, box, kg" />
        </Form.Item>
        <Form.Item label="Price" name="price">
          <InputNumber min={0} style={{ width: "100%" }} />
        </Form.Item>

        <Form.Item label="Supplier" name="supplierId">
          <Select placeholder="Select supplier">
            {suppliers.map((s) => (
              <Select.Option key={s.id} value={s.id}>
                {s.name}
              </Select.Option>
            ))}
          </Select>
        </Form.Item>
      </Form>
    </Modal>
  );
}
