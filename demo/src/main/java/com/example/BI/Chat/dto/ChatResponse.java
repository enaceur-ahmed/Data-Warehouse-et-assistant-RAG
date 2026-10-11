package com.example.BI.Chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    /** Réponse en markdown */
    private String answer;
    private List<ChatSource> sources;
    /** true si la réponse a été construite sans le LLM (mode de secours) */
    private boolean llmFallback;
    /** Type court de l'erreur LLM (jamais de prompt, jamais de clé) */
    private String llmError;
}
