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
  HomeOutlined,
  SettingOutlined,
} from "@ant-design/icons";

// Import pages - Purchasing Module
import ProductList from "./pages/ProductList";
import SupplierList from "./pages/SupplierList";
import PurchaseOrderList from "./pages/PurchaseOrderList";
import PurchaseOrderDetail from "./pages/PurchaseOrderDetail";
import PurchaseSuggestions from "./pages/PurchaseSuggestions";
import GoodsReceiptList from "./pages/GoodsReceiptList";
import GoodsReceiptDetail from "./pages/GoodsReceiptDetail";
import InventoryList from "./pages/InventoryList";
import DeliveryPlanList from "./pages/DeliveryPlanList";
import DeliveryPlanDetail from "./pages/DeliveryPlanDetail";
import AssignedTrips from "./pages/AssignedTrips";
import PurchaseOrderForm from "./pages/PurchaseOrderForm";
import GoodsReceiptForm from "./pages/GoodsReceiptForm";
import DeliveryPlanForm from "./pages/DeliveryPlanForm";

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

// Import pages - Accounting Module
import PaymentList from "./pages/PaymentList";
import LedgerPage from "./pages/LedgerPage";

// Import pages - Dashboard & Reporting (Phân hệ 5)
import DashboardPage from "./pages/DashboardPage";

// Import pages - Warehouse (Phân hệ 3 bổ sung)
import WarehouseList from "./pages/WarehouseList";

// Import pages - Admin
import UserManagement from "./pages/UserManagement";

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
    if (path.includes('/purchase-suggestions')) return 'purchase-suggestions';
    if (path.includes('/purchase-orders')) return 'purchase-orders';
    if (path.includes('/goods-receipts')) return 'goods-receipts';
    if (path.includes('/inventory')) return 'inventory';
    if (path.includes('/warehouses')) return 'warehouses';
    if (path.includes('/products')) return 'products';
    if (path.includes('/suppliers')) return 'suppliers';
    if (path.includes('/assigned-trips')) return 'assigned-trips';
    if (path.includes('/delivery-plans')) return 'delivery-plans';
    if (path.includes('/customers')) return 'customers';
    if (path.includes('/sales-orders')) return 'sales-orders';
    if (path.includes('/goods-issues')) return 'goods-issues';
    if (path.includes('/sales-invoices')) return 'sales-invoices';
    if (path.includes('/payments')) return 'payments';
    if (path.includes('/ledger')) return 'ledger';
    if (path.includes('/dashboard/accounting')) return 'accounting-dashboard';
    if (path.includes('/admin/users')) return 'admin-users';
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
    // Accounting Module
    {
      key: 'accounting',
      icon: <DollarOutlined />,
      label: 'Kế toán',
      hidden: !hasAnyRole([ROLES.ADMIN, ROLES.ACCOUNTANT]),
      children: [
        {
          key: 'payments',
          label: 'Phiếu thu/chi',
          hidden: !hasAnyRole([ROLES.ADMIN, ROLES.ACCOUNTANT]),
          onClick: () => navigate('/payments'),
        },
        {
          key: 'ledger',
          label: 'Sổ cái',
          hidden: !hasAnyRole([ROLES.ADMIN, ROLES.ACCOUNTANT]),
          onClick: () => navigate('/ledger'),
        },
        {
          key: 'accounting-dashboard',
          label: 'Dashboard Kế toán',
          hidden: !hasAnyRole([ROLES.ADMIN, ROLES.ACCOUNTANT]),
          onClick: () => navigate('/dashboard/accounting'),
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
          key: 'purchase-suggestions',
          label: 'Đề xuất mua hàng',
          hidden: !hasAnyRole([ROLES.ADMIN, ROLES.PURCHASE_STAFF, ROLES.PURCHASE_MANAGER]),
          onClick: () => navigate('/purchase-suggestions'),
        },
        {
          key: 'goods-receipts',
          label: 'Phiếu nhập kho',
          hidden: !hasAnyRole([ROLES.ADMIN, ROLES.PURCHASE_MANAGER, ROLES.WAREHOUSE_STAFF]),
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
        {
          key: 'warehouses',
          label: 'Danh sách kho',
          hidden: !hasAnyRole([ROLES.ADMIN, ROLES.WAREHOUSE_STAFF]),
          onClick: () => navigate('/warehouses'),
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
          onClick: () => navigate('/assigned-trips'),
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
    // Admin Management (Admin only)
    {
      key: 'admin',
      icon: <SettingOutlined />,
      label: 'Quản trị',
      hidden: !hasAnyRole([ROLES.ADMIN]),
      children: [
        {
          key: 'admin-users',
          label: 'Tài khoản nhân viên',
          onClick: () => navigate('/admin/users'),
        },
      ],
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
          defaultOpenKeys={['sales', 'accounting', 'purchasing', 'warehouse', 'master-data', 'delivery', 'admin']}
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
              <DashboardPage />
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

        {/* Accounting: Payments & Ledger */}
        <Route path="/payments" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN, ROLES.ACCOUNTANT]}>
              <MainLayout>
                <PaymentList />
              </MainLayout>
            </RoleProtectedRoute>
          </ProtectedRoute>
        } />
        <Route path="/ledger" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN, ROLES.ACCOUNTANT]}>
              <MainLayout>
                <LedgerPage />
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
        <Route path="/purchase-suggestions" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN, ROLES.PURCHASE_STAFF, ROLES.PURCHASE_MANAGER]}>
              <MainLayout>
                <PurchaseSuggestions />
              </MainLayout>
            </RoleProtectedRoute>
          </ProtectedRoute>
        } />
        <Route path="/purchase-orders/new" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN, ROLES.PURCHASE_STAFF]}>
              <MainLayout>
                <PurchaseOrderForm />
              </MainLayout>
            </RoleProtectedRoute>
          </ProtectedRoute>
        } />
        <Route path="/purchase-orders/:id/edit" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN, ROLES.PURCHASE_STAFF]}>
              <MainLayout>
                <PurchaseOrderForm />
              </MainLayout>
            </RoleProtectedRoute>
          </ProtectedRoute>
        } />
        <Route path="/purchase-orders/:poId/receive" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN, ROLES.WAREHOUSE_STAFF]}>
              <MainLayout>
                <GoodsReceiptForm />
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
        <Route path="/goods-receipts/:id/edit" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN, ROLES.WAREHOUSE_STAFF]}>
              <MainLayout>
                <GoodsReceiptForm />
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

        {/* Warehouses (Phân hệ 3 bổ sung) */}
        <Route path="/warehouses" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN, ROLES.WAREHOUSE_STAFF]}>
              <MainLayout>
                <WarehouseList />
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
        <Route path="/delivery-plans/new" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN, ROLES.DELIVERY_ADMIN]}>
              <MainLayout>
                <DeliveryPlanForm />
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
        <Route path="/assigned-trips" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN, ROLES.DELIVERY_ADMIN, ROLES.SHIPPER]}>
              <MainLayout>
                <AssignedTrips />
              </MainLayout>
            </RoleProtectedRoute>
          </ProtectedRoute>
        } />

        {/* Admin: User Management */}
        <Route path="/admin/users" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN]}>
              <MainLayout>
                <UserManagement />
              </MainLayout>
            </RoleProtectedRoute>
          </ProtectedRoute>
        } />
      </Routes>
    </BrowserRouter>
  );
}
