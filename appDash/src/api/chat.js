/* ============================================================
   API Assistant (RAG) : chat hybride sur l'entrepôt DW-Projet.
   - POST /api/chat         question → réponse markdown + sources
   - GET  /api/chat/status  provider, modèle, clé configurée
   - POST /api/chat/reindex reconstruction de l'index
   Le thread est persisté en localStorage (dernières conversations).
   ============================================================ */

import { apiFetch, apiPost } from './client';

const STORAGE_KEY = 'hr-bi-chat-thread-v1';
export const MAX_MESSAGE_LENGTH = 1000;
export const MAX_HISTORY = 10;

export const EXAMPLE_QUESTIONS = [
  "Combien d'employés y a-t-il au total ?",
  'Quel est le salaire moyen par département ?',
  'Qui sont les managers avec le plus de subordonnés ?',
  'Combien de femmes dans le département IT ?',
  'Quel est le budget annuel total des départements ?',
  'Who are the top paid employees ?',
];

/* ---------- Endpoints ---------- */

export function fetchChatStatus(signal) {
  return apiFetch('/api/chat/status', { signal });
}

export function sendChatMessage(message, history = [], signal) {
  return apiPost('/api/chat', {
    body: {
      message: String(message).slice(0, MAX_MESSAGE_LENGTH),
      history: history.slice(-MAX_HISTORY).map((m) => ({
        role: m.role === 'assistant' ? 'assistant' : 'user',
        content: String(m.content).slice(0, 500),
      })),
    },
    signal,
  });
}

export function reindexChat(signal) {
  return apiPost('/api/chat/reindex', { body: {}, signal });
}

/* ---------- Persistance localStorage ---------- */

const isValidMessage = (m) =>
  m != null &&
  (m.role === 'user' || m.role === 'assistant') &&
  typeof m.content === 'string';

export function loadThread() {
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? parsed.filter(isValidMessage) : [];
  } catch {
    return [];
  }
}

export function saveThread(messages) {
  try {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(messages.slice(-50)));
  } catch {
    /* stockage plein ou indisponible : ignoré */
  }
}

export function clearThread() {
  try {
    window.localStorage.removeItem(STORAGE_KEY);
  } catch {
    /* ignoré */
  }
}
