package com.example.BI.Chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatStatusResponse {
    private String provider;   // "openrouter"
    private String model;      // id du modèle configuré
    private boolean keyConfigured; // true/false uniquement, jamais la clé
}
