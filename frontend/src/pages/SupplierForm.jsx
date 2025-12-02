import React, { useEffect } from "react";
import { Modal, Form, Input } from "antd";

export default function SupplierForm({ open, onCancel, onSave, supplier }) {
  const [form] = Form.useForm();

  useEffect(() => {
    if (supplier) form.setFieldsValue(supplier);
    else form.resetFields();
  }, [supplier]);

  const handleOk = () => {
    form
      .validateFields()
      .then((values) => {
        onSave(values);
      })
      .catch(() => {});
  };

  return (
    <Modal
      title={supplier ? "Edit Supplier" : "Add Supplier"}
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
          rules={[{ required: true, message: "Please enter name" }]}
        >
          <Input />
        </Form.Item>
        <Form.Item label="Contact Name" name="contactName">
          <Input />
        </Form.Item>
        <Form.Item label="Phone" name="phone">
          <Input />
        </Form.Item>
        <Form.Item label="Email" name="email">
          <Input />
        </Form.Item>
        <Form.Item label="Address" name="address">
          <Input />
        </Form.Item>
      </Form>
    </Modal>
  );
}
