#!/usr/bin/env python3
"""
Ghép 3 tài liệu ôn phản biện thành 1 file HTML tự chứa (offline, mở trên Mac):
  - LUONG_HE_THONG.html                  -> Phần A: Luồng hệ thống (trực quan)
  - ON_TAP_PHAN_BIEN_TRUC_QUAN.html      -> Phần B: Đào sâu bảo mật & concurrency (trực quan)
  - ON_TAP_PHAN_BIEN_DATN.md             -> Phần C: Tra cứu chi tiết (bản chữ đầy đủ)

Chạy lại bất cứ lúc nào:  python3 scripts/build_on_tap_combined.py
Kết quả:  ON_TAP_DATN_TONG_HOP.html
"""
import os, re, markdown

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LUONG = os.path.join(ROOT, "LUONG_HE_THONG.html")
TRUCQUAN = os.path.join(ROOT, "ON_TAP_PHAN_BIEN_TRUC_QUAN.html")
MD = os.path.join(ROOT, "ON_TAP_PHAN_BIEN_DATN.md")
OUT = os.path.join(ROOT, "ON_TAP_DATN_TONG_HOP.html")


def read(p):
    with open(p, encoding="utf-8") as f:
        return f.read()


def wrap_inner(html):
    """Lấy phần bên trong <div class="wrap">...</div> (chỉ các section card)."""
    after = html.split('<div class="wrap">', 1)[1]
    return after.rsplit("</div>", 1)[0].strip()


# Bỏ emoji theo yêu cầu (thay bằng chữ trung tính)
EMOJI_MAP = {
    "✅": "(có)", "❌": "(không)", "⭐": "(trọng tâm)", "🔑": "Mấu chốt:",
    "🔜": "(định hướng)", "⚠️": "Lưu ý:", "⚠": "Lưu ý:", "️": "",
}
def strip_emoji(s):
    for k, v in EMOJI_MAP.items():
        s = s.replace(k, v)
    # quét nốt mọi emoji còn sót trong các dải Unicode phổ biến (giữ nguyên → ◄ ► và ký tự kẻ khung)
    s = re.sub(r"[\U0001F000-\U0001FAFF\U00002700-\U000027BF\U00002600-\U000026FF]", "", s)
    return s


md_text = strip_emoji(read(MD))
md_html = markdown.markdown(md_text, extensions=["tables", "fenced_code"])
# Gắn id cho từng PHẦN để mục lục nhảy tới được
for n in range(1, 9):
    md_html = md_html.replace(f"<h1>PHẦN {n}", f'<h1 id="ref{n}">PHẦN {n}', 1)

part_a = wrap_inner(read(LUONG))
part_b = wrap_inner(read(TRUCQUAN))

