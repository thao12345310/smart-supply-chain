import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { 
  Button, Descriptions, Table, message, Tag, Modal, 
  Space, Card, Divider, Typography, Alert, Statistic, Row, Col
} from "antd";
import { 
  CheckCircleOutlined, CloseCircleOutlined, DeleteOutlined,
  ArrowLeftOutlined, PrinterOutlined
} from "@ant-design/icons";
import { goodsReceiptApi } from "../services/api";
import dayjs from "dayjs";

const { Title } = Typography;

// Status configuration
const STATUS_CONFIG = {
  DRAFT: { color: "blue", label: "Nháp" },
  CONFIRMED: { color: "green", label: "Đã xác nhận" },
  CANCELLED: { color: "default", label: "Đã hủy" },
};

export default function GoodsReceiptDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [gr, setGr] = useState(null);
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchDetail = async () => {
    setLoading(true);
    try {
      const res = await goodsReceiptApi.getById(id);
      setGr(res.data);
      setItems(res.data.items || []);
    } catch (e) {
      console.error(e);
      message.error("Lỗi tải phiếu nhập");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDetail();
  }, [id]);

  const handleConfirm = async () => {
    Modal.confirm({
      title: "Xác nhận phiếu nhập",
      content: "Bạn có chắc muốn xác nhận phiếu nhập này? Số lượng tồn kho sẽ được cập nhật.",
      okText: "Xác nhận",
      cancelText: "Hủy",
      onOk: async () => {
        try {
          await goodsReceiptApi.confirm(id);
          message.success("Xác nhận phiếu nhập thành công! Tồn kho đã được cập nhật.");
          fetchDetail();
        } catch (e) {
          message.error(e.message || "Không thể xác nhận phiếu nhập");
        }
      },
    });
  };

  const handleCancel = async () => {
    Modal.confirm({
      title: "Hủy phiếu nhập",
      content: "Bạn có chắc muốn hủy phiếu nhập này?",
      okText: "Hủy phiếu",
      okButtonProps: { danger: true },
      cancelText: "Quay lại",
      onOk: async () => {
        try {
          await goodsReceiptApi.cancel(id);
          message.success("Đã hủy phiếu nhập");
          fetchDetail();
        } catch (e) {
          message.error(e.message || "Không thể hủy phiếu nhập");
        }
      },
    });
  };

  const handleDelete = async () => {
    Modal.confirm({
      title: "Xóa phiếu nhập",
      content: "Bạn có chắc muốn xóa phiếu nhập này? Thao tác này không thể hoàn tác.",
      okText: "Xóa",
      okButtonProps: { danger: true },
      cancelText: "Hủy",
      onOk: async () => {
        try {
          await goodsReceiptApi.delete(id);
          message.success("Xóa phiếu nhập thành công!");
          navigate("/goods-receipts");
        } catch (e) {
          message.error(e.message || "Không thể xóa phiếu nhập");
        }
      },
    });
  };

  const columns = [
    { 
      title: "STT", 
      width: 60,
      render: (_, __, index) => index + 1 
    },
    { 
      title: "Mã SP", 
      dataIndex: "productCode",
      width: 100,
    },
    { 
      title: "Tên sản phẩm", 
      dataIndex: "productName" 
    },
    { 
      title: "ĐVT", 
      dataIndex: "unit",
      width: 80,
    },
    { 
      title: "SL đặt", 
      dataIndex: "orderedQuantity",
      width: 80,
      align: "center",
    },
    { 
      title: "SL nhận", 
      dataIndex: "receivedQuantity",
      width: 90,
      align: "center",
    },
    { 
      title: "SL lỗi", 
      dataIndex: "rejectedQuantity",
      width: 80,
      align: "center",
      render: (val) => (
        <span style={{ color: val > 0 ? '#ff4d4f' : '#999' }}>{val || 0}</span>
      ),
    },
    { 
      title: "SL nhập kho", 
      dataIndex: "acceptedQuantity",
      width: 100,
      align: "center",
      render: (val) => (
        <span style={{ color: '#52c41a', fontWeight: 'bold' }}>{val || 0}</span>
      ),
    },
    { 
      title: "Số lô", 
      dataIndex: "batchNumber",
      width: 120,
    },
    { 
      title: "Đơn giá", 
      dataIndex: "unitPrice",
      width: 120,
      align: "right",
      render: (val) => val ? `${val.toLocaleString("vi-VN")} ₫` : "-",
    },
    { 
      title: "Thành tiền", 
      dataIndex: "totalAmount",
      width: 130,
      align: "right",
      render: (val) => val ? `${val.toLocaleString("vi-VN")} ₫` : "-",
    },
  ];

  if (loading || !gr) {
    return <div style={{ padding: 20 }}>Đang tải...</div>;
  }

  const statusConfig = STATUS_CONFIG[gr.status] || { color: "default", label: gr.status };
  const canConfirm = gr.status === "DRAFT";
  const canCancel = gr.status === "DRAFT";
  const canDelete = gr.status === "DRAFT";

  return (
    <div style={{ padding: 20 }}>
      {/* Header */}
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 20 }}>
        <Space>
          <Button icon={<ArrowLeftOutlined />} onClick={() => navigate(-1)}>
            Quay lại
          </Button>
          <Title level={3} style={{ margin: 0 }}>
            Chi tiết phiếu nhập: {gr.code}
          </Title>
          <Tag color={statusConfig.color} style={{ fontSize: 14, padding: "4px 12px" }}>
            {statusConfig.label}
          </Tag>
        </Space>
      </div>

      {/* Status Alert */}
      {gr.status === "CONFIRMED" && (
        <Alert
          message="Phiếu nhập đã được xác nhận"
          description={`Xác nhận lúc: ${gr.confirmedDate ? dayjs(gr.confirmedDate).format("DD/MM/YYYY HH:mm") : "-"}`}
          type="success"
          showIcon
          style={{ marginBottom: 16 }}
        />
      )}

      {gr.status === "CANCELLED" && (
        <Alert
          message="Phiếu nhập đã bị hủy"
          type="error"
          showIcon
          style={{ marginBottom: 16 }}
        />
      )}

      {/* Action Buttons */}
      <Card style={{ marginBottom: 20 }}>
        <Space wrap>
          {canConfirm && (
            <Button 
              type="primary" 
              icon={<CheckCircleOutlined />}
              onClick={handleConfirm}
              style={{ background: '#52c41a', borderColor: '#52c41a' }}
            >
              Xác nhận nhập kho
            </Button>
          )}

          {canCancel && (
            <Button 
              icon={<CloseCircleOutlined />}
              onClick={handleCancel}
            >
              Hủy phiếu
            </Button>
          )}

          <Button icon={<PrinterOutlined />}>In phiếu</Button>

          {canDelete && (
            <Button 
              danger 
              icon={<DeleteOutlined />}
              onClick={handleDelete}
            >
              Xóa
            </Button>
          )}
        </Space>
      </Card>

      {/* Summary Stats */}
      <Row gutter={16} style={{ marginBottom: 20 }}>
        <Col span={6}>
          <Card>
            <Statistic 
              title="Tổng SL nhận" 
              value={gr.totalReceivedQuantity || 0} 
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic 
              title="SL lỗi" 
              value={gr.totalRejectedQuantity || 0} 
              valueStyle={{ color: '#ff4d4f' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic 
              title="SL nhập kho" 
              value={gr.totalAcceptedQuantity || 0} 
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic 
              title="Tổng tiền" 
              value={gr.totalAmount || 0} 
              suffix="₫"
              formatter={(val) => val.toLocaleString("vi-VN")}
            />
          </Card>
        </Col>
      </Row>

      {/* Receipt Info */}
      <Descriptions bordered column={2} title="Thông tin phiếu nhập">
        <Descriptions.Item label="Mã phiếu">{gr.code}</Descriptions.Item>
        <Descriptions.Item label="Trạng thái">
          <Tag color={statusConfig.color}>{statusConfig.label}</Tag>
        </Descriptions.Item>
        <Descriptions.Item label="Mã đơn hàng">
          <a onClick={() => navigate(`/purchase-orders/${gr.purchaseOrderId}`)}>
            {gr.purchaseOrderCode}
          </a>
        </Descriptions.Item>
        <Descriptions.Item label="Kho nhập">{gr.warehouseName}</Descriptions.Item>
        <Descriptions.Item label="Ngày nhận hàng">
          {gr.receiptDate ? dayjs(gr.receiptDate).format("DD/MM/YYYY") : "-"}
        </Descriptions.Item>
        <Descriptions.Item label="Số phiếu giao hàng">{gr.deliveryNoteNumber || "-"}</Descriptions.Item>
        <Descriptions.Item label="Số hóa đơn">{gr.invoiceNumber || "-"}</Descriptions.Item>
        <Descriptions.Item label="Ngày tạo">
          {gr.createdAt ? dayjs(gr.createdAt).format("DD/MM/YYYY HH:mm") : "-"}
        </Descriptions.Item>
        {gr.confirmedDate && (
          <Descriptions.Item label="Ngày xác nhận">
            {dayjs(gr.confirmedDate).format("DD/MM/YYYY HH:mm")}
          </Descriptions.Item>
        )}
        {gr.notes && (
          <Descriptions.Item label="Ghi chú" span={2}>{gr.notes}</Descriptions.Item>
        )}
      </Descriptions>

      <Divider />

      {/* Items Table */}
      <Title level={5}>Chi tiết sản phẩm nhập</Title>
      <Table
        dataSource={items}
        columns={columns}
        rowKey="id"
        pagination={false}
        scroll={{ x: 1100 }}
        summary={() => (
          <Table.Summary>
            <Table.Summary.Row>
              <Table.Summary.Cell index={0} colSpan={9} align="right">
                <strong>Tổng cộng:</strong>
              </Table.Summary.Cell>
              <Table.Summary.Cell index={9} colSpan={2} align="right">
                <strong style={{ color: '#1890ff', fontSize: 16 }}>
                  {(gr.totalAmount || 0).toLocaleString("vi-VN")} ₫
                </strong>
              </Table.Summary.Cell>
            </Table.Summary.Row>
          </Table.Summary>
        )}
      />
    </div>
  );
}
