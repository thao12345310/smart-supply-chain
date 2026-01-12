import React, { useEffect } from "react";
import { Modal, Form, Input, Select, DatePicker } from "antd";

export default function DeliveryPlanForm({ open, onCancel, onSave, plan }) {
  const [form] = Form.useForm();

  useEffect(() => {
    if (plan) form.setFieldsValue(plan);
    else form.resetFields();
  }, [plan, open]);

  const handleOk = () => {
    form.validateFields().then((values) => {
      onSave(values);
    });
  };

  return (
    <Modal
      title={plan ? "Sửa đợt giao hàng" : "Tạo đợt giao hàng"}
      open={open}
      onOk={handleOk}
      onCancel={onCancel}
      okText="Lưu"
      cancelText="Hủy"
      width={600}
    >
      <Form form={form} layout="vertical">
        <Form.Item
          label="Mã đợt giao"
          name="code"
          rules={[{ required: true, message: "Vui lòng nhập mã đợt giao" }]}
        >
          <Input placeholder="VD: DGH-2026-001" />
        </Form.Item>
        <Form.Item label="Ngày giao dự kiến" name="plannedDate">
          <DatePicker 
            style={{ width: "100%" }} 
            format="DD/MM/YYYY"
            placeholder="Chọn ngày giao"
          />
        </Form.Item>
        <Form.Item label="Mô tả" name="description">
          <Input.TextArea rows={3} placeholder="Mô tả đợt giao hàng" />
        </Form.Item>
        <Form.Item label="Trạng thái" name="status" initialValue="DRAFT">
          <Select>
            <Select.Option value="DRAFT">Nháp</Select.Option>
            <Select.Option value="PENDING">Chờ xử lý</Select.Option>
            <Select.Option value="IN_PROGRESS">Đang giao</Select.Option>
            <Select.Option value="COMPLETED">Hoàn thành</Select.Option>
            <Select.Option value="CANCELLED">Đã hủy</Select.Option>
          </Select>
        </Form.Item>
      </Form>
    </Modal>
  );
}
