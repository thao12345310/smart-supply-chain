import { UserOutlined } from "@ant-design/icons";

/**
 * Sidebar user panel: avatar, display name and roles, read from localStorage.
 */
export default function UserInfo() {
  let user = {};
  try {
    user = JSON.parse(localStorage.getItem('user') || '{}');
  } catch (e) {
    user = {};
  }
  const displayName = user.fullName || user.username || 'Guest';
  const roleText =
    (user.roles || []).map((r) => r.replace('ROLE_', '')).join(', ') || 'No Role';

  return (
    <div style={{
      marginTop: 16,
      paddingTop: 16,
      borderTop: '1px solid rgba(255,255,255,0.2)',
      display: 'flex',
      alignItems: 'center',
      gap: 12
    }}>
      <div style={{
        width: 32,
        height: 32,
        borderRadius: '50%',
        backgroundColor: 'rgba(255,255,255,0.2)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        color: 'white'
      }}>
        <UserOutlined />
      </div>
      <div style={{ overflow: 'hidden' }}>
        <div style={{
          color: 'white',
          fontSize: 14,
          fontWeight: 600,
          whiteSpace: 'nowrap',
          textOverflow: 'ellipsis'
        }}>
          {displayName}
        </div>
        <div style={{
          color: 'rgba(255,255,255,0.7)',
          fontSize: 11,
          textTransform: 'uppercase'
        }}>
          {roleText}
        </div>
      </div>
    </div>
  );
}
