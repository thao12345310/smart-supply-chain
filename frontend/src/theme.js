// Single source of truth for the app's visual identity ("Freight Teal").
// antd components read `antdTheme` via ConfigProvider; recharts / antd Progress
// do not read antd tokens, so they pull colors from `palette` / `CHART_COLORS`.

export const palette = {
  primary: '#14716A', // brand accent: primary buttons, links, charts, selected menu
  ink: '#1E2A32', // headings, KPI numbers, primary text
  muted: '#6B7A82', // secondary text
  canvas: '#F2F4F5', // page background
  surface: '#FFFFFF', // cards
  border: '#E3E7EA', // hairlines / card borders
  success: '#3E7D5A', // semantic — muted vs antd default
  warning: '#B8791F', // semantic — "cần xử lý"
  danger: '#B4453C', // semantic — quá hạn / lỗi
};

// Subtle tint of the primary, for the selected-menu background.
const primarySoft = '#E6F0EF';

export const antdTheme = {
  token: {
    colorPrimary: palette.primary,
    colorInfo: palette.primary,
    colorSuccess: palette.success,
    colorWarning: palette.warning,
    colorError: palette.danger,
    colorText: palette.ink,
    colorTextHeading: palette.ink,
    colorTextSecondary: palette.muted,
    colorBorderSecondary: palette.border,
    colorBgLayout: palette.canvas,
    borderRadius: 8,
    fontFamily:
      "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif",
  },
  components: {
    Menu: { itemSelectedBg: primarySoft, itemSelectedColor: palette.primary },
    Card: { borderRadiusLG: 10 },
  },
};

// Ordered accent colors for charts that need more than one series.
export const CHART_COLORS = [palette.primary, '#5BA199', '#9CC4BF', palette.muted];
