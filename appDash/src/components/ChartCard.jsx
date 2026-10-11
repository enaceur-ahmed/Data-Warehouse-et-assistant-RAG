export default function ChartCard({ title, subtitle, children, className = '' }) {
  return (
    <div className={`card chart-card ${className}`.trim()}>
      {(title || subtitle) && (
        <div className="chart-card-header">
          <div>
            {title && <h3 className="chart-card-title">{title}</h3>}
            {subtitle && <p className="chart-card-subtitle">{subtitle}</p>}
          </div>
        </div>
      )}
      <div className="chart-container">{children}</div>
    </div>
  );
}
