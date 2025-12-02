import React, { useEffect, useState } from "react";
import { Table, Button, Modal, message, Tag } from "antd";
import api from "../services/api";
import { useNavigate } from "react-router-dom";

export default function DeliveryPlanList() {
  const [plans, setPlans] = useState([]);
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [formState, setFormState] = useState({ code: "", description: "", status: "Created" });
  const nav = useNavigate();

  const fetchData = async () => {
    try { const res = await api.get("/delivery-plans"); setPlans(res.data); }
    catch { message.error("Failed to fetch delivery plans"); }
  };
  useEffect(() => { fetchData(); }, []);

  const save = async () => {
    try {
      if (editing) await api.put(`/delivery-plans/${editing.id}`, formState);
      else await api.post(`/delivery-plans`, formState);
      setOpen(false); setEditing(null); setFormState({ code: "", description: "", status: "Created" });
      fetchData();
    } catch { message.error("Save failed"); }
  };

  const columns = [
    { title: "Code", dataIndex: "code" },
    { title: "Created Date", dataIndex: "createdDate" },
    { title: "Description", dataIndex: "description" },
    { title: "Status", dataIndex: "status", render: s => <Tag>{s}</Tag> },
    {
      title: "Actions", render: (_, r) => (<>
        <Button type="link" onClick={() => nav(`/delivery-plans/${r.id}`)}>Open</Button>
      </>)
    }
  ];

  return (
    <div style={{ padding: 20 }}>
      <h2>Delivery Plans</h2>
      <Button type="primary" onClick={() => setOpen(true)} style={{ marginBottom: 12 }}>
        Add Delivery Plan
      </Button>
      <Table dataSource={plans} columns={columns} rowKey="id" />

      <Modal title={editing ? "Edit Plan" : "Add Plan"} open={open} onOk={save} onCancel={() => setOpen(false)}>
        <div style={{ display: "grid", gap: 8 }}>
          <input placeholder="Code" value={formState.code} onChange={e => setFormState(s => ({ ...s, code: e.target.value }))} />
          <input placeholder="Description" value={formState.description} onChange={e => setFormState(s => ({ ...s, description: e.target.value }))} />
        </div>
      </Modal>
    </div>
  );
}
