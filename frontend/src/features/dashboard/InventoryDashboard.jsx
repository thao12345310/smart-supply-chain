import React, { useEffect, useState } from 'react';
import { dashboardApi } from '@/api';
import { ROLES } from '@/services/roleService';
import DashboardShell from './components/DashboardShell';
import ActionCenter from './components/ActionCenter';
import KpiRow from './components/KpiRow';
import BarCard from './components/BarCard';

const fmt = (n) => Number(n || 0).toLocaleString('vi-VN');

export default function InventoryDashboard() {
  const [d, setD] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    (async () => {
      try {
        const res = await dashboardApi.getInventory();
        setD(res.data);
      } catch (e) {
        setError('Không tải được dữ liệu dashboard kho: ' + (e.message || ''));
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const invRoles = [ROLES.ADMIN, ROLES.WAREHOUSE_STAFF, ROLES.PURCHASE_STAFF, ROLES.SALES_STAFF];
  const actions = [
    { key: 'low', label: 'SP sắp hết hàng', count: d?.lowStockCount ?? 0, to: '/inventory', roles: invRoles },
    { key: 'expiring', label: 'Lô sắp hết hạn', count: d?.expiringSoonCount ?? 0, to: '/inventory', roles: invRoles },
    { key: 'expired', label: 'Lô đã hết hạn', count: d?.expiredCount ?? 0, to: '/inventory', roles: invRoles },
  ];

  const kpis = [
    { key: 'stockValue', title: 'Giá trị tồn kho', value: fmt(d?.totalStockValue), suffix: '₫' },
  ];

  return (
    <DashboardShell
      title="Dashboard Kho hàng"
      subtitle="Tổng quan vận hành phân hệ kho"
      loading={loading}
      error={error}
    >
      <ActionCenter items={actions} />
      <KpiRow items={kpis} />
      <BarCard title="Giá trị tồn theo kho" data={d?.stockByWarehouse} />
    </DashboardShell>
  );
}
