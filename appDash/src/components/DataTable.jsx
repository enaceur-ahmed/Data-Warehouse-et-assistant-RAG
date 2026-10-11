/* Tableau générique : triable côté client + recherche. */

import { useMemo, useState } from 'react';
import { SearchIcon } from './icons';

export default function DataTable({
  columns,
  rows,
  searchPlaceholder = 'Rechercher…',
  defaultSortKey = null,
  defaultSortDir = 'desc',
  emptyMessage = 'Aucune donnée',
}) {
  const [query, setQuery] = useState('');
  const [sortKey, setSortKey] = useState(defaultSortKey);
  const [sortDir, setSortDir] = useState(defaultSortDir);

  const visibleRows = useMemo(() => {
    let out = rows || [];

    if (query.trim()) {
      const q = query.trim().toLowerCase();
      out = out.filter((row) =>
        columns.some((col) => String(row[col.key] ?? '').toLowerCase().includes(q)),
      );
    }

    if (sortKey) {
      out = [...out].sort((a, b) => {
        const va = a[sortKey];
        const vb = b[sortKey];
        const cmp =
          typeof va === 'number' && typeof vb === 'number'
            ? va - vb
            : String(va ?? '').localeCompare(String(vb ?? ''));
        return sortDir === 'asc' ? cmp : -cmp;
      });
    }

    return out;
  }, [rows, query, sortKey, sortDir, columns]);

  const toggleSort = (key) => {
    if (sortKey === key) {
      setSortDir((d) => (d === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortKey(key);
      setSortDir('desc');
    }
  };

  return (
    <div className="card data-table-card">
      <div className="data-table-toolbar">
        <span className="data-table-title">Détails</span>
        <div style={{ position: 'relative' }}>
          <input
            type="search"
            className="data-table-search"
            placeholder={searchPlaceholder}
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            aria-label={searchPlaceholder}
          />
          <SearchIcon
            size={14}
            style={{ position: 'absolute', right: 9, top: '50%', transform: 'translateY(-50%)', color: 'var(--muted)' }}
          />
        </div>
      </div>

      <div className="table-wrap">
        <table className="data-table">
          <thead>
            <tr>
              {columns.map((col) => (
                <th
                  key={col.key}
                  className={`${col.numeric ? 'num' : ''} ${col.sortable === false ? '' : 'sortable'}`}
                  onClick={col.sortable === false ? undefined : () => toggleSort(col.key)}
                >
                  {col.header}
                  {sortKey === col.key && (
                    <span className="sort-arrow">{sortDir === 'asc' ? '▲' : '▼'}</span>
                  )}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {visibleRows.length === 0 ? (
              <tr>
                <td colSpan={columns.length} className="muted-text" style={{ textAlign: 'center' }}>
                  {emptyMessage}
                </td>
              </tr>
            ) : (
              visibleRows.map((row, i) => (
                <tr key={row.id ?? i}>
                  {columns.map((col) => (
                    <td key={col.key} className={col.numeric ? 'num' : ''}>
                      {col.render ? col.render(row[col.key], row) : row[col.key]}
                    </td>
                  ))}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
