package com.example.BI.Chat.service;

/**
 * Réponse de POST /api/chat/reindex.
 */
public record ReindexResponse(boolean success, String message) {
}
