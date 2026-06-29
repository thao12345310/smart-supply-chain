import React, { useEffect, useState } from 'react';
import { dashboardApi } from '@/api';
import { ROLES } from '@/services/roleService';
import DashboardShell from './components/DashboardShell';
import ActionCenter from './components/ActionCenter';
import KpiRow from './components/KpiRow';
import BarCard from './components/BarCard';

const fmt = (n) => Number(n || 0).toLocaleString('vi-VN');

export default function AccountingDashboard() {
  const [d, setD] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    (async () => {
      try {
        const res = await dashboardApi.getAccounting();
        setD(res.data);
      } catch (e) {
        setError('Không tải được dữ liệu dashboard kế toán: ' + (e.message || ''));
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const actions = [
    {
      key: 'overdue',
      label: 'Hóa đơn quá hạn',
      count: d?.overdueInvoices ?? 0,
      to: '/sales-invoices',
      roles: [ROLES.ADMIN, ROLES.SALES_MANAGER, ROLES.ACCOUNTANT],
    },
  ];

  const kpis = [
    { key: 'ar', title: 'Phải thu (AR)', value: fmt(d?.totalReceivable), suffix: '₫' },
    { key: 'ap', title: 'Phải trả (AP)', value: fmt(d?.totalPayable), suffix: '₫' },
    { key: 'in', title: 'Tổng thu', value: fmt(d?.cashIn), suffix: '₫' },
    { key: 'out', title: 'Tổng chi', value: fmt(d?.cashOut), suffix: '₫' },
  ];

  // Backend currently returns an empty cashFlowByMonth, so visualise cash in vs out
  // from the scalar totals instead of an always-empty monthly chart.
  const cashChart = [
    { label: 'Tổng thu', value: d?.cashIn },
    { label: 'Tổng chi', value: d?.cashOut },
  ];

  return (
    <DashboardShell
      title="Dashboard Kế toán"
      subtitle="Tổng quan vận hành phân hệ kế toán"
      loading={loading}
      error={error}
    >
      <ActionCenter items={actions} />
      <KpiRow items={kpis} />
      <BarCard title="Thu / Chi (lũy kế)" data={cashChart} />
    </DashboardShell>
  );
}
