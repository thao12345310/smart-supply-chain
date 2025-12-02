import React, { useEffect } from "react";
import { Modal, Form, Input, DatePicker } from "antd";
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
  }, [shipment]);

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
      title={shipment ? "Edit Shipment" : "Add Shipment"}
      open={open}
      onOk={handleOk}
      onCancel={onCancel}
      okText="Save"
    >
      <Form form={form} layout="vertical">
        <Form.Item
          label="Code"
          name="code"
          rules={[{ required: true, message: "Please enter shipment code" }]}
        >
          <Input />
        </Form.Item>
        <Form.Item label="Receive Date" name="receiveDate">
          <DatePicker format="YYYY-MM-DD" style={{ width: "100%" }} />
        </Form.Item>
        <Form.Item
          label="Warehouse"
          name="warehouse"
          rules={[{ required: true, message: "Please enter warehouse name" }]}
        >
          <Input />
        </Form.Item>
        <Form.Item label="Purchase Order ID" name="purchaseOrderId">
          <Input placeholder="Enter PO ID (optional)" />
        </Form.Item>
      </Form>
    </Modal>
  );
}
