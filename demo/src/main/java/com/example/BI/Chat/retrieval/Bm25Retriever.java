package com.example.BI.Chat.retrieval;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BM25 pur (aucun embedding payant). k1=1.5, b=0.75.
 * Rebuildable à chaud quand l'index de l'entrepôt change.
 */
@Component
public class Bm25Retriever implements Retriever {

    private static final Logger logger = LoggerFactory.getLogger(Bm25Retriever.class);
    private static final double K1 = 1.5;
    private static final double B = 0.75;

    private List<Chunk> chunks = List.of();
    private final Map<String, Integer> docFrequency = new HashMap<>();
    private final List<Map<String, Integer>> termFrequencies = new ArrayList<>();
    private double averageDocLength = 0;

    @Override
    public void rebuild(List<Chunk> newChunks) {
        this.chunks = newChunks == null ? List.of() : List.copyOf(newChunks);
        this.docFrequency.clear();
        this.termFrequencies.clear();

        long totalLength = 0;
        for (Chunk chunk : this.chunks) {
            List<String> tokens = TextNormalizer.tokenize(chunk.content() + " " + chunk.label());
            totalLength += tokens.size();
            Map<String, Integer> tf = new HashMap<>();
            for (String token : tokens) {
                tf.merge(token, 1, Integer::sum);
            }
            termFrequencies.add(tf);
            for (String term : tf.keySet()) {
                docFrequency.merge(term, 1, Integer::sum);
            }
        }
        this.averageDocLength = this.chunks.isEmpty() ? 0 : (double) totalLength / this.chunks.size();
        logger.info("[BM25] index rebuild : {} chunks, {} termes uniques, longueur moyenne {}{}",
                this.chunks.size(), docFrequency.size(), String.format("%.1f", averageDocLength),
                this.chunks.isEmpty() ? " (index vide !)" : "");
    }

    @Override
    public List<Chunk> retrieve(String query, int k) {
        if (query == null || query.isBlank() || chunks.isEmpty()) {
            return List.of();
        }
        List<String> queryTokens = TextNormalizer.tokenize(query);
        if (queryTokens.isEmpty()) {
            return List.of();
        }

        int totalDocs = chunks.size();
        Map<Integer, Double> scores = new HashMap<>();

        for (String term : queryTokens) {
            int df = docFrequency.getOrDefault(term, 0);
            if (df == 0) {
                continue; // terme absent de l'index
            }
            double idf = Math.log((totalDocs - df + 0.5) / (df + 0.5) + 1.0);
            for (int i = 0; i < chunks.size(); i++) {
                Integer tf = termFrequencies.get(i).get(term);
                if (tf == null) {
                    continue;
                }
                double docLen = termFrequencies.get(i).values().stream().mapToInt(Integer::intValue).sum();
                double denominator = tf + K1 * (1 - B + B * (docLen / Math.max(1, averageDocLength)));
                double score = idf * (tf * (K1 + 1)) / Math.max(1e-9, denominator);
                scores.merge(i, score, Double::sum);
            }
        }

        return scores.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(Math.max(1, k))
                .map(e -> chunks.get(e.getKey()))
                .toList();
    }
}
