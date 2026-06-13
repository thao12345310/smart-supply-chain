import React, { useEffect, useState } from 'react';
import { Card, Table, Select, Space, message } from 'antd';
import { accountingApi } from '../services/api';

const ACCOUNTS = [
  { value: 'CASH', label: 'Tiền mặt/Ngân hàng' },
  { value: 'AR', label: 'Phải thu khách hàng' },
  { value: 'AP', label: 'Phải trả nhà cung cấp' },
  { value: 'REVENUE', label: 'Doanh thu' },
  { value: 'INVENTORY', label: 'Hàng tồn kho' },
  { value: 'EXPENSE', label: 'Chi phí / Giá vốn' },
];

const fmt = (n) => Number(n || 0).toLocaleString('vi-VN');

export default function LedgerPage() {
  const [account, setAccount] = useState('AR');
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);

  const load = async (acc) => {
    setLoading(true);
    try {
      const res = await accountingApi.getLedger(acc);
      setRows(res.data || []);
    } catch (e) { message.error(e.message); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(account); }, [account]);

  const columns = [
    { title: 'Thời gian', dataIndex: 'txDate', render: (t) => (t ? t.replace('T', ' ').slice(0, 16) : '') },
    { title: 'Diễn giải', dataIndex: 'description' },
    { title: 'Nợ', dataIndex: 'debit', align: 'right', render: fmt },
    { title: 'Có', dataIndex: 'credit', align: 'right', render: fmt },
    { title: 'Số dư', dataIndex: 'runningBalance', align: 'right', render: fmt },
  ];

  return (
    <Card title="Sổ cái tài khoản">
      <Space style={{ marginBottom: 16 }}>
        Tài khoản:
        <Select value={account} onChange={setAccount} options={ACCOUNTS} style={{ width: 240 }} />
      </Space>
      <Table rowKey={(_, i) => i} loading={loading} dataSource={rows} columns={columns} />
    </Card>
  );
}
