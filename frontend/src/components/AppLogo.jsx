import { Typography } from "antd";
import { ShopOutlined } from "@ant-design/icons";

const { Title } = Typography;

/**
 * Sidebar brand header: product logo and name.
 */
export default function AppLogo() {
  return (
    <>
      <Title level={4} style={{ margin: 0, color: 'white' }}>
        <ShopOutlined style={{ marginRight: 8 }} />
        DMS
      </Title>
      <div style={{ fontSize: 12, color: 'rgba(255,255,255,0.8)', marginBottom: 12 }}>
        Distribution Management
      </div>
    </>
  );
}
