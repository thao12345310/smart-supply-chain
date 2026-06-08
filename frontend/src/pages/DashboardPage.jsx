import React, { useState, useEffect, useMemo } from 'react';
import {
  Card, Row, Col, Typography, Statistic, Table, Tag, Select, DatePicker,
  Space, Spin, Alert, Tabs, Progress, Tooltip, Badge, Empty
} from 'antd';
import {
  DollarOutlined, ShoppingCartOutlined, InboxOutlined, WarningOutlined,
  RiseOutlined, FallOutlined, FileTextOutlined, TruckOutlined,
  BarChartOutlined, AppstoreOutlined, TeamOutlined, ClockCircleOutlined
} from '@ant-design/icons';
import dayjs from 'dayjs';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip as RTooltip,
  ResponsiveContainer
} from 'recharts';
import { dashboardApi, warehouseApi } from '../services/api';

const { Title, Text } = Typography;
const { RangePicker } = DatePicker;
const { TabPane } = Tabs;

/**
 * DashboardPage - Phân hệ 5: Báo cáo & Dashboard
 *
 * Tabs:
 *  1. Tổng quan (Overview) - Summary cards + Revenue chart + Top Products
 *  2. Nhập-Xuất-Tồn (Inventory Report)
 *  3. Công nợ (Receivables Report)
 */
