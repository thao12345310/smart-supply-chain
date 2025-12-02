import React from "react";
import { BrowserRouter, Routes, Route, useNavigate } from "react-router-dom";
import { Tabs } from "antd";

// import các module chính
import ProductList from "./pages/ProductList";
import SupplierList from "./pages/SupplierList";
import PurchaseOrderList from "./pages/PurchaseOrderList";
import DeliveryPlanList from "./pages/DeliveryPlanList";
import DeliveryPlanDetail from "./pages/DeliveryPlanDetail";

// Component chứa Tabs menu chính
function MainTabs() {
  const navigate = useNavigate();

  return (
    <div style={{ padding: 20 }}>
      <h1>Distribution Management</h1>
      <Tabs
        onChange={(key) => {
          // điều hướng theo tab
          if (key === "5") navigate("/delivery-plans");
        }}
        items={[
          { key: "1", label: "Products", children: <ProductList /> },
          { key: "2", label: "Suppliers", children: <SupplierList /> },
          { key: "3", label: "Warehouse", children: <div /> },
          {
            key: "4",
            label: "Purchase Orders",
            children: <PurchaseOrderList />,
          },
          { key: "5", label: "Delivery Plans", children: <div /> }, // điều hướng router
        ]}
      />
    </div>
  );
}

// Router tổng
export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Mặc định hiển thị menu chính */}
        <Route path="/" element={<MainTabs />} />

        {/* Route riêng cho giao hàng theo đợt */}
        <Route path="/delivery-plans" element={<DeliveryPlanList />} />
        <Route path="/delivery-plans/:id" element={<DeliveryPlanDetail />} />
      </Routes>
    </BrowserRouter>
  );
}
