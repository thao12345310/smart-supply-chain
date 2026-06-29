import React, { useEffect, useState } from 'react';
import { dashboardApi } from '@/api';
import { ROLES } from '@/services/roleService';
import DashboardShell from './components/DashboardShell';
import ActionCenter from './components/ActionCenter';
import KpiRow from './components/KpiRow';
import RankedList from './components/RankedList';
import BarCard from './components/BarCard';
import { TRIP_STATUS_VI, mapPoints, sumStatus } from './statusLabels';

export default function DeliveryDashboard() {
  const [d, setD] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    (async () => {
      try {
        const res = await dashboardApi.getDelivery();
        setD(res.data);
      } catch (e) {
        setError('Không tải được dữ liệu dashboard giao hàng: ' + (e.message || ''));
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const delRoles = [ROLES.ADMIN, ROLES.DELIVERY_ADMIN];
  const actions = [
    {
      key: 'trip-created',
      label: 'Chuyến chờ khởi hành',
      count: sumStatus(d?.tripsByStatus, ['CREATED']),
      to: '/delivery-plans',
      roles: delRoles,
    },
    {
      key: 'trip-inprogress',
      label: 'Chuyến đang giao',
      count: sumStatus(d?.tripsByStatus, ['IN_PROGRESS']),
      to: '/delivery-plans',
      roles: delRoles,
    },
  ];

  const kpis = [
    { key: 'total', title: 'Tổng chuyến', value: d?.totalTrips ?? 0 },
    { key: 'done', title: 'Đã hoàn thành', value: d?.completedTrips ?? 0 },
    { key: 'rate', title: 'Tỉ lệ thành công', value: Number(d?.successRate || 0).toFixed(1), suffix: '%' },
  ];

  return (
    <DashboardShell
      title="Dashboard Giao hàng"
      subtitle="Tổng quan vận hành phân hệ giao hàng"
      loading={loading}
      error={error}
    >
      <ActionCenter items={actions} />
      <KpiRow items={kpis} />
      <RankedList title="Số chuyến theo shipper" data={d?.ordersByShipper} />
      <BarCard title="Chuyến theo trạng thái" data={mapPoints(d?.tripsByStatus, TRIP_STATUS_VI)} />
    </DashboardShell>
  );
}
