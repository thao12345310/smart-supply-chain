/**
 * Role Constants mapping to backend ROLE names
 */
export const ROLES = {
  ADMIN: 'ROLE_ADMIN',
  PURCHASE_STAFF: 'ROLE_PURCHASE_STAFF',
  PURCHASE_MANAGER: 'ROLE_PURCHASE_MANAGER',
  SALES_STAFF: 'ROLE_SALES_STAFF',
  SALES_MANAGER: 'ROLE_SALES_MANAGER',
  WAREHOUSE_STAFF: 'ROLE_WAREHOUSE_STAFF',
  DELIVERY_ADMIN: 'ROLE_DELIVERY_ADMIN',
  SHIPPER: 'ROLE_SHIPPER',
  ACCOUNTANT: 'ROLE_ACCOUNTANT',
};

/**
 * Check if the user has any of the specified roles
 * @param {string[]} requiredRoles - Array of role names
 * @returns {boolean}
 */
export const hasAnyRole = (requiredRoles = []) => {
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    const userRoles = user.roles || [];
    
    // Admin has all permissions
    if (userRoles.includes(ROLES.ADMIN)) return true;
    
    if (requiredRoles.length === 0) return true;
    
    return requiredRoles.some(role => userRoles.includes(role));
  } catch (e) {
    return false;
  }
};

/**
 * Get the current user's roles
 * @returns {string[]}
 */
export const getUserRoles = () => {
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    return user.roles || [];
  } catch (e) {
    return [];
  }
};

/**
 * Check if user is an admin
 * @returns {boolean}
 */
export const isAdmin = () => hasAnyRole([ROLES.ADMIN]);

/**
 * Check if user is a purchase staff/manager
 * @returns {boolean}
 */
export const isPurchasing = () => hasAnyRole([ROLES.PURCHASE_STAFF, ROLES.PURCHASE_MANAGER]);

/**
 * Check if user is a warehouse staff
 * @returns {boolean}
 */
export const isWarehouse = () => hasAnyRole([ROLES.WAREHOUSE_STAFF]);

/**
 * Check if user is a manager (Purchase or Sales)
 * @returns {boolean}
 */
export const isManager = () => hasAnyRole([ROLES.PURCHASE_MANAGER, ROLES.SALES_MANAGER]);

/**
 * Check if user is an accountant
 * @returns {boolean}
 */
export const isAccountant = () => hasAnyRole([ROLES.ACCOUNTANT]);

/**
 * Check if user is a delivery admin
 * @returns {boolean}
 */
export const isDeliveryAdmin = () => hasAnyRole([ROLES.DELIVERY_ADMIN]);

/**
 * Check if user is a shipper
 * @returns {boolean}
 */
export const isShipper = () => hasAnyRole([ROLES.SHIPPER]);
