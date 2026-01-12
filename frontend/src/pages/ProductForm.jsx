import React, { useEffect, useState } from "react";
import { Modal, Form, Input, InputNumber, Select, message, Switch } from "antd";
import api from "../services/api";

export default function ProductForm({ open, onCancel, onSave, product }) {
  const [form] = Form.useForm();
  const [suppliers, setSuppliers] = useState([]);

  const fetchSuppliers = async () => {
    try {
      const res = await api.get("/suppliers");
      setSuppliers(res.data || []);
    } catch {
      message.error("Không thể tải danh sách nhà cung cấp");
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
        active: product.active !== false,
      });
    } else {
      form.resetFields();
      form.setFieldsValue({ active: true });
    }
  }, [product, open]);

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
      title={product ? "Sửa sản phẩm" : "Thêm sản phẩm"}
      open={open}
      onOk={handleOk}
      onCancel={onCancel}
      okText="Lưu"
      cancelText="Hủy"
      width={600}
    >
      <Form form={form} layout="vertical">
        <Form.Item
          label="Mã sản phẩm"
          name="code"
          rules={[{ required: true, message: "Vui lòng nhập mã sản phẩm" }]}
        >
          <Input placeholder="VD: SP001" />
        </Form.Item>
        <Form.Item
          label="Tên sản phẩm"
          name="name"
          rules={[{ required: true, message: "Vui lòng nhập tên sản phẩm" }]}
        >
          <Input placeholder="Nhập tên sản phẩm" />
        </Form.Item>
        <Form.Item label="Mô tả" name="description">
          <Input.TextArea rows={3} placeholder="Mô tả sản phẩm" />
        </Form.Item>
        <Form.Item label="Đơn vị tính" name="unit">
          <Input placeholder="VD: Cái, Hộp, Kg" />
        </Form.Item>
        <Form.Item label="Giá bán" name="price">
          <InputNumber 
            min={0} 
            style={{ width: "100%" }} 
            formatter={(val) => `${val}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
            parser={(val) => val.replace(/\,/g, '')}
            placeholder="Nhập giá bán"
          />
        </Form.Item>
        <Form.Item label="Nhà cung cấp" name="supplierId">
          <Select placeholder="Chọn nhà cung cấp" allowClear>
            {suppliers.map((s) => (
              <Select.Option key={s.id} value={s.id}>
                {s.code} - {s.name}
              </Select.Option>
            ))}
          </Select>
        </Form.Item>
        <Form.Item label="Hoạt động" name="active" valuePropName="checked">
          <Switch checkedChildren="Có" unCheckedChildren="Không" />
        </Form.Item>
      </Form>
    </Modal>
  );
}
