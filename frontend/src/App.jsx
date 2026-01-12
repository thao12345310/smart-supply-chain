import React from "react";
import { BrowserRouter, Routes, Route, useNavigate, useLocation } from "react-router-dom";
import { Layout, Menu, Typography, Space, Badge } from "antd";
import {
  ShoppingCartOutlined,
  InboxOutlined,
  ShopOutlined,
  TeamOutlined,
  AppstoreOutlined,
  TruckOutlined,
  DashboardOutlined,
  DollarOutlined,
  FileTextOutlined,
  UserOutlined,
} from "@ant-design/icons";

// Import pages - Purchasing Module
import ProductList from "./pages/ProductList";
import SupplierList from "./pages/SupplierList";
import PurchaseOrderList from "./pages/PurchaseOrderList";
import PurchaseOrderDetail from "./pages/PurchaseOrderDetail";
import GoodsReceiptList from "./pages/GoodsReceiptList";
import GoodsReceiptDetail from "./pages/GoodsReceiptDetail";
import InventoryList from "./pages/InventoryList";
import DeliveryPlanList from "./pages/DeliveryPlanList";
import DeliveryPlanDetail from "./pages/DeliveryPlanDetail";

// Import pages - Sales Module
import CustomerList from "./pages/CustomerList";
import SalesOrderList from "./pages/SalesOrderList";
import SalesOrderForm from "./pages/SalesOrderForm";
import SalesOrderDetail from "./pages/SalesOrderDetail";
import GoodsIssueList from "./pages/GoodsIssueList";
import GoodsIssueForm from "./pages/GoodsIssueForm";
import GoodsIssueDetail from "./pages/GoodsIssueDetail";
import SalesInvoiceList from "./pages/SalesInvoiceList";
import SalesInvoiceDetail from "./pages/SalesInvoiceDetail";

const { Header, Sider, Content } = Layout;
const { Title } = Typography;

// Main Layout Component with Sidebar Navigation
function MainLayout({ children }) {
  const navigate = useNavigate();
  const location = useLocation();

  // Determine selected key from current path
  const getSelectedKey = () => {
    const path = location.pathname;
    if (path.includes('/purchase-orders')) return 'purchase-orders';
    if (path.includes('/goods-receipts')) return 'goods-receipts';
    if (path.includes('/inventory')) return 'inventory';
    if (path.includes('/products')) return 'products';
    if (path.includes('/suppliers')) return 'suppliers';
    if (path.includes('/delivery-plans')) return 'delivery-plans';
    if (path.includes('/customers')) return 'customers';
    if (path.includes('/sales-orders')) return 'sales-orders';
    if (path.includes('/goods-issues')) return 'goods-issues';
    if (path.includes('/sales-invoices')) return 'sales-invoices';
    if (path === '/') return 'dashboard';
    return 'dashboard';
  };

  const menuItems = [
    {
      key: 'dashboard',
      icon: <DashboardOutlined />,
      label: 'Tổng quan',
      onClick: () => navigate('/'),
    },
    {
      type: 'divider',
    },
    {
      key: 'sales',
      icon: <DollarOutlined />,
      label: 'Bán hàng',
      children: [
        {
          key: 'sales-orders',
          label: 'Đơn bán hàng',
          onClick: () => navigate('/sales-orders'),
        },
        {
          key: 'goods-issues',
          label: 'Phiếu xuất kho',
          onClick: () => navigate('/goods-issues'),
        },
        {
          key: 'sales-invoices',
          label: 'Hóa đơn',
          onClick: () => navigate('/sales-invoices'),
        },
      ],
    },
    {
      key: 'purchasing',
      icon: <ShoppingCartOutlined />,
      label: 'Mua hàng',
      children: [
        {
          key: 'purchase-orders',
          label: 'Đơn mua hàng',
          onClick: () => navigate('/purchase-orders'),
        },
        {
          key: 'goods-receipts',
          label: 'Phiếu nhập kho',
          onClick: () => navigate('/goods-receipts'),
        },
      ],
    },
    {
      key: 'warehouse',
      icon: <InboxOutlined />,
      label: 'Kho hàng',
      children: [
        {
          key: 'inventory',
          label: 'Tồn kho',
          onClick: () => navigate('/inventory'),
        },
      ],
    },
    {
      key: 'master-data',
      icon: <AppstoreOutlined />,
      label: 'Danh mục',
      children: [
        {
          key: 'products',
          label: 'Sản phẩm',
          onClick: () => navigate('/products'),
        },
        {
          key: 'suppliers',
          label: 'Nhà cung cấp',
          onClick: () => navigate('/suppliers'),
        },
        {
          key: 'customers',
          label: 'Khách hàng',
          onClick: () => navigate('/customers'),
        },
      ],
    },
    {
      key: 'delivery',
      icon: <TruckOutlined />,
      label: 'Giao hàng',
      children: [
        {
          key: 'delivery-plans',
          label: 'Kế hoạch giao hàng',
          onClick: () => navigate('/delivery-plans'),
        },
      ],
    },
  ];

  return (
    <Layout style={{ minHeight: '100vh' }}>
      {/* Sidebar */}
      <Sider
        width={240}
        theme="light"
        style={{
          overflow: 'auto',
          height: '100vh',
          position: 'fixed',
          left: 0,
          top: 0,
          bottom: 0,
          borderRight: '1px solid #f0f0f0',
        }}
      >
        <div style={{ 
          padding: '16px 20px', 
          borderBottom: '1px solid #f0f0f0',
          background: 'linear-gradient(135deg, #1890ff 0%, #096dd9 100%)',
        }}>
          <Title level={4} style={{ margin: 0, color: 'white' }}>
            <ShopOutlined style={{ marginRight: 8 }} />
            DMS
          </Title>
          <div style={{ fontSize: 12, color: 'rgba(255,255,255,0.8)' }}>
            Distribution Management
          </div>
        </div>
        <Menu
          mode="inline"
          selectedKeys={[getSelectedKey()]}
          defaultOpenKeys={['sales', 'purchasing', 'warehouse', 'master-data']}
          style={{ borderRight: 0, paddingTop: 8 }}
          items={menuItems}
        />
      </Sider>

      {/* Main Content */}
      <Layout style={{ marginLeft: 240 }}>
        <Content style={{ 
          background: '#f5f5f5', 
          minHeight: '100vh',
        }}>
          {children}
        </Content>
      </Layout>
    </Layout>
  );
}

