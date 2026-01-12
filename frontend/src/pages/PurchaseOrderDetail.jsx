import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { 
  Tabs, Button, Descriptions, Table, message, Tag, Modal, 
  Input, Space, Card, Progress, Divider, Typography, Alert,
  Statistic, Row, Col
} from "antd";
import { 
  CheckCircleOutlined, CloseCircleOutlined, EditOutlined,
  DeleteOutlined, PrinterOutlined, CopyOutlined, InboxOutlined,
  ArrowLeftOutlined
} from "@ant-design/icons";
import { purchaseOrderApi, goodsReceiptApi } from "../services/api";
import dayjs from "dayjs";
import GoodsReceiptForm from "./GoodsReceiptForm";

const { TextArea } = Input;
const { Title, Text } = Typography;

// Status configuration
const STATUS_CONFIG = {
  ORDER_OPEN: { color: "blue", label: "Chờ duyệt" },
  ORDER_APPROVED: { color: "green", label: "Đã duyệt" },
  ORDER_PARTIALLY_RECEIVED: { color: "orange", label: "Nhận một phần" },
  ORDER_COMPLETED: { color: "cyan", label: "Hoàn thành" },
  ORDER_CANCELLED: { color: "default", label: "Đã hủy" },
};

export default function PurchaseOrderDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [po, setPo] = useState(null);
  const [items, setItems] = useState([]);
  const [goodsReceipts, setGoodsReceipts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [rejectModalVisible, setRejectModalVisible] = useState(false);
  const [rejectReason, setRejectReason] = useState("");
  const [grFormVisible, setGrFormVisible] = useState(false);

  const fetchDetail = async () => {
    setLoading(true);
    try {
      const res = await purchaseOrderApi.getById(id);
      setPo(res.data);
      setItems(res.data.items || []);
    } catch (e) {
      console.error(e);
      message.error("Lỗi tải đơn mua");
    } finally {
      setLoading(false);
    }
  };

  const fetchGoodsReceipts = async () => {
    try {
      const res = await goodsReceiptApi.getByPurchaseOrderId(id);
      setGoodsReceipts(res.data || []);
    } catch (e) {
      console.error(e);
    }
  };

  useEffect(() => {
    fetchDetail();
    fetchGoodsReceipts();
  }, [id]);

  const handleApprove = async () => {
    Modal.confirm({
      title: "Xác nhận duyệt đơn hàng",
      content: "Bạn có chắc muốn duyệt đơn hàng này?",
      okText: "Duyệt",
      cancelText: "Hủy",
      onOk: async () => {
        try {
          await purchaseOrderApi.approve(id);
          message.success("Duyệt đơn thành công!");
          fetchDetail();
        } catch (e) {
          message.error(e.message || "Không thể duyệt đơn");
        }
      },
    });
  };

  const handleReject = async () => {
    if (!rejectReason.trim()) {
      message.warning("Vui lòng nhập lý do từ chối");
      return;
    }
    try {
      await purchaseOrderApi.reject(id, null, rejectReason);
      message.success("Đã từ chối đơn hàng");
      setRejectModalVisible(false);
      setRejectReason("");
      fetchDetail();
    } catch (e) {
      message.error(e.message || "Không thể từ chối đơn");
    }
  };

  const handleCancel = async () => {
    Modal.confirm({
      title: "Xác nhận hủy đơn hàng",
      content: "Bạn có chắc muốn hủy đơn hàng này?",
      okText: "Hủy đơn",
      okButtonProps: { danger: true },
      cancelText: "Quay lại",
      onOk: async () => {
        try {
          await purchaseOrderApi.cancel(id, "Hủy bởi người dùng");
          message.success("Đã hủy đơn hàng");
          fetchDetail();
        } catch (e) {
          message.error(e.message || "Không thể hủy đơn");
        }
      },
    });
  };

  const handleDelete = async () => {
    Modal.confirm({
      title: "Xác nhận xóa đơn hàng",
      content: "Bạn có chắc muốn xóa đơn hàng này? Thao tác này không thể hoàn tác.",
      okText: "Xóa",
      okButtonProps: { danger: true },
      cancelText: "Hủy",
      onOk: async () => {
        try {
          await purchaseOrderApi.delete(id);
          message.success("Xóa đơn thành công!");
          navigate("/");
        } catch (e) {
          message.error(e.message || "Không thể xóa đơn");
        }
      },
    });
  };

  const handleGoodsReceiptSaved = () => {
    setGrFormVisible(false);
    fetchDetail();
    fetchGoodsReceipts();
    message.success("Tạo phiếu nhập thành công!");
  };

  const itemColumns = [
    { 
      title: "STT", 
      width: 60,
      render: (_, __, index) => index + 1 
    },
    { 
      title: "Mã SP", 
      dataIndex: "productCode",
      width: 120,
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
      dataIndex: "quantity",
      width: 80,
      align: "center",
    },
    { 
      title: "SL đã nhận", 
      dataIndex: "receivedQuantity",
      width: 100,
      align: "center",
      render: (val, record) => (
        <span style={{ 
          color: val >= record.quantity ? '#52c41a' : val > 0 ? '#faad14' : '#999' 
        }}>
          {val || 0}
        </span>
      ),
    },
    { 
      title: "Còn lại", 
      dataIndex: "remainingQuantity",
      width: 80,
      align: "center",
      render: (val) => (
        <span style={{ color: val > 0 ? '#ff4d4f' : '#52c41a' }}>
          {val || 0}
        </span>
      ),
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
      width: 130,
      align: "right",
      render: (_, record) => {
        const amount = (record.unitPrice || 0) * (record.quantity || 0);
        return `${amount.toLocaleString("vi-VN")} ₫`;
      },
    },
  ];

  const grColumns = [
    { 
      title: "Mã phiếu", 
      dataIndex: "code",
      render: (text, record) => (
        <a onClick={() => navigate(`/goods-receipts/${record.id}`)}>
          {text}
        </a>
      ),
    },
    { 
      title: "Ngày nhận", 
      dataIndex: "receiptDate",
      render: (text) => text ? dayjs(text).format("DD/MM/YYYY") : "-",
    },
    {
      title: "Trạng thái",
      dataIndex: "status",
      render: (status) => {
        const colors = { DRAFT: "blue", CONFIRMED: "green", CANCELLED: "default" };
        const labels = { DRAFT: "Nháp", CONFIRMED: "Đã xác nhận", CANCELLED: "Đã hủy" };
        return <Tag color={colors[status]}>{labels[status]}</Tag>;
      },
    },
    { 
      title: "SL nhận", 
      dataIndex: "totalReceivedQuantity",
      align: "center",
    },
    { 
      title: "Tổng tiền", 
      dataIndex: "totalAmount",
      align: "right",
      render: (val) => val ? `${val.toLocaleString("vi-VN")} ₫` : "-",
    },
  ];

  if (loading || !po) {
    return <div style={{ padding: 20 }}>Đang tải...</div>;
  }

  const statusConfig = STATUS_CONFIG[po.status] || { color: "default", label: po.status };
  const canApprove = po.status === "ORDER_OPEN";
  const canReceiveGoods = po.status === "ORDER_APPROVED" || po.status === "ORDER_PARTIALLY_RECEIVED";
  const canEdit = po.status === "ORDER_OPEN";
  const canCancel = po.status === "ORDER_OPEN" || po.status === "ORDER_APPROVED";
  const receivedPercentage = po.receivedPercentage || 0;

  const tabItems = [
    {
      key: "1",
      label: "Tổng quan",
      children: (
        <>
          {/* Status Alert */}
          {po.status === "ORDER_CANCELLED" && po.rejectionReason && (
            <Alert
              message="Đơn hàng đã bị hủy"
              description={`Lý do: ${po.rejectionReason}`}
              type="error"
              showIcon
              style={{ marginBottom: 16 }}
            />
          )}

          {/* Summary Stats */}
          <Row gutter={16} style={{ marginBottom: 20 }}>
            <Col span={6}>
              <Card>
                <Statistic 
                  title="Tổng tiền đơn hàng" 
                  value={po.totalAmount || 0} 
                  suffix="₫"
                  formatter={(val) => val.toLocaleString("vi-VN")}
                />
              </Card>
            </Col>
            <Col span={6}>
              <Card>
                <Statistic 
                  title="Số sản phẩm" 
                  value={po.totalItems || items.length} 
                />
              </Card>
            </Col>
            <Col span={6}>
              <Card>
                <Statistic 
                  title="Số phiếu nhập" 
                  value={goodsReceipts.length} 
                />
              </Card>
            </Col>
            <Col span={6}>
              <Card>
                <div style={{ marginBottom: 8 }}>Tiến độ nhận hàng</div>
                <Progress 
                  percent={receivedPercentage} 
                  status={receivedPercentage >= 100 ? "success" : "active"}
                  format={(p) => `${p.toFixed(0)}%`}
                />
              </Card>
            </Col>
          </Row>

          {/* Order Info */}
          <Descriptions bordered column={2} title="Thông tin đơn hàng">
            <Descriptions.Item label="Mã đơn">{po.code}</Descriptions.Item>
            <Descriptions.Item label="Trạng thái">
              <Tag color={statusConfig.color}>{statusConfig.label}</Tag>
            </Descriptions.Item>
            <Descriptions.Item label="Tên đơn hàng">{po.orderName || "-"}</Descriptions.Item>
            <Descriptions.Item label="Ngày tạo">
              {po.createdDate ? dayjs(po.createdDate).format("DD/MM/YYYY") : "-"}
            </Descriptions.Item>
            <Descriptions.Item label="Nhà cung cấp">{po.supplierName}</Descriptions.Item>
            <Descriptions.Item label="Kho nhập">{po.warehouseName}</Descriptions.Item>
            <Descriptions.Item label="Ngày giao dự kiến">
              {po.deliveryDate ? dayjs(po.deliveryDate).format("DD/MM/YYYY HH:mm") : "-"}
            </Descriptions.Item>
            <Descriptions.Item label="Loại thuế">{po.taxType || "-"}</Descriptions.Item>
            {po.approvedDate && (
              <Descriptions.Item label="Ngày duyệt">
                {dayjs(po.approvedDate).format("DD/MM/YYYY HH:mm")}
              </Descriptions.Item>
            )}
            {po.completedDate && (
              <Descriptions.Item label="Ngày hoàn thành">
                {dayjs(po.completedDate).format("DD/MM/YYYY HH:mm")}
              </Descriptions.Item>
            )}
            {po.notes && (
              <Descriptions.Item label="Ghi chú" span={2}>{po.notes}</Descriptions.Item>
            )}
          </Descriptions>

          <Divider />

          <Title level={5}>Danh sách sản phẩm</Title>
          <Table
            dataSource={items}
            columns={itemColumns}
            rowKey="id"
            pagination={false}
            summary={() => (
              <Table.Summary>
                <Table.Summary.Row>
                  <Table.Summary.Cell index={0} colSpan={7} align="right">
                    <strong>Chi phí vận chuyển:</strong>
                  </Table.Summary.Cell>
                  <Table.Summary.Cell index={7} colSpan={2} align="right">
                    <strong>{(po.shippingCost || 0).toLocaleString("vi-VN")} ₫</strong>
                  </Table.Summary.Cell>
                </Table.Summary.Row>
                <Table.Summary.Row>
                  <Table.Summary.Cell index={0} colSpan={7} align="right">
                    <strong>Tổng cộng:</strong>
                  </Table.Summary.Cell>
                  <Table.Summary.Cell index={7} colSpan={2} align="right">
                    <strong style={{ color: '#1890ff', fontSize: 16 }}>
                      {(po.totalAmount || 0).toLocaleString("vi-VN")} ₫
                    </strong>
                  </Table.Summary.Cell>
                </Table.Summary.Row>
              </Table.Summary>
            )}
          />
        </>
      ),
    },
    {
      key: "2",
      label: "Phiếu nhập kho",
      children: (
        <>
          {canReceiveGoods && (
            <div style={{ marginBottom: 16 }}>
              <Button 
                type="primary" 
                icon={<InboxOutlined />}
                onClick={() => setGrFormVisible(true)}
              >
                Tạo phiếu nhập mới
              </Button>
            </div>
          )}
          
          {goodsReceipts.length > 0 ? (
            <Table
              dataSource={goodsReceipts}
              columns={grColumns}
              rowKey="id"
              pagination={false}
            />
          ) : (
            <div style={{ textAlign: "center", padding: 40, color: "#999" }}>
              Chưa có phiếu nhập nào
            </div>
          )}
        </>
      ),
    },
    {
      key: "3",
      label: "Sản phẩm",
      children: (
        <Table 
          dataSource={items} 
          columns={itemColumns} 
          rowKey="id" 
          pagination={false}
        />
      ),
    },
    {
      key: "4",
      label: "Thanh toán",
      children: <div style={{ textAlign: "center", padding: 40, color: "#999" }}>Đang cập nhật...</div>,
    },
    {
      key: "5",
      label: "Lịch sử",
      children: <div style={{ textAlign: "center", padding: 40, color: "#999" }}>Đang cập nhật...</div>,
    },
  ];

  return (
    <div style={{ padding: 20 }}>
      {/* Header */}
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 20 }}>
        <Space>
          <Button icon={<ArrowLeftOutlined />} onClick={() => navigate("/")}>
            Quay lại
          </Button>
          <Title level={3} style={{ margin: 0 }}>
            Chi tiết đơn mua hàng: {po.code}
          </Title>
          <Tag color={statusConfig.color} style={{ fontSize: 14, padding: "4px 12px" }}>
            {statusConfig.label}
          </Tag>
        </Space>
      </div>

      {/* Action Buttons */}
      <Card style={{ marginBottom: 20 }}>
        <Space wrap>
          {canEdit && (
            <Button type="primary" icon={<EditOutlined />}>
              Chỉnh sửa
            </Button>
          )}

          {canApprove && (
            <>
              <Button 
                type="primary" 
                icon={<CheckCircleOutlined />}
                onClick={handleApprove}
                style={{ background: '#52c41a', borderColor: '#52c41a' }}
              >
                Duyệt đơn
              </Button>
              <Button 
                danger 
                icon={<CloseCircleOutlined />}
                onClick={() => setRejectModalVisible(true)}
              >
                Từ chối
              </Button>
            </>
          )}

          {canReceiveGoods && (
            <Button 
              type="primary"
              icon={<InboxOutlined />}
              onClick={() => setGrFormVisible(true)}
            >
              Tạo phiếu nhập
            </Button>
          )}

          <Button icon={<CopyOutlined />}>Sao chép</Button>
          <Button icon={<PrinterOutlined />}>In</Button>

          {canCancel && (
            <Button danger onClick={handleCancel}>
              Hủy đơn
            </Button>
          )}

          {canEdit && (
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

      {/* Tabs Content */}
      <Tabs items={tabItems} />

      {/* Reject Modal */}
      <Modal
        title="Từ chối đơn hàng"
        open={rejectModalVisible}
        onOk={handleReject}
        onCancel={() => {
          setRejectModalVisible(false);
          setRejectReason("");
        }}
        okText="Xác nhận từ chối"
        cancelText="Hủy"
        okButtonProps={{ danger: true }}
      >
        <div style={{ marginBottom: 8 }}>Vui lòng nhập lý do từ chối:</div>
        <TextArea
          rows={4}
          value={rejectReason}
          onChange={(e) => setRejectReason(e.target.value)}
          placeholder="Nhập lý do..."
        />
      </Modal>

      {/* Goods Receipt Form */}
      <GoodsReceiptForm
        visible={grFormVisible}
        purchaseOrderId={po.id}
        onCancel={() => setGrFormVisible(false)}
        onSuccess={handleGoodsReceiptSaved}
      />
    </div>
  );
}
