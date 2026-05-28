import React, { useEffect, useMemo, useState } from "react";
import {
  Table,
  Button,
  Modal,
  Form,
  Input,
  Select,
  Switch,
  Tag,
  Space,
  Card,
  Row,
  Col,
  Statistic,
  Tooltip,
  Popconfirm,
  message,
} from "antd";
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  LockOutlined,
  UserOutlined,
  CheckCircleOutlined,
  StopOutlined,
  TeamOutlined,
  SearchOutlined,
} from "@ant-design/icons";
import { userApi } from "../services/api";

const ROLE_LABELS = {
  ROLE_ADMIN: { label: "Quản trị viên", color: "red" },
  ROLE_PURCHASE_STAFF: { label: "NV Mua hàng", color: "blue" },
  ROLE_PURCHASE_MANAGER: { label: "QL Mua hàng", color: "geekblue" },
  ROLE_SALES_STAFF: { label: "NV Bán hàng", color: "green" },
  ROLE_SALES_MANAGER: { label: "QL Bán hàng", color: "cyan" },
  ROLE_WAREHOUSE_STAFF: { label: "NV Kho", color: "orange" },
  ROLE_DELIVERY_ADMIN: { label: "QL Giao hàng", color: "purple" },
  ROLE_SHIPPER: { label: "Shipper", color: "magenta" },
  ROLE_ACCOUNTANT: { label: "Kế toán", color: "gold" },
};

const formatRole = (r) =>
  ROLE_LABELS[r]?.label || r.replace("ROLE_", "");
const roleColor = (r) => ROLE_LABELS[r]?.color || "default";

