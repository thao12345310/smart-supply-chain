import React from "react";
import { BrowserRouter, Routes, Route, useNavigate, useLocation, Navigate } from "react-router-dom";
import { Layout, Menu, Typography, Space, Badge, Card, Row, Col, Statistic, Result, Button as AntButton } from "antd";
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
  CreditCardOutlined,
  HistoryOutlined,
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
import LoginPage from "./pages/LoginPage";
import { authApi } from "./services/api";
import { ROLES, hasAnyRole } from "./services/roleService";

const { Header, Sider, Content } = Layout;
const { Title, Text } = Typography;

/**
 * Protected Route Component
 * Redirects to login if user is not authenticated
 */
function ProtectedRoute({ children }) {
  const token = localStorage.getItem('token');
  const location = useLocation();

  if (!token) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return children;
}

/**
 * Role-Based Protected Route
 * Checks if user has required roles
 */
function RoleProtectedRoute({ roles = [], children }) {
  const isAuthorized = hasAnyRole(roles);

  if (!isAuthorized) {
    // Silently redirect to home/dashboard instead of showing 403
    return <Navigate to="/" replace />;
  }

  return children;
}

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
    // Sales Module
    {
      key: 'sales',
      icon: <DollarOutlined />,
      label: 'Bán hàng',
      hidden: !hasAnyRole([ROLES.ADMIN, ROLES.SALES_STAFF, ROLES.SALES_MANAGER, ROLES.WAREHOUSE_STAFF, ROLES.ACCOUNTANT]),
      children: [
        {
          key: 'sales-orders',
          label: 'Đơn bán hàng',
          hidden: !hasAnyRole([ROLES.ADMIN, ROLES.SALES_STAFF, ROLES.SALES_MANAGER, ROLES.WAREHOUSE_STAFF, ROLES.ACCOUNTANT]),
          onClick: () => navigate('/sales-orders'),
        },
        {
          key: 'goods-issues',
          label: 'Phiếu xuất kho',
          hidden: !hasAnyRole([ROLES.ADMIN, ROLES.SALES_STAFF, ROLES.SALES_MANAGER, ROLES.WAREHOUSE_STAFF]),
          onClick: () => navigate('/goods-issues'),
        },
        {
          key: 'sales-invoices',
          label: 'Hóa đơn & Thanh toán',
          hidden: !hasAnyRole([ROLES.ADMIN, ROLES.SALES_MANAGER, ROLES.ACCOUNTANT]),
          onClick: () => navigate('/sales-invoices'),
        },
      ].filter(item => !item.hidden),
    },
    // Purchasing Module
    {
      key: 'purchasing',
      icon: <ShoppingCartOutlined />,
      label: 'Mua hàng',
      hidden: !hasAnyRole([ROLES.ADMIN, ROLES.PURCHASE_STAFF, ROLES.PURCHASE_MANAGER, ROLES.WAREHOUSE_STAFF, ROLES.ACCOUNTANT]),
      children: [
        {
          key: 'purchase-orders',
          label: 'Đơn mua hàng',
          hidden: !hasAnyRole([ROLES.ADMIN, ROLES.PURCHASE_STAFF, ROLES.PURCHASE_MANAGER, ROLES.WAREHOUSE_STAFF, ROLES.ACCOUNTANT]),
          onClick: () => navigate('/purchase-orders'),
        },
        {
          key: 'goods-receipts',
          label: 'Phiếu nhập kho',
          hidden: !hasAnyRole([ROLES.ADMIN, ROLES.PURCHASE_STAFF, ROLES.PURCHASE_MANAGER, ROLES.WAREHOUSE_STAFF]),
          onClick: () => navigate('/goods-receipts'),
        },
      ].filter(item => !item.hidden),
    },
    // Warehouse & Inventory
    {
      key: 'warehouse',
      icon: <InboxOutlined />,
      label: 'Kho hàng',
      hidden: !hasAnyRole([ROLES.ADMIN, ROLES.WAREHOUSE_STAFF, ROLES.PURCHASE_STAFF, ROLES.SALES_STAFF]),
      children: [
        {
          key: 'inventory',
          label: 'Tồn kho',
          hidden: !hasAnyRole([ROLES.ADMIN, ROLES.WAREHOUSE_STAFF, ROLES.PURCHASE_STAFF, ROLES.SALES_STAFF]),
          onClick: () => navigate('/inventory'),
        },
      ].filter(item => !item.hidden),
    },
    // Delivery Management
    {
      key: 'delivery',
      icon: <TruckOutlined />,
      label: 'Giao hàng',
      hidden: !hasAnyRole([ROLES.ADMIN, ROLES.DELIVERY_ADMIN, ROLES.SHIPPER]),
      children: [
        {
          key: 'delivery-plans',
          label: 'Kế hoạch giao hàng',
          hidden: !hasAnyRole([ROLES.ADMIN, ROLES.DELIVERY_ADMIN]),
          onClick: () => navigate('/delivery-plans'),
        },
        {
          key: 'assigned-trips',
          label: 'Chuyến giao hàng',
          hidden: !hasAnyRole([ROLES.SHIPPER]),
          onClick: () => navigate('/delivery-plans'), // Reusing or adding a trip page later
        },
      ].filter(item => !item.hidden),
    },
    // Master Data
    {
      key: 'master-data',
      icon: <AppstoreOutlined />,
      label: 'Danh mục',
      hidden: !hasAnyRole([ROLES.ADMIN, ROLES.PURCHASE_STAFF, ROLES.SALES_STAFF]),
      children: [
        {
          key: 'products',
          label: 'Sản phẩm',
          hidden: !hasAnyRole([ROLES.ADMIN, ROLES.PURCHASE_STAFF, ROLES.SALES_STAFF]),
          onClick: () => navigate('/products'),
        },
        {
          key: 'suppliers',
          label: 'Nhà cung cấp',
          hidden: !hasAnyRole([ROLES.ADMIN, ROLES.PURCHASE_STAFF]),
          onClick: () => navigate('/suppliers'),
        },
        {
          key: 'customers',
          label: 'Khách hàng',
          hidden: !hasAnyRole([ROLES.ADMIN, ROLES.SALES_STAFF]),
          onClick: () => navigate('/customers'),
        },
      ].filter(item => !item.hidden),
    },
    {
      key: 'logout',
      icon: <UserOutlined />,
      label: 'Đăng xuất',
      onClick: () => {
        authApi.logout();
        navigate('/login');
      },
      danger: true,
    },
  ].filter(item => !item.hidden);

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
          <div style={{ fontSize: 12, color: 'rgba(255,255,255,0.8)', marginBottom: 12 }}>
            Distribution Management
          </div>
          
          {/* User Info Section */}
          <div style={{ 
            marginTop: 16, 
            paddingTop: 16, 
            borderTop: '1px solid rgba(255,255,255,0.2)',
            display: 'flex',
            alignItems: 'center',
            gap: 12
          }}>
            <div style={{
              width: 32,
              height: 32,
              borderRadius: '50%',
              backgroundColor: 'rgba(255,255,255,0.2)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'white'
            }}>
              <UserOutlined />
            </div>
            <div style={{ overflow: 'hidden' }}>
              <div style={{ 
                color: 'white', 
                fontSize: 14, 
                fontWeight: 600,
                whiteSpace: 'nowrap',
                textOverflow: 'ellipsis'
              }}>
                {(() => {
                  try {
                    const user = JSON.parse(localStorage.getItem('user') || '{}');
                    return user.fullName || user.username || 'Guest';
                  } catch (e) { return 'Guest'; }
                })()}
              </div>
              <div style={{ 
                color: 'rgba(255,255,255,0.7)', 
                fontSize: 11,
                textTransform: 'uppercase'
              }}>
                {(() => {
                  try {
                    const user = JSON.parse(localStorage.getItem('user') || '{}');
                    return (user.roles || []).map(r => r.replace('ROLE_', '')).join(', ') || 'No Role';
                  } catch (e) { return ''; }
                })()}
              </div>
            </div>
          </div>
        </div>
        <Menu
          mode="inline"
          selectedKeys={[getSelectedKey()]}
          defaultOpenKeys={['sales', 'purchasing', 'warehouse', 'master-data', 'delivery']}
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
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const roles = user.roles || [];

  const getDashboardTitle = () => {
    if (roles.includes(ROLES.ADMIN)) return "Bảng điều khiển quản trị";
    if (roles.includes(ROLES.PURCHASE_STAFF)) return "Quản lý mua hàng";
    if (roles.includes(ROLES.WAREHOUSE_STAFF)) return "Điều hành kho hàng";
    if (roles.includes(ROLES.SALES_STAFF)) return "Quản lý kinh doanh";
    if (roles.includes(ROLES.ACCOUNTANT)) return "Phân tích tài chính";
    if (roles.includes(ROLES.DELIVERY_ADMIN)) return "Điều hành vận chuyển";
    if (roles.includes(ROLES.SHIPPER)) return "Chuyến giao hàng của tôi";
    return "Hệ thống quản lý phân phối";
  };

  const cards = [
    // Purchasing Cards
    {
      title: "Đơn mua hàng",
      desc: "Quản lý đơn hàng mua từ nhà cung cấp",
      icon: <ShoppingCartOutlined />,
      color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
      path: "/purchase-orders",
      roles: [ROLES.ADMIN, ROLES.PURCHASE_STAFF, ROLES.PURCHASE_MANAGER, ROLES.ACCOUNTANT]
    },
    {
      title: "Phiếu nhập kho",
      desc: "Nhập hàng vào kho từ đơn PO",
      icon: <InboxOutlined />,
      color: 'linear-gradient(135deg, #6a11cb 0%, #2575fc 100%)',
      path: "/goods-receipts",
      roles: [ROLES.ADMIN, ROLES.WAREHOUSE_STAFF, ROLES.PURCHASE_MANAGER]
    },
    // Sales Cards
    {
      title: "Đơn bán hàng",
      desc: "Quản lý đơn khách hàng đặt",
      icon: <DollarOutlined />,
      color: 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
      path: "/sales-orders",
      roles: [ROLES.ADMIN, ROLES.SALES_STAFF, ROLES.SALES_MANAGER, ROLES.ACCOUNTANT]
    },
    {
      title: "Phiếu xuất kho",
      desc: "Xuất hàng giao cho khách hàng",
      icon: <TruckOutlined />,
      color: 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)',
      path: "/goods-issues",
      roles: [ROLES.ADMIN, ROLES.WAREHOUSE_STAFF, ROLES.SALES_MANAGER]
    },
    // Accounting Cards
    {
      title: "Hóa đơn & Thanh toán",
      desc: "Quản lý công nợ và thanh toán",
      icon: <FileTextOutlined />,
      color: 'linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%)',
      path: "/sales-invoices",
      roles: [ROLES.ADMIN, ROLES.ACCOUNTANT, ROLES.SALES_MANAGER]
    },
    // Delivery Cards
    {
      title: "Kế hoạch giao hàng",
      desc: "Điều phối vận chuyển và giao hàng",
      icon: <TruckOutlined />,
      color: 'linear-gradient(135deg, #ec4899 0%, #be185d 100%)',
      path: "/delivery-plans",
      roles: [ROLES.ADMIN, ROLES.DELIVERY_ADMIN]
    },
    {
      title: "Chuyến giao hàng",
      desc: "Xem chuyến giao được phân công",
      icon: <HistoryOutlined />,
      color: 'linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%)',
      path: "/delivery-plans", // Tạm thời dẫn vào đây
      roles: [ROLES.SHIPPER]
    },
    // Inventory
    {
      title: "Tồn kho",
      desc: "Kiểm tra số lượng sản phẩm trong kho",
      icon: <AppstoreOutlined />,
      color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
      path: "/inventory",
      roles: [ROLES.ADMIN, ROLES.WAREHOUSE_STAFF, ROLES.PURCHASE_STAFF, ROLES.SALES_STAFF]
    },
    // Master Data
    {
      title: "Khách hàng",
      desc: "Thông tin đối tác khách hàng",
      icon: <UserOutlined />,
      color: 'linear-gradient(135deg, #06b6d4 0%, #0891b2 100%)',
      path: "/customers",
      roles: [ROLES.ADMIN, ROLES.SALES_STAFF]
    },
  ].filter(card => card.roles.some(r => roles.includes(r)) || roles.includes(ROLES.ADMIN));

  return (
    <div style={{ padding: 24 }}>
      <Title level={2}>{getDashboardTitle()}</Title>
      <p style={{ color: '#666', marginBottom: 24 }}>
        Chào mừng <strong>{user.fullName || user.username}</strong>. Chúc bạn một ngày làm việc hiệu quả!
      </p>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: 20 }}>
        {cards.map((card, idx) => (
          <div
            key={idx}
            onClick={() => navigate(card.path)}
            style={{
              background: card.color,
              borderRadius: 16,
              padding: 24,
              cursor: 'pointer',
              color: 'white',
              transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
              boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
              display: 'flex',
              flexDirection: 'column',
              justifyContent: 'space-between',
              height: 160,
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.transform = 'translateY(-5px)';
              e.currentTarget.style.boxShadow = '0 8px 24px rgba(0,0,0,0.15)';
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.transform = 'translateY(0)';
              e.currentTarget.style.boxShadow = '0 4px 12px rgba(0,0,0,0.1)';
            }}
          >
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <div style={{ 
                  background: 'rgba(255,255,255,0.2)', 
                  padding: 10, 
                  borderRadius: 12, 
                  fontSize: 24,
                  display: 'flex'
                }}>
                  {card.icon}
                </div>
              </div>
              <div style={{ fontSize: 20, fontWeight: 700, marginTop: 12 }}>{card.title}</div>
            </div>
            <div style={{ opacity: 0.85, fontSize: 13 }}>{card.desc}</div>
          </div>
        ))}
      </div>
    </div>
  );
}

