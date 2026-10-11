/* Couche API centrale : requêtes HTTP vers le backend Spring Boot,
   avec AbortController (annulation des requêtes périmées) et mode mock. */

import { mockHandler, mockPostHandler } from './mock';

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
const USE_MOCK = String(import.meta.env.VITE_USE_MOCK || '').toLowerCase() === 'true';

export const apiInfo = {
  baseUrl: BASE_URL,
  useMock: USE_MOCK,
};

export class ApiError extends Error {
  constructor(message, status) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }
}

/**
 * Effectue un GET vers le backend.
 * - Les paramètres vides / null / undefined ne sont JAMAIS envoyés.
 * - Les erreurs réseau et HTTP non-2xx lèvent une ApiError.
 * - Logue le statut HTTP en console (le backend peut renvoyer 200 avec un
 *   corps vide lorsqu'il avale une exception — d'où les états "vide" possibles).
 */
export async function apiFetch(path, { params, signal } = {}) {
  if (USE_MOCK) {
    return mockHandler(path, { params, signal });
  }

  const url = new URL(BASE_URL + path);
  if (params) {
    for (const [key, value] of Object.entries(params)) {
      if (value !== undefined && value !== null && value !== '') {
        url.searchParams.set(key, String(value));
      }
    }
  }

  console.info(`[API] GET ${url.pathname}${url.search}`);

  let response;
  try {
    response = await fetch(url.toString(), { signal });
  } catch (err) {
    if (err.name === 'AbortError') throw err;
    throw new ApiError(
      `Impossible de joindre le backend à ${BASE_URL}. Vérifiez que le serveur Spring Boot est démarré.`,
      0,
    );
  }

  console.info(`[API] ${url.pathname}${url.search} → HTTP ${response.status}`);

  if (!response.ok) {
    throw new ApiError(`Erreur HTTP ${response.status} sur ${url.pathname}`, response.status);
  }

  return response.json();
}

/**
 * Effectue un POST JSON vers le backend (Assistant / chat).
 * Mêmes garanties que apiFetch : erreur réseau/HTTP → ApiError,
 * statut HTTP logué en console.
 */
export async function apiPost(path, { body, signal } = {}) {
  if (USE_MOCK) {
    return mockPostHandler(path, { body, signal });
  }

  const url = new URL(BASE_URL + path);

  console.info(`[API] POST ${url.pathname}`);

  let response;
  try {
    response = await fetch(url.toString(), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body ?? {}),
      signal,
    });
  } catch (err) {
    if (err.name === 'AbortError') throw err;
    throw new ApiError(
      `Impossible de joindre le backend à ${BASE_URL}. Vérifiez que le serveur Spring Boot est démarré.`,
      0,
    );
  }

  console.info(`[API] POST ${url.pathname} → HTTP ${response.status}`);

  if (!response.ok) {
    throw new ApiError(`Erreur HTTP ${response.status} sur ${url.pathname}`, response.status);
  }

  return response.json();
}
