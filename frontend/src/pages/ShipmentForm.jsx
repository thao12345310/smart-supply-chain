import React, { useEffect } from "react";
import { Modal, Form, Input, DatePicker, Select } from "antd";
import dayjs from "dayjs";

export default function ShipmentForm({ open, onCancel, onSave, shipment }) {
  const [form] = Form.useForm();

  useEffect(() => {
    if (shipment)
      form.setFieldsValue({
        ...shipment,
        receiveDate: shipment.receiveDate ? dayjs(shipment.receiveDate) : null,
      });
    else form.resetFields();
  }, [shipment, open]);

  const handleOk = () => {
    form
      .validateFields()
      .then((values) => {
        const formatted = {
          ...values,
          receiveDate: values.receiveDate
            ? values.receiveDate.format("YYYY-MM-DD")
            : null,
        };
        onSave(formatted);
      })
      .catch(() => {});
  };

  return (
    <Modal
      title={shipment ? "Sửa lô hàng" : "Thêm lô hàng"}
      open={open}
      onOk={handleOk}
      onCancel={onCancel}
      okText="Lưu"
      cancelText="Hủy"
      width={600}
    >
      <Form form={form} layout="vertical">
        <Form.Item
          label="Mã lô hàng"
          name="code"
          rules={[{ required: true, message: "Vui lòng nhập mã lô hàng" }]}
        >
          <Input placeholder="VD: LH-2026-001" />
        </Form.Item>
        <Form.Item label="Ngày nhận" name="receiveDate">
          <DatePicker 
            format="DD/MM/YYYY" 
            style={{ width: "100%" }} 
            placeholder="Chọn ngày nhận"
          />
        </Form.Item>
        <Form.Item
          label="Kho hàng"
          name="warehouse"
          rules={[{ required: true, message: "Vui lòng nhập tên kho hàng" }]}
        >
          <Input placeholder="Nhập tên kho hàng" />
        </Form.Item>
        <Form.Item label="Mã đơn mua hàng" name="purchaseOrderId">
          <Input placeholder="Nhập mã đơn mua (tùy chọn)" />
        </Form.Item>
      </Form>
    </Modal>
  );
}
