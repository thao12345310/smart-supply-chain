import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Layout, Form, Input, Button, Card, Typography, Alert, Space } from 'antd';
import { UserOutlined, LockOutlined, ShopOutlined } from '@ant-design/icons';
import { authApi } from '../services/api';

const { Content } = Layout;
const { Title, Text } = Typography;

/**
 * Login Page - Trang đăng nhập
 */
export default function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Redirect to original location or home after login
  const from = location.state?.from?.pathname || "/";

  const onFinish = async (values) => {
    try {
      setLoading(true);
      setError(null);
      
      const response = await authApi.login(values);
      const { accessToken, user } = response.data;
      
      // Store token and user info
      localStorage.setItem('token', accessToken);
      localStorage.setItem('user', JSON.stringify(user));
      
      // Redirect
      navigate(from, { replace: true });
    } catch (err) {
      console.error('Login failed:', err);
      setError(err.message || 'Sai tên đăng nhập hoặc mật khẩu');
    } finally {
      setLoading(false);
    }
  };

  const containerStyle = {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    minHeight: '100vh',
    background: 'linear-gradient(135deg, #f0f2f5 0%, #d8e3f0 100%)',
    overflow: 'hidden',
    position: 'relative',
  };

  const bgCircles = [
    { size: 400, top: -100, right: -100, color: 'rgba(24, 144, 255, 0.05)' },
    { size: 300, bottom: -50, left: -50, color: 'rgba(24, 144, 255, 0.08)' },
  ];

  return (
    <div style={containerStyle}>
      {bgCircles.map((circle, i) => (
        <div key={i} style={{
          position: 'absolute',
          width: circle.size,
          height: circle.size,
          borderRadius: '50%',
          backgroundColor: circle.color,
          top: circle.top,
          right: circle.right,
          bottom: circle.bottom,
          left: circle.left,
          zIndex: 0,
        }} />
      ))}
      
      <Content style={{ maxWidth: 450, width: '90%', zIndex: 1, padding: '24px 0' }}>
        <div style={{ textAlign: 'center', marginBottom: 32 }}>
          <Space direction="vertical">
            <div style={{
              width: 64,
              height: 64,
              background: '#1890ff',
              borderRadius: 16,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              margin: '0 auto',
              boxShadow: '0 4px 12px rgba(24, 144, 255, 0.3)',
            }}>
              <ShopOutlined style={{ fontSize: 32, color: 'white' }} />
            </div>
            <Title level={2} style={{ margin: '16px 0 0 0', fontWeight: 700 }}>DMS LOGIN</Title>
            <Text type="secondary">Hệ thống Quản lý Phân phối Thông minh</Text>
          </Space>
        </div>

        <Card 
          bordered={false} 
          style={{ 
            borderRadius: 16, 
            boxShadow: '0 10px 25px rgba(0,0,0,0.05)',
            border: '1px solid #f0f0f0',
          }}
        >
          {error && (
            <Alert
              message="Lỗi đăng nhập"
              description={error}
              type="error"
              showIcon
              style={{ marginBottom: 24, borderRadius: 8 }}
            />
          )}

          <Form
            name="login_form"
            initialValues={{ remember: true }}
            onFinish={onFinish}
            layout="vertical"
            size="large"
          >
            <Form.Item
              name="username"
              rules={[{ required: true, message: 'Vui lòng nhập tên đăng nhập!' }]}
            >
              <Input 
                prefix={<UserOutlined style={{ color: '#bfbfbf' }} />} 
                placeholder="Tên đăng nhập" 
                style={{ borderRadius: 8 }}
              />
            </Form.Item>

            <Form.Item
              name="password"
              rules={[{ required: true, message: 'Vui lòng nhập mật khẩu!' }]}
            >
              <Input.Password
                prefix={<LockOutlined style={{ color: '#bfbfbf' }} />}
                placeholder="Mật khẩu"
                style={{ borderRadius: 8 }}
              />
            </Form.Item>

            <Form.Item style={{ marginBottom: 8, marginTop: 12 }}>
              <Button 
                type="primary" 
                htmlType="submit" 
                loading={loading} 
                block
                style={{ 
                  borderRadius: 8, 
                  height: 48, 
                  fontSize: 16, 
                  fontWeight: 600,
                  boxShadow: '0 4px 10px rgba(24, 144, 255, 0.2)',
                }}
              >
                Đăng nhập
              </Button>
            </Form.Item>
          </Form>

          <div style={{ marginTop: 24, textAlign: 'center' }}>
            <Text type="secondary" style={{ fontSize: 13 }}>
              Mặc định: admin / admin123
            </Text>
          </div>
        </Card>
      </Content>
    </div>
  );
}
