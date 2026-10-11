package com.example.BI.Chat.llm;

import com.example.BI.Chat.dto.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Découverte de modèle : GET /models, ne garde que les modèles
 * gratuits (pricing 0, id ":free") supportant le chat, les
 * essaie un par un avec une minuscule complétion jusqu'à HTTP 200.
 * Gère 401 (mauvaise clé → STOP), 404 (modèle supprimé),
 * 429 (rate limit). Le premier id fonctionnel est à placer dans
 * application-local.properties (OPENROUTER_MODEL).
 *
 * NOTE : les modèles ":free" peuvent disparaître ou être
 * rate-limités à tout moment.
 */
@Component
public class ModelDiscoveryService {

    private static final Logger logger = LoggerFactory.getLogger(ModelDiscoveryService.class);
    private static final String PROBE_PROMPT = "Say OK";

    private final OpenRouterClient client;

    public ModelDiscoveryService(OpenRouterClient client) {
        this.client = client;
    }

    /**
     * Renvoie le premier modèle gratuit fonctionnel, ou null.
     * N'effectue aucune modification de configuration.
     */
    public String discoverFirstWorkingModel() {
        List<String> models;
        try {
            models = client.listModels();
        } catch (OpenRouterClient.OpenRouterException e) {
            logger.error("[DISCOVERY] liste des modèles indisponible : {}", e.getMessage());
            return null;
        }

        List<String> freeChatModels = models.stream()
                .filter(id -> id != null && id.endsWith(":free"))
                .filter(id -> !id.contains("audio") && !id.contains("image") && !id.contains("-vl"))
                .toList();
        logger.info("[DISCOVERY] {} modèles gratuits candidats", freeChatModels.size());

        for (String modelId : freeChatModels) {
            try {
                String answer = probe(modelId);
                if (answer != null && !answer.isBlank()) {
                    logger.info("[DISCOVERY] modèle gratuit fonctionnel : {}", modelId);
                    return modelId;
                }
                logger.warn("[DISCOVERY] {} : HTTP 200 mais complétion vide, ignoré", modelId);
            } catch (OpenRouterClient.OpenRouterException e) {
                int status = e.getStatusCode();
                if (status == 401) {
                    // Clé invalide : STOP immédiat
                    logger.error("[DISCOVERY] 401 — clé API invalide, arrêt de la découverte");
                    return null;
                }
                logger.warn("[DISCOVERY] {} échoué ({}), essai du suivant", modelId, e.getMessage());
            }
        }
        logger.error("[DISCOVERY] aucun modèle gratuit fonctionnel trouvé");
        return null;
    }

    private String probe(String modelId) {
        // Sonde via le client avec le modèle forcé temporairement
        return probeWithModel(client, modelId);
    }

    private String probeWithModel(OpenRouterClient openRouterClient, String modelId) {
        // Le client expose complete() avec le modèle actif ; pour la sonde
        // on appelle directement l'API avec le modèle candidat.
        return openRouterClient.completeWithModel(modelId,
                List.of(new ChatMessage("user", PROBE_PROMPT)), 5);
    }
}
