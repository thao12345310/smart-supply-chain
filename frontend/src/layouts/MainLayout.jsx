import { Layout, Menu } from "antd";
import {
  ShoppingCartOutlined,
  InboxOutlined,
  AppstoreOutlined,
  TruckOutlined,
  DashboardOutlined,
  DollarOutlined,
  UserOutlined,
  SettingOutlined,
} from "@ant-design/icons";
import { useNavigate, useLocation } from "react-router-dom";

import { ROLES, hasAnyRole } from "@/services/roleService";
import { palette } from "@/theme";
import { authApi } from "@/api";
import AppLogo from "@/components/AppLogo";
import UserInfo from "@/components/UserInfo";

const { Sider, Content } = Layout;

// Main layout: fixed left sidebar (brand, user panel, role-filtered menu) + content area.
export default function MainLayout({ children }) {
  const navigate = useNavigate();
  const location = useLocation();

  // Determine selected key from current path
  const getSelectedKey = () => {
    const path = location.pathname;
    if (path.includes('/dashboard/purchase')) return 'purchase-dashboard';
    if (path.includes('/dashboard/sales')) return 'sales-dashboard';
    if (path.includes('/dashboard/inventory')) return 'inventory-dashboard';
    if (path.includes('/dashboard/delivery')) return 'delivery-dashboard';
    if (path.includes('/dashboard/accounting')) return 'accounting-dashboard';
    if (path.includes('/purchase-suggestions')) return 'purchase-suggestions';
    if (path.includes('/purchase-orders')) return 'purchase-orders';
    if (path.includes('/goods-receipts')) return 'goods-receipts';
    if (path.includes('/inventory')) return 'inventory';
    if (path.includes('/warehouses')) return 'warehouses';
    if (path.includes('/products')) return 'products';
    if (path.includes('/suppliers')) return 'suppliers';
    if (path.includes('/assigned-trips')) return 'assigned-trips';
    if (path.includes('/delivery-orders')) return 'delivery-orders';
    if (path.includes('/delivery-plans')) return 'delivery-plans';
    if (path.includes('/customers')) return 'customers';
    if (path.includes('/sales-orders')) return 'sales-orders';
    if (path.includes('/goods-issues')) return 'goods-issues';
    if (path.includes('/sales-invoices')) return 'sales-invoices';
    if (path.includes('/payments')) return 'payments';
    if (path.includes('/ledger')) return 'ledger';
    if (path.includes('/admin/users')) return 'admin-users';
    if (path === '/' || path.includes('/reports')) return 'reports';
    return 'reports';
  };

  const menuItems = [
    {
      key: 'reports',
      icon: <DashboardOutlined />,
      label: 'Báo cáo tổng',
      hidden: !hasAnyRole([ROLES.ADMIN, ROLES.SALES_MANAGER, ROLES.PURCHASE_MANAGER, ROLES.ACCOUNTANT]),
      onClick: () => navigate('/reports'),
    },
    {
      type: 'divider',
      hidden: !hasAnyRole([ROLES.ADMIN, ROLES.SALES_MANAGER, ROLES.PURCHASE_MANAGER, ROLES.ACCOUNTANT]),
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
        {
          key: 'sales-dashboard',
          label: 'Dashboard Bán hàng',
          hidden: !hasAnyRole([ROLES.ADMIN, ROLES.SALES_STAFF, ROLES.SALES_MANAGER]),
          onClick: () => navigate('/dashboard/sales'),
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
        {
          key: 'purchase-dashboard',
          label: 'Dashboard Mua hàng',
          hidden: !hasAnyRole([ROLES.ADMIN, ROLES.PURCHASE_STAFF, ROLES.PURCHASE_MANAGER]),
          onClick: () => navigate('/dashboard/purchase'),
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
        {
          key: 'inventory-dashboard',
          label: 'Dashboard Kho',
          hidden: !hasAnyRole([ROLES.ADMIN, ROLES.WAREHOUSE_STAFF]),
          onClick: () => navigate('/dashboard/inventory'),
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
          key: 'delivery-orders',
          label: 'Vận đơn',
          hidden: !hasAnyRole([ROLES.ADMIN, ROLES.DELIVERY_ADMIN]),
          onClick: () => navigate('/delivery-orders'),
        },
        {
          key: 'assigned-trips',
          label: 'Chuyến giao hàng',
          hidden: !hasAnyRole([ROLES.SHIPPER]),
          onClick: () => navigate('/assigned-trips'),
        },
        {
          key: 'delivery-dashboard',
          label: 'Dashboard Giao hàng',
          hidden: !hasAnyRole([ROLES.ADMIN, ROLES.DELIVERY_ADMIN]),
          onClick: () => navigate('/dashboard/delivery'),
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
          borderRight: `1px solid ${palette.border}`,
        }}
      >
        <div style={{
          padding: '16px 20px',
          borderBottom: `1px solid ${palette.border}`,
          background: palette.primary,
        }}>
          <AppLogo />
          <UserInfo />
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
          background: palette.canvas,
          minHeight: '100vh',
        }}>
          {children}
        </Content>
      </Layout>
    </Layout>
  );
}
