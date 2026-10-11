package com.example.BI.Chat.llm;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration OpenRouter. La clé vient de la variable
 * d'environnement OPENROUTER_API_KEY ou du fichier local
 * git-ignored application-local.properties (jamais d'un
 * fichier tracké, jamais du frontend).
 */
@Data
@Component
@ConfigurationProperties(prefix = "openrouter")
public class OpenRouterProperties {
    private String baseUrl = "https://openrouter.ai/api/v1";
    private String model = "";
    private String apiKey = "";
    private String referer = "http://localhost:5173";
    private String title = "HR BI Dashboard";
    private int timeoutSeconds = 60;
    private int maxRetries = 2;
    private double temperature = 0.2;
    private int maxTokens = 1500;
}
