/* Données de référence (companies, départements, jobs, employés)
   chargées une seule fois et mises en cache pour toute l'app. */

import { useEffect, useState } from 'react';
import { fetchCompanies, fetchDepartments, fetchJobs, fetchEmployees } from '../api/dashboard';

let cache = null; // { status, data, error }
const listeners = new Set();

function notify() {
  listeners.forEach((fn) => fn(cache));
}

export function resetReferenceCache() {
  cache = null;
  notify();
}

export function useReferenceData() {
  const [state, setState] = useState(
    cache ?? { status: 'loading', data: null, error: null },
  );

  useEffect(() => {
    listeners.add(setState);

    if (!cache) {
      cache = { status: 'loading', data: null, error: null };
      notify();
      Promise.all([fetchCompanies(), fetchDepartments(), fetchJobs(), fetchEmployees()])
        .then(([companies, departments, jobs, employees]) => {
          cache = {
            status: 'success',
            data: { companies, departments, jobs, employees },
            error: null,
          };
        })
        .catch((error) => {
          console.error('[Dashboard] données de référence :', error);
          cache = { status: 'error', data: null, error };
        })
        .finally(notify);
    }

    return () => {
      listeners.delete(setState);
    };
  }, []);

  return state;
}