export default function UserManagement() {
  const [users, setUsers] = useState([]);
  const [roles, setRoles] = useState([]);
  const [loading, setLoading] = useState(false);
  const [search, setSearch] = useState("");
  const [roleFilter, setRoleFilter] = useState(null);
  const [statusFilter, setStatusFilter] = useState("all");

  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form] = Form.useForm();

  const [pwdOpen, setPwdOpen] = useState(false);
  const [pwdTarget, setPwdTarget] = useState(null);
  const [pwdForm] = Form.useForm();

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const res = await userApi.getAll();
      setUsers(res.data || []);
    } catch (err) {
      message.error(err.message || "Không thể tải danh sách tài khoản");
    } finally {
      setLoading(false);
    }
  };

  const fetchRoles = async () => {
    try {
      const res = await userApi.getRoles();
      setRoles(res.data || []);
    } catch (err) {
      // fallback to predefined list if endpoint fails
      setRoles(Object.keys(ROLE_LABELS));
    }
  };

  useEffect(() => {
    fetchUsers();
    fetchRoles();
  }, []);

  const stats = useMemo(() => {
    const total = users.length;
    const active = users.filter((u) => u.active).length;
    const inactive = total - active;
    const admins = users.filter((u) =>
      (u.roles || []).includes("ROLE_ADMIN")
    ).length;
    return { total, active, inactive, admins };
  }, [users]);

  const filtered = useMemo(() => {
    return users.filter((u) => {
      if (statusFilter === "active" && !u.active) return false;
      if (statusFilter === "inactive" && u.active) return false;
      if (roleFilter && !(u.roles || []).includes(roleFilter)) return false;
      if (search) {
        const s = search.toLowerCase();
        if (
          !u.username?.toLowerCase().includes(s) &&
          !u.fullName?.toLowerCase().includes(s) &&
          !u.email?.toLowerCase().includes(s)
        )
          return false;
      }
      return true;
    });
  }, [users, search, roleFilter, statusFilter]);

  const openCreate = () => {
    setEditing(null);
    form.resetFields();
    form.setFieldsValue({ active: true, roles: [] });
    setFormOpen(true);
  };

  const openEdit = (user) => {
    setEditing(user);
    form.setFieldsValue({
      username: user.username,
      fullName: user.fullName,
      email: user.email,
      active: user.active,
      roles: user.roles || [],
    });
    setFormOpen(true);
  };

  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      if (editing) {
        await userApi.update(editing.id, {
          fullName: values.fullName,
          email: values.email,
          active: values.active,
          roles: values.roles,
        });
        message.success("Cập nhật tài khoản thành công");
      } else {
        await userApi.create(values);
        message.success("Tạo tài khoản thành công");
      }
      setFormOpen(false);
      fetchUsers();
    } catch (err) {
      if (err?.errorFields) return;
      message.error(err.message || "Lưu thất bại");
    }
  };

  const handleToggleActive = async (user) => {
    try {
      if (user.active) {
        await userApi.deactivate(user.id);
        message.success(`Đã khóa tài khoản ${user.username}`);
      } else {
        await userApi.activate(user.id);
        message.success(`Đã mở khóa tài khoản ${user.username}`);
      }
      fetchUsers();
    } catch (err) {
      message.error(err.message || "Thao tác thất bại");
    }
  };

  const handleDelete = async (user) => {
    try {
      await userApi.delete(user.id);
      message.success("Đã xóa tài khoản");
      fetchUsers();
    } catch (err) {
      message.error(err.message || "Không thể xóa tài khoản");
    }
  };

  const openResetPassword = (user) => {
    setPwdTarget(user);
    pwdForm.resetFields();
    setPwdOpen(true);
  };

  const handleResetPassword = async () => {
    try {
      const values = await pwdForm.validateFields();
      await userApi.resetPassword(pwdTarget.id, values.password);
      message.success(`Đã đặt lại mật khẩu cho ${pwdTarget.username}`);
      setPwdOpen(false);
    } catch (err) {
      if (err?.errorFields) return;
      message.error(err.message || "Đặt lại mật khẩu thất bại");
    }
  };

  const columns = [
    {
      title: "ID",
      dataIndex: "id",
      width: 70,
    },
    {
      title: "Tên đăng nhập",
      dataIndex: "username",
      render: (v) => <strong>{v}</strong>,
    },
    {
      title: "Họ tên",
      dataIndex: "fullName",
    },
    {
      title: "Email",
      dataIndex: "email",
      render: (v) => v || <span style={{ color: "#999" }}>—</span>,
    },
    {
      title: "Vai trò",
      dataIndex: "roles",
      render: (rs = []) =>
        rs.length === 0 ? (
          <Tag>Chưa gán</Tag>
        ) : (
          <Space size={[4, 4]} wrap>
            {rs.map((r) => (
              <Tag key={r} color={roleColor(r)}>
                {formatRole(r)}
              </Tag>
            ))}
          </Space>
        ),
    },
    {
      title: "Trạng thái",
      dataIndex: "active",
      width: 130,
      render: (active) =>
        active ? (
          <Tag icon={<CheckCircleOutlined />} color="success">
            Đang hoạt động
          </Tag>
        ) : (
          <Tag icon={<StopOutlined />} color="default">
            Đã khóa
          </Tag>
        ),
    },
    {
      title: "Hành động",
      width: 240,
      render: (_, user) => (
        <Space size={4}>
          <Tooltip title="Sửa">
            <Button
              size="small"
              icon={<EditOutlined />}
              onClick={() => openEdit(user)}
            />
          </Tooltip>
          <Tooltip title="Đặt lại mật khẩu">
            <Button
              size="small"
              icon={<LockOutlined />}
              onClick={() => openResetPassword(user)}
            />
          </Tooltip>
          <Tooltip title={user.active ? "Khóa tài khoản" : "Mở khóa"}>
            <Button
              size="small"
              icon={user.active ? <StopOutlined /> : <CheckCircleOutlined />}
              danger={user.active}
              onClick={() => handleToggleActive(user)}
            />
          </Tooltip>
          <Popconfirm
            title="Xóa tài khoản này?"
            description="Hành động không thể hoàn tác."
            okText="Xóa"
            cancelText="Hủy"
            okButtonProps={{ danger: true }}
            onConfirm={() => handleDelete(user)}
          >
            <Tooltip title="Xóa">
              <Button size="small" danger icon={<DeleteOutlined />} />
            </Tooltip>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="Tổng nhân viên"
              value={stats.total}
              prefix={<TeamOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="Đang hoạt động"
              value={stats.active}
              valueStyle={{ color: "#52c41a" }}
              prefix={<CheckCircleOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="Đã khóa"
              value={stats.inactive}
              valueStyle={{ color: "#bfbfbf" }}
              prefix={<StopOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="Quản trị viên"
              value={stats.admins}
              valueStyle={{ color: "#cf1322" }}
              prefix={<UserOutlined />}
            />
          </Card>
        </Col>
      </Row>

      <Card
        title="Quản lý tài khoản nhân viên"
        extra={
          <Space>
            <Input
              prefix={<SearchOutlined />}
              placeholder="Tìm theo tên, username, email"
              allowClear
              style={{ width: 260 }}
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
            <Select
              placeholder="Lọc theo vai trò"
              allowClear
              style={{ width: 200 }}
              value={roleFilter}
              onChange={setRoleFilter}
              options={roles.map((r) => ({
                value: r,
                label: formatRole(r),
              }))}
            />
            <Select
              style={{ width: 150 }}
              value={statusFilter}
              onChange={setStatusFilter}
              options={[
                { value: "all", label: "Tất cả trạng thái" },
                { value: "active", label: "Đang hoạt động" },
                { value: "inactive", label: "Đã khóa" },
              ]}
            />
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={openCreate}
            >
              Thêm tài khoản
            </Button>
          </Space>
        }
      >
        <Table
          rowKey="id"
          loading={loading}
          dataSource={filtered}
          columns={columns}
          pagination={{ pageSize: 10, showSizeChanger: true }}
        />
      </Card>

      <Modal
        title={editing ? `Sửa tài khoản: ${editing.username}` : "Thêm tài khoản mới"}
        open={formOpen}
        onCancel={() => setFormOpen(false)}
        onOk={handleSave}
        okText={editing ? "Lưu" : "Tạo"}
        cancelText="Hủy"
        width={600}
        destroyOnClose
      >
        <Form form={form} layout="vertical" preserve={false}>
          <Form.Item
            name="username"
            label="Tên đăng nhập"
            rules={[
              { required: true, message: "Vui lòng nhập tên đăng nhập" },
              { min: 3, message: "Tối thiểu 3 ký tự" },
            ]}
          >
            <Input disabled={!!editing} placeholder="vd: nv001" />
          </Form.Item>

          {!editing && (
            <Form.Item
              name="password"
              label="Mật khẩu"
              rules={[
                { required: true, message: "Vui lòng nhập mật khẩu" },
                { min: 6, message: "Mật khẩu tối thiểu 6 ký tự" },
              ]}
            >
              <Input.Password placeholder="Tối thiểu 6 ký tự" />
            </Form.Item>
          )}

          <Form.Item
            name="fullName"
            label="Họ và tên"
            rules={[{ required: true, message: "Vui lòng nhập họ tên" }]}
          >
            <Input placeholder="vd: Nguyễn Văn A" />
          </Form.Item>

          <Form.Item
            name="email"
            label="Email"
            rules={[{ type: "email", message: "Email không hợp lệ" }]}
          >
            <Input placeholder="email@example.com" />
          </Form.Item>

          <Form.Item name="roles" label="Vai trò">
            <Select
              mode="multiple"
              placeholder="Chọn một hoặc nhiều vai trò"
              options={roles.map((r) => ({
                value: r,
                label: formatRole(r),
              }))}
            />
          </Form.Item>

          <Form.Item name="active" label="Trạng thái" valuePropName="checked">
            <Switch
              checkedChildren="Hoạt động"
              unCheckedChildren="Khóa"
            />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title={`Đặt lại mật khẩu: ${pwdTarget?.username || ""}`}
        open={pwdOpen}
        onCancel={() => setPwdOpen(false)}
        onOk={handleResetPassword}
        okText="Đặt lại"
        cancelText="Hủy"
        destroyOnClose
      >
        <Form form={pwdForm} layout="vertical" preserve={false}>
          <Form.Item
            name="password"
            label="Mật khẩu mới"
            rules={[
              { required: true, message: "Vui lòng nhập mật khẩu mới" },
              { min: 6, message: "Mật khẩu tối thiểu 6 ký tự" },
            ]}
          >
            <Input.Password placeholder="Tối thiểu 6 ký tự" />
          </Form.Item>
          <Form.Item
            name="confirm"
            label="Xác nhận mật khẩu"
            dependencies={["password"]}
            rules={[
              { required: true, message: "Vui lòng nhập lại mật khẩu" },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue("password") === value) {
                    return Promise.resolve();
                  }
                  return Promise.reject(new Error("Mật khẩu không khớp"));
                },
              }),
            ]}
          >
            <Input.Password placeholder="Nhập lại mật khẩu" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