CSS = """
:root{
  --bg:#f6f7f9; --card:#fff; --ink:#1d2733; --muted:#6b7280; --line:#e3e7ec;
  --blue:#2563eb; --blue-bg:#eff5ff; --green:#15803d; --green-bg:#ecfdf3;
  --red:#b42318; --red-bg:#fef3f2; --amber:#b45309; --amber-bg:#fffaeb;
  --violet:#6d28d9; --violet-bg:#f5f3ff; --code:#0f172a;
}
*{box-sizing:border-box} html{scroll-behavior:smooth}
body{margin:0;background:var(--bg);color:var(--ink);
  font-family:-apple-system,BlinkMacSystemFont,"SF Pro Text","Helvetica Neue",Arial,sans-serif;
  font-size:16px;line-height:1.62;-webkit-font-smoothing:antialiased}
.wrap{max-width:1000px;margin:0 auto;padding:0 20px 80px}
header.top{background:linear-gradient(135deg,#0b2545,#13315c);color:#fff;padding:48px 20px 40px}
header.top .inner{max-width:1000px;margin:0 auto}
header.top h1{margin:0 0 8px;font-size:31px;letter-spacing:-.02em}
header.top p{margin:0;opacity:.92;font-size:15.5px;max-width:800px}
nav.toc{position:sticky;top:0;z-index:30;background:rgba(255,255,255,.94);
  backdrop-filter:saturate(180%) blur(8px);border-bottom:1px solid var(--line);padding:10px 0}
nav.toc .inner{max-width:1000px;margin:0 auto;padding:0 20px;display:flex;flex-wrap:wrap;gap:7px;align-items:center}
nav.toc .grp{font-size:11px;font-weight:800;letter-spacing:.05em;text-transform:uppercase;color:#94a3b8;margin-left:6px}
nav.toc a{text-decoration:none;color:var(--ink);font-size:13px;font-weight:600;
  padding:5px 10px;border:1px solid var(--line);border-radius:999px;background:#fff;white-space:nowrap}
nav.toc a:hover{border-color:var(--blue);color:var(--blue)}
.partbar{margin:36px 0 22px;padding:16px 22px;border-radius:14px;color:#fff}
.partbar.a{background:linear-gradient(135deg,#0f3a2e,#15803d)}
.partbar.b{background:linear-gradient(135deg,#1e3a8a,#2563eb)}
.partbar.c{background:linear-gradient(135deg,#4c1d95,#6d28d9)}
.partbar .pk{font-size:12px;font-weight:800;letter-spacing:.08em;text-transform:uppercase;opacity:.85}
.partbar h2{margin:3px 0 0;font-size:24px;color:#fff;border:0}
section.card{background:var(--card);border:1px solid var(--line);border-radius:16px;
  padding:28px 30px;margin:0 0 30px;box-shadow:0 1px 2px rgba(16,24,40,.04)}
.kicker{font-size:13px;font-weight:700;letter-spacing:.04em;text-transform:uppercase;color:var(--blue)}
h2{font-size:23px;margin:4px 0 6px;letter-spacing:-.01em} h3{font-size:17px;margin:26px 0 10px}
p{margin:10px 0} .lead{color:#374151}
code{font-family:ui-monospace,SFMono-Regular,"SF Mono",Menlo,Consolas,monospace;font-size:13.5px;
  background:#eef1f5;color:var(--code);padding:1.5px 6px;border-radius:6px}
.fig{margin:18px 0;text-align:center} .fig svg{max-width:100%;height:auto}
.cap{font-size:13px;color:var(--muted);margin-top:8px;text-align:center}
table{width:100%;border-collapse:collapse;margin:14px 0;font-size:14px}
th,td{border:1px solid var(--line);padding:8px 11px;text-align:left;vertical-align:top}
th{background:#f3f5f8;font-weight:700}
ul,ol{margin:10px 0;padding-left:22px} li{margin:5px 0} .key{font-weight:700}
.footnote{font-size:13.5px;color:var(--muted)} .badge-file{font-size:12.5px;color:var(--muted);font-family:ui-monospace,monospace}
hr{border:none;border-top:1px solid var(--line);margin:24px 0}
/* status pills (Phần A) */
.st{display:inline-block;font-size:11.5px;font-weight:700;padding:2px 8px;border-radius:6px;white-space:nowrap}
.st.draft{background:#f1f5f9;color:#475569;border:1px solid #d8e0ea}
.st.open{background:var(--blue-bg);color:var(--blue);border:1px solid #cfe0ff}
.st.appr{background:var(--violet-bg);color:var(--violet);border:1px solid #e2d9fb}
.st.conf{background:var(--green-bg);color:var(--green);border:1px solid #bbe7c9}
.st.warn{background:var(--amber-bg);color:var(--amber);border:1px solid #fae6b8}
.st.done{background:#e8f5ee;color:#0f5132;border:1px solid #bfe3cd}
.st.cancel{background:var(--red-bg);color:var(--red);border:1px solid #f6cfcb}
/* pipeline (Phần A) */
.pipe{margin:18px 0}
.stage{display:grid;grid-template-columns:150px 1fr 200px;gap:14px;align-items:stretch}
@media(max-width:720px){.stage{grid-template-columns:1fr}}
.stage .role{display:flex;align-items:center;justify-content:flex-end;gap:8px;font-size:13px;color:#334155;text-align:right}
@media(max-width:720px){.stage .role{justify-content:flex-start}}
.role .who{font-weight:700}
.stage .act{border:1px solid var(--line);border-radius:12px;padding:13px 16px;background:#fbfcfd}
.stage .act h4{margin:0 0 4px;font-size:15.5px} .stage .act p{margin:2px 0;font-size:13.5px;color:#475569}
.stage .meta{display:flex;flex-direction:column;gap:6px;justify-content:center;font-size:12.5px}
.meta .inv.up{color:var(--green);font-weight:700} .meta .inv.down{color:var(--red);font-weight:700} .meta .inv.none{color:var(--muted)}
.connector{display:flex;justify-content:center;margin:4px 0} .connector svg{display:block}
.accent-mua{border-left:4px solid var(--blue)} .accent-nhap{border-left:4px solid var(--green)}
.accent-ban{border-left:4px solid var(--violet)} .accent-xuat{border-left:4px solid var(--red)}
.accent-tien{border-left:4px solid var(--amber)} .accent-giao{border-left:4px solid #0891b2}
.accent-master{border-left:4px solid #64748b}
.gold{background:var(--amber-bg);border:1px solid #fae6b8;border-left:4px solid var(--amber);border-radius:10px;padding:14px 18px;margin:18px 0}
.gold .lbl{font-weight:700;color:var(--amber);text-transform:uppercase;font-size:13px;letter-spacing:.03em}
.note{background:var(--blue-bg);border:1px solid #d7e6ff;border-radius:10px;padding:12px 16px;margin:14px 0;font-size:14.5px}
/* Q/A blocks (Phần B) */
.q{background:var(--blue-bg);border:1px solid #d7e6ff;border-left:4px solid var(--blue);border-radius:10px;padding:14px 16px;margin:14px 0 6px;font-size:15px}
.q b{color:var(--blue)}
.ans{background:var(--green-bg);border:1px solid #cdeed8;border-radius:10px;padding:14px 16px;margin:16px 0}
.ans .lbl{font-weight:700;color:var(--green);font-size:13px;text-transform:uppercase;letter-spacing:.03em}
.pill{display:inline-block;font-size:12px;font-weight:700;padding:2px 9px;border-radius:999px;vertical-align:middle}
.pill.ok{background:var(--green-bg);color:var(--green);border:1px solid #bbe7c9}
.pill.no{background:var(--red-bg);color:var(--red);border:1px solid #f6cfcb}
.pill.warn{background:var(--amber-bg);color:var(--amber);border:1px solid #fae6b8}
.twocol{display:grid;grid-template-columns:1fr 1fr;gap:16px;margin:16px 0}
@media(max-width:680px){.twocol{grid-template-columns:1fr}}
.mini{border:1px solid var(--line);border-radius:12px;padding:14px 16px;background:#fbfcfd}
.mini h4{margin:0 0 6px;font-size:15px}
.chot{background:var(--violet-bg);border:1px solid #e4dcfb;border-radius:10px;padding:14px 16px;margin:16px 0}
.chot .lbl{font-weight:700;color:var(--violet);font-size:13px;text-transform:uppercase;letter-spacing:.03em}
.chot i{font-style:italic;color:#3b246e}
/* Phần C: bản chữ từ Markdown */
.md{background:var(--card);border:1px solid var(--line);border-radius:16px;padding:10px 32px 30px;box-shadow:0 1px 2px rgba(16,24,40,.04)}
.md h1{font-size:22px;margin:34px 0 10px;padding-top:14px;border-top:2px solid var(--line);color:#0b2545}
.md h1:first-child{border-top:0;margin-top:10px}
.md h2{font-size:19px;border:0;color:#13315c} .md h3{font-size:16px}
.md blockquote{margin:12px 0;padding:10px 16px;background:#f8fafc;border-left:4px solid #94a3b8;border-radius:0 8px 8px 0;color:#334155}
.md pre{background:#0f172a;color:#e2e8f0;padding:14px 16px;border-radius:10px;overflow:auto;font-size:12.5px;line-height:1.5}
.md pre code{background:none;color:inherit;padding:0;font-size:12.5px}
.md table{font-size:13.5px} .md hr{margin:20px 0}
@media print{
  nav.toc{display:none} body{background:#fff} section.card,.md{box-shadow:none;break-inside:avoid}
  header.top{background:#0b2545 !important} .partbar{-webkit-print-color-adjust:exact;print-color-adjust:exact}
  *{-webkit-print-color-adjust:exact;print-color-adjust:exact}
}
"""

