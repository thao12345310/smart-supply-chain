import React from 'react';
import { Navigate } from 'react-router-dom';
import { Empty } from 'antd';
import { ROLES, getUserRoles } from '@/services/roleService';
import MainLayout from '@/layouts/MainLayout';

/**
 * Resolve the default landing route for a set of role names.
 *
 * Pure function (no I/O) so it can be unit-tested in isolation. Priority matters
 * for users holding multiple roles: admin oversees everything (-> reports), the
 * operational roles land on their own cluster home. Returns null when no known
 * role matches, so the caller can render a safe fallback instead of looping.
 */
export function resolveHome(roles = []) {
  const has = (r) => roles.includes(r);
  if (has(ROLES.ADMIN)) return '/reports';
  if (has(ROLES.SALES_STAFF) || has(ROLES.SALES_MANAGER)) return '/dashboard/sales';
  if (has(ROLES.PURCHASE_STAFF) || has(ROLES.PURCHASE_MANAGER)) return '/dashboard/purchase';
  if (has(ROLES.WAREHOUSE_STAFF)) return '/dashboard/inventory';
  if (has(ROLES.DELIVERY_ADMIN)) return '/dashboard/delivery';
  if (has(ROLES.ACCOUNTANT)) return '/dashboard/accounting';
  if (has(ROLES.SHIPPER)) return '/assigned-trips';
  return null;
}

/**
 * Landing dispatcher mounted at "/": sends each role to its operational home.
 * Unknown roles get a friendly fallback (rendered, not redirected) to avoid any
 * redirect loop against role-guarded routes.
 */
export default function HomeRedirect() {
  const home = resolveHome(getUserRoles());
  if (home) return <Navigate to={home} replace />;

  return (
    <MainLayout>
      <div style={{ padding: 48 }}>
        <Empty description="Chưa có trang chủ phù hợp với vai trò của bạn" />
      </div>
    </MainLayout>
  );
}
