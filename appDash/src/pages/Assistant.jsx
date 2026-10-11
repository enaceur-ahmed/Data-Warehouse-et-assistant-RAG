/* Page Assistant : chat RAG hybride sur l'entrepôt DW-Projet.
   Thread type ChatGPT (utilisateur à droite, assistant à gauche),
   réponses en markdown, sources repliables, persistance
   localStorage, état d'erreur avec réessai.
   Le CSS est inclus dans ce fichier (balise <style>), scopé sous .assistant-page. */

import { useCallback, useEffect, useRef, useState } from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import {
  EXAMPLE_QUESTIONS,
  MAX_MESSAGE_LENGTH,
  clearThread,
  fetchChatStatus,
  loadThread,
  reindexChat,
  saveThread,
  sendChatMessage,
} from '../api/chat';
import {
  AlertIcon,
  ChatIcon,
  ChevronDownIcon,
  RefreshIcon,
  TrashIcon,
} from '../components/icons';

/* ------------------------------------------------------------------ */
/*  STYLES                                                             */
/* ------------------------------------------------------------------ */
const ASSISTANT_CSS = `
.assistant-page {
  --a-bg: #f6f7fb;
  --a-surface: #ffffff;
  --a-surface-2: #f0f2f8;
  --a-border: #e3e6ef;
  --a-text: black;
  --a-muted: black;
  --a-primary: white;
  --a-primary-hover: #4338ca;
  --a-primary-soft: black;
  --a-user-bg: linear-gradient(135deg, black);
  --a-danger: #dc2626;
  --a-danger-soft: black;
  --a-warn: #b45309;
  --a-warn-soft: black;
  --a-ok: #16a34a;
  --a-shadow: 0 1px 2px rgba(20, 24, 50, 0.06), 0 4px 16px rgba(20, 24, 50, 0.06);
  --a-radius: 16px;

  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 560px;
  max-height: calc(100vh - 32px);
  background: var(--a-bg);
  color: var(--a-text);
  font-family: Inter, system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif;
  border: 1px solid var(--a-border);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: var(--a-shadow);
}

@media (prefers-color-scheme: dark) {
  .assistant-page {
    --a-bg: white;
    --a-surface: white;
    --a-surface-2: white;
    --a-border: #2a2f45;
    --a-text: black;
    --a-muted: black;
    --a-primary-soft: #232650;
    --a-danger-soft: #2a1618;
    --a-warn-soft: #2a2110;
    --a-warn: #fbbf24;
    --a-shadow: 0 1px 2px rgba(0, 0, 0, 0.4), 0 4px 16px rgba(0, 0, 0, 0.35);
  }
}

.assistant-page *,
.assistant-page *::before,
.assistant-page *::after { box-sizing: border-box; }

/* ---------- Toolbar ---------- */
.assistant-page .chat-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 10px;
  padding: 12px 18px;
  background: var(--a-surface);
  border-bottom: 1px solid var(--a-border);
}

.assistant-page .chat-status-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  font-size: 13px;
  color: var(--a-muted);
  background: var(--a-surface-2);
  border: 1px solid var(--a-border);
  border-radius: 999px;
}
.assistant-page .chat-status-chip strong { color: var(--a-text); font-weight: 600; }
.assistant-page .status-dot {
  width: 8px; height: 8px; border-radius: 50%;
  background: var(--a-ok);
  box-shadow: 0 0 0 3px rgba(22, 163, 74, 0.18);
}
.assistant-page .status-dot.mock {
  background: var(--a-warn);
  box-shadow: 0 0 0 3px rgba(180, 83, 9, 0.18);
}
.assistant-page .chip-warning {
  margin-left: 8px;
  font-style: normal;
  font-size: 11px;
  font-weight: 600;
  color: var(--a-warn);
  background: var(--a-warn-soft);
  padding: 2px 8px;
  border-radius: 999px;
}

.assistant-page .chat-toolbar-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.assistant-page .chat-reindex-msg {
  font-size: 12px;
  color: var(--a-muted);
  max-width: 240px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ---------- Boutons ---------- */
.assistant-page .btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  font: inherit;
  font-size: 13px;
  font-weight: 600;
  border-radius: 10px;
  border: 1px solid transparent;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s, transform 0.05s, opacity 0.15s;
}
.assistant-page .btn:active:not(:disabled) { transform: translateY(1px); }
.assistant-page .btn:disabled { opacity: 0.5; cursor: not-allowed; }
.assistant-page .btn-primary { background: var(--a-primary); color: #fff; }
.assistant-page .btn-primary:hover:not(:disabled) { background: var(--a-primary-hover); }
.assistant-page .btn-ghost {
  background: transparent;
  color: var(--a-text);
  border-color: var(--a-border);
}
.assistant-page .btn-ghost:hover:not(:disabled) { background: var(--a-surface-2); }

/* ---------- Thread ---------- */
.assistant-page .chat-thread {
  flex: 1;
  overflow-y: auto;
  padding: 24px 18px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  scroll-behavior: smooth;
}
.assistant-page .chat-thread::-webkit-scrollbar { width: 8px; }
.assistant-page .chat-thread::-webkit-scrollbar-thumb {
  background: var(--a-border);
  border-radius: 8px;
}

.assistant-page .chat-message {
  display: flex;
  width: 100%;
  animation: a-pop 0.25s ease-out;
}
.assistant-page .chat-message.user { justify-content: flex-end; }
.assistant-page .chat-message.assistant { justify-content: flex-start; }

@keyframes a-pop {
  from { opacity: 0; transform: translateY(6px); }
  to   { opacity: 1; transform: translateY(0); }
}

.assistant-page .chat-bubble {
  max-width: min(780px, 85%);
  padding: 12px 16px;
  font-size: 14.5px;
  line-height: 1.6;
  word-wrap: break-word;
  overflow-wrap: anywhere;
}
.assistant-page .user-bubble {
  background: var(--a-user-bg);
  color: #fff;
  border-radius: var(--a-radius) var(--a-radius) 4px var(--a-radius);
  white-space: pre-wrap;
  box-shadow: 0 4px 14px rgba(79, 70, 229, 0.25);
}
.assistant-page .assistant-bubble {
  background: var(--a-surface);
  border: 1px solid var(--a-border);
  border-radius: var(--a-radius) var(--a-radius) var(--a-radius) 4px;
  box-shadow: var(--a-shadow);
}

/* ---------- Erreur ---------- */
.assistant-page .error-bubble {
  background: var(--a-danger-soft);
  border: 1px solid var(--a-danger);
  border-radius: var(--a-radius) var(--a-radius) var(--a-radius) 4px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: flex-start;
}
.assistant-page .state-title { margin: 0; font-weight: 700; color: var(--a-danger); }
.assistant-page .state-message { margin: 0 0 6px; color: var(--a-text); }

/* ---------- Bandeau fallback ---------- */
.assistant-page .chat-fallback-banner {
  display: flex;
  gap: 8px;
  align-items: flex-start;
  margin-bottom: 10px;
  padding: 8px 12px;
  font-size: 12.5px;
  color: var(--a-warn);
  background: var(--a-warn-soft);
  border: 1px solid rgba(180, 83, 9, 0.35);
  border-radius: 10px;
}
.assistant-page .chat-fallback-banner svg { flex-shrink: 0; margin-top: 2px; }

/* ---------- Markdown ---------- */
.assistant-page .assistant-bubble > :first-child { margin-top: 0; }
.assistant-page .assistant-bubble p { margin: 0 0 10px; }
.assistant-page .assistant-bubble ul,
.assistant-page .assistant-bubble ol { margin: 0 0 10px; padding-left: 22px; }
.assistant-page .assistant-bubble li { margin: 3px 0; }
.assistant-page .assistant-bubble h1,
.assistant-page .assistant-bubble h2,
.assistant-page .assistant-bubble h3 { margin: 14px 0 8px; line-height: 1.3; }
.assistant-page .assistant-bubble h1 { font-size: 18px; }
.assistant-page .assistant-bubble h2 { font-size: 16px; }
.assistant-page .assistant-bubble h3 { font-size: 15px; }
.assistant-page .assistant-bubble a { color: var(--a-primary); }
.assistant-page .assistant-bubble code {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 0.88em;
  background: var(--a-surface-2);
  padding: 2px 6px;
  border-radius: 6px;
}
.assistant-page .assistant-bubble pre {
  background: var(--a-surface-2);
  border: 1px solid var(--a-border);
  border-radius: 10px;
  padding: 12px;
  overflow-x: auto;
}
.assistant-page .assistant-bubble pre code { background: none; padding: 0; }
.assistant-page .assistant-bubble blockquote {
  margin: 0 0 10px;
  padding: 4px 12px;
  border-left: 3px solid var(--a-primary);
  color: var(--a-muted);
}
.assistant-page .assistant-bubble table {
  display: block;
  max-width: 100%;
  overflow-x: auto;
  border-collapse: collapse;
  margin: 8px 0 12px;
  font-size: 13px;
}
.assistant-page .assistant-bubble th,
.assistant-page .assistant-bubble td {
  padding: 7px 12px;
  border: 1px solid var(--a-border);
  text-align: left;
  white-space: nowrap;
}
.assistant-page .assistant-bubble th { background: var(--a-surface-2); font-weight: 600; }
.assistant-page .assistant-bubble tr:nth-child(even) td { background: color-mix(in srgb, var(--a-surface-2) 50%, transparent); }

/* ---------- Sources ---------- */
.assistant-page .chat-sources {
  margin-top: 10px;
  border-top: 1px dashed var(--a-border);
  padding-top: 8px;
  font-size: 13px;
}
.assistant-page .chat-sources summary {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  list-style: none;
  color: var(--a-muted);
  font-weight: 600;
  user-select: none;
}
.assistant-page .chat-sources summary::-webkit-details-marker { display: none; }
.assistant-page .chat-sources summary svg { transition: transform 0.2s; }
.assistant-page .chat-sources[open] summary svg { transform: rotate(180deg); }
.assistant-page .chat-sources summary:hover { color: var(--a-primary); }
.assistant-page .chat-sources ul {
  list-style: none;
  margin: 8px 0 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.assistant-page .chat-sources li {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 8px;
  padding: 7px 10px;
  background: var(--a-surface-2);
  border-radius: 10px;
}
.assistant-page .source-type {
  font-size: 10.5px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  padding: 2px 8px;
  border-radius: 999px;
  color: var(--a-primary);
  background: var(--a-primary-soft);
}
.assistant-page .source-detail { color: var(--a-muted); font-size: 12.5px; }

/* ---------- Typing ---------- */
.assistant-page .typing-bubble {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 14px 18px;
}
.assistant-page .typing-bubble .dot {
  width: 7px; height: 7px;
  border-radius: 50%;
  background: var(--a-muted);
  animation: a-bounce 1.2s infinite ease-in-out;
}
.assistant-page .typing-bubble .dot:nth-child(2) { animation-delay: 0.15s; }
.assistant-page .typing-bubble .dot:nth-child(3) { animation-delay: 0.3s; }
@keyframes a-bounce {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.45; }
  30% { transform: translateY(-5px); opacity: 1; }
}

/* ---------- État vide ---------- */
.assistant-page .chat-empty {
  margin: auto;
  max-width: 640px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 24px 0;
}
.assistant-page .chat-empty-icon {
  width: 60px; height: 60px;
  display: grid; place-items: center;
  border-radius: 18px;
  color: #fff;
  background: var(--a-user-bg);
}
.assistant-page .chat-empty-title { margin: 8px 0 0; font-size: 22px; font-weight: 700; }
.assistant-page .chat-empty-subtitle {
  margin: 0 0 14px;
  color: var(--a-muted);
  font-size: 14px;
  line-height: 1.6;
}
.assistant-page .chat-examples {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
}
.assistant-page .chat-example {
  font: inherit;
  font-size: 13px;
  color: var(--a-text);
  background: var(--a-surface);
  border: 1px solid var(--a-border);
  border-radius: 999px;
  padding: 8px 14px;
  cursor: pointer;
  transition: border-color 0.15s, background 0.15s, transform 0.1s;
}
.assistant-page .chat-example:hover {
  border-color: var(--a-primary);
  background: var(--a-primary-soft);
  transform: translateY(-1px);
}

/* ---------- Composer ---------- */
.assistant-page .chat-composer {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  padding: 14px 18px 18px;
  background: var(--a-surface);
  border-top: 1px solid var(--a-border);
}
.assistant-page .chat-input {
  flex: 1;
  resize: none;
  font: inherit;
  font-size: 14.5px;
  line-height: 1.5;
  color: var(--a-text);
  background: var(--a-surface-2);
  border: 1px solid var(--a-border);
  border-radius: 14px;
  padding: 11px 14px;
  max-height: 160px;
  outline: none;
  transition: border-color 0.15s, box-shadow 0.15s, background 0.15s;
}
.assistant-page .chat-input::placeholder { color: var(--a-muted); }
.assistant-page .chat-input:focus {
  border-color: var(--a-primary);
  background: var(--a-surface);
  box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.18);
}
.assistant-page .chat-input:disabled { opacity: 0.6; }
.assistant-page .chat-send {
  font: inherit;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  background: var(--a-user-bg);
  border: none;
  border-radius: 14px;
  padding: 11px 22px;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
  transition: transform 0.1s, opacity 0.15s, box-shadow 0.15s;
}
.assistant-page .chat-send:hover:not(:disabled) { box-shadow: 0 6px 16px rgba(79, 70, 229, 0.4); }
.assistant-page .chat-send:active:not(:disabled) { transform: translateY(1px); }
.assistant-page .chat-send:disabled { opacity: 0.45; cursor: not-allowed; box-shadow: none; }

/* ---------- Mobile ---------- */
@media (max-width: 640px) {
  .assistant-page { border-radius: 0; max-height: 100vh; }
  .assistant-page .chat-bubble { max-width: 94%; }
  .assistant-page .chat-toolbar { padding: 10px 12px; }
  .assistant-page .chat-thread { padding: 16px 12px; }
  .assistant-page .chat-composer { padding: 10px 12px 14px; }
  .assistant-page .chat-send { padding: 11px 16px; }
}
`;

