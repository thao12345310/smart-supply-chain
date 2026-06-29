import React, { useEffect } from "react";
import { Modal, Form, Input, Switch } from "antd";

export default function SupplierForm({ open, onCancel, onSave, supplier }) {
  const [form] = Form.useForm();

  useEffect(() => {
    if (supplier) {
      form.setFieldsValue({
        ...supplier,
        active: supplier.active !== false,
      });
    } else {
      form.resetFields();
      form.setFieldsValue({ active: true });
    }
  }, [supplier, open]);

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
      title={supplier ? "Sửa nhà cung cấp" : "Thêm nhà cung cấp"}
      open={open}
      onOk={handleOk}
      onCancel={onCancel}
      okText="Lưu"
      cancelText="Hủy"
      width={600}
    >
      <Form form={form} layout="vertical">
        <Form.Item
          label="Mã nhà cung cấp"
          name="code"
          rules={[{ required: true, message: "Vui lòng nhập mã nhà cung cấp" }]}
        >
          <Input placeholder="VD: NCC001" />
        </Form.Item>
        <Form.Item
          label="Tên nhà cung cấp"
          name="name"
          rules={[{ required: true, message: "Vui lòng nhập tên nhà cung cấp" }]}
        >
          <Input placeholder="Nhập tên nhà cung cấp" />
        </Form.Item>
        <Form.Item label="Người liên hệ" name="contactName">
          <Input placeholder="Tên người liên hệ" />
        </Form.Item>
        <Form.Item label="Điện thoại" name="phone">
          <Input placeholder="Số điện thoại" />
        </Form.Item>
        <Form.Item label="Email" name="email">
          <Input placeholder="Địa chỉ email" />
        </Form.Item>
        <Form.Item label="Địa chỉ" name="address">
          <Input.TextArea rows={2} placeholder="Địa chỉ nhà cung cấp" />
        </Form.Item>
        <Form.Item label="Hoạt động" name="active" valuePropName="checked">
          <Switch checkedChildren="Có" unCheckedChildren="Không" />
        </Form.Item>
      </Form>
    </Modal>
  );
}
