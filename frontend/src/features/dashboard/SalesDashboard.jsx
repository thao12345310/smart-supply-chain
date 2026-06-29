import React, { useEffect, useState } from 'react';
import { dashboardApi } from '@/api';
import { ROLES } from '@/services/roleService';
import DashboardShell from './components/DashboardShell';
import ActionCenter from './components/ActionCenter';
import KpiRow from './components/KpiRow';
import RankedList from './components/RankedList';
import BarCard from './components/BarCard';
import { SO_STATUS_VI, mapPoints, sumStatus } from './statusLabels';

const fmt = (n) => Number(n || 0).toLocaleString('vi-VN');

export default function SalesDashboard() {
  const [d, setD] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    (async () => {
      try {
        const res = await dashboardApi.getSales();
        setD(res.data);
      } catch (e) {
        setError('Không tải được dữ liệu dashboard bán hàng: ' + (e.message || ''));
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const actions = [
    {
      key: 'so-approval',
      label: 'SO chờ duyệt',
      count: sumStatus(d?.soByStatus, ['ORDER_OPEN']),
      to: '/sales-orders',
      roles: [ROLES.ADMIN, ROLES.SALES_STAFF, ROLES.SALES_MANAGER],
    },
    {
      key: 'so-issue',
      label: 'SO chờ xuất kho',
      count: sumStatus(d?.soByStatus, ['ORDER_APPROVED', 'ORDER_PARTIALLY_DELIVERED']),
      to: '/goods-issues',
      roles: [ROLES.ADMIN, ROLES.SALES_STAFF, ROLES.SALES_MANAGER, ROLES.WAREHOUSE_STAFF],
    },
  ];

  const kpis = [
    { key: 'totalSO', title: 'Tổng đơn bán (SO)', value: d?.totalSO ?? 0 },
    { key: 'rev', title: 'Doanh thu tháng này', value: fmt(d?.revenueThisMonth), suffix: '₫' },
  ];

  return (
    <DashboardShell
      title="Dashboard Bán hàng"
      subtitle="Tổng quan vận hành phân hệ bán hàng"
      loading={loading}
      error={error}
    >
      <ActionCenter items={actions} />
      <KpiRow items={kpis} />
      <RankedList title="Top khách hàng (theo doanh số)" data={d?.topCustomers} />
      <BarCard title="Đơn bán theo trạng thái" data={mapPoints(d?.soByStatus, SO_STATUS_VI)} />
    </DashboardShell>
  );
}
