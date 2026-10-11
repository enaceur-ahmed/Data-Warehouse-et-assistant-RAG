/* Utilitaires partagés pour Recharts : palette, tooltip,
   graduations tronquées avec titre complet au survol. */

import { formatNumber, truncate } from '../utils/format';

export const PALETTE = [
  '#6366f1', '#22d3ee', '#f59e0b', '#10b981', '#f472b6',
  '#a78bfa', '#fbbf24', '#34d399', '#fb7185', '#60a5fa',
  '#94a3b8', '#f97316',
];

export const GENDER_COLORS = { M: '#6366f1', F: '#f472b6' };

export const EMPLOYMENT_COLORS = { 'Full-Time': '#10b981', 'Part-Time': '#f59e0b' };

export function DashboardTooltip({ active, payload, label, valueFormatter = formatNumber }) {
  if (!active || !payload || !payload.length) return null;
  return (
    <div className="chart-tooltip">
      {label !== undefined && label !== '' && label !== null && (
        <p className="chart-tooltip-label">{label}</p>
      )}
      {payload.map((entry, i) => (
        <div className="chart-tooltip-row" key={i}>
          <span
            className="chart-tooltip-dot"
            style={{ background: entry.color || entry.payload?.fill || '#64748b' }}
          />
          <span>{entry.name}</span>
          <strong>{valueFormatter(entry.value)}</strong>
        </div>
      ))}
    </div>
  );
}

/* Graduation X tronquée (label complet = tooltip natif SVG) */
export function TruncatedTick({ x, y, payload, max = 14 }) {
  const label = String(payload?.value ?? '');
  return (
    <text
      x={x}
      y={y}
      textAnchor="end"
      transform={`rotate(-35 ${x} ${y})`}
      fontSize={11}
      fill="#64748b"
    >
      <title>{label}</title>
      {truncate(label, max)}
    </text>
  );
}

/* Graduation Y tronquée (graphiques horizontaux) */
export function TruncatedYTick({ x, y, payload, max = 18 }) {
  const label = String(payload?.value ?? '');
  return (
    <text x={x} y={y} textAnchor="end" verticalAnchor="middle" fontSize={11} fill="#64748b">
      <title>{label}</title>
      {truncate(label, max)}
    </text>
  );
}
