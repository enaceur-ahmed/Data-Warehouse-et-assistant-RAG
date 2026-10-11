package com.example.BI.Chat.retrieval;

import java.util.Map;

/**
 * Morceau de texte indexé pour la recherche BM25. Construit UNIQUEMENT
 * à partir de l'entrepôt (aucune donnée de contact : email/téléphone exclus).
 */
public record Chunk(
        String id,
        String type,      // company | department | job | aggregate | manager | kpi | employee
        String label,     // titre lisible
        String content,   // texte indexé + affiché dans les sources
        Map<String, String> metadata
) {
}
