/* Dérivations front-end : les KPI sont calculés UNIQUEMENT à partir
   des données retournées par l'API (aucun nombre inventé). */

export function sumBy(list, fn) {
  return (list || []).reduce((acc, item) => acc + fn(item), 0);
}

export function averageOf(list, fn) {
  if (!list || !list.length) return null;
  return sumBy(list, fn) / list.length;
}

export function maxBy(list, fn) {
  if (!list || !list.length) return null;
  return Math.max(...list.map(fn));
}

export function minBy(list, fn) {
  if (!list || !list.length) return null;
  return Math.min(...list.map(fn));
}

/**
 * Moyenne pondérée des salaires : associe les comptes (employeesByX)
 * et les salaires moyens (averageSalaryByX) par nom commun.
 * Retourne null si les tableaux ne peuvent pas être associés.
 */
export function weightedAverage(countEntries, valueEntries) {
  if (!countEntries?.length || !valueEntries?.length) return null;
  const counts = new Map(countEntries.map((e) => [e.name, e.count]));
  let totalCount = 0;
  let totalMass = 0;
  for (const entry of valueEntries) {
    const count = counts.get(entry.name);
    if (typeof count === 'number' && count > 0) {
      totalCount += count;
      totalMass += entry.value * count;
    }
  }
  return totalCount > 0 ? totalMass / totalCount : null;
}
