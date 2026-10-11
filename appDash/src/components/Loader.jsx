/* États de chargement (squelettes) */

export function KpiSkeleton({ count = 4 }) {
  return (
    <div className="kpi-row" aria-busy="true" aria-label="Chargement des indicateurs">
      {Array.from({ length: count }).map((_, i) => (
        <div key={i} className="skeleton skeleton-kpi" />
      ))}
    </div>
  );
}

export function ChartSkeleton({ title = false } = {}) {
  return (
    <div className="card chart-card" aria-busy="true">
      {title && <div className="skeleton skeleton-block" style={{ width: '38%' }} />}
      {title && <div className="skeleton skeleton-block" style={{ width: '22%', height: 12 }} />}
      <div className="skeleton" style={{ height: 260 }} />
    </div>
  );
}

export function PageSkeleton({ kpiCount = 4, charts = 2 }) {
  return (
    <div aria-busy="true">
      <KpiSkeleton count={kpiCount} />
      <div className="grid">
        {Array.from({ length: charts }).map((_, i) => (
          <div key={i} className="span-6">
            <ChartSkeleton title />
          </div>
        ))}
      </div>
    </div>
  );
}
