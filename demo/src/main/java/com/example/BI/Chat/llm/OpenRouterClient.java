package com.example.BI.Chat.llm;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.example.BI.Chat.dto.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client OpenRouter (API compatible OpenAI : POST /chat/completions).
 * En-têtes : Authorization Bearer, HTTP-Referer, X-Title.
 * Timeout 60 s, 1-2 retries sur 429/5xx. Ne logue JAMAIS
 * ni la clé ni les prompts.
 */
@Component
public class OpenRouterClient {

    private static final Logger logger = LoggerFactory.getLogger(OpenRouterClient.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final OpenRouterProperties properties;
    private final RestClient restClient;
    /** Modèle actif (peut être mis à jour par la découverte si le configuré est supprimé) */
    private volatile String activeModel;

    public OpenRouterClient(OpenRouterProperties properties) {
        this.properties = properties;
        this.activeModel = properties.getModel();
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()));
        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.getApiKey())
                .defaultHeader("HTTP-Referer", properties.getReferer())
                .defaultHeader("X-Title", properties.getTitle())
                .requestFactory(requestFactory)
                .build();
    }

    public boolean isConfigured() {
        return properties.getApiKey() != null && !properties.getApiKey().isBlank();
    }

    public String getModel() {
        return activeModel;
    }

    /**
     * Appel de complétion avec retries sur 429/5xx.
     *
     * @throws OpenRouterException en cas d'échec définitif
     */
    public String complete(List<ChatMessage> messages) {
        if (!isConfigured()) {
            throw new OpenRouterException("OpenRouterApiKeyMissing", "API key not configured");
        }
        if (activeModel == null || activeModel.isBlank()) {
            throw new OpenRouterException("OpenRouterModelMissing", "model not configured");
        }

        int maxAttempts = 1 + Math.max(0, properties.getMaxRetries());
        OpenRouterException lastError = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return doComplete(messages);
            } catch (OpenRouterException e) {
                lastError = e;
                boolean retryable = e.isRetryable();
                if (attempt < maxAttempts && retryable) {
                    long backoff = 400L * attempt;
                    logger.warn("[OPENROUTER] tentative {}/{} échouée (HTTP {}), retry dans {} ms",
                            attempt, maxAttempts, e.getStatusCode(), backoff);
                    try {
                        Thread.sleep(backoff);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new OpenRouterException("OpenRouterInterrupted", "interrupted");
                    }
                } else {
                    throw e;
                }
            }
        }
        throw lastError;
    }

    private String doComplete(List<ChatMessage> messages) {
        return completeWithModel(activeModel, messages, properties.getMaxTokens());
    }

    /**
     * Complétion avec un modèle et un budget de tokens
     * explicites (utilisé par la sonde de découverte).
     */
    public String completeWithModel(String model, List<ChatMessage> messages, int maxTokens) {
        if (!isConfigured()) {
            throw new OpenRouterException("OpenRouterApiKeyMissing", "API key not configured");
        }
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages.stream().map(m -> {
            Map<String, String> msg = new HashMap<>();
            msg.put("role", m.getRole());
            msg.put("content", m.getContent());
            return msg;
        }).toList());
        body.put("temperature", properties.getTemperature());
        body.put("max_tokens", maxTokens);

        String responseJson;
        try {
            responseJson = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException e) {
            throw new OpenRouterException(e.getStatusCode().value(),
                    "HTTP " + e.getStatusCode().value());
        } catch (RestClientException e) {
            throw new OpenRouterException("OpenRouterNetworkError",
                    e.getClass().getSimpleName());
        }
        return extractContent(responseJson);
    }

    private String extractContent(String responseJson) {
        try {
            JsonNode root = MAPPER.readTree(responseJson);
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || content.isNull()) {
                throw new OpenRouterException("OpenRouterEmptyResponse", "empty completion");
            }
            String text = content.asText();
            if (text == null || text.isBlank()) {
                throw new OpenRouterException("OpenRouterEmptyResponse", "blank completion");
            }
            return text;
        } catch (OpenRouterException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenRouterException("OpenRouterParseError", "unparsable response");
        }
    }

    /** Exception interne : code court + message, sans clé ni prompt. */
    public static class OpenRouterException extends RuntimeException {
        private final int statusCode;
        private final boolean retryable;

        public OpenRouterException(String code, String message) {
            super(code + (message == null || message.isBlank() ? "" : ": " + message));
            this.statusCode = extractStatus(code);
            this.retryable = "OpenRouterEmptyResponse".equals(code)
                    || this.statusCode == 429
                    || this.statusCode >= 500;
        }

        public OpenRouterException(int statusCode, String message) {
            super("HTTP " + statusCode);
            this.statusCode = statusCode;
            this.retryable = statusCode == 429 || statusCode >= 500;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public boolean isRetryable() {
            return retryable;
        }

        private static int extractStatus(String code) {
            return code.startsWith("HTTP ") ? 502 : 0;
        }
    }

    /** Liste brute des modèles (pour tests/diagnostic). */
    public List<String> listModels() {
        try {
            String json = restClient.get().uri("/models").retrieve().body(String.class);
            JsonNode root = MAPPER.readTree(json);
            List<String> ids = new ArrayList<>();
            for (JsonNode m : root.path("data")) {
                ids.add(m.path("id").asText());
            }
            return ids;
        } catch (Exception e) {
            throw new OpenRouterException("OpenRouterModelsError", "models list unavailable");
        }
    }
}
