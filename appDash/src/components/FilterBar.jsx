/* Barre de filtres : dropdowns avec option "Tous" + reset.
   Props : filters[{key,label,value,options}], onChange(key,value),
   onReset, disabled. */

import { RefreshIcon } from './icons';

function FilterSelect({ filter, onChange, disabled }) {
  const id = `filter-${filter.key}`;
  return (
    <div className="filter-field">
      <label htmlFor={id}>{filter.label}</label>
      <select
        id={id}
        value={filter.value ?? ''}
        disabled={disabled}
        onChange={(e) => onChange(filter.key, e.target.value)}
      >
        <option value="">Tous</option>
        {(filter.options || []).map((opt) => (
          <option key={opt.value} value={opt.value}>
            {opt.label}
          </option>
        ))}
      </select>
    </div>
  );
}

export default function FilterBar({ filters, onChange, onReset, disabled = false }) {
  return (
    <div className="filter-bar" role="search">
      {filters.map((filter) => (
        <FilterSelect key={filter.key} filter={filter} onChange={onChange} disabled={disabled} />
      ))}
      <button type="button" className="btn" onClick={onReset} disabled={disabled}>
        <RefreshIcon size={15} />
        Réinitialiser
      </button>
    </div>
  );
}
