import React, { useEffect } from "react";
import { Modal, Form, Input, Select } from "antd";

export default function DeliveryPlanForm({ open, onCancel, onSave, plan }) {
  const [form] = Form.useForm();

  useEffect(() => {
    if (plan) form.setFieldsValue(plan);
    else form.resetFields();
  }, [plan]);

  const handleOk = () => {
    form.validateFields().then((values) => {
      onSave(values);
    });
  };

  return (
    <Modal
      title={plan ? "Edit Delivery Plan" : "Add Delivery Plan"}
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
        <Form.Item label="Description" name="description">
          <Input.TextArea rows={2} />
        </Form.Item>
        <Form.Item label="Status" name="status" initialValue="Created">
          <Select>
            <Select.Option value="Created">Created</Select.Option>
            <Select.Option value="InProgress">InProgress</Select.Option>
            <Select.Option value="Completed">Completed</Select.Option>
          </Select>
        </Form.Item>
      </Form>
    </Modal>
  );
}
