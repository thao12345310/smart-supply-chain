import { BrowserRouter, Routes, Route } from "react-router-dom";
import { ConfigProvider } from "antd";

import { antdTheme } from "@/theme";
import { ROLES } from "@/services/roleService";
import MainLayout from "@/layouts/MainLayout";
import ProtectedRoute from "@/app/ProtectedRoute";
import RoleProtectedRoute from "@/app/RoleProtectedRoute";
import HomeRedirect from "@/app/HomeRedirect";

// Purchasing module
import ProductList from "@/features/purchasing/ProductList";
import SupplierList from "@/features/purchasing/SupplierList";
import PurchaseOrderList from "@/features/purchasing/PurchaseOrderList";
import PurchaseOrderDetail from "@/features/purchasing/PurchaseOrderDetail";
import PurchaseOrderForm from "@/features/purchasing/PurchaseOrderForm";
import PurchaseSuggestions from "@/features/purchasing/PurchaseSuggestions";
import GoodsReceiptList from "@/features/purchasing/GoodsReceiptList";
import GoodsReceiptDetail from "@/features/purchasing/GoodsReceiptDetail";
import GoodsReceiptForm from "@/features/purchasing/GoodsReceiptForm";

// Sales module
import CustomerList from "@/features/sales/CustomerList";
import SalesOrderList from "@/features/sales/SalesOrderList";
import SalesOrderForm from "@/features/sales/SalesOrderForm";
import SalesOrderDetail from "@/features/sales/SalesOrderDetail";
import GoodsIssueList from "@/features/sales/GoodsIssueList";
import GoodsIssueForm from "@/features/sales/GoodsIssueForm";
import GoodsIssueDetail from "@/features/sales/GoodsIssueDetail";
import SalesInvoiceList from "@/features/sales/SalesInvoiceList";
import SalesInvoiceDetail from "@/features/sales/SalesInvoiceDetail";

// Inventory & warehouse module
import InventoryList from "@/features/inventory/InventoryList";
import WarehouseList from "@/features/inventory/WarehouseList";

// Delivery module
import DeliveryPlanList from "@/features/delivery/DeliveryPlanList";
import DeliveryPlanDetail from "@/features/delivery/DeliveryPlanDetail";
import DeliveryPlanForm from "@/features/delivery/DeliveryPlanForm";
import DeliveryOrderList from "@/features/delivery/DeliveryOrderList";
import DeliveryOrderDetail from "@/features/delivery/DeliveryOrderDetail";
import AssignedTrips from "@/features/delivery/AssignedTrips";
import WaybillPrint from "@/features/delivery/WaybillPrint";

// Accounting module
import PaymentList from "@/features/accounting/PaymentList";
import LedgerPage from "@/features/accounting/LedgerPage";

// Dashboard & reporting module
import DashboardPage from "@/features/dashboard/DashboardPage";
import PurchaseDashboard from "@/features/dashboard/PurchaseDashboard";
import SalesDashboard from "@/features/dashboard/SalesDashboard";
import InventoryDashboard from "@/features/dashboard/InventoryDashboard";
import DeliveryDashboard from "@/features/dashboard/DeliveryDashboard";
import AccountingDashboard from "@/features/dashboard/AccountingDashboard";

// Auth & admin module
import LoginPage from "@/features/auth/LoginPage";
import UserManagement from "@/features/admin/UserManagement";

// Router with Routes
export default function App() {
  return (
    <ConfigProvider theme={antdTheme}>
    <BrowserRouter>
      <Routes>
        {/* Public Routes */}
        <Route path="/login" element={<LoginPage />} />

        {/* Landing dispatcher: route each role to its operational home */}
        <Route path="/" element={
          <ProtectedRoute>
            <HomeRedirect />
          </ProtectedRoute>
        } />

        {/* Cross-functional reporting (Phân hệ 5) */}
        <Route path="/reports" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN, ROLES.SALES_MANAGER, ROLES.PURCHASE_MANAGER, ROLES.ACCOUNTANT]}>
              <MainLayout>
                <DashboardPage />
              </MainLayout>
            </RoleProtectedRoute>
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

        {/* Delivery Orders (Vận đơn) */}
        <Route path="/delivery-orders/:id/print" element={
          <ProtectedRoute>
            <WaybillPrint />
          </ProtectedRoute>
        } />
        <Route path="/delivery-orders" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN, ROLES.DELIVERY_ADMIN]}>
              <MainLayout>
                <DeliveryOrderList />
              </MainLayout>
            </RoleProtectedRoute>
          </ProtectedRoute>
        } />
        <Route path="/delivery-orders/:id" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN, ROLES.DELIVERY_ADMIN]}>
              <MainLayout>
                <DeliveryOrderDetail />
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

        {/* Per-cluster Dashboards */}
        <Route path="/dashboard/purchase" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN, ROLES.PURCHASE_MANAGER, ROLES.PURCHASE_STAFF]}>
              <MainLayout>
                <PurchaseDashboard />
              </MainLayout>
            </RoleProtectedRoute>
          </ProtectedRoute>
        } />
        <Route path="/dashboard/sales" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN, ROLES.SALES_MANAGER, ROLES.SALES_STAFF]}>
              <MainLayout>
                <SalesDashboard />
              </MainLayout>
            </RoleProtectedRoute>
          </ProtectedRoute>
        } />
        <Route path="/dashboard/inventory" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN, ROLES.WAREHOUSE_STAFF]}>
              <MainLayout>
                <InventoryDashboard />
              </MainLayout>
            </RoleProtectedRoute>
          </ProtectedRoute>
        } />
        <Route path="/dashboard/delivery" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN, ROLES.DELIVERY_ADMIN]}>
              <MainLayout>
                <DeliveryDashboard />
              </MainLayout>
            </RoleProtectedRoute>
          </ProtectedRoute>
        } />
        <Route path="/dashboard/accounting" element={
          <ProtectedRoute>
            <RoleProtectedRoute roles={[ROLES.ADMIN, ROLES.ACCOUNTANT]}>
              <MainLayout>
                <AccountingDashboard />
              </MainLayout>
            </RoleProtectedRoute>
          </ProtectedRoute>
        } />
      </Routes>
    </BrowserRouter>
    </ConfigProvider>
  );
}