// Dashboard Component
function Dashboard() {
  const navigate = useNavigate();

  return (
    <div style={{ padding: 24 }}>
      <Title level={2}>Hệ thống quản lý phân phối</Title>
      <p style={{ color: '#666', marginBottom: 24 }}>
        Chào mừng bạn đến với hệ thống quản lý phân phối. Chọn module từ menu bên trái để bắt đầu.
      </p>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 16 }}>
        {/* Sales Orders Card */}
        <div
          onClick={() => navigate('/sales-orders')}
          style={{
            background: 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
            borderRadius: 12,
            padding: 24,
            cursor: 'pointer',
            color: 'white',
            transition: 'transform 0.2s',
          }}
          onMouseEnter={(e) => e.currentTarget.style.transform = 'scale(1.02)'}
          onMouseLeave={(e) => e.currentTarget.style.transform = 'scale(1)'}
        >
          <DollarOutlined style={{ fontSize: 32, marginBottom: 12 }} />
          <div style={{ fontSize: 18, fontWeight: 600 }}>Đơn bán hàng</div>
          <div style={{ opacity: 0.8 }}>Quản lý đơn hàng bán ra</div>
        </div>

        {/* Purchase Orders Card */}
        <div
          onClick={() => navigate('/purchase-orders')}
          style={{
            background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
            borderRadius: 12,
            padding: 24,
            cursor: 'pointer',
            color: 'white',
            transition: 'transform 0.2s',
          }}
          onMouseEnter={(e) => e.currentTarget.style.transform = 'scale(1.02)'}
          onMouseLeave={(e) => e.currentTarget.style.transform = 'scale(1)'}
        >
          <ShoppingCartOutlined style={{ fontSize: 32, marginBottom: 12 }} />
          <div style={{ fontSize: 18, fontWeight: 600 }}>Đơn mua hàng</div>
          <div style={{ opacity: 0.8 }}>Quản lý đơn hàng mua từ nhà cung cấp</div>
        </div>

        {/* Goods Issue Card */}
        <div
          onClick={() => navigate('/goods-issues')}
          style={{
            background: 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)',
            borderRadius: 12,
            padding: 24,
            cursor: 'pointer',
            color: 'white',
            transition: 'transform 0.2s',
          }}
          onMouseEnter={(e) => e.currentTarget.style.transform = 'scale(1.02)'}
          onMouseLeave={(e) => e.currentTarget.style.transform = 'scale(1)'}
        >
          <TruckOutlined style={{ fontSize: 32, marginBottom: 12 }} />
          <div style={{ fontSize: 18, fontWeight: 600 }}>Xuất kho</div>
          <div style={{ opacity: 0.8 }}>Quản lý phiếu xuất kho giao hàng</div>
        </div>

        {/* Sales Invoices Card */}
        <div
          onClick={() => navigate('/sales-invoices')}
          style={{
            background: 'linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%)',
            borderRadius: 12,
            padding: 24,
            cursor: 'pointer',
            color: 'white',
            transition: 'transform 0.2s',
          }}
          onMouseEnter={(e) => e.currentTarget.style.transform = 'scale(1.02)'}
          onMouseLeave={(e) => e.currentTarget.style.transform = 'scale(1)'}
        >
          <FileTextOutlined style={{ fontSize: 32, marginBottom: 12 }} />
          <div style={{ fontSize: 18, fontWeight: 600 }}>Hóa đơn</div>
          <div style={{ opacity: 0.8 }}>Quản lý hóa đơn và thanh toán</div>
        </div>

        {/* Inventory Card */}
        <div
          onClick={() => navigate('/inventory')}
          style={{
            background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
            borderRadius: 12,
            padding: 24,
            cursor: 'pointer',
            color: 'white',
            transition: 'transform 0.2s',
          }}
          onMouseEnter={(e) => e.currentTarget.style.transform = 'scale(1.02)'}
          onMouseLeave={(e) => e.currentTarget.style.transform = 'scale(1)'}
        >
          <AppstoreOutlined style={{ fontSize: 32, marginBottom: 12 }} />
          <div style={{ fontSize: 18, fontWeight: 600 }}>Tồn kho</div>
          <div style={{ opacity: 0.8 }}>Theo dõi số lượng tồn kho</div>
        </div>

        {/* Customers Card */}
        <div
          onClick={() => navigate('/customers')}
          style={{
            background: 'linear-gradient(135deg, #06b6d4 0%, #0891b2 100%)',
            borderRadius: 12,
            padding: 24,
            cursor: 'pointer',
            color: 'white',
            transition: 'transform 0.2s',
          }}
          onMouseEnter={(e) => e.currentTarget.style.transform = 'scale(1.02)'}
          onMouseLeave={(e) => e.currentTarget.style.transform = 'scale(1)'}
        >
          <UserOutlined style={{ fontSize: 32, marginBottom: 12 }} />
          <div style={{ fontSize: 18, fontWeight: 600 }}>Khách hàng</div>
          <div style={{ opacity: 0.8 }}>Quản lý thông tin khách hàng</div>
        </div>
      </div>
    </div>
  );
}