let nextId = 1;

export default function Assistant() {
  const [messages, setMessages] = useState(() => loadThread());
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [status, setStatus] = useState(null);
  const [statusError, setStatusError] = useState(false);
  const [reindexing, setReindexing] = useState(false);
  const [reindexMsg, setReindexMsg] = useState(null);

  const threadRef = useRef(null);
  const textareaRef = useRef(null);
  const abortRef = useRef(null);

  /* --- statut du fournisseur LLM (montage) --- */
  useEffect(() => {
    const controller = new AbortController();
    let cancelled = false;
    fetchChatStatus(controller.signal)
      .then((data) => {
        if (!cancelled) {
          setStatus(data);
          setStatusError(false);
        }
      })
      .catch(() => {
        if (!cancelled) setStatusError(true);
      });
    return () => {
      cancelled = true;
      controller.abort();
    };
  }, []);

  /* --- persistance localStorage --- */
  useEffect(() => {
    saveThread(messages);
  }, [messages]);

  /* --- défilement automatique --- */
  useEffect(() => {
    const el = threadRef.current;
    if (el) el.scrollTop = el.scrollHeight;
  }, [messages, loading]);

  const resetTextarea = () => {
    const el = textareaRef.current;
    if (el) el.style.height = 'auto';
  };

  const growTextarea = () => {
    const el = textareaRef.current;
    if (!el) return;
    el.style.height = 'auto';
    el.style.height = `${Math.min(el.scrollHeight, 160)}px`;
  };

  const send = useCallback(
    (text) => {
      const question = (text ?? input).trim();
      if (!question || loading) return;

      abortRef.current?.abort();
      const controller = new AbortController();
      abortRef.current = controller;

      const userMessage = { id: nextId++, role: 'user', content: question };
      setMessages((prev) => [...prev, userMessage]);
      setInput('');
      resetTextarea();
      setLoading(true);

      // historique = messages précédents (hors placeholders d'erreur)
      const history = messages.filter((m) => m.role && m.content);

      sendChatMessage(question, history, controller.signal)
        .then((data) => {
          setMessages((prev) => [
            ...prev,
            {
              id: nextId++,
              role: 'assistant',
              content: data?.answer ?? '(réponse vide du backend)',
              sources: Array.isArray(data?.sources) ? data.sources : [],
              llmFallback: Boolean(data?.llmFallback),
              llmError: data?.llmError || null,
            },
          ]);
        })
        .catch((err) => {
          if (err.name === 'AbortError') return;
          setMessages((prev) => [
            ...prev,
            {
              id: nextId++,
              role: 'assistant',
              error:
                err?.status === 429
                  ? 'Trop de requêtes (limite de l’API atteinte). Attendez quelques instants.'
                  : err?.message || 'Erreur réseau inattendue.',
              retryQuestion: question,
            },
          ]);
        })
        .finally(() => {
          setLoading(false);
        });
    },
    [input, loading, messages],
  );

  const retry = (msg) => {
    setMessages((prev) => prev.filter((m) => m.id !== msg.id));
    send(msg.retryQuestion);
  };

  const handleNewConversation = () => {
    abortRef.current?.abort();
    setMessages([]);
    clearThread();
    setInput('');
    resetTextarea();
  };

  const handleReindex = () => {
    setReindexing(true);
    setReindexMsg(null);
    reindexChat()
      .then((res) => setReindexMsg(res?.message || 'Index reconstruit'))
      .catch((err) => setReindexMsg(err?.message || 'Échec de la réindexation'))
      .finally(() => setReindexing(false));
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      send();
    }
  };

  const renderMessage = (m) => {
    if (m.role === 'user') {
      return (
        <div key={m.id} className="chat-message user">
          <div className="chat-bubble user-bubble">{m.content}</div>
        </div>
      );
    }

    if (m.error) {
      return (
        <div key={m.id} className="chat-message assistant">
          <div className="chat-bubble error-bubble" role="alert">
            <p className="state-title">Erreur</p>
            <p className="state-message">{m.error}</p>
            {m.retryQuestion && (
              <button
                type="button"
                className="btn btn-primary"
                onClick={() => retry(m)}
              >
                <RefreshIcon size={15} />
                Réessayer
              </button>
            )}
          </div>
        </div>
      );
    }

    return (
      <div key={m.id} className="chat-message assistant">
        <div className="chat-bubble assistant-bubble">
          {m.llmFallback && (
            <div className="chat-fallback-banner" role="alert">
              <AlertIcon size={15} />
              <span>
                LLM indisponible
                {m.llmError ? ` (${m.llmError})` : ''} — réponse construite
                sans le LLM, directement à partir des données de l’entrepôt.
              </span>
            </div>
          )}
          <ReactMarkdown remarkPlugins={[remarkGfm]}>{m.content}</ReactMarkdown>
          {m.sources && m.sources.length > 0 && (
            <details className="chat-sources">
              <summary>
                <ChevronDownIcon size={14} />
                Sources ({m.sources.length})
              </summary>
              <ul>
                {m.sources.map((s, i) => (
                  <li key={i}>
                    <span className={`source-type source-${s.type}`}>
                      {s.type}
                    </span>
                    <strong>{s.label}</strong>
                    {s.detail && (
                      <span className="source-detail">{s.detail}</span>
                    )}
                  </li>
                ))}
              </ul>
            </details>
          )}
        </div>
      </div>
    );
  };

  const chipModel = status?.model && status.model !== '(non configuré)'
    ? status.model
    : 'modèle non configuré';

  return (
    <div className="assistant-page">
      <style>{ASSISTANT_CSS}</style>

      <div className="chat-toolbar">
        <div
          className="chat-status-chip"
          title={
            status
              ? `Fournisseur : ${status.provider || 'inconnu'} — clé configurée : ${
                  status.keyConfigured ? 'oui' : 'non'
                }`
              : 'Statut du fournisseur LLM'
          }
        >
          <span
            className={`status-dot ${
              statusError ? 'mock' : status?.keyConfigured ? '' : 'mock'
            }`}
          />
          {statusError ? (
            <span>Statut LLM indisponible</span>
          ) : status ? (
            <span>
              <strong>{status.provider || 'LLM'}</strong> · {chipModel}
              {!status.keyConfigured && (
                <em className="chip-warning">clé non configurée</em>
              )}
            </span>
          ) : (
            <span>Chargement du statut…</span>
          )}
        </div>
        <div className="chat-toolbar-actions">
          {reindexMsg && <span className="chat-reindex-msg">{reindexMsg}</span>}
          <button
            type="button"
            className="btn btn-ghost"
            onClick={handleReindex}
            disabled={reindexing || loading}
          >
            <RefreshIcon size={15} />
            {reindexing ? 'Réindexation…' : 'Réindexer'}
          </button>
          <button
            type="button"
            className="btn btn-ghost"
            onClick={handleNewConversation}
            disabled={loading}
            title="Effacer la conversation (localStorage)"
          >
            <TrashIcon size={15} />
            Nouvelle conversation
          </button>
        </div>
      </div>

      <div className="chat-thread" ref={threadRef}>
        {messages.length === 0 && !loading ? (
          <div className="chat-empty">
            <div className="chat-empty-icon">
              <ChatIcon size={28} />
            </div>
            <p className="chat-empty-title">Assistant HR BI</p>
            <p className="chat-empty-subtitle">
              Posez vos questions en français, anglais ou arabe tunisien —
              les réponses sont construites uniquement à partir de
              l’entrepôt de données RH DW-Projet.
            </p>
            <div className="chat-examples">
              {EXAMPLE_QUESTIONS.map((q) => (
                <button
                  key={q}
                  type="button"
                  className="chat-example"
                  onClick={() => send(q)}
                >
                  {q}
                </button>
              ))}
            </div>
          </div>
        ) : (
          messages.map(renderMessage)
        )}
        {loading && (
          <div className="chat-message assistant">
            <div className="chat-bubble assistant-bubble typing-bubble" aria-label="Réponse en cours">
              <span className="dot" />
              <span className="dot" />
              <span className="dot" />
            </div>
          </div>
        )}
      </div>

      <div className="chat-composer">
        <textarea
          ref={textareaRef}
          className="chat-input"
          rows={1}
          value={input}
          maxLength={MAX_MESSAGE_LENGTH}
          placeholder="Posez une question sur les données RH… (Entrée = envoyer, Maj+Entrée = nouvelle ligne)"
          onChange={(e) => {
            setInput(e.target.value);
            growTextarea();
          }}
          onKeyDown={handleKeyDown}
          disabled={loading}
          aria-label="Votre question"
        />
        <button
          type="button"
          className="chat-send"
          onClick={() => send()}
          disabled={loading || !input.trim()}
        >
          Envoyer
        </button>
      </div>
    </div>
  );
}