// Router with Routes
export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public Routes */}
        <Route path="/login" element={<LoginPage />} />

        {/* Protected Routes */}
        <Route path="/" element={
          <ProtectedRoute>
            <MainLayout>
              <Dashboard />
            </MainLayout>
          </ProtectedRoute>
        } />

        {/* Sales Orders */}
        <Route path="/sales-orders" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN, ROLES.SALES_STAFF, ROLES.SALES_MANAGER, ROLES.ACCOUNTANT, ROLES.WAREHOUSE_STAFF]}>
              <MainLayout>
                <SalesOrderList />
              </MainLayout>
            </RoleProtectedRoute>
          </ProtectedRoute>
        } />
        <Route path="/sales-orders/new" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN, ROLES.SALES_STAFF]}>
              <MainLayout>
                <SalesOrderForm />
              </MainLayout>
            </RoleProtectedRoute>
          </ProtectedRoute>
        } />
        <Route path="/sales-orders/:id" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN, ROLES.SALES_STAFF, ROLES.SALES_MANAGER, ROLES.ACCOUNTANT, ROLES.WAREHOUSE_STAFF]}>
              <MainLayout>
                <SalesOrderDetail />
              </MainLayout>
            </RoleProtectedRoute>
          </ProtectedRoute>
        } />
        <Route path="/sales-orders/:id/edit" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN, ROLES.SALES_STAFF]}>
              <MainLayout>
                <SalesOrderForm />
              </MainLayout>
            </RoleProtectedRoute>
          </ProtectedRoute>
        } />

        {/* Goods Issues */}
        <Route path="/goods-issues" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN, ROLES.WAREHOUSE_STAFF, ROLES.SALES_MANAGER]}>
              <MainLayout>
                <GoodsIssueList />
              </MainLayout>
            </RoleProtectedRoute>
          </ProtectedRoute>
        } />
        <Route path="/goods-issues/new" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN, ROLES.WAREHOUSE_STAFF]}>
              <MainLayout>
                <GoodsIssueForm />
              </MainLayout>
            </RoleProtectedRoute>
          </ProtectedRoute>
        } />
        <Route path="/goods-issues/:id" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN, ROLES.WAREHOUSE_STAFF, ROLES.SALES_MANAGER]}>
              <MainLayout>
                <GoodsIssueDetail />
              </MainLayout>
            </RoleProtectedRoute>
          </ProtectedRoute>
        } />
        <Route path="/goods-issues/:id/edit" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN, ROLES.WAREHOUSE_STAFF]}>
              <MainLayout>
                <GoodsIssueForm />
              </MainLayout>
            </RoleProtectedRoute>
          </ProtectedRoute>
        } />

        {/* Sales Invoices */}
        <Route path="/sales-invoices" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN, ROLES.ACCOUNTANT, ROLES.SALES_MANAGER]}>
              <MainLayout>
                <SalesInvoiceList />
              </MainLayout>
            </RoleProtectedRoute>
          </ProtectedRoute>
        } />
        <Route path="/sales-invoices/:id" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN, ROLES.ACCOUNTANT, ROLES.SALES_MANAGER]}>
              <MainLayout>
                <SalesInvoiceDetail />
              </MainLayout>
            </RoleProtectedRoute>
          </ProtectedRoute>
        } />

        {/* Customers */}
        <Route path="/customers" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN, ROLES.SALES_STAFF, ROLES.SALES_MANAGER]}>
              <MainLayout>
                <CustomerList />
              </MainLayout>
            </RoleProtectedRoute>
          </ProtectedRoute>
        } />

        {/* Purchase Orders */}
        <Route path="/purchase-orders" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN, ROLES.PURCHASE_STAFF, ROLES.PURCHASE_MANAGER, ROLES.ACCOUNTANT, ROLES.WAREHOUSE_STAFF]}>
              <MainLayout>
                <PurchaseOrderList />
              </MainLayout>
            </RoleProtectedRoute>
          </ProtectedRoute>
        } />
        <Route path="/purchase-orders/:id" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN, ROLES.PURCHASE_STAFF, ROLES.PURCHASE_MANAGER, ROLES.ACCOUNTANT, ROLES.WAREHOUSE_STAFF]}>
              <MainLayout>
                <PurchaseOrderDetail />
              </MainLayout>
            </RoleProtectedRoute>
          </ProtectedRoute>
        } />

        {/* Goods Receipts */}
        <Route path="/goods-receipts" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN, ROLES.WAREHOUSE_STAFF, ROLES.PURCHASE_MANAGER]}>
              <MainLayout>
                <GoodsReceiptList />
              </MainLayout>
            </RoleProtectedRoute>
          </ProtectedRoute>
        } />
        <Route path="/goods-receipts/:id" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN, ROLES.WAREHOUSE_STAFF, ROLES.PURCHASE_MANAGER]}>
              <MainLayout>
                <GoodsReceiptDetail />
              </MainLayout>
            </RoleProtectedRoute>
          </ProtectedRoute>
        } />

        {/* Inventory */}
        <Route path="/inventory" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN, ROLES.WAREHOUSE_STAFF, ROLES.PURCHASE_STAFF, ROLES.SALES_STAFF]}>
              <MainLayout>
                <InventoryList />
              </MainLayout>
            </RoleProtectedRoute>
          </ProtectedRoute>
        } />

        {/* Products */}
        <Route path="/products" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN, ROLES.PURCHASE_STAFF, ROLES.SALES_STAFF]}>
              <MainLayout>
                <ProductList />
              </MainLayout>
            </RoleProtectedRoute>
          </ProtectedRoute>
        } />

        {/* Suppliers */}
        <Route path="/suppliers" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN, ROLES.PURCHASE_STAFF]}>
              <MainLayout>
                <SupplierList />
              </MainLayout>
            </RoleProtectedRoute>
          </ProtectedRoute>
        } />

        {/* Delivery */}
        <Route path="/delivery-plans" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN, ROLES.DELIVERY_ADMIN, ROLES.SHIPPER]}>
              <MainLayout>
                <DeliveryPlanList />
              </MainLayout>
            </RoleProtectedRoute>
          </ProtectedRoute>
        } />
        <Route path="/delivery-plans/:id" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN, ROLES.DELIVERY_ADMIN, ROLES.SHIPPER]}>
              <MainLayout>
                <DeliveryPlanDetail />
              </MainLayout>
            </RoleProtectedRoute>
          </ProtectedRoute>
        } />
      </Routes>
    </BrowserRouter>
  );
}