HTML = f"""<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Ôn phản biện DATN — Bộ tài liệu tổng hợp</title>
<style>{CSS}</style>
</head>
<body>
<header class="top"><div class="inner">
  <h1>Ôn phản biện DATN — Bộ tài liệu tổng hợp</h1>
  <p>Hệ thống Quản lý Chuỗi cung ứng Thông minh. Ba phần trong một file: Luồng hệ thống (trực quan) · Đào sâu Bảo mật &amp; Concurrency (trực quan) · Tra cứu chi tiết (bản chữ đầy đủ). Mở bằng Safari/Chrome trên Mac; xuất PDF bằng Cmd+P.</p>
</div></header>

<nav class="toc"><div class="inner">
  <span class="grp">A · Luồng</span>
  <a href="#arch">Kiến trúc</a><a href="#pipe">Luồng xương sống</a><a href="#state">Trạng thái</a><a href="#rbac">Phân quyền</a>
  <span class="grp">B · Bảo mật</span>
  <a href="#c1">Khóa lô</a><a href="#c2">JWT</a><a href="#c3">BCrypt</a><a href="#c4">CSRF</a><a href="#c5">CORS</a>
  <span class="grp">C · Tra cứu</span>
  <a href="#ref2">Techstack</a><a href="#ref4">Nghiệp vụ</a><a href="#ref5">Câu hỏi</a><a href="#ref6">Điểm yếu</a><a href="#ref7">Checklist</a>
</div></nav>

<div class="wrap">

  <div class="partbar a"><div class="pk">Phần A — trực quan</div><h2>Luồng toàn hệ thống</h2></div>
  {part_a}

  <div class="partbar b"><div class="pk">Phần B — trực quan</div><h2>Đào sâu Bảo mật &amp; Concurrency</h2></div>
  {part_b}

  <div class="partbar c"><div class="pk">Phần C — bản chữ đầy đủ</div><h2>Tra cứu chi tiết (kèm số dòng code)</h2></div>
  <div class="md">
  {md_html}
  </div>

</div>
</body>
</html>
"""

with open(OUT, "w", encoding="utf-8") as f:
    f.write(HTML)

print("Đã tạo:", OUT)
print("Kích thước:", round(len(HTML) / 1024, 1), "KB")