// Router with Routes
export default function App() {
  return (
    <BrowserRouter>
      <MainLayout>
        <Routes>
          {/* Dashboard */}
          <Route path="/" element={<Dashboard />} />

          {/* Sales Orders */}
          <Route path="/sales-orders" element={<SalesOrderList />} />
          <Route path="/sales-orders/new" element={<SalesOrderForm />} />
          <Route path="/sales-orders/:id" element={<SalesOrderDetail />} />
          <Route path="/sales-orders/:id/edit" element={<SalesOrderForm />} />

          {/* Goods Issues */}
          <Route path="/goods-issues" element={<GoodsIssueList />} />
          <Route path="/goods-issues/new" element={<GoodsIssueForm />} />
          <Route path="/goods-issues/:id" element={<GoodsIssueDetail />} />
          <Route path="/goods-issues/:id/edit" element={<GoodsIssueForm />} />

          {/* Sales Invoices */}
          <Route path="/sales-invoices" element={<SalesInvoiceList />} />
          <Route path="/sales-invoices/:id" element={<SalesInvoiceDetail />} />

          {/* Customers */}
          <Route path="/customers" element={<CustomerList />} />

          {/* Purchase Orders */}
          <Route path="/purchase-orders" element={<PurchaseOrderList />} />
          <Route path="/purchase-orders/:id" element={<PurchaseOrderDetail />} />

          {/* Goods Receipts */}
          <Route path="/goods-receipts" element={<GoodsReceiptList />} />
          <Route path="/goods-receipts/:id" element={<GoodsReceiptDetail />} />

          {/* Inventory */}
          <Route path="/inventory" element={<InventoryList />} />

          {/* Products */}
          <Route path="/products" element={<ProductList />} />

          {/* Suppliers */}
          <Route path="/suppliers" element={<SupplierList />} />

          {/* Delivery */}
          <Route path="/delivery-plans" element={<DeliveryPlanList />} />
          <Route path="/delivery-plans/:id" element={<DeliveryPlanDetail />} />
        </Routes>
      </MainLayout>
    </BrowserRouter>
  );
}

