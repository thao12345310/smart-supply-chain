import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Tabs, Table, Button, Modal, message, Input } from "antd";
import api from "../services/api";

export default function DeliveryPlanDetail(){
  const { id } = useParams();
  const nav = useNavigate();
  const [plan, setPlan] = useState(null);
  const [orders, setOrders] = useState([]);
  const [availableOrders, setAvailableOrders] = useState([]);
  const [shippers, setShippers] = useState([]);
  const [trips, setTrips] = useState([]);
  const [addOrderOpen, setAddOrderOpen] = useState(false);
  const [selectedOrderIds, setSelectedOrderIds] = useState([]);
  const [shipperModal, setShipperModal] = useState(false);
  const [shipperForm, setShipperForm] = useState({ shipperName: "", phone: "" });

  const fetchPlan = async () => {
    const p = await api.get(`/delivery-plans/${id}`); setPlan(p.data);
  };
  const fetchOrders = async () => {
    const res = await api.get(`/delivery-plans/${id}/orders`); setOrders(res.data);
  };
  const fetchShippers = async () => {
    const res = await api.get(`/delivery-plans/${id}/shippers`); setShippers(res.data);
  };
  const fetchTrips = async () => {
    const res = await api.get(`/delivery-plans/${id}/trips`); setTrips(res.data);
  };
  const fetchAvailableOrders = async () => {
    const res = await api.get(`/delivery-orders`); // simple - list all
    setAvailableOrders(res.data);
  };

  useEffect(() => {
    fetchPlan(); fetchOrders(); fetchShippers(); fetchTrips(); fetchAvailableOrders();
  }, [id]);

  const addOrders = async () => {
    try {
      await api.post(`/delivery-plans/${id}/orders`, selectedOrderIds);
      message.success("Added orders to plan");
      setAddOrderOpen(false); setSelectedOrderIds([]);
      fetchOrders();
    } catch { message.error("Failed to add orders"); }
  };

  const addShipper = async () => {
    try {
      await api.post(`/delivery-plans/${id}/shippers`, shipperForm);
      message.success("Shipper added");
      setShipperModal(false); setShipperForm({ shipperName: "", phone: "" });
      fetchShippers();
    } catch { message.error("Failed to add shipper"); }
  };

  const removePlanOrder = async (row) => {
    await api.delete(`/delivery-plans/${id}/orders/${row.id}`); fetchOrders();
  };
  const removeShipper = async (row) => {
    await api.delete(`/delivery-plans/${id}/shippers/${row.id}`); fetchShippers();
  };

  const generateTrips = async () => {
    try {
      await api.post(`/delivery-plans/${id}/generate-trips`);
      message.success("Trips generated");
      fetchTrips();
    } catch (e){
      message.error(e?.response?.data?.message || "Failed to generate trips");
    }
  };

  return (
    <div style={{ padding: 20 }}>
      <Button onClick={() => nav(-1)} style={{ marginBottom: 10 }}>Back</Button>
      <h2>Delivery Plan: {plan?.code}</h2>
      <Tabs
        items={[
          { key: "1", label: "Orders", children: (
            <div>
              <Button type="primary" onClick={() => setAddOrderOpen(true)} style={{ marginBottom: 10 }}>Attach Delivery Orders</Button>
              <Table dataSource={orders} rowKey="id" columns={[
                { title: "PlanOrder ID", dataIndex: "id" },
                { title: "Delivery Order Code", dataIndex: ["deliveryOrder","code"] },
                { title: "Destination", dataIndex: ["deliveryOrder","destinationAddress"] },
                { title: "Action", render: (_, r) => <Button danger type="link" onClick={() => removePlanOrder(r)}>Remove</Button> },
              ]} />
              <Modal title="Attach Delivery Orders" open={addOrderOpen} onOk={addOrders} onCancel={() => setAddOrderOpen(false)}>
                <Table
                  dataSource={availableOrders}
                  rowKey="id"
                  rowSelection={{ selectedRowKeys: selectedOrderIds, onChange: setSelectedOrderIds }}
                  columns={[
                    { title: "ID", dataIndex: "id" },
                    { title: "Code", dataIndex: "code" },
                    { title: "Status", dataIndex: "status" },
                    { title: "Destination", dataIndex: "destinationAddress" },
                  ]}
                  pagination={false}
                />
              </Modal>
            </div>
          )},
          { key: "2", label: "Shippers", children: (
            <div>
              <Button type="primary" onClick={() => setShipperModal(true)} style={{ marginBottom: 10 }}>Add Shipper</Button>
              <Table dataSource={shippers} rowKey="id" columns={[
                { title: "ID", dataIndex: "id" },
                { title: "Name", dataIndex: "shipperName" },
                { title: "Phone", dataIndex: "phone" },
                { title: "Action", render: (_, r) => <Button danger type="link" onClick={() => removeShipper(r)}>Remove</Button> },
              ]}/>
              <Modal title="Add Shipper" open={shipperModal} onOk={addShipper} onCancel={() => setShipperModal(false)}>
                <Input placeholder="Shipper name" value={shipperForm.shipperName} onChange={e => setShipperForm(s => ({...s, shipperName: e.target.value}))} style={{ marginBottom: 8 }} />
                <Input placeholder="Phone" value={shipperForm.phone} onChange={e => setShipperForm(s => ({...s, phone: e.target.value}))} />
              </Modal>
            </div>
          )},
          { key: "3", label: "Trips", children: (
            <div>
              <Button type="primary" onClick={generateTrips} style={{ marginBottom: 10 }}>Auto-generate Trips</Button>
              <Table dataSource={trips} rowKey="id" columns={[
                { title: "Trip Code", dataIndex: "code" },
                { title: "Shipper", dataIndex: "shipperName" },
                { title: "Status", dataIndex: "status" },
              ]}/>
            </div>
          )},
        ]}
      />
    </div>
  );
}
