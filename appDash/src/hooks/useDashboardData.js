/* Hook principal de fetching : debounce ~300 ms, annulation des
   requêtes périmées via AbortController, état de retry. */

import { useCallback, useEffect, useRef, useState } from 'react';

export function useDashboardData(fetcher, params, { debounceMs = 300 } = {}) {
  const [state, setState] = useState({ status: 'loading', data: null, error: null });
  const [retryCount, setRetryCount] = useState(0);

  // Sérialisation stable des paramètres pour la dépendance d'effet
  const paramsKey = JSON.stringify(params ?? {});
  const fetcherRef = useRef(fetcher);
  fetcherRef.current = fetcher;

  useEffect(() => {
    const controller = new AbortController();

    // On garde les anciennes données affichées pendant le rafraîchissement
    setState((prev) =>
      prev.data
        ? { status: 'refreshing', data: prev.data, error: null }
        : { status: 'loading', data: null, error: null },
    );

    const timer = setTimeout(async () => {
      try {
        const data = await fetcherRef.current(JSON.parse(paramsKey), controller.signal);
        setState({ status: 'success', data, error: null });
      } catch (err) {
        if (err && err.name === 'AbortError') return; // requête annulée : on ignore
        console.error('[Dashboard] échec de la requête :', err);
        setState({ status: 'error', data: null, error: err });
      }
    }, debounceMs);

    return () => {
      clearTimeout(timer);
      controller.abort(); // annule la requête périmée au changement de filtre
    };
  }, [paramsKey, retryCount, debounceMs]);

  const retry = useCallback(() => setRetryCount((c) => c + 1), []);

  return { ...state, retry };
}
