import React, { useEffect, useState } from 'react';
import { dashboardApi } from '@/api';
import { ROLES } from '@/services/roleService';
import DashboardShell from './components/DashboardShell';
import ActionCenter from './components/ActionCenter';
import KpiRow from './components/KpiRow';
import RankedList from './components/RankedList';
import BarCard from './components/BarCard';
import { PO_STATUS_VI, mapPoints } from './statusLabels';

const fmt = (n) => Number(n || 0).toLocaleString('vi-VN');

export default function PurchaseDashboard() {
  const [d, setD] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    (async () => {
      try {
        const res = await dashboardApi.getPurchase();
        setD(res.data);
      } catch (e) {
        setError('Không tải được dữ liệu dashboard mua hàng: ' + (e.message || ''));
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const actions = [
    {
      key: 'po-approval',
      label: 'PO chờ duyệt',
      count: d?.pendingApproval ?? 0,
      to: '/purchase-orders',
      roles: [ROLES.ADMIN, ROLES.PURCHASE_STAFF, ROLES.PURCHASE_MANAGER],
    },
    {
      key: 'po-receipt',
      label: 'PO chờ nhập kho',
      count: d?.pendingReceipt ?? 0,
      to: '/goods-receipts',
      roles: [ROLES.ADMIN, ROLES.PURCHASE_MANAGER, ROLES.WAREHOUSE_STAFF],
    },
  ];

  const kpis = [
    { key: 'totalPO', title: 'Tổng đơn mua (PO)', value: d?.totalPO ?? 0 },
    { key: 'val', title: 'Giá trị mua tháng này', value: fmt(d?.purchaseValueThisMonth), suffix: '₫' },
  ];

  return (
    <DashboardShell
      title="Dashboard Mua hàng"
      subtitle="Tổng quan vận hành phân hệ mua hàng"
      loading={loading}
      error={error}
    >
      <ActionCenter items={actions} />
      <KpiRow items={kpis} />
      <RankedList title="Top nhà cung cấp (theo giá trị)" data={d?.topSuppliers} />
      <BarCard title="Đơn mua theo trạng thái" data={mapPoints(d?.poByStatus, PO_STATUS_VI)} />
    </DashboardShell>
  );
}
