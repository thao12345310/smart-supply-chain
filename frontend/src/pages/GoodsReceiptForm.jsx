import React, { useEffect, useState } from "react";
import {
  Modal, Form, Input, DatePicker, InputNumber, Button, Table, 
  message, Divider, Space, Alert, Card
} from "antd";
import { goodsReceiptApi } from "../services/api";
import dayjs from "dayjs";

export default function GoodsReceiptForm({ visible, purchaseOrderId, onCancel, onSuccess }) {
  const [form] = Form.useForm();
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(false);
  const [fetching, setFetching] = useState(false);

  // Fetch receiving summary when modal opens
  useEffect(() => {
    if (visible && purchaseOrderId) {
      fetchReceivingSummary();
    }
  }, [visible, purchaseOrderId]);

  const fetchReceivingSummary = async () => {
    setFetching(true);
    try {
      const res = await goodsReceiptApi.getReceivingSummary(purchaseOrderId);
      const summary = res.data;
      
      // Set form values
      form.setFieldsValue({
        receiptDate: dayjs(),
        warehouseId: summary.warehouseId,
      });
      
      // Initialize items with 0 received quantity
      setItems(
        (summary.items || []).map((item) => ({
          ...item,
          receivedQuantity: 0,
          rejectedQuantity: 0,
          batchNumber: "",
          notes: "",
        }))
      );
    } catch (err) {
      console.error(err);
      message.error("Không thể tải thông tin nhận hàng");
    } finally {
      setFetching(false);
    }
  };

  const handleChangeItem = (index, field, value) => {
    const newItems = [...items];
    newItems[index][field] = value;
    
    // Auto-calculate accepted quantity
    if (field === "receivedQuantity" || field === "rejectedQuantity") {
      const received = newItems[index].receivedQuantity || 0;
      const rejected = newItems[index].rejectedQuantity || 0;
      newItems[index].acceptedQuantity = Math.max(0, received - rejected);
    }
    
    setItems(newItems);
  };

  const validateItems = () => {
    let hasError = false;
    let hasItems = false;

    for (const item of items) {
      if (item.receivedQuantity > 0) {
        hasItems = true;
        
        // Check if received exceeds remaining
        if (item.receivedQuantity > item.remainingQuantity) {
          message.error(`Số lượng nhận (${item.receivedQuantity}) vượt quá số lượng còn lại (${item.remainingQuantity}) cho sản phẩm ${item.productName}`);
          hasError = true;
          break;
        }
        
        // Check rejected doesn't exceed received
        if ((item.rejectedQuantity || 0) > item.receivedQuantity) {
          message.error(`Số lượng từ chối không thể lớn hơn số lượng nhận cho sản phẩm ${item.productName}`);
          hasError = true;
          break;
        }
      }
    }

    if (!hasItems) {
      message.warning("Vui lòng nhập số lượng nhận cho ít nhất một sản phẩm");
      return false;
    }

    return !hasError;
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      
      if (!validateItems()) {
        return;
      }

      const itemsToSubmit = items
        .filter((item) => item.receivedQuantity > 0)
        .map((item) => ({
          purchaseOrderItemId: item.purchaseOrderItemId,
          productId: item.productId,
          receivedQuantity: item.receivedQuantity,
          rejectedQuantity: item.rejectedQuantity || 0,
          batchNumber: item.batchNumber || null,
          expiryDate: item.expiryDate || null,
          notes: item.notes || null,
        }));

      const payload = {
        purchaseOrderId: purchaseOrderId,
        receiptDate: values.receiptDate ? values.receiptDate.format("YYYY-MM-DD") : null,
        deliveryNoteNumber: values.deliveryNoteNumber,
        invoiceNumber: values.invoiceNumber,
        notes: values.notes,
        items: itemsToSubmit,
      };

      setLoading(true);
      await goodsReceiptApi.create(payload);
      
      form.resetFields();
      setItems([]);
      onSuccess();
    } catch (err) {
      console.error("Error creating goods receipt:", err);
      message.error(err.message || "Lưu phiếu nhập thất bại");
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    form.resetFields();
    setItems([]);
    onCancel();
  };

  const columns = [
    { 
      title: "#", 
      width: 50, 
      render: (_, __, index) => index + 1 
    },
    { 
      title: "Mã SP", 
      dataIndex: "productCode", 
      width: 100 
    },
    { 
      title: "Tên sản phẩm", 
      dataIndex: "productName",
      ellipsis: true,
    },
    { 
      title: "ĐVT", 
      dataIndex: "unit", 
      width: 70 
    },
    { 
      title: "SL đặt", 
      dataIndex: "orderedQuantity", 
      width: 80,
      align: "center",
    },
    { 
      title: "Đã nhận", 
      dataIndex: "previouslyReceivedQuantity", 
      width: 80,
      align: "center",
      render: (val) => <span style={{ color: val > 0 ? '#52c41a' : '#999' }}>{val || 0}</span>,
    },
    { 
      title: "Còn lại", 
      dataIndex: "remainingQuantity", 
      width: 80,
      align: "center",
      render: (val) => <span style={{ color: val > 0 ? '#ff4d4f' : '#52c41a' }}>{val || 0}</span>,
    },
    {
      title: "SL nhận",
      dataIndex: "receivedQuantity",
      width: 100,
      render: (v, record, i) => (
        <InputNumber
          min={0}
          max={record.remainingQuantity}
          value={v}
          onChange={(val) => handleChangeItem(i, "receivedQuantity", val)}
          style={{ width: "100%" }}
          disabled={record.remainingQuantity <= 0}
        />
      ),
    },
    {
      title: "SL lỗi",
      dataIndex: "rejectedQuantity",
      width: 80,
      render: (v, record, i) => (
        <InputNumber
          min={0}
          max={record.receivedQuantity || 0}
          value={v}
          onChange={(val) => handleChangeItem(i, "rejectedQuantity", val)}
          style={{ width: "100%" }}
          disabled={!record.receivedQuantity}
        />
      ),
    },
    {
      title: "Số lô",
      dataIndex: "batchNumber",
      width: 120,
      render: (v, record, i) => (
        <Input
          value={v}
          onChange={(e) => handleChangeItem(i, "batchNumber", e.target.value)}
          placeholder="Số lô"
          disabled={!record.receivedQuantity}
        />
      ),
    },
  ];

  const totalReceiving = items.reduce((sum, i) => sum + (i.receivedQuantity || 0), 0);
  const totalRejected = items.reduce((sum, i) => sum + (i.rejectedQuantity || 0), 0);
  const totalAccepted = totalReceiving - totalRejected;

  return (
    <Modal
      title="Tạo phiếu nhập kho"
      open={visible}
      onCancel={handleClose}
      footer={null}
      width={1200}
      destroyOnClose
    >
      {fetching ? (
        <div style={{ textAlign: "center", padding: 40 }}>Đang tải...</div>
      ) : (
        <>
          <Form form={form} layout="vertical">
            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: 16 }}>
              <Form.Item 
                label="Ngày nhận hàng" 
                name="receiptDate"
                initialValue={dayjs()}
              >
                <DatePicker 
                  format="DD/MM/YYYY" 
                  style={{ width: "100%" }}
                  placeholder="Chọn ngày nhận"
                />
              </Form.Item>

              <Form.Item label="Số phiếu giao hàng" name="deliveryNoteNumber">
                <Input placeholder="Nhập số phiếu giao hàng" />
              </Form.Item>

              <Form.Item label="Số hóa đơn" name="invoiceNumber">
                <Input placeholder="Nhập số hóa đơn" />
              </Form.Item>
            </div>

            <Form.Item label="Ghi chú" name="notes">
              <Input.TextArea rows={2} placeholder="Ghi chú" />
            </Form.Item>
          </Form>

          <Divider />

          <Alert
            message="Hướng dẫn"
            description="Nhập số lượng nhận được cho từng sản phẩm. Nếu có sản phẩm lỗi, nhập vào cột 'SL lỗi'. Chỉ số lượng hàng tốt (SL nhận - SL lỗi) sẽ được nhập kho."
            type="info"
            showIcon
            style={{ marginBottom: 16 }}
          />

          <h3>Danh sách sản phẩm</h3>
          <Table
            dataSource={items}
            columns={columns}
            pagination={false}
            rowKey="purchaseOrderItemId"
            scroll={{ x: 1000 }}
            size="small"
          />

          <Card style={{ marginTop: 16, background: '#f5f5f5' }}>
            <Space size="large">
              <span><strong>Tổng SL nhận:</strong> {totalReceiving}</span>
              <span><strong>Tổng SL lỗi:</strong> <span style={{ color: '#ff4d4f' }}>{totalRejected}</span></span>
              <span><strong>Tổng SL nhập kho:</strong> <span style={{ color: '#52c41a' }}>{totalAccepted}</span></span>
            </Space>
          </Card>

          <div style={{ textAlign: "right", marginTop: 24 }}>
            <Space>
              <Button onClick={handleClose}>Hủy</Button>
              <Button type="primary" onClick={handleSubmit} loading={loading}>
                Lưu phiếu nhập
              </Button>
            </Space>
          </div>
        </>
      )}
    </Modal>
  );
}
