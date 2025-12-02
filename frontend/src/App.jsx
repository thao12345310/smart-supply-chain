import React from "react";
import { Tabs } from "antd";
import { BrowserRouter, Routes, Route, Link } from "react-router-dom";
import ProductList from "./pages/ProductList";
import SupplierList from "./pages/SupplierList";
import ShipmentList from "./pages/ShipmentList";
import PurchaseOrderList from "./pages/PurchaseOrderList";
import DeliveryPlanList from "./pages/DeliveryPlanList";
import DeliveryPlanDetail from "./pages/DeliveryPlanDetail";

export default function App(){
  return (
    <BrowserRouter>
      <div style={{ padding: 20 }}>
        <h1>Distribution Management</h1>
        <Routes>
          <Route path="/" element={
            <Tabs items={[
              { key: "1", label: "Products", children: <ProductList /> },
              { key: "2", label: "Suppliers", children: <SupplierList /> },
              { key: "3", label: "Shipments", children: <ShipmentList /> },
              { key: "4", label: "Purchase Orders", children: <PurchaseOrderList /> },
              { key: "5", label: "Delivery Plans", children: <DeliveryPlanList /> },
            ]} />
          } />
          <Route path="/delivery-plans/:id" element={<DeliveryPlanDetail />} />
        </Routes>
        <div style={{ marginTop: 12 }}>
          <Link to="/">Home</Link>
        </div>
      </div>
    </BrowserRouter>
  );
}
