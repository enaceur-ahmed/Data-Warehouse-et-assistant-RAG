/* Utilitaires de formatage (fr-FR) */

const fmtInt = new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 0 });

export function formatNumber(value, { decimals = 0 } = {}) {
  if (value === null || value === undefined || Number.isNaN(value)) return '—';
  const n = Number(value);
  if (!Number.isFinite(n)) return '—';
  if (decimals > 0) {
    return new Intl.NumberFormat('fr-FR', {
      minimumFractionDigits: decimals,
      maximumFractionDigits: decimals,
    }).format(n);
  }
  return fmtInt.format(n);
}

/**
 * Formate un salaire. L'API n'expose pas le champ Currency des employés,
 * on affiche donc des nombres "plain" avec séparateur de milliers.
 * Si un code de devise est fourni (ex. 'TND'), on essaie le formatage devise.
 */
export function formatSalary(value, currency) {
  if (value === null || value === undefined || Number.isNaN(value)) return '—';
  const n = Number(value);
  if (!Number.isFinite(n)) return '—';
  if (currency) {
    try {
      return new Intl.NumberFormat('fr-FR', {
        style: 'currency',
        currency,
        maximumFractionDigits: 0,
      }).format(n);
    } catch {
      /* code de devise inconnu : on tombe sur le format plain */
    }
  }
  return formatNumber(n);
}

export function truncate(text, max = 18) {
  const s = String(text ?? '');
  return s.length > max ? `${s.slice(0, Math.max(1, max - 1))}…` : s;
}
