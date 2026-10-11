export default function KpiCard({ icon, color = 'var(--primary)', label, value, subtitle }) {
  return (
    <div className="kpi-card">
      <div className="kpi-icon" style={{ background: `${color}1a`, color }}>
        {icon}
      </div>
      <div className="kpi-body">
        <div className="kpi-label">{label}</div>
        <div className="kpi-value">{value}</div>
        {subtitle && <div className="kpi-subtitle">{subtitle}</div>}
      </div>
    </div>
  );
}
