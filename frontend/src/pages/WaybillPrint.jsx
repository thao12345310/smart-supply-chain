import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { deliveryOrderApi } from '../services/api';

const fmtDate = (d) => d || '';

export default function WaybillPrint() {
  const { id } = useParams();
  const [data, setData] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    (async () => {
      try {
        const res = await deliveryOrderApi.getById(id);
        setData(res.data);
        setTimeout(() => window.print(), 400);
      } catch (e) {
        setError(e.message || 'Không tải được vận đơn');
      }
    })();
  }, [id]);

  if (error) return <p style={{ padding: 24, color: '#cf1322' }}>Lỗi: {error}</p>;
  if (!data) return <p style={{ padding: 24 }}>Đang tải vận đơn...</p>;

  return (
    <div style={{ maxWidth: 720, margin: '0 auto', padding: 24, fontFamily: 'Arial, sans-serif', color: '#000' }}>
      <style>{`@media print { button { display: none; } @page { margin: 12mm; } }`}</style>
      <h2 style={{ textAlign: 'center', margin: 0 }}>PHIẾU VẬN ĐƠN</h2>
      <p style={{ textAlign: 'center', marginTop: 4 }}>Mã: <strong>{data.code}</strong></p>
      <table style={{ width: '100%', marginTop: 16 }}>
        <tbody>
          <tr><td><strong>Khách hàng:</strong> {data.customerName || '-'}</td><td><strong>Phiếu xuất:</strong> {data.goodsIssueCode || '-'}</td></tr>
          <tr><td><strong>Người nhận:</strong> {data.recipientName || '-'}</td><td><strong>SĐT:</strong> {data.recipientPhone || '-'}</td></tr>
          <tr><td colSpan={2}><strong>Địa chỉ giao:</strong> {data.destinationAddress || '-'}</td></tr>
          <tr><td colSpan={2}><strong>Ngày giao dự kiến:</strong> {fmtDate(data.plannedDate)}</td></tr>
        </tbody>
      </table>
      <table border={1} cellPadding={6} style={{ width: '100%', marginTop: 16, borderCollapse: 'collapse' }}>
        <thead>
          <tr><th>STT</th><th>Mã SP</th><th>Tên sản phẩm</th><th>SL</th><th>ĐVT</th></tr>
        </thead>
        <tbody>
          {(data.items || []).map((it, i) => (
            <tr key={i}><td style={{ textAlign: 'center' }}>{i + 1}</td><td>{it.productCode}</td><td>{it.productName}</td><td style={{ textAlign: 'right' }}>{it.quantity}</td><td>{it.unit}</td></tr>
          ))}
        </tbody>
      </table>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 48 }}>
        <div style={{ textAlign: 'center' }}>Người giao hàng<br /><br /><br />(Ký, ghi rõ họ tên)</div>
        <div style={{ textAlign: 'center' }}>Người nhận hàng<br /><br /><br />(Ký, ghi rõ họ tên)</div>
      </div>
      <button style={{ marginTop: 24 }} onClick={() => window.print()}>In lại</button>
    </div>
  );
}
