package com.example.BI.Chat.retrieval;

import java.util.List;

/**
 * Interface de recherche — implémentée en BM25 pur (sans embeddings payants)
 * pour permettre d'ajouter des embeddings plus tard sans changer le service.
 */
public interface Retriever {
    /** Renvoie les k chunks les plus pertinents pour la requête. */
    List<Chunk> retrieve(String query, int k);

    /** Reconstruit l'index interne à partir des chunks fournis. */
    void rebuild(List<Chunk> chunks);
}