export default function DashboardPage() {
  const [loading, setLoading] = useState(true);
  const [summary, setSummary] = useState(null);
  const [revenueData, setRevenueData] = useState(null);
  const [topProducts, setTopProducts] = useState([]);
  const [inventoryReport, setInventoryReport] = useState(null);
  const [receivablesReport, setReceivablesReport] = useState(null);
  const [warehouses, setWarehouses] = useState([]);
  const [error, setError] = useState(null);

  // Filters
  const [activeTab, setActiveTab] = useState('overview');
  const thisYear = dayjs().year();
  const [revenueDates, setRevenueDates] = useState([
    dayjs(`${thisYear}-01-01`), dayjs()
  ]);
  const [revenueGroupBy, setRevenueGroupBy] = useState('monthly');
  const [invDates, setInvDates] = useState([
    dayjs().startOf('month'), dayjs()
  ]);
  const [invWarehouse, setInvWarehouse] = useState(null);
  const [overdueOnly, setOverdueOnly] = useState(false);

  // ==================== Data Fetchers ====================

  useEffect(() => {
    loadDashboard();
    loadWarehouses();
  }, []);

  useEffect(() => {
    if (activeTab === 'overview') {
      loadRevenueChart();
      loadTopProducts();
    }
  }, [revenueDates, revenueGroupBy, activeTab]);

  useEffect(() => {
    if (activeTab === 'inventory') loadInventoryReport();
  }, [invDates, invWarehouse, activeTab]);

  useEffect(() => {
    if (activeTab === 'receivables') loadReceivablesReport();
  }, [overdueOnly, activeTab]);

  const loadDashboard = async () => {
    try {
      setLoading(true);
      const res = await dashboardApi.getSummary();
      setSummary(res.data);
    } catch (e) {
      setError('Không thể tải dữ liệu dashboard: ' + (e.message || ''));
    } finally {
      setLoading(false);
    }
  };

  const loadWarehouses = async () => {
    try {
      const res = await warehouseApi.getAll();
      setWarehouses(Array.isArray(res.data) ? res.data : []);
    } catch (e) { /* warehouse list is optional */ }
  };

  const loadRevenueChart = async () => {
    try {
      const [start, end] = revenueDates;
      const res = await dashboardApi.getRevenueChart(
        start.format('YYYY-MM-DD'), end.format('YYYY-MM-DD'), revenueGroupBy
      );
      setRevenueData(res.data);
    } catch (e) { /* silent */ }
  };

  const loadTopProducts = async () => {
    try {
      const [start, end] = revenueDates;
      const res = await dashboardApi.getTopProducts(
        start.format('YYYY-MM-DD'), end.format('YYYY-MM-DD'), 10
      );
      setTopProducts(Array.isArray(res.data) ? res.data : []);
    } catch (e) { /* silent */ }
  };

  const loadInventoryReport = async () => {
    try {
      setLoading(true);
      const [start, end] = invDates;
      const res = await dashboardApi.getInventoryReport(
        start.format('YYYY-MM-DD'), end.format('YYYY-MM-DD'), invWarehouse
      );
      setInventoryReport(res.data);
    } catch (e) {
      setError('Lỗi tải báo cáo tồn kho');
    } finally {
      setLoading(false);
    }
  };

  const loadReceivablesReport = async () => {
    try {
      setLoading(true);
      const res = await dashboardApi.getReceivablesReport(overdueOnly);
      setReceivablesReport(res.data);
    } catch (e) {
      setError('Lỗi tải báo cáo công nợ');
    } finally {
      setLoading(false);
    }
  };

  // ==================== Format helpers ====================
  const fmt = (val) => {
    if (val == null) return '0';
    return Number(val).toLocaleString('vi-VN');
  };

  const fmtMoney = (val) => {
    if (val == null) return '0 ₫';
    return Number(val).toLocaleString('vi-VN') + ' ₫';
  };

  // Compact money formatter for axis ticks: 1.2 tỷ / 850 tr
  const fmtCompact = (val) => {
    const n = Number(val) || 0;
    if (Math.abs(n) >= 1e9) return (n / 1e9).toLocaleString('vi-VN', { maximumFractionDigits: 1 }) + ' tỷ';
    if (Math.abs(n) >= 1e6) return (n / 1e6).toLocaleString('vi-VN', { maximumFractionDigits: 0 }) + ' tr';
    return n.toLocaleString('vi-VN');
  };

  const BAR_COLOR = '#2563eb';

  const renderChartTooltip = ({ active, payload }) => {
    if (!active || !payload || !payload.length) return null;
    const item = payload[0].payload;
    return (
      <div style={{
        background: '#fff', border: '1px solid #e5e7eb', borderRadius: 8,
        padding: '8px 12px', boxShadow: '0 2px 8px rgba(0,0,0,0.12)', fontSize: 13
      }}>
        <div style={{ fontWeight: 600, marginBottom: 4 }}>{item.label}</div>
        <div>Doanh thu: <strong>{fmtMoney(item.revenue)}</strong></div>
        <div style={{ color: '#888' }}>Số đơn: {item.orderCount}</div>
      </div>
    );
  };

  // ==================== RENDER ====================

  if (loading && !summary) {
    return (
      <div style={{ display: 'flex', flexDirection: 'column', gap: 12, justifyContent: 'center', alignItems: 'center', height: '60vh' }}>
        <Spin size="large" />
        <Text type="secondary">Đang tải dữ liệu...</Text>
      </div>
    );
  }

  if (error && !summary) {
    return (
      <div style={{ padding: 24 }}>
        <Alert type="error" message={error} showIcon />
      </div>
    );
  }

  return (
    <div style={{ padding: 24, background: '#f5f5f5', minHeight: '100vh' }}>
      <Tabs
        activeKey={activeTab}
        onChange={setActiveTab}
        size="large"
        tabBarStyle={{ marginBottom: 24 }}
        items={[
          {
            key: 'overview',
            label: <span><BarChartOutlined /> Tổng quan</span>,
            children: renderOverview()
          },
          {
            key: 'inventory',
            label: <span><InboxOutlined /> Nhập – Xuất – Tồn</span>,
            children: renderInventoryReport()
          },
          {
            key: 'receivables',
            label: <span><FileTextOutlined /> Công nợ khách hàng</span>,
            children: renderReceivablesReport()
          }
        ]}
      />
    </div>
  );

  // ====================================================
  // TAB 1: Overview
  // ====================================================
  function renderOverview() {
    if (!summary) return <Spin />;

    const growthIcon = summary.revenueGrowthPercent >= 0 ? <RiseOutlined /> : <FallOutlined />;
    const growthColor = summary.revenueGrowthPercent >= 0 ? '#3f8600' : '#cf1322';

    return (
      <>
        {/* Summary Cards */}
        <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
          <Col xs={24} sm={12} md={6}>
            <Card bordered={false} style={{
              background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
              borderRadius: 16, color: '#fff'
            }}>
              <Statistic
                title={<span style={{ color: 'rgba(255,255,255,0.85)' }}>Doanh thu tháng này</span>}
                value={summary.totalRevenueThisMonth}
                prefix={<DollarOutlined />}
                suffix="₫"
                valueStyle={{ color: '#fff', fontSize: 22 }}
                formatter={(v) => fmt(v)}
              />
              {summary.revenueGrowthPercent != null && (
                <div style={{ marginTop: 8, fontSize: 13, color: 'rgba(255,255,255,0.9)' }}>
                  {growthIcon} {summary.revenueGrowthPercent >= 0 ? '+' : ''}{summary.revenueGrowthPercent?.toFixed(1)}% so với tháng trước
                </div>
              )}
            </Card>
          </Col>
          <Col xs={24} sm={12} md={6}>
            <Card bordered={false} style={{
              background: 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
              borderRadius: 16
            }}>
              <Statistic
                title={<span style={{ color: 'rgba(255,255,255,0.85)' }}>Đơn bán hàng</span>}
                value={summary.totalSalesOrders}
                prefix={<ShoppingCartOutlined />}
                valueStyle={{ color: '#fff', fontSize: 22 }}
              />
              <div style={{ marginTop: 8, fontSize: 13, color: 'rgba(255,255,255,0.9)' }}>
                <Badge count={summary.pendingSalesOrders} showZero size="small"
                  style={{ backgroundColor: '#faad14' }} /> chờ duyệt
              </div>
            </Card>
          </Col>
          <Col xs={24} sm={12} md={6}>
            <Card bordered={false} style={{
              background: 'linear-gradient(135deg, #6a11cb 0%, #2575fc 100%)',
              borderRadius: 16
            }}>
              <Statistic
                title={<span style={{ color: 'rgba(255,255,255,0.85)' }}>Giá trị tồn kho</span>}
                value={summary.totalInventoryValue}
                prefix={<InboxOutlined />}
                suffix="₫"
                valueStyle={{ color: '#fff', fontSize: 22 }}
                formatter={(v) => fmt(v)}
              />
              <div style={{ marginTop: 8, fontSize: 13, color: 'rgba(255,255,255,0.9)' }}>
                <WarningOutlined /> {summary.lowStockItems} sắp hết &bull; {summary.outOfStockItems} hết hàng
              </div>
            </Card>
          </Col>
          <Col xs={24} sm={12} md={6}>
            <Card bordered={false} style={{
              background: summary.totalOverdueReceivables > 0
                ? 'linear-gradient(135deg, #ef4444 0%, #dc2626 100%)'
                : 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)',
              borderRadius: 16
            }}>
              <Statistic
                title={<span style={{ color: 'rgba(255,255,255,0.85)' }}>Công nợ chưa thu</span>}
                value={summary.totalReceivables}
                prefix={<FileTextOutlined />}
                suffix="₫"
                valueStyle={{ color: '#fff', fontSize: 22 }}
                formatter={(v) => fmt(v)}
              />
              <div style={{ marginTop: 8, fontSize: 13, color: 'rgba(255,255,255,0.9)' }}>
                <ClockCircleOutlined /> {summary.overdueInvoiceCount} hóa đơn quá hạn ({fmtMoney(summary.totalOverdueReceivables)})
              </div>
            </Card>
          </Col>
        </Row>

        {/* Revenue Chart + Top Products */}
        <Row gutter={[16, 16]}>
          <Col xs={24} lg={16}>
            <Card
              title={<><BarChartOutlined /> Biểu đồ doanh thu</>}
              bordered={false}
              style={{ borderRadius: 12 }}
              extra={
                <Space size="small">
                  <RangePicker
                    value={revenueDates}
                    onChange={(dates) => dates && setRevenueDates(dates)}
                    format="DD/MM/YYYY"
                    size="small"
                  />
                  <Select value={revenueGroupBy} onChange={setRevenueGroupBy} size="small" style={{ width: 100 }}>
                    <Select.Option value="monthly">Theo tháng</Select.Option>
                    <Select.Option value="daily">Theo ngày</Select.Option>
                  </Select>
                </Space>
              }
            >
              {renderBarChart()}
            </Card>
          </Col>
          <Col xs={24} lg={8}>
            <Card
              title={<><AppstoreOutlined /> Top sản phẩm bán chạy</>}
              bordered={false}
              style={{ borderRadius: 12 }}
            >
              {renderTopProducts()}
            </Card>
          </Col>
        </Row>

        {/* Additional stats row */}
        <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
          <Col xs={24} sm={12}>
            <Card bordered={false} style={{ borderRadius: 12 }}>
              <Statistic
                title="Tổng đơn mua hàng"
                value={summary.totalPurchaseOrders}
                prefix={<ShoppingCartOutlined style={{ color: '#8b5cf6' }} />}
              />
              <div style={{ marginTop: 8 }}>
                <Tag color="warning">{summary.pendingPurchaseOrders} đang chờ duyệt</Tag>
              </div>
            </Card>
          </Col>
          <Col xs={24} sm={12}>
            <Card bordered={false} style={{ borderRadius: 12 }}>
              <Statistic
                title="Tổng doanh thu (tất cả)"
                value={summary.totalRevenue}
                prefix={<DollarOutlined style={{ color: '#10b981' }} />}
                suffix="₫"
                formatter={(v) => fmt(v)}
              />
            </Card>
          </Col>
        </Row>
      </>
    );
  }

  // ==================== Bar Chart (recharts) ====================

  function renderBarChart() {
    if (!revenueData || !revenueData.data || revenueData.data.length === 0) {
      return <Empty description="Chưa có dữ liệu doanh thu" />;
    }

    const chartData = revenueData.data.map(d => ({
      label: d.label,
      shortLabel: String(d.label).replace(/^\d{4}-/, ''),
      revenue: Number(d.revenue) || 0,
      orderCount: d.orderCount
    }));

    return (
      <div>
        <div style={{ marginBottom: 12, textAlign: 'right' }}>
          <Text strong style={{ fontSize: 16 }}>
            Tổng: {fmtMoney(revenueData.total)}
          </Text>
        </div>
        <ResponsiveContainer width="100%" height={260}>
          <BarChart data={chartData} margin={{ top: 8, right: 8, left: 8, bottom: 0 }}>
            <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#eef0f3" />
            <XAxis
              dataKey="shortLabel"
              tick={{ fontSize: 12, fill: '#6b7280' }}
              axisLine={{ stroke: '#e5e7eb' }}
              tickLine={false}
            />
            <YAxis
              tickFormatter={fmtCompact}
              tick={{ fontSize: 12, fill: '#6b7280' }}
              axisLine={false}
              tickLine={false}
              width={56}
            />
            <RTooltip content={renderChartTooltip} cursor={{ fill: 'rgba(37,99,235,0.06)' }} />
            <Bar dataKey="revenue" fill={BAR_COLOR} radius={[4, 4, 0, 0]} maxBarSize={48} />
          </BarChart>
        </ResponsiveContainer>
      </div>
    );
  }

  // ==================== Top Products ====================

  function renderTopProducts() {
    if (!topProducts || topProducts.length === 0) {
      return <Empty description="Chưa có dữ liệu" />;
    }

    const maxRev = Math.max(...topProducts.map(p => Number(p.totalRevenue) || 0), 1);

    return (
      <div style={{ maxHeight: 360, overflow: 'auto' }}>
        {topProducts.map((product, idx) => (
          <div key={idx} style={{
            padding: '10px 0',
            borderBottom: idx < topProducts.length - 1 ? '1px solid #f0f0f0' : 'none'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
              <Text strong style={{ fontSize: 13 }}>
                <Badge count={idx + 1} size="small"
                  style={{
                    backgroundColor: idx < 3 ? '#1890ff' : '#d9d9d9',
                    marginRight: 8
                  }}
                />
                {product.productName}
              </Text>
              <Text type="secondary" style={{ fontSize: 12 }}>
                SL: {fmt(product.totalQuantitySold)}
              </Text>
            </div>
            <Progress
              percent={Math.round((Number(product.totalRevenue) / maxRev) * 100)}
              format={() => fmtMoney(product.totalRevenue)}
              strokeColor={idx < 3 ? '#1890ff' : '#8c8c8c'}
              size="small"
            />
          </div>
        ))}
      </div>
    );
  }

  // ====================================================
  // TAB 2: Inventory Report
  // ====================================================
  function renderInventoryReport() {
    const columns = [
      { title: 'Mã SP', dataIndex: 'productCode', key: 'code', width: 100, fixed: 'left' },
      { title: 'Tên sản phẩm', dataIndex: 'productName', key: 'name', width: 200 },
      {
        title: 'Tồn đầu kỳ', dataIndex: 'openingStock', key: 'opening', width: 100,
        render: (v) => <span style={{ color: '#8c8c8c' }}>{fmt(v)}</span>
      },
      {
        title: 'Nhập', dataIndex: 'totalReceived', key: 'received', width: 90,
        render: (v) => v > 0 ? <Tag color="green">+{fmt(v)}</Tag> : <span>0</span>
      },
      {
        title: 'Xuất', dataIndex: 'totalIssued', key: 'issued', width: 90,
        render: (v) => v > 0 ? <Tag color="red">-{fmt(v)}</Tag> : <span>0</span>
      },
      {
        title: 'Tồn cuối kỳ', dataIndex: 'closingStock', key: 'closing', width: 100,
        render: (v) => (
          <Text strong style={{ color: v <= 0 ? '#cf1322' : v <= 10 ? '#faad14' : '#3f8600' }}>
            {fmt(v)}
          </Text>
        )
      },
      {
        title: 'Giá vốn TB', dataIndex: 'averageCost', key: 'avgCost', width: 120,
        render: (v) => fmtMoney(v)
      },
      {
        title: 'Giá trị tồn', dataIndex: 'closingValue', key: 'closingValue', width: 140,
        render: (v) => <Text strong>{fmtMoney(v)}</Text>
      },
    ];

    return (
      <div>
        {/* Filters */}
        <Card bordered={false} style={{ borderRadius: 12, marginBottom: 16 }}>
          <Space wrap>
            <span>Kỳ báo cáo:</span>
            <RangePicker
              value={invDates}
              onChange={(dates) => dates && setInvDates(dates)}
              format="DD/MM/YYYY"
            />
            <span>Kho:</span>
            <Select
              value={invWarehouse}
              onChange={setInvWarehouse}
              allowClear
              placeholder="Tất cả kho"
              style={{ width: 200 }}
            >
              {warehouses.map(w => (
                <Select.Option key={w.id} value={w.id}>{w.name}</Select.Option>
              ))}
            </Select>
          </Space>
        </Card>

        {/* Summary Cards */}
        {inventoryReport && (
          <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
            <Col xs={12} sm={6}>
              <Card bordered={false} size="small" style={{ borderRadius: 12 }}>
                <Statistic title="Tổng sản phẩm" value={inventoryReport.totalProducts}
                  prefix={<AppstoreOutlined />} />
              </Card>
            </Col>
            <Col xs={12} sm={6}>
              <Card bordered={false} size="small" style={{ borderRadius: 12 }}>
                <Statistic title="Tổng SL tồn" value={inventoryReport.totalQuantityOnHand}
                  prefix={<InboxOutlined />} />
              </Card>
            </Col>
            <Col xs={12} sm={6}>
              <Card bordered={false} size="small" style={{ borderRadius: 12 }}>
                <Statistic title="Sắp hết hàng" value={inventoryReport.lowStockCount}
                  prefix={<WarningOutlined />}
                  valueStyle={{ color: '#faad14' }} />
              </Card>
            </Col>
            <Col xs={12} sm={6}>
              <Card bordered={false} size="small" style={{ borderRadius: 12 }}>
                <Statistic
                  title="Tổng giá trị tồn"
                  value={inventoryReport.totalInventoryValue}
                  suffix="₫"
                  formatter={(v) => fmt(v)}
                  prefix={<DollarOutlined />}
                />
              </Card>
            </Col>
          </Row>
        )}

        {/* Table */}
        <Card bordered={false} style={{ borderRadius: 12 }}>
          <Table
            dataSource={inventoryReport?.items || []}
            columns={columns}
            rowKey="productId"
            loading={loading}
            scroll={{ x: 1000 }}
            pagination={{ pageSize: 20, showSizeChanger: true, showTotal: (t) => `Tổng ${t} sản phẩm` }}
            expandable={{
              expandedRowRender: (record) => (
                <div style={{ padding: '0 16px 16px' }}>
                  <Text strong>Chi tiết theo kho:</Text>
                  <Table
                    size="small"
                    dataSource={record.warehouseBreakdown || []}
                    columns={[
                      { title: 'Kho', dataIndex: 'warehouseName' },
                      { title: 'Tồn kho', dataIndex: 'quantity', render: (v) => fmt(v) },
                      { title: 'Đã đặt', dataIndex: 'reserved', render: (v) => fmt(v) },
                      { title: 'Khả dụng', dataIndex: 'available', render: (v) => fmt(v) },
                    ]}
                    rowKey="warehouseId"
                    pagination={false}
                    style={{ marginTop: 8 }}
                  />
                </div>
              ),
              rowExpandable: (r) => r.warehouseBreakdown && r.warehouseBreakdown.length > 0,
            }}
          />
        </Card>
      </div>
    );
  }

  // ====================================================
  // TAB 3: Receivables Report
  // ====================================================
  function renderReceivablesReport() {
    const columns = [
      {
        title: 'Khách hàng', key: 'customer', width: 220, fixed: 'left',
        render: (_, r) => (
          <div>
            <Text strong>{r.customerName}</Text>
            <br />
            <Text type="secondary" style={{ fontSize: 12 }}>{r.customerCode}</Text>
          </div>
        )
      },
      { title: 'SĐT', dataIndex: 'phone', key: 'phone', width: 120 },
      {
        title: 'Tổng HĐ', dataIndex: 'totalInvoiceAmount', key: 'total', width: 140,
        render: (v) => fmtMoney(v)
      },
      {
        title: 'Đã thanh toán', dataIndex: 'totalPaidAmount', key: 'paid', width: 140,
        render: (v) => <Text style={{ color: '#3f8600' }}>{fmtMoney(v)}</Text>
      },
      {
        title: 'Còn nợ', dataIndex: 'outstandingAmount', key: 'outstanding', width: 140,
        render: (v) => <Text strong style={{ color: '#cf1322' }}>{fmtMoney(v)}</Text>
      },
      {
        title: 'Số HĐ', dataIndex: 'totalInvoices', key: 'count', width: 80,
        render: (v) => <Tag>{v}</Tag>
      },
      {
        title: 'Quá hạn', dataIndex: 'overdueInvoices', key: 'overdue', width: 90,
        render: (v, r) => v > 0
          ? <Tag color="red">{v} HĐ ({r.maxOverdueDays} ngày)</Tag>
          : <Tag color="green">Không</Tag>
      },
    ];

    return (
      <div>
        {/* Filters */}
        <Card bordered={false} style={{ borderRadius: 12, marginBottom: 16 }}>
          <Space>
            <span>Lọc:</span>
            <Select value={overdueOnly} onChange={setOverdueOnly} style={{ width: 220 }}>
              <Select.Option value={false}>Tất cả khách hàng có công nợ</Select.Option>
              <Select.Option value={true}>Chỉ khách hàng quá hạn</Select.Option>
            </Select>
          </Space>
        </Card>

        {/* Summary Cards */}
        {receivablesReport && (
          <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
            <Col xs={12} sm={6}>
              <Card bordered={false} size="small" style={{ borderRadius: 12 }}>
                <Statistic title="Tổng công nợ" value={receivablesReport.totalOutstanding}
                  suffix="₫" formatter={(v) => fmt(v)}
                  prefix={<DollarOutlined />}
                  valueStyle={{ color: '#cf1322' }} />
              </Card>
            </Col>
            <Col xs={12} sm={6}>
              <Card bordered={false} size="small" style={{ borderRadius: 12 }}>
                <Statistic title="Nợ quá hạn" value={receivablesReport.totalOverdue}
                  suffix="₫" formatter={(v) => fmt(v)}
                  prefix={<WarningOutlined />}
                  valueStyle={{ color: '#cf1322' }} />
              </Card>
            </Col>
            <Col xs={12} sm={6}>
              <Card bordered={false} size="small" style={{ borderRadius: 12 }}>
                <Statistic title="Khách hàng có nợ" value={receivablesReport.totalCustomersWithDebt}
                  prefix={<TeamOutlined />} />
              </Card>
            </Col>
            <Col xs={12} sm={6}>
              <Card bordered={false} size="small" style={{ borderRadius: 12 }}>
                <Statistic title="HĐ quá hạn" value={receivablesReport.totalOverdueInvoices}
                  prefix={<ClockCircleOutlined />}
                  valueStyle={{ color: '#faad14' }} />
              </Card>
            </Col>
          </Row>
        )}

        {/* Table */}
        <Card bordered={false} style={{ borderRadius: 12 }}>
          <Table
            dataSource={receivablesReport?.customers || []}
            columns={columns}
            rowKey="customerId"
            loading={loading}
            scroll={{ x: 1000 }}
            pagination={{ pageSize: 20, showSizeChanger: true, showTotal: (t) => `Tổng ${t} khách hàng` }}
            expandable={{
              expandedRowRender: (record) => (
                <div style={{ padding: '0 16px 16px' }}>
                  <Text strong>Chi tiết hóa đơn:</Text>
                  <Table
                    size="small"
                    dataSource={record.invoices || []}
                    columns={[
                      { title: 'Mã HĐ', dataIndex: 'invoiceCode' },
                      { title: 'Mã SO', dataIndex: 'salesOrderCode' },
                      { title: 'Ngày', dataIndex: 'invoiceDate', render: (v) => v ? dayjs(v).format('DD/MM/YYYY') : '-' },
                      { title: 'Hạn TT', dataIndex: 'dueDate', render: (v) => v ? dayjs(v).format('DD/MM/YYYY') : '-' },
                      {
                        title: 'Trạng thái', dataIndex: 'status',
                        render: (v) => {
                          const colorMap = {
                            'DRAFT': 'default', 'ISSUED': 'processing',
                            'PARTIALLY_PAID': 'warning', 'PAID': 'success',
                            'CANCELLED': 'error', 'OVERDUE': 'error'
                          };
                          return <Tag color={colorMap[v] || 'default'}>{v}</Tag>;
                        }
                      },
                      { title: 'Tổng tiền', dataIndex: 'totalAmount', render: (v) => fmtMoney(v) },
                      { title: 'Đã trả', dataIndex: 'paidAmount', render: (v) => fmtMoney(v) },
                      {
                        title: 'Còn lại', dataIndex: 'remainingAmount',
                        render: (v) => <Text strong style={{ color: '#cf1322' }}>{fmtMoney(v)}</Text>
                      },
                      {
                        title: 'Quá hạn', dataIndex: 'overdueDays',
                        render: (v) => v > 0 ? <Tag color="red">{v} ngày</Tag> : '-'
                      },
                    ]}
                    rowKey="invoiceId"
                    pagination={false}
                    style={{ marginTop: 8 }}
                  />
                </div>
              ),
              rowExpandable: (r) => r.invoices && r.invoices.length > 0,
            }}
          />
        </Card>
      </div>
    );
  }
}